#!/bin/bash
# Startup script of the MQTT broker VMs (Container-Optimized OS). Runs on every boot.
# Terraform passes everything as instance metadata attributes (see brokers.tf), so this
# file has no template placeholders and can be checked with `bash -n`.
#
#   mqtt-auth-mode = static : password_file + acl_file (the original behaviour).
#   mqtt-auth-mode = dynsec : Mosquitto dynamic-security plugin; accounts of paired
#                             sensors are created at runtime by the backend.
#
# Testing outside GCP: BROKER_STARTUP_LOCAL_DIR=<dir> reads the metadata from
# <dir>/attributes/<name>, the VM IP from <dir>/ip and the secrets from <dir>/secrets/<name>.
set -euo pipefail

LOCAL_DIR=${BROKER_STARTUP_LOCAL_DIR:-}

md() {
  curl -sf -H "Metadata-Flavor: Google" \
    "http://metadata.google.internal/computeMetadata/v1/$1"
}
attr() {
  if [ -n "$LOCAL_DIR" ]; then cat "$LOCAL_DIR/attributes/$1"; else md "instance/attributes/$1"; fi
}
vm_ip() {
  if [ -n "$LOCAL_DIR" ]; then cat "$LOCAL_DIR/ip"; else md instance/network-interfaces/0/ip; fi
}
# COS has no gcloud: call the Secret Manager API with the VM's token.
secret() {
  if [ -n "$LOCAL_DIR" ]; then cat "$LOCAL_DIR/secrets/$1"; return; fi
  local token
  token=$(md instance/service-accounts/default/token \
    | grep -o '"access_token": *"[^"]*"' | cut -d'"' -f4)
  curl -sf -H "Authorization: Bearer $token" \
    "https://secretmanager.googleapis.com/v1/projects/$PROJECT_ID/secrets/$SECRET_PREFIX-$1/versions/latest:access" \
    | grep -o '"data": *"[^"]*"' | cut -d'"' -f4 | base64 -d
}
# Retried: on first boot the IAM grant can take a moment to apply.
fetch_secret() {
  for _ in $(seq 1 30); do
    if secret "$1" > "$2" && [ -s "$2" ]; then return 0; fi
    sleep 10
  done
  echo "Could not read secret $1" >&2
  return 1
}
log() { echo "broker-startup: $*"; }

PROJECT_ID=$(attr project-id)
SECRET_PREFIX=$(attr secret-prefix)
IMAGE=$(attr mosquitto-image)
AUTH_MODE=$(attr mqtt-auth-mode)
BACKEND_USER=$(attr mqtt-backend-username)

DIR=/home/chronos/mosquitto
# Not under $DIR: the config directory is wiped on every boot, the dynsec state must survive.
STATE=/var/lib/mosquitto-dynsec
DYNSEC_JSON=$STATE/dynamic-security.json

rm -rf "$DIR"
mkdir -p "$DIR/certs"
attr mosquitto-conf > "$DIR/mosquitto.conf"
attr mqtt-ca-cert   > "$DIR/certs/ca.crt"
attr mqtt-tls-cert  > "$DIR/certs/server.crt"
fetch_secret mqtt-tls-key "$DIR/certs/server.key"

# ---------------------------------------------------------------- static mode
if [ "$AUTH_MODE" = "static" ]; then
  attr mosquitto-acl > "$DIR/acl"
  fetch_secret mqtt-broker-users "$DIR/passwd"

  # Hash the plaintext passwords in place.
  docker run --rm -v "$DIR:/mosquitto/config" "$IMAGE" \
    mosquitto_passwd -U /mosquitto/config/passwd

  # The container runs as the mosquitto user (uid 1883).
  chown -R 1883:1883 "$DIR"
  chmod 700 "$DIR" "$DIR/certs"
  chmod 600 "$DIR/passwd" "$DIR/acl" "$DIR/certs/server.key"

  docker rm -f mosquitto || true
  docker run -d --name mosquitto --restart=always -p 8883:8883 \
    --log-opt max-size=10m --log-opt max-file=3 \
    -v "$DIR:/mosquitto/config" \
    "$IMAGE"
  exit 0
fi

if [ "$AUTH_MODE" != "dynsec" ]; then
  echo "Unknown mqtt-auth-mode '$AUTH_MODE' (expected static or dynsec)" >&2
  exit 1
fi

# ---------------------------------------------------------------- dynsec mode
ADMIN_USER=$(attr mqtt-dynsec-admin-username)
BROKER_IP=$(vm_ip)
PLAIN=$(mktemp -d)
trap 'rm -rf "$PLAIN"' EXIT
chmod 700 "$PLAIN"

# "user:password" lines: the backend, the admin and the legacy sensors of mqtt_device_ids.
fetch_secret mqtt-broker-users "$PLAIN/users"
user_password() {
  local line
  line=$(grep -m1 "^$1:" "$PLAIN/users") || { echo "No password for $1 in mqtt-broker-users" >&2; return 1; }
  printf '%s' "${line#*:}"
}
ADMIN_PW=$(user_password "$ADMIN_USER")
BACKEND_PW=$(user_password "$BACKEND_USER")
export MQ_ADMIN_PW=$ADMIN_PW

chown -R 1883:1883 "$DIR"
chmod 700 "$DIR" "$DIR/certs"
chmod 600 "$DIR/certs/server.key"

# Docker may have restarted the old container (--restart=always) before this script: the state
# file must not be edited while a broker holds it.
docker rm -f mosquitto >/dev/null 2>&1 || true
mkdir -p "$STATE"
if [ ! -s "$DYNSEC_JSON" ]; then
  log "first boot: creating the dynamic-security state offline"
  docker run --rm -v "$STATE:/mosquitto/dynsec" -e MQ_ADMIN_PW "$IMAGE" \
    sh -c 'exec mosquitto_ctrl dynsec init /mosquitto/dynsec/dynamic-security.json "$0" "$MQ_ADMIN_PW"' "$ADMIN_USER"
else
  # The broker is stopped, so the admin password can be reset straight in the state file.
  log "resetting the dynsec admin password offline"
  docker run --rm -v "$STATE:/mosquitto/dynsec" -e MQ_ADMIN_PW "$IMAGE" \
    sh -c 'exec mosquitto_ctrl -f /mosquitto/dynsec/dynamic-security.json dynsec setClientPassword "$0" "$MQ_ADMIN_PW"' "$ADMIN_USER"
fi
[ -s "$DYNSEC_JSON" ] || { echo "dynamic-security state was not created" >&2; exit 1; }
chown -R 1883:1883 "$STATE"
chmod 700 "$STATE"
chmod 600 "$DYNSEC_JSON"

run_broker() { # $1 = published address
  docker rm -f mosquitto >/dev/null 2>&1 || true
  docker run -d --name mosquitto "${@:2}" -p "$1" \
    --log-opt max-size=10m --log-opt max-file=3 \
    -v "$DIR:/mosquitto/config" \
    -v "$STATE:/mosquitto/dynsec" \
    "$IMAGE"
}
fail() {
  echo "broker-startup: $*" >&2
  echo "broker-startup: the broker is NOT serving traffic" >&2
  docker rm -f mosquitto >/dev/null 2>&1 || true
  exit 1
}

# Until the seeding is verified the listener is published on the VM's internal IP only: the
# load balancer delivers to the balancer IP, so no device can reach the broker half-configured.
run_broker "$BROKER_IP:8883:8883" --restart=no >/dev/null

# mosquitto_ctrl runs on the host network and connects to this broker's internal IP, which the
# certificate covers; the CA is verified (no --insecure). Its exit code is NOT reliable: a TLS
# failure exits 0 without output, and logical errors ("Client already exists") also exit 0.
# That is why every reply is inspected and the final state is verified, never $?.
ctrl() {
  docker run --rm --network host -e MQ_ADMIN_PW -v "$DIR/certs/ca.crt:/ca.crt:ro" "$IMAGE" \
    sh -c 'host=$0; user=$1; shift; exec mosquitto_ctrl -h "$host" -p 8883 --cafile /ca.crt -u "$user" -P "$MQ_ADMIN_PW" "$@"' \
    "$BROKER_IP" "$ADMIN_USER" "$@" 2>&1 || true
}
# True when the reply of a command matches a pattern (the reply goes through a variable, not a pipe,
# so that grep -q closing early cannot make pipefail report a failure).
ctrl_has() { # $1 = extended regex, rest = mosquitto_ctrl arguments
  local out
  out=$(ctrl "${@:2}")
  grep -Eq "$1" <<< "$out"
}
# Replies that mean "already in the wanted state".
TOLERATED='Client already exists|Role already exists|ACL with this topic already exists|ACL not found|Client not found'
run() {
  local out
  out=$(ctrl "$@")
  if grep -qi 'error' <<< "$out"; then
    grep -Eq "$TOLERATED" <<< "$out" || fail "mosquitto_ctrl ${1:-} ${2:-} failed: $(head -3 <<< "$out")"
  fi
}
has_client() { grep -Fqx -- "$1" <<< "$CLIENTS"; }

log "waiting for the broker to accept the admin account over TLS"
READY=0
for _ in $(seq 1 30); do
  CLIENTS=$(ctrl dynsec listClients)
  if has_client "$ADMIN_USER"; then READY=1; break; fi
  sleep 2
done
[ "$READY" = 1 ] || fail "the broker never answered listClients as $ADMIN_USER: $(echo "$CLIENTS" | head -3)"

# Makes a role contain exactly the wanted ACLs ("type|allow|topic" lines), removing the others.
role_acls() {
  ctrl dynsec getRole "$1" \
    | sed -nE 's/^(ACLs: +| +)?([A-Za-z]+) +: (allow|deny) : (.*) \(priority: -?[0-9]+\)$/\2|\3|\4/p' | sort
}
reconcile_role() { # $1 = role, rest = "type|allow|topic"
  local role=$1 current wanted line type effect topic
  shift
  run dynsec createRole "$role"
  current=$(role_acls "$role")
  wanted=$(printf '%s\n' "$@" | sort)
  for line in $(comm -23 <(echo "$current") <(echo "$wanted")); do
    IFS='|' read -r type effect topic <<< "$line"
    run dynsec removeRoleACL "$role" "$type" "$topic"
  done
  for line in $(comm -13 <(echo "$current") <(echo "$wanted")); do
    IFS='|' read -r type effect topic <<< "$line"
    run dynsec addRoleACL "$role" "$type" "$topic" "$effect"
  done
  [ "$(role_acls "$role")" = "$wanted" ] || fail "role $role does not have exactly the expected ACLs"
}

log "seeding roles"
# 'dynsec init' makes the admin role read the whole broker ('#', '$SYS/#'); only the control topic is needed.
reconcile_role admin \
  'publishClientSend|allow|$CONTROL/dynamic-security/#' \
  'publishClientReceive|allow|$CONTROL/dynamic-security/#' \
  'subscribePattern|allow|$CONTROL/dynamic-security/#'
# A sensor may only publish to its own topics (%u = its username) and cannot subscribe to anything.
reconcile_role sensor \
  'publishClientSend|allow|sensors/%u/signal' \
  'publishClientSend|allow|sensors/%u/heartbeat'
# The backend only reads the sensors' topics.
reconcile_role backend-reader \
  'subscribeLiteral|allow|sensors/+/signal' \
  'subscribeLiteral|allow|sensors/+/heartbeat' \
  'publishClientReceive|allow|sensors/+/signal' \
  'publishClientReceive|allow|sensors/+/heartbeat'
for access in publishClientSend publishClientReceive subscribe unsubscribe; do
  run dynsec setDefaultACLAccess "$access" deny
done

log "seeding accounts"
CLIENTS=$(ctrl dynsec listClients)
if has_client "$BACKEND_USER"; then
  run dynsec setClientPassword "$BACKEND_USER" "$BACKEND_PW"
else
  run dynsec createClient "$BACKEND_USER" -p "$BACKEND_PW"
fi
# addClientRole answers "Internal error." when the role is already assigned: ask first.
ensure_role() { # $1 = user, $2 = role
  ctrl_has "(^|[[:space:]])$2 \(priority" dynsec getClient "$1" || run dynsec addClientRole "$1" "$2"
}
ensure_role "$BACKEND_USER" backend-reader

# Sensors of mqtt_device_ids keep working. Only created when absent: a device that was paired
# later has a newer password that must not be overwritten.
while IFS= read -r line; do
  device=${line%%:*}
  [ "$device" = "$BACKEND_USER" ] || [ "$device" = "$ADMIN_USER" ] || [ -z "$device" ] && continue
  if ! has_client "$device"; then
    run dynsec createClient "$device" -p "${line#*:}"
    ensure_role "$device" sensor
  fi
done < "$PLAIN/users"

log "verifying the final state"
CLIENTS=$(ctrl dynsec listClients)
has_client "$ADMIN_USER" || fail "$ADMIN_USER is missing after the seeding"
has_client "$BACKEND_USER" || fail "$BACKEND_USER is missing after the seeding"
ctrl_has '(^|[[:space:]])backend-reader \(priority' dynsec getClient "$BACKEND_USER" || fail "$BACKEND_USER has no backend-reader role"
ctrl_has '(^|[[:space:]])admin \(priority' dynsec getClient "$ADMIN_USER" || fail "$ADMIN_USER has no admin role"
while IFS= read -r line; do
  device=${line%%:*}
  [ "$device" = "$BACKEND_USER" ] || [ "$device" = "$ADMIN_USER" ] || [ -z "$device" ] && continue
  has_client "$device" || fail "legacy sensor $device is missing after the seeding"
done < "$PLAIN/users"
DEFAULTS=$(ctrl dynsec getDefaultACLAccess)
[ "$(grep -c ': deny' <<< "$DEFAULTS")" = 4 ] || fail "the default ACL access is not deny for every type"

log "seeding verified, opening the listener to the load balancer"
docker stop -t 10 mosquitto >/dev/null
run_broker "8883:8883" --restart=always >/dev/null
for _ in $(seq 1 30); do
  CLIENTS=$(ctrl dynsec listClients)
  if has_client "$ADMIN_USER"; then log "broker up in dynsec mode"; exit 0; fi
  sleep 2
done
fail "the broker did not come back after opening the listener"
