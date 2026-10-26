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
