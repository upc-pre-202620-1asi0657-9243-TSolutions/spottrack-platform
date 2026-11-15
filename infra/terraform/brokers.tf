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
  # instance metadata. scripts/broker-startup.sh writes them to disk and (re)starts
  # Mosquitto. Changing them needs a VM reset, since the script only runs on boot.
  metadata = merge({
    startup-script             = file("${path.module}/scripts/broker-startup.sh")
    project-id                 = var.project_id
    secret-prefix              = local.name
    mosquitto-image            = var.mosquitto_image
    mqtt-auth-mode             = var.mqtt_auth_mode
    mqtt-backend-username      = local.mqtt_backend_username
    mqtt-dynsec-admin-username = local.mqtt_dynsec_admin_username
    mosquitto-conf             = local.mosquitto_conf
    mqtt-ca-cert               = tls_self_signed_cert.mqtt_ca.cert_pem
    mqtt-tls-cert              = tls_locally_signed_cert.mqtt_server.cert_pem
  }, local.mqtt_dynsec ? {} : { mosquitto-acl = local.mqtt_acl })

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