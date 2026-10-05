resource "google_compute_firewall" "allow_lb" {
  name = "spottrack-allow-lb"
  network = "default"
  source_ranges = ["130.211.0.0/22", "35.191.0.0/16"]
  target_tags = ["spottrack-backend"]

  allow {
    protocol = "tcp"
    ports = ["8080"]
  }
}

resource "google_compute_instance_template" "backend" {
  name_prefix = "spottrack-"
  machine_type = "e2-medium"
  tags = ["spottrack-backend"]

  disk {
    source_image = "cos-cloud/cos-stable"
    boot = true
  }

  network_interface {
    network = "default"
    access_config {}
  }

  service_account {
    scopes = ["cloud-platform"]
  }

  metadata = {
    startup-script = <<-EOT
    #!/bin/bash
    export HOME=/home/chronos
    docker-credential-gcr configure-docker --registries=us-central1-docker.pkg.dev
    docker rm -f spottrack || true
    docker run -d --name spottrack --restart=always -p 8080:8080 \
      ${join(" ", [for k, v in var.app_env : "-e ${k}='${v}'"])} \
        ${var.image}
    EOT
  }


  lifecycle {
    create_before_destroy = true
  }
}


resource "google_compute_health_check" "backend" {
  name = "spottrack-hc"
  http_health_check {
    port = 8080
    request_path = "/v3/api-docs"

  }
}

resource "google_compute_instance_group_manager" "backend" {
  name = "spottrack-mig"
  base_instance_name = "spottrack"
  target_size = var.instance_count


  version { 
    instance_template = google_compute_instance_template.backend.id 
  }
  named_port {
    name = "http"
    port =8080
  }
  auto_healing_policies {
    health_check = google_compute_health_check.backend.id
    initial_delay_sec = 300
  }
}
