#Pick a VM 
resource "google_compute_backend_service" "backend" {
  name = "spottrack-backend"
  load_balancing_scheme = "EXTERNAL_MANAGED"
  protocol = "HTTP"
  port_name = "http"
  timeout_sec = 3600

  health_checks = [google_compute_health_check.bkacned.id]

  backend {
    group = google_compute_instance_group_manager.backend.instance_group
  }

}

#Spread requests acrross healthy VMs.


# Url map
resource "google+compute_url_map" "lb" {
  name = "spottrack-urlmap"
  default_service = google_compute_backend_service.backend.id
}



#Proxy: reads http

resource "google_compute_target_http_proxy" "lb" {
  name = "spottrack-proxy"
  url_map = google_compute_url_map.lb.id
}

#Take connection. Read request. Hand to URL map 


resource "google_compute_global_address" "lb" {
  name = "spottrack-ip"
}

resource "google_compute_global_forwarding_rule" "lb" {
   name = "spottrack-rule"
   load_balancing_scheme = "EXTERNAL_MANAGED"
   ip_address = google_compute_global_address.lb.id
   port_range = "80"
   target = google_compute_target_http_proxy.lb.id
 }


