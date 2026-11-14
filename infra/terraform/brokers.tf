# 3 Mosquitto brokers. Emulator reaches them through the TCP load balancer;
# the backend subscribes to each one directly by its fixed internal IP.

# Fixed internal IPs, so the backend always knows where each broker is.
resource "google_compute_address" "broker_internal" {
  count        = var.broker_count
  name         = "${local.name}-broker-${count.index + 1}-internal"
  address_type = "INTERNAL"
  subnetwork   = "default"
  region       = var.region
}

resource "google_compute_instance" "broker" {
  count        = var.broker_count
  name         = "${local.name}-broker-${count.index + 1}"
  machine_type = "e2-micro"
  zone         = var.zone
  tags         = ["${local.name}-broker"]

  boot_disk {
    initialize_params {
      image = "cos-cloud/cos-stable"
    }
  }

  network_interface {
    subnetwork = "default"
    network_ip = google_compute_address.broker_internal[count.index].address
    # Public IP so the VM can pull eclipse-mosquitto from Docker Hub.
    access_config {}
  }

  # Reads the broker secrets (passwords, TLS key) from Secret Manager on boot.
  service_account {
    email  = google_service_account.broker.email
    scopes = ["cloud-platform"]
  }
  allow_stopping_for_update = true

  # Public config and certs come from metadata; passwords and the TLS key come
  # from Secret Manager (see mqtt_security.tf), so they never show up in the
  # instance metadata. The startup script writes them to disk and (re)starts
  # Mosquitto. Changing them needs a VM reset, since the script only runs on boot.
  metadata = {
    mosquitto-conf = local.mosquitto_conf
    mosquitto-acl  = local.mqtt_acl
    mqtt-ca-cert   = tls_self_signed_cert.mqtt_ca.cert_pem
    mqtt-tls-cert  = tls_locally_signed_cert.mqtt_server.cert_pem
    startup-script = <<-EOT
      #!/bin/bash
      set -eo pipefail
      md() {
        curl -sf -H "Metadata-Flavor: Google" \
          "http://metadata.google.internal/computeMetadata/v1/$1"
      }
      # COS has no gcloud: call the Secret Manager API with the VM's token.
      secret() {
        local token
        token=$(md instance/service-accounts/default/token \
          | grep -o '"access_token": *"[^"]*"' | cut -d'"' -f4)
        curl -sf -H "Authorization: Bearer $token" \
          "https://secretmanager.googleapis.com/v1/projects/${var.project_id}/secrets/${local.name}-$1/versions/latest:access" \
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
      DIR=/home/chronos/mosquitto
      rm -rf "$DIR"
      mkdir -p "$DIR/certs"
      md instance/attributes/mosquitto-conf > "$DIR/mosquitto.conf"
      md instance/attributes/mosquitto-acl  > "$DIR/acl"
      md instance/attributes/mqtt-ca-cert   > "$DIR/certs/ca.crt"
      md instance/attributes/mqtt-tls-cert  > "$DIR/certs/server.crt"
      fetch_secret mqtt-broker-users "$DIR/passwd"
      fetch_secret mqtt-tls-key      "$DIR/certs/server.key"

      # Hash the plaintext passwords in place.
      docker run --rm -v "$DIR:/mosquitto/config" eclipse-mosquitto:2 \
        mosquitto_passwd -U /mosquitto/config/passwd

      # The container runs as the mosquitto user (uid 1883).
      chown -R 1883:1883 "$DIR"
      chmod 700 "$DIR" "$DIR/certs"
      chmod 600 "$DIR/passwd" "$DIR/acl" "$DIR/certs/server.key"

      docker rm -f mosquitto || true
      docker run -d --name mosquitto --restart=always -p 8883:8883 \
        --log-opt max-size=10m --log-opt max-file=3 \
        -v "$DIR:/mosquitto/config" \
        eclipse-mosquitto:2
    EOT
  }

  depends_on = [
    google_secret_manager_secret_iam_member.broker,
    google_secret_manager_secret_version.mqtt_broker,
  ]
}

# Unmanaged group: just a list of the 3 broker VMs for the load balancer to use.
resource "google_compute_instance_group" "brokers" {
  name      = "${local.name}-brokers"
  zone      = var.zone
  instances = google_compute_instance.broker[*].self_link
}

# Passthrough load balancing keeps the client's real IP, so the emulator itself
# (not a Google proxy) connects to the brokers. Health checkers are covered too.
resource "google_compute_firewall" "allow_mqtt" {
  name          = "${local.name}-allow-mqtt"
  network       = "default"
  source_ranges = ["0.0.0.0/0"]
  target_tags   = ["${local.name}-broker"]

  # Only the TLS listener; 1883 is no longer published (DDR-12).
  allow {
    protocol = "tcp"
    ports    = ["8883"]
  }
}