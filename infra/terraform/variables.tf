variable "project_id" {
  type = string
}

variable "env" {
  type = string
  validation {
    condition     = contains(["dev", "prod"], var.env)
    error_message = "env must be dev or prod."
  }
}

variable "region" {
  type    = string
  default = "us-central1"
}

variable "zone" {
  type    = string
  default = "us-central1-a"
}

# Backend VM runs the API, MySQL and Caddy together.
variable "backend_machine_type" {
  type    = string
  default = "e2-medium"
}

variable "data_disk_size_gb" {
  type    = number
  default = 10
}

# Empty = <ip-with-dashes>.sslip.io, which Caddy can get a free HTTPS cert for.
variable "domain" {
  type    = string
  default = ""
}

variable "broker_count" {
  type    = number
  default = 3
}

# Extra non-secret env vars for the app (e.g. STRIPE_SUCCESS_URL).
variable "extra_app_env" {
  type    = map(string)
  default = {}
}

# db-f1-micro: shared core, 0.6 GB RAM, cheapest Cloud SQL tier.
variable "db_tier" {
  type    = string
  default = "db-f1-micro"
}

# MQTT user per sensor (username = device_id). Add a sensor here and re-apply
# so it can connect to the brokers; it gets its own generated password.
variable "mqtt_device_ids" {
  type    = list(string)
  default = []
}

# static: password_file + acl_file, accounts of sensors only through mqtt_device_ids.
# dynsec: Mosquitto dynamic-security plugin; the backend creates and revokes the account of each
#         paired sensor at runtime (needs pairing_provisioner_mode = "dynsec"). Changing it needs a
#         broker reset, one broker at a time (see infra/README.md).
variable "mqtt_auth_mode" {
  type    = string
  default = "static"
  validation {
    condition     = contains(["static", "dynsec"], var.mqtt_auth_mode)
    error_message = "mqtt_auth_mode must be static or dynsec."
  }
}

# noop: the backend issues QR payloads but creates no broker account (PR 1 behaviour).
# dynsec: the backend provisions the account on every broker; requires mqtt_auth_mode = "dynsec".
variable "pairing_provisioner_mode" {
  type    = string
  default = "noop"
  validation {
    condition     = contains(["noop", "dynsec"], var.pairing_provisioner_mode)
    error_message = "pairing_provisioner_mode must be noop or dynsec."
  }
  validation {
    condition     = var.pairing_provisioner_mode != "dynsec" || var.mqtt_auth_mode == "dynsec"
    error_message = "pairing_provisioner_mode = dynsec requires mqtt_auth_mode = dynsec."
  }
}

# Pinned patch version. Verified digest (2026-11-15, arm64): see infra/README.md.
variable "mosquitto_image" {
  type    = string
  default = "eclipse-mosquitto:2.1.2-alpine"
}
