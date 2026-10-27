# TCP (network) load balancer for MQTT:
#   IP:1883 -> forwarding rule -> backend service -> one of the 3 brokers
# It picks a broker per connection, not per message.

# Healthy = broker accepts a TCP connection on 1883.
# Must be regional: the TCP balancer rejects global health checks.
resource "google_compute_region_health_check" "mqtt" {
  name   = "${local.name}-mqtt-hc"
  region = var.region

  tcp_health_check {
    port = 1883
  }
}

resource "google_compute_region_backend_service" "mqtt" {
  name                  = "${local.name}-mqtt-backend"
  region                = var.region
  load_balancing_scheme = "EXTERNAL"
  protocol              = "TCP"
  health_checks         = [google_compute_region_health_check.mqtt.id]

  backend {
    group          = google_compute_instance_group.brokers.id
    balancing_mode = "CONNECTION"
  }
}

resource "google_compute_address" "mqtt" {
  name   = "${local.name}-mqtt-ip"
  region = var.region
}

resource "google_compute_forwarding_rule" "mqtt" {
  name                  = "${local.name}-mqtt-rule"
  region                = var.region
  load_balancing_scheme = "EXTERNAL"
  ip_protocol           = "TCP"
  ports                 = ["1883"]
  ip_address            = google_compute_address.mqtt.address
  backend_service       = google_compute_region_backend_service.mqtt.id
}
