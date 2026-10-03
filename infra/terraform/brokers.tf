# 3 Mosquitto brokers. Emulator reaches them through the TCP load balancer;
# the backend subscribes to each one directly by its fixed internal IP.

# Fixed internal IPs, so the backend always knows where each broker is.
resource "google_compute_address" "broker_internal" {
  count        = var.broker_count
  name         = "spottrack-broker-${count.index + 1}-internal"
  address_type = "INTERNAL"
  subnetwork   = "default"
  region       = "us-central1"
}

resource "google_compute_instance" "broker" {
  count        = var.broker_count
  name         = "spottrack-broker-${count.index + 1}"
  machine_type = "e2-micro"
  zone         = "us-central1-a"
  tags         = ["spottrack-broker"]

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

  # Mosquitto 2 only listens on localhost unless a listener is configured.
  metadata = {
    startup-script = <<-EOT
      #!/bin/bash
      mkdir -p /home/chronos/mosquitto
      cat > /home/chronos/mosquitto/mosquitto.conf <<CONF
      listener 1883
      allow_anonymous true
      CONF
      docker rm -f mosquitto || true
      docker run -d --name mosquitto --restart=always -p 1883:1883 \
        -v /home/chronos/mosquitto/mosquitto.conf:/mosquitto/config/mosquitto.conf:ro \
        eclipse-mosquitto:2
    EOT
  }
}

# Unmanaged group: just a list of the 3 broker VMs for the load balancer to use.
resource "google_compute_instance_group" "brokers" {
  name      = "spottrack-brokers"
  zone      = "us-central1-a"
  instances = google_compute_instance.broker[*].self_link
}

# Passthrough load balancing keeps the client's real IP, so the emulator itself
# (not a Google proxy) connects to the brokers. Health checkers are covered too.
resource "google_compute_firewall" "allow_mqtt" {
  name          = "spottrack-allow-mqtt"
  network       = "default"
  source_ranges = ["0.0.0.0/0"]
  target_tags   = ["spottrack-broker"]

  allow {
    protocol = "tcp"
    ports    = ["1883"]
  }
}
