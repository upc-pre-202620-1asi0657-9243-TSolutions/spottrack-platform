# TCP (network) load balancer for MQTT:
#   IP:1883 -> forwarding rule -> backend service -> one of the 3 brokers
# It picks a broker per connection, not per message.

# Healthy = broker accepts a TCP connection on 1883.
# Must be regional: the TCP balancer rejects global health checks.
resource "google_compute_region_health_check" "mqtt" {
  name   = "spottrack-mqtt-hc"
  region = "us-central1"

  tcp_health_check {
    port = 1883
  }
}

resource "google_compute_region_backend_service" "mqtt" {
  name                  = "spottrack-mqtt-backend"
  region                = "us-central1"
  load_balancing_scheme = "EXTERNAL"
  protocol              = "TCP"
  health_checks         = [google_compute_region_health_check.mqtt.id]

  backend {
    group          = google_compute_instance_group.brokers.id
    balancing_mode = "CONNECTION"
  }
}

resource "google_compute_address" "mqtt" {
  name   = "spottrack-mqtt-ip"
  region = "us-central1"
}

resource "google_compute_forwarding_rule" "mqtt" {
  name                  = "spottrack-mqtt-rule"
  region                = "us-central1"
  load_balancing_scheme = "EXTERNAL"
  ip_protocol           = "TCP"
  ports                 = ["1883"]
  ip_address            = google_compute_address.mqtt.address
  backend_service       = google_compute_region_backend_service.mqtt.id
}
