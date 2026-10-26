# SpotTrack infra (GCP)

Two environments, one GCP project (`spottrack-507400`). Every resource name starts with `spottrack-dev-` or `spottrack-prod-`.

| Branch    | Env  | Deployed by                                  |
|-----------|------|----------------------------------------------|
| `develop` | dev  | `.github/workflows/deploy.yml`, on push      |
| `main`    | prod | same workflow, after approval on `prod` env  |

Each env has:
- **Backend VM** (`spottrack-<env>-backend`): Caddy (HTTPS) → API → MySQL 8.4, all in Docker. MySQL data sits on a separate disk (`spottrack-<env>-data`).
- **3 Mosquitto brokers** + a TCP load balancer for the emulator.
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
- **Manual redeploy / debug:**
  ```bash
  gcloud compute ssh spottrack-dev-backend --zone us-central1-a --tunnel-through-iap
  sudo docker logs -f spottrack
  sudo docker exec -it mysql mysql -u spottrack -p spottrack   # password: gcloud secrets versions access latest --secret spottrack-dev-db-password
  ```

## Notes

- `./tf.sh <env> destroy` keeps nothing, including the MySQL data disk. Back up first: `sudo docker exec mysql mysqldump ...`.
- The backend talks to the brokers by internal IP (same VPC). The emulator uses the `mqtt_lb_endpoint` output.
- Point the frontend's API base URL at the env's `api_url`. If the frontend moves to a new host, add it to `allowedOriginPatterns` in `WebConfig.java`.
