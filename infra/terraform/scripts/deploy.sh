#!/bin/bash
# Idempotent deploy, run as root on the backend VM:
#   - by startup.sh on every boot
#   - by GitHub Actions after it sets the app-image metadata
# Keeps caddy running, replaces the app container. The database is Cloud SQL.
set -euo pipefail

md() {
  curl -sf -H "Metadata-Flavor: Google" \
    "http://metadata.google.internal/computeMetadata/v1/instance/attributes/$1"
}
secret() { gcloud secrets versions access latest --secret="$PREFIX-$1"; }
running() { [ "$(docker inspect -f '{{.State.Running}}' "$1" 2>/dev/null)" = "true" ]; }

PREFIX=$(md name-prefix)
DOMAIN=$(md domain)
IMAGE=$(md app-image || true)
DATA=/mnt/data
NET=spottrack

docker network inspect "$NET" >/dev/null 2>&1 || docker network create "$NET"

# Caddy: HTTPS with an automatic Let's Encrypt cert, proxies to the app.
mkdir -p "$DATA/caddy"
cat > "$DATA/caddy/Caddyfile" <<CADDY
$DOMAIN {
  reverse_proxy spottrack:8080
}
CADDY
if running caddy; then
  docker exec caddy caddy reload --config /etc/caddy/Caddyfile
else
  docker rm -f caddy 2>/dev/null || true
  docker run -d --name caddy --network "$NET" --restart=always \
    -p 80:80 -p 443:443 \
    -v "$DATA/caddy/Caddyfile:/etc/caddy/Caddyfile:ro" \
    -v "$DATA/caddy/data:/data" \
    caddy:2
fi

if [ -z "$IMAGE" ]; then
  echo "No app-image set yet; skipping the app."
  exit 0
fi

gcloud auth configure-docker "${IMAGE%%/*}" --quiet
docker pull "$IMAGE"

ENV_FILE=$(mktemp)
trap 'rm -f "$ENV_FILE"' EXIT
{
  md app-env
  echo
  echo "DATABASE_PASSWORD=$(secret db-password)"
  echo "JWT_SECRET=$(secret jwt-secret)"
  echo "STRIPE_SECRET_API_KEY=$(secret stripe-api-key)"
  echo "STRIPE_WEBHOOK_SECRET=$(secret stripe-webhook-secret)"
} > "$ENV_FILE"

docker rm -f spottrack 2>/dev/null || true
docker run -d --name spottrack --network "$NET" --restart=always \
  -p 127.0.0.1:8080:8080 \
  --env-file "$ENV_FILE" \
  "$IMAGE"

# Wait for Spring Boot; fail the deploy (and show logs) if it never comes up.
for _ in $(seq 1 60); do
  if curl -sf -o /dev/null http://127.0.0.1:8080/v3/api-docs; then
    echo "App is up: https://$DOMAIN"
    docker image prune -af --filter "until=168h" >/dev/null || true
    exit 0
  fi
  sleep 5
done
echo "App did not become healthy in 5 minutes." >&2
docker logs --tail 100 spottrack >&2
exit 1
