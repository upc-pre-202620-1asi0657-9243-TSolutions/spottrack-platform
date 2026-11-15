# SpotTrack infra (GCP)

Two environments, one GCP project (`spottrack-507400`). Every resource name starts with `spottrack-dev-` or `spottrack-prod-`.

| Branch    | Env  | Deployed by                                  |
|-----------|------|----------------------------------------------|
| `develop` | dev  | `.github/workflows/deploy.yml`, on push      |
| `main`    | prod | same workflow, after approval on `prod` env  |

Each env has:
- **Backend VM** (`spottrack-<env>-backend`): Caddy (HTTPS) → API, both in Docker.
- **Cloud SQL MySQL 8.4** (`spottrack-<env>-mysql`, `db-f1-micro`). Only the backend VM's IP can connect, and only over TLS. Prod has daily backups and deletion protection.
- **3 Mosquitto brokers** + a TCP load balancer for the emulator. TLS only (port 8883), no anonymous clients (see [MQTT channel](#mqtt-channel)).
- **Secrets** in Secret Manager (`spottrack-<env>-*`).

Terraform builds the infra. GitHub Actions ships the app. They don't overlap.

## One-time setup

```bash
gcloud config set project spottrack-507400

# 1. State bucket (shared by bootstrap, dev and prod)
gcloud storage buckets create gs://spottrack-507400-tfstate --location us-central1 --uniform-bucket-level-access
gcloud storage buckets update gs://spottrack-507400-tfstate --versioning

# 2. Project-wide pieces: APIs, registry, GitHub deploy identity
cd infra/terraform/bootstrap
terraform init -backend-config="bucket=spottrack-507400-tfstate"
terraform apply -var project_id=spottrack-507400
terraform output github_variables
```

3. In GitHub → Settings → Secrets and variables → Actions → **Variables**, add the three values printed above: `GCP_PROJECT_ID`, `GCP_WIF_PROVIDER` and `GCP_DEPLOY_SA`.
4. In GitHub → Settings → Environments, create `dev` and `prod`. Add required reviewers to `prod`.

## Create or update an env

```bash
cd infra/terraform
./tf.sh dev plan
./tf.sh dev apply

# Stripe values are never in Terraform. Add them once per env:
printf %s 'sk_test_...' | gcloud secrets versions add spottrack-dev-stripe-api-key --data-file=-
printf %s 'whsec_...'   | gcloud secrets versions add spottrack-dev-stripe-webhook-secret --data-file=-

./tf.sh dev output api_url    # https://<ip>.sslip.io
```

Same thing for `prod`. After the first apply, push to `develop` (or re-run the workflow) to ship the app.

## Day to day

- **App change:** merge to `develop` → dev updates. Merge `develop` into `main` → prod updates after approval.
- **Infra change:** edit the `.tf` files, then `./tf.sh dev apply`, check it, then `./tf.sh prod apply`.
- **Bad deploy:** if the new image doesn't answer `/v3/api-docs` within 5 minutes, `deploy.sh` restarts the previous container and the workflow fails. `app-image` (what the VM redeploys on boot) only moves to images that came up healthy.
- **`scripts/deploy.sh` change:** GitHub deploys use the repo's copy right away; run `./tf.sh <env> apply` so boots use it too.
- **Manual redeploy / debug:**
  ```bash
  gcloud compute ssh spottrack-dev-backend --zone us-central1-a --tunnel-through-iap
  sudo docker logs -f spottrack
  ```

## MQTT channel

Brokers accept only TLS on **8883**. Port 1883 is closed. Every client logs in:

| Client  | Username            | Can do                                         |
|---------|---------------------|------------------------------------------------|
| Sensor  | its `device_id`     | write `sensors/<device_id>/signal` and `/heartbeat` |
| Backend | `spottrack-backend` | read `sensors/+/signal` and `sensors/+/heartbeat`   |

- TLS uses a private CA made by Terraform (`mqtt_security.tf`). The broker certificate covers the balancer IP and the brokers' internal IPs.
- Passwords and the TLS key live in Secret Manager (`spottrack-<env>-mqtt-broker-users`, `spottrack-<env>-mqtt-tls-key`). Only the broker VMs' service account reads them.
- **Add a sensor:** put its `device_id` in `mqtt_device_ids` (`envs/<env>.tfvars`), run `./tf.sh <env> apply`, then reset the brokers (below).
- **Broker config, ACL or credentials changed:** the startup script only runs on boot, so reset the brokers after the apply:
  ```bash
  gcloud compute instances reset spottrack-dev-broker-1 spottrack-dev-broker-2 spottrack-dev-broker-3 --zone us-central1-a
  ```
- **Emulator settings:**
  ```bash
  ./tf.sh dev output mqtt_lb_endpoint                    # ssl://<ip>:8883
  ./tf.sh dev output -raw mqtt_ca_cert > mqtt-ca.crt
  ./tf.sh dev output -json mqtt_device_credentials       # device_id -> password
  ```
- **Broker image:** pinned to `eclipse-mosquitto:2.1.2-alpine` (`mosquitto_image`, default in `variables.tf`). The tag `2.1.2` without `-alpine` does not exist. Digest verified on 2026-11-15 (arm64, = what `:2` resolved to then): `sha256:75d6662dee52c947dc9dd9c29f1d8d7ffef78ddaf38001caa09755681e8bbc98`. To reproduce exactly, set `mosquitto_image = "eclipse-mosquitto@sha256:75d6662dee52c947dc9dd9c29f1d8d7ffef78ddaf38001caa09755681e8bbc98"`.
- **Auth modes** (`mqtt_auth_mode`, default `static`): `static` is the table above (password file + ACL file). `dynsec` uses Mosquitto's dynamic-security plugin so the backend creates and revokes each paired sensor's account at runtime; see [Activating pairing](#activating-pairing-dynsec).
- **Check that it rejects strangers** (QA-05):
  ```bash
  mosquitto_pub -h <lb-ip> -p 8883 --cafile mqtt-ca.crt -t sensors/x/signal -m hi                 # not authorised
  mosquitto_pub -h <lb-ip> -p 8883 --cafile mqtt-ca.crt -u <device_id> -P wrong -t sensors/x/signal -m hi
  mosquitto_pub -h <lb-ip> -p 8883 --cafile mqtt-ca.crt -u <device_id> -P <pw> -q 1 -V mqttv5 \
    -t sensors/other-device/signal -m hi                                                            # PUBACK 135
  ```
  Broker side: `sudo docker logs mosquitto` on a broker VM shows the refused connections and `Denied PUBLISH`.

## Activating pairing (dynsec)

Pairing QR codes (TA-04, `docs/sensor-qr-pairing.md`) only work against brokers whose accounts the backend can create. That needs two switches, both off by default:

| Variable | Value | Effect |
|---|---|---|
| `mqtt_auth_mode` | `dynsec` | Brokers load the dynamic-security plugin; `scripts/broker-startup.sh` seeds roles and accounts. |
| `pairing_provisioner_mode` | `dynsec` | The backend provisions/revokes the sensor account on **every** broker. Requires `mqtt_auth_mode = "dynsec"` (validated). |

What the startup script does in `dynsec` mode (every boot, `scripts/broker-startup.sh`):

1. Wipes `/home/chronos/mosquitto` (config, certs) as before. The plugin state lives in **`/var/lib/mosquitto-dynsec`**, which is never wiped: it survives a VM reset but **not** a VM recreate.
2. First boot: `mosquitto_ctrl dynsec init` offline (temporary container, mounted directory) creates the state with `spottrack-dynsec-admin`. Later boots: the admin password is reset offline (`mosquitto_ctrl -f <json> dynsec setClientPassword`) with the broker stopped, so the secret in Secret Manager always wins.
3. Starts the broker published **only on the VM's internal IP** (`--restart=no`). The load balancer and its health check use the balancer IP, so no sensor reaches the broker, and the VM is unhealthy in the LB, until the seeding is verified.
4. Seeds over TLS with `mosquitto_ctrl -h <internal IP> --cafile ca.crt` (no `--insecure`, no `127.0.0.1`): roles `sensor`, `backend-reader`, the narrowed `admin`, default ACL access `deny`, the `spottrack-backend` account and the legacy sensors of `mqtt_device_ids` (created only if absent).
5. Verifies the final state (`listClients`, `getClient`, `getDefaultACLAccess`, and every role must contain **exactly** the expected ACLs), then restarts the broker published on `8883` with `--restart=always`.
6. If anything fails it prints why, removes the container and exits non-zero: **the broker does not accept traffic** and stays unhealthy in the LB.

`mosquitto_ctrl` exit codes are not trusted (a TLS failure and "Client already exists" both exit `0`), so the script inspects every reply and tolerates only `Client already exists`, `Role already exists`, `ACL with this topic already exists`, `ACL not found` and `Client not found`. `addClientRole` on an already assigned role answers `Error: Internal error.`, so the script asks `getClient` first.

Roles (Mosquitto 2.1.2 ACL type names; `%u` is the username):

| Role | ACLs |
|---|---|
| `sensor` | `publishClientSend sensors/%u/signal`, `publishClientSend sensors/%u/heartbeat`. Nothing else, no subscribe. |
| `backend-reader` | `subscribeLiteral` + `publishClientReceive` on `sensors/+/signal` and `sensors/+/heartbeat`. |
| `admin` | `publishClientSend`, `publishClientReceive`, `subscribePattern` on `$CONTROL/dynamic-security/#` only (`dynsec init` also grants `#` and `$SYS/#`; the script removes them). |

> **Finding.** With the static ACL a sensor can subscribe to `#` and `sensors/#` (SUBACK `0`, checked on 2.0.22 and 2.1.2). In a test with a second sensor publishing, the subscriber received nothing (the ACL is also applied on delivery), so no leak was observed, but the subscription is granted. In `dynsec` mode the sensor has no subscribe permission: SUBACK `135`.

### Secrets

- `spottrack-<env>-mqtt-dynsec-admin-password` (generated, readable by the backend's service account; `deploy.sh` writes it to the container env as `MQTT_DYNSEC_ADMIN_PASSWORD`, tolerating its absence on an older environment).
- In `dynsec` mode the brokers read the admin password as the line `spottrack-dynsec-admin:<password>` of `mqtt-broker-users` (the same secret that already carries the backend line and the legacy sensors). In `static` mode that line is not written.
- During seeding each account's password is visible in the `docker run` arguments on the broker VM for an instant.

### Rolling out (one broker at a time)

Nothing here runs `apply` for you. Dev first, then prod.

1. In `envs/<env>.tfvars` set `mqtt_auth_mode = "dynsec"`; leave `pairing_provisioner_mode = "noop"`. `./tf.sh <env> plan`, review (broker metadata changes, a new secret, the broker secret gets the admin line), `./tf.sh <env> apply`. Nothing restarts yet: the startup script only runs on boot.
2. Reset **one** broker, wait, then the next:
   ```bash
   gcloud compute instances reset spottrack-<env>-broker-1 --zone us-central1-a
   # 1) the broker is healthy in the load balancer again (it stays unhealthy until the seeding is verified):
   gcloud compute backend-services get-health spottrack-<env>-mqtt-backend --region us-central1
   # 2) the backend sees it connected again (log line 'MQTT broker ssl://<ip>:8883 connected'):
   gcloud compute ssh spottrack-<env>-backend --zone us-central1-a --tunnel-through-iap -- \
     "sudo docker logs --since 5m spottrack 2>&1 | grep 'MQTT broker'"
   # 3) on the broker VM the log ends with 'broker-startup: broker up in dynsec mode':
   gcloud compute ssh spottrack-<env>-broker-1 --zone us-central1-a --tunnel-through-iap -- 'sudo journalctl -u google-startup-scripts --no-pager | tail -20'
   ```
   Only then reset broker-2, then broker-3. The two other brokers keep serving sensors meanwhile; resetting all three together takes MQTT down.
3. Set `pairing_provisioner_mode = "dynsec"`, `apply`, and redeploy the backend so the new env reaches the container (run the deploy workflow, or `sudo /usr/local/bin/deploy.sh`-equivalent on the VM, or reset the backend VM). If the config is incomplete the app **refuses to start** with `pairing.provisioner.mode=dynsec but the configuration is incomplete: ...` (the previous container is restored by `deploy.sh`).
4. Verify (below).

Sensors listed in `mqtt_device_ids` keep working: they are created in the plugin with the `sensor` role if absent. A legacy sensor revoked later is **recreated on the next broker boot** while it is still listed; remove it from `mqtt_device_ids` once it is paired.

### Rollback

- **Provisioner only:** `pairing_provisioner_mode = "noop"`, apply, redeploy the backend. Brokers and the accounts already created are untouched.
- **Brokers back to static:** `mqtt_auth_mode = "static"`, apply, then reset the brokers one at a time as above. The accounts created at runtime by pairing are **lost** (the static mode only knows `mqtt_device_ids`); the plugin state stays in `/var/lib/mosquitto-dynsec` and is picked up again if you switch back. Do not leave `pairing_provisioner_mode = "dynsec"` with static brokers: the apply is rejected by the variable validation.

### Verify manually (issued credentials against the LB)

Issue a pairing (`POST /api/v1/monitoring/motion-sensors/<id>/pairing`, ADMIN) and take `deviceId` and `secret` from the QR payload (`host` is the balancer), then:

```bash
./tf.sh <env> output -raw mqtt_ca_cert > mqtt-ca.crt
# accepted: PUBACK 0 (16 = no subscriber right now, also fine)
mosquitto_pub -h <lb-ip> -p 8883 --cafile mqtt-ca.crt -V mqttv5 -u <device_id> -P <secret> -q 1 -d -t sensors/<device_id>/signal    -m '{"deviceId":"<device_id>","signalType":"MOTION","durationMs":800}'
mosquitto_pub -h <lb-ip> -p 8883 --cafile mqtt-ca.crt -V mqttv5 -u <device_id> -P <secret> -q 1 -d -t sensors/<device_id>/heartbeat -m '{}'
# refused: PUBACK 135
mosquitto_pub ... -t sensors/someone-else/signal -m x
mosquitto_pub ... -t other/topic -m x
# refused: 'Subscribed (mid: 1): 135'
mosquitto_sub -h <lb-ip> -p 8883 --cafile mqtt-ca.crt -V mqttv5 -u <device_id> -P <secret> -t '#' -C 1 -W 2 -d
# after regenerating the pairing the OLD secret gets CONNACK 135
```

Repeat against each broker's internal IP from the backend VM (`<broker-ip>` instead of `<lb-ip>`; the certificate covers them) because the balancer picks one broker per connection: a credential must work on all three.

### Local test procedure (Docker, checklist used for this PR)

Run on a laptop with Docker; it exercises the real startup script and the Java adapter, no GCP.

- [ ] A CA and a server certificate with SAN IPs of the three broker containers and `127.0.0.1`; a `docker network` with fixed IPs.
- [ ] Three `docker:dind` containers (`--privileged`, `-e DOCKER_TLS_CERTDIR=`), each with `eclipse-mosquitto:2.1.2-alpine` loaded (`docker save | docker exec -i vmN docker load`) and `bash` installed (`apk add bash curl`). The inner daemon takes ~15 s to come up.
- [ ] A fixture per broker: `attributes/` (`project-id`, `secret-prefix`, `mosquitto-image`, `mqtt-auth-mode`, `mqtt-backend-username`, `mqtt-dynsec-admin-username`, `mosquitto-conf`, `mqtt-ca-cert`, `mqtt-tls-cert`), `ip`, `secrets/` (`mqtt-tls-key`, `mqtt-broker-users` with the backend, admin and one legacy sensor).
- [ ] `docker exec vmN env BROKER_STARTUP_LOCAL_DIR=/fixture bash /broker-startup.sh` (the variable is a test hook: attributes, VM IP and secrets come from the fixture directory). First boot: `init`, seed, verify, open. Exit `0` and a `mosquitto` container published on `8883`.
- [ ] Run it again: the offline admin reset and the seeding are idempotent. Tamper first (add an ACL to `sensor`, set a default ACL to `allow`, remove the backend's role, change the admin password in the fixture) and check the script puts everything back.
- [ ] Failure: feed an invalid username (`printf 'bad\xffname:pw'`) or a wrong CA: exit `1`, message `the broker is NOT serving traffic`, no `mosquitto` container left.
- [ ] Java adapter, with the brokers' `8883` published on `127.0.0.1:18881-3`: `provision` creates the account on all three; `mosquitto_pub -V mqttv5` as that device gets PUBACK `0` on its own `signal`/`heartbeat` and `135` on another device's topic, on `other/topic` and on `subscribe '#'`; the backend account receives the messages on every broker.
- [ ] `revoke` while the device is connected: `disconnected: administrative action`, then CONNACK `135`; a second `revoke` is a no-op.
- [ ] `provision` with one broker unreachable: `BrokerProvisioningException`, and the account does not exist on the other two. Re-issuing: the old secret gets CONNACK `135` on all three. `revoke` with one broker down: the others are cleaned and the exception is raised at the end.
- [ ] Static mode regression (`mqtt-auth-mode=static` fixture): a legacy sensor publishes only to its own topics.

## Notes

- DB shell from your laptop: `gcloud sql connect spottrack-dev-mysql --user spottrack` (password: `gcloud secrets versions access latest --secret spottrack-dev-db-password`).
- `./tf.sh dev destroy` deletes the dev database too. Prod refuses until you set `deletion_protection` off.
- The backend talks to the brokers by internal IP (same VPC). The emulator uses the `mqtt_lb_endpoint` output.
- Point the frontend's API base URL at the env's `api_url`. If the frontend moves to a new host, add it to `allowedOriginPatterns` in `WebConfig.java`.
