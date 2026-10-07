output "mqtt_lb_endpoint" {
  description = "Point the emulator here."
  value       = "tcp://${google_compute_address.mqtt.address}:1883"
}

output "broker_internal_ips" {
  description = "Backend subscribes to each of these directly."
  value       = google_compute_address.broker_internal[*].address
}
