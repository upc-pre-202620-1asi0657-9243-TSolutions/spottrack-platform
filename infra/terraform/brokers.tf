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

  # Config, certs and credentials come from metadata (see mqtt_security.tf);
  # the startup script writes them to disk and (re)starts Mosquitto.
  # Changing them needs a VM reset, since the script only runs on boot.
  metadata = {
    mosquitto-conf  = local.mosquitto_conf
    mosquitto-acl   = local.mqtt_acl
    mosquitto-users = local.mqtt_passwd
    mqtt-ca-cert    = tls_self_signed_cert.mqtt_ca.cert_pem
    mqtt-tls-cert   = tls_locally_signed_cert.mqtt_server.cert_pem
    mqtt-tls-key    = tls_private_key.mqtt_server.private_key_pem
    startup-script  = <<-EOT
      #!/bin/bash
      set -e
      md() {
        curl -sf -H "Metadata-Flavor: Google" \
          "http://metadata.google.internal/computeMetadata/v1/instance/attributes/$1"
      }
      DIR=/home/chronos/mosquitto
      rm -rf "$DIR"
      mkdir -p "$DIR/certs"
      md mosquitto-conf  > "$DIR/mosquitto.conf"
      md mosquitto-acl   > "$DIR/acl"
      md mosquitto-users > "$DIR/passwd"
      md mqtt-ca-cert    > "$DIR/certs/ca.crt"
      md mqtt-tls-cert   > "$DIR/certs/server.crt"
      md mqtt-tls-key    > "$DIR/certs/server.key"

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