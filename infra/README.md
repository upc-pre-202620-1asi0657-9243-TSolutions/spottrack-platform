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
- **Check that it rejects strangers** (QA-05):
  ```bash
  mosquitto_pub -h <lb-ip> -p 8883 --cafile mqtt-ca.crt -t sensors/x/signal -m hi                 # not authorised
  mosquitto_pub -h <lb-ip> -p 8883 --cafile mqtt-ca.crt -u <device_id> -P wrong -t sensors/x/signal -m hi
  mosquitto_pub -h <lb-ip> -p 8883 --cafile mqtt-ca.crt -u <device_id> -P <pw> -q 1 -V mqttv5 \
    -t sensors/other-device/signal -m hi                                                            # PUBACK 135
  ```
  Broker side: `sudo docker logs mosquitto` on a broker VM shows the refused connections and `Denied PUBLISH`.

## Notes

- DB shell from your laptop: `gcloud sql connect spottrack-dev-mysql --user spottrack` (password: `gcloud secrets versions access latest --secret spottrack-dev-db-password`).
- `./tf.sh dev destroy` deletes the dev database too. Prod refuses until you set `deletion_protection` off.
- The backend talks to the brokers by internal IP (same VPC). The emulator uses the `mqtt_lb_endpoint` output.
- Point the frontend's API base URL at the env's `api_url`. If the frontend moves to a new host, add it to `allowedOriginPatterns` in `WebConfig.java`.
