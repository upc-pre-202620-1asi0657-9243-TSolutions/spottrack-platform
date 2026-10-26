# One VM per env runs three containers on a shared Docker network:
#   caddy (HTTPS :443) -> spottrack (API :8080) -> mysql (:3306)
# MySQL data and Caddy certs live on a separate disk, so they survive VM rebuilds.
# Terraform builds the box; GitHub Actions ships the app (see scripts/deploy.sh).

resource "google_service_account" "backend" {
  account_id   = "${local.name}-vm"
  display_name = "SpotTrack ${var.env} backend VM"
}

resource "google_project_iam_member" "backend" {
  for_each = toset([
    "roles/artifactregistry.reader",
    "roles/logging.logWriter",
  ])
  project = var.project_id
  role    = each.value
  member  = google_service_account.backend.member
}

resource "google_compute_address" "backend" {
  name   = "${local.name}-backend-ip"
  region = var.region
}

resource "google_compute_disk" "data" {
  name = "${local.name}-data"
  type = "pd-balanced"
  zone = var.zone
  size = var.data_disk_size_gb
}

locals {
  domain = var.domain != "" ? var.domain : "${replace(google_compute_address.backend.address, ".", "-")}.sslip.io"

  # Non-secret settings. Secrets are read from Secret Manager on the VM.
  app_env = merge({
    SPRING_PROFILES_ACTIVE = "prod"
    PORT                   = "8080"
    DATABASE_URL           = "mysql"
    DATABASE_PORT          = "3306"
    DATABASE_NAME          = "spottrack"
    DATABASE_USER          = "spottrack"
    MQTT_HOST              = google_compute_address.broker_internal[0].address
    MQTT_PORT              = "1883"
    MQTT_BROKER_URLS       = join(",", [for a in google_compute_address.broker_internal : "tcp://${a.address}:1883"])
  }, var.extra_app_env)
}

resource "google_compute_instance" "backend" {
  name                      = "${local.name}-backend"
  machine_type              = var.backend_machine_type
  zone                      = var.zone
  tags                      = ["${local.name}-backend"]
  allow_stopping_for_update = true

  boot_disk {
    initialize_params {
      # Debian GCE images ship with gcloud, used for registry login and secrets.
      image = "debian-cloud/debian-12"
      size  = 20
    }
  }

  attached_disk {
    source      = google_compute_disk.data.id
    device_name = "data"
  }

  network_interface {
    subnetwork = "default"
    access_config {
      nat_ip = google_compute_address.backend.address
    }
  }

  service_account {
    email  = google_service_account.backend.email
    scopes = ["cloud-platform"]
  }

  metadata = {
    enable-oslogin = "TRUE"
    name-prefix    = local.name
    domain         = local.domain
    app-env        = join("\n", [for k, v in local.app_env : "${k}=${v}"])
    deploy-script  = file("${path.module}/scripts/deploy.sh")
    startup-script = file("${path.module}/scripts/startup.sh")
  }

  lifecycle {
    # GitHub Actions writes app-image on every deploy; Terraform must not undo it.
    ignore_changes = [metadata["app-image"]]
  }

  depends_on = [
    google_secret_manager_secret_iam_member.backend,
    google_secret_manager_secret_version.generated,
  ]
}

resource "google_compute_firewall" "backend_web" {
  name          = "${local.name}-allow-web"
  network       = "default"
  source_ranges = ["0.0.0.0/0"]
  target_tags   = ["${local.name}-backend"]

  allow {
    protocol = "tcp"
    ports    = ["80", "443"]
  }
}

# SSH only through IAP (used by the deploy workflow), never from the open internet.
resource "google_compute_firewall" "backend_iap_ssh" {
  name          = "${local.name}-allow-iap-ssh"
  network       = "default"
  source_ranges = ["35.235.240.0/20"]
  target_tags   = ["${local.name}-backend"]

  allow {
    protocol = "tcp"
    ports    = ["22"]
  }
}
