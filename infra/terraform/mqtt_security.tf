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