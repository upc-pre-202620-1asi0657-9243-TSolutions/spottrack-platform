output "api_url" {
  description = "Point the frontend here."
  value       = "https://${local.domain}"
}

output "backend_vm" {
  value = google_compute_instance.backend.name
}

output "mqtt_lb_endpoint" {
  description = "Point the emulator here."
  value       = "ssl://${google_compute_address.mqtt.address}:${local.mqtt_tls_port}"
}

output "broker_internal_ips" {
  description = "Backend subscribes to each of these directly."
  value       = google_compute_address.broker_internal[*].address
}

output "mqtt_ca_cert" {
  description = "CA that signed the brokers' certificate. Clients need it to trust TLS."
  value       = tls_self_signed_cert.mqtt_ca.cert_pem
}

output "mqtt_device_credentials" {
  description = "device_id -> password, for the emulator (MQTT_CREDENTIALS_FILE)."
  value       = { for id, pw in random_password.mqtt_device : id => pw.result }
  sensitive   = true
}
