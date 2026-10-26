# Generated secrets get a value here. Stripe secrets are only created empty:
# add their values by hand (see infra/README.md), so they never touch Terraform state.

resource "random_password" "generated" {
  for_each = {
    db-password         = 32
    mysql-root-password = 32
    # HS512 needs at least 64 bytes.
    jwt-secret = 96
  }
  length  = each.value
  special = false
}

locals {
  manual_secrets = ["stripe-api-key", "stripe-webhook-secret"]
}

resource "google_secret_manager_secret" "app" {
  for_each  = toset(concat(keys(random_password.generated), local.manual_secrets))
  secret_id = "${local.name}-${each.value}"

  replication {
    auto {}
  }
}

resource "google_secret_manager_secret_version" "generated" {
  for_each    = random_password.generated
  secret      = google_secret_manager_secret.app[each.key].id
  secret_data = each.value.result
}

resource "google_secret_manager_secret_iam_member" "backend" {
  for_each  = google_secret_manager_secret.app
  secret_id = each.value.id
  role      = "roles/secretmanager.secretAccessor"
  member    = google_service_account.backend.member
}
