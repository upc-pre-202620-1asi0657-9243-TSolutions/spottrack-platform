# Managed MySQL (Cloud SQL), one per env.
# Public IP, but only the backend VM's static IP may connect, and only over TLS.

resource "google_sql_database_instance" "main" {
  name             = "${local.name}-mysql"
  database_version = "MYSQL_8_4"
  region           = var.region

  # Prod can't be dropped by accident; dev can be torn down freely.
  deletion_protection = var.env == "prod"

  settings {
    # Shared-core tiers only exist in the Enterprise edition.
    edition           = "ENTERPRISE"
    tier              = var.db_tier
    disk_type         = "PD_SSD"
    disk_size         = 10
    disk_autoresize   = true
    availability_type = "ZONAL"

    backup_configuration {
      enabled    = var.env == "prod"
      start_time = "07:00" # 02:00 in Lima
    }

    ip_configuration {
      ipv4_enabled = true
      ssl_mode     = "ENCRYPTED_ONLY"

      authorized_networks {
        name  = "${local.name}-backend"
        value = "${google_compute_address.backend.address}/32"
      }
    }

    deletion_protection_enabled = var.env == "prod"
  }
}

resource "google_sql_database" "spottrack" {
  name     = "spottrack"
  instance = google_sql_database_instance.main.name
}

resource "google_sql_user" "spottrack" {
  name     = "spottrack"
  instance = google_sql_database_instance.main.name
  password = random_password.generated["db-password"].result
}
