#!/bin/bash
# Idempotent deploy, run as root on the backend VM:
#   - by startup.sh on every boot (deploys the app-image metadata)
#   - by GitHub Actions with IMAGE=<new image>; it records app-image only on success
# Keeps caddy running, replaces the app container. If the new one never gets
# healthy, the previous container is brought back. The database is Cloud SQL.
set -euo pipefail

md() {
  curl -sf -H "Metadata-Flavor: Google" \
    "http://metadata.google.internal/computeMetadata/v1/instance/attributes/$1"
}
secret() { gcloud secrets versions access latest --secret="$PREFIX-$1"; }
running() { [ "$(docker inspect -f '{{.State.Running}}' "$1" 2>/dev/null)" = "true" ]; }
exists() { docker inspect "$1" >/dev/null 2>&1; }
# Wait up to 5 minutes for Spring Boot to answer.
healthy() {
  for _ in $(seq 1 60); do
    curl -sf -o /dev/null http://127.0.0.1:8080/v3/api-docs && return 0
    sleep 5
  done
  return 1
}

PREFIX=$(md name-prefix)
DOMAIN=$(md domain)
IMAGE=${IMAGE:-$(md app-image || true)}
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

# Park the current container (stopped, no auto-restart) as the rollback target.
docker rm -f spottrack-prev 2>/dev/null || true
if exists spottrack; then
  docker stop spottrack >/dev/null
  docker update --restart=no spottrack >/dev/null
  docker rename spottrack spottrack-prev
fi

docker run -d --name spottrack --network "$NET" --restart=always \
  -p 127.0.0.1:8080:8080 \
  --env-file "$ENV_FILE" \
  "$IMAGE"

if healthy; then
  docker rm -f spottrack-prev >/dev/null 2>&1 || true
  echo "App is up: https://$DOMAIN ($IMAGE)"
  docker image prune -af --filter "until=168h" >/dev/null || true
  exit 0
fi

echo "App did not become healthy in 5 minutes." >&2
docker logs --tail 100 spottrack >&2
docker rm -f spottrack
if exists spottrack-prev; then
  docker rename spottrack-prev spottrack
  docker update --restart=always spottrack >/dev/null
  docker start spottrack >/dev/null
  if healthy; then
    echo "Rolled back to $(docker inspect -f '{{.Config.Image}}' spottrack)." >&2
  else
    echo "Rolled back, but the previous version is not healthy either." >&2
  fi
fi
exit 1
