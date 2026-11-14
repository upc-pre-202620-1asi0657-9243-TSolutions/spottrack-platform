# MQTT channel hardening (Iteration 5: QA-05, CRN-12, CRN-01; DDR-11, DDR-12).
#   - No anonymous access: one user per sensor (username = device_id) and one
#     service account for the backend, checked against a password file.
#   - ACL: each sensor may only write to its own topics; the backend only reads.
#   - TLS on 8883. The TCP balancer is layer 4 and doesn't touch TLS, so each
#     broker presents the same certificate, valid for the balancer IP and for the
#     internal IPs the backend uses.

locals {
  mqtt_backend_username = "spottrack-backend"
  mqtt_tls_port         = 8883
}

# ---------- Certificates (private CA, so no domain is needed) ----------

resource "tls_private_key" "mqtt_ca" {
  algorithm = "RSA"
  rsa_bits  = 2048
}

resource "tls_self_signed_cert" "mqtt_ca" {
  private_key_pem       = tls_private_key.mqtt_ca.private_key_pem
  is_ca_certificate     = true
  validity_period_hours = 24 * 365 * 5

  subject {
    common_name  = "SpotTrack ${var.env} MQTT CA"
    organization = "TSolutions"
  }

  allowed_uses = ["cert_signing", "crl_signing"]
}

resource "tls_private_key" "mqtt_server" {
  algorithm = "RSA"
  rsa_bits  = 2048
}

resource "tls_cert_request" "mqtt_server" {
  private_key_pem = tls_private_key.mqtt_server.private_key_pem

  subject {
    common_name  = google_compute_address.mqtt.address
    organization = "TSolutions"
  }

  # Sensors connect through the balancer IP, the backend to each internal IP.
  ip_addresses = concat(
    [google_compute_address.mqtt.address],
    google_compute_address.broker_internal[*].address,
  )
}

resource "tls_locally_signed_cert" "mqtt_server" {
  cert_request_pem      = tls_cert_request.mqtt_server.cert_request_pem
  ca_private_key_pem    = tls_private_key.mqtt_ca.private_key_pem
  ca_cert_pem           = tls_self_signed_cert.mqtt_ca.cert_pem
  validity_period_hours = 24 * 365 * 3

  allowed_uses = ["digital_signature", "key_encipherment", "server_auth"]
}

# ---------- Credentials ----------

resource "random_password" "mqtt_device" {
  for_each = toset(var.mqtt_device_ids)
  length   = 24
  special  = false
}

locals {
  # Plaintext "user:password" lines; each broker hashes them on boot
  # (mosquitto_passwd -U), so only hashes are stored on disk.
  mqtt_passwd = join("\n", concat(
    ["${local.mqtt_backend_username}:${random_password.generated["mqtt-backend-password"].result}"],
    [for id, pw in random_password.mqtt_device : "${id}:${pw.result}"],
  ))

  mqtt_acl = <<-ACL
    # Backend service account: read only, every sensor.
    user ${local.mqtt_backend_username}
    topic read sensors/+/signal
    topic read sensors/+/heartbeat

    # Sensors (username = device_id): write only, and only their own topics.
    pattern write sensors/%u/signal
    pattern write sensors/%u/heartbeat
  ACL

  mosquitto_conf = <<-CONF
    listener ${local.mqtt_tls_port}
    cafile   /mosquitto/config/certs/ca.crt
    certfile /mosquitto/config/certs/server.crt
    keyfile  /mosquitto/config/certs/server.key
    tls_version tlsv1.2

    allow_anonymous false
    password_file /mosquitto/config/passwd
    acl_file      /mosquitto/config/acl

    # "all" includes the debug lines with denied publishes (QA-05 evidence).
    log_dest stdout
    log_type all
    connection_messages true
    log_timestamp_format %Y-%m-%dT%H:%M:%S
  CONF
}

# ---------- Broker secrets (Secret Manager, not instance metadata) ----------
# Anyone allowed to read the VMs' metadata would otherwise see every password
# and the TLS private key. Each broker reads them on boot with its own service
# account, which can read only these two secrets.

resource "google_service_account" "broker" {
  account_id   = "${local.name}-broker"
  display_name = "SpotTrack ${var.env} MQTT broker VMs"
}

locals {
  mqtt_broker_secrets = {
    # Plaintext "user:password" lines, hashed by each broker on boot.
    mqtt-broker-users = local.mqtt_passwd
    mqtt-tls-key      = tls_private_key.mqtt_server.private_key_pem
  }
}

resource "google_secret_manager_secret" "mqtt_broker" {
  for_each  = toset(keys(local.mqtt_broker_secrets))
  secret_id = "${local.name}-${each.value}"

  replication {
    auto {}
  }
}

resource "google_secret_manager_secret_version" "mqtt_broker" {
  for_each    = google_secret_manager_secret.mqtt_broker
  secret      = each.value.id
  secret_data = local.mqtt_broker_secrets[each.key]
}

resource "google_secret_manager_secret_iam_member" "broker" {
  for_each  = google_secret_manager_secret.mqtt_broker
  secret_id = each.value.id
  role      = "roles/secretmanager.secretAccessor"
  member    = google_service_account.broker.member
}
