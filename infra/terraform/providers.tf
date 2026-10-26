terraform {
  # Bucket and prefix (env/dev, env/prod) come from tf.sh, so each env has its own state.
  backend "gcs" {}

  required_providers {
    google = {
      source  = "hashicorp/google"
      version = "~> 8.5"
    }
    random = {
      source  = "hashicorp/random"
      version = "~> 3.6"
    }
  }
}

provider "google" {
  project = var.project_id
  region  = var.region
  zone    = var.zone
}

locals {
  # Every resource name starts with this, so dev and prod live side by side.
  name = "spottrack-${var.env}"
}
