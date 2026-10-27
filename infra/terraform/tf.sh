#!/usr/bin/env bash
# Run Terraform against one environment, with its own remote state.
#   ./tf.sh dev plan
#   ./tf.sh prod apply
#   ./tf.sh dev output api_url
# State bucket defaults to <project_id>-tfstate; override with TF_STATE_BUCKET.
set -euo pipefail

ENV=${1:?usage: tf.sh <dev|prod> <terraform command> [args]}
shift
cd "$(dirname "$0")"

VARS="envs/$ENV.tfvars"
[ -f "$VARS" ] || { echo "No $VARS" >&2; exit 1; }
PROJECT=$(sed -n 's/^project_id *= *"\(.*\)"/\1/p' "$VARS")
BUCKET=${TF_STATE_BUCKET:-$PROJECT-tfstate}

terraform init -reconfigure -input=false \
  -backend-config="bucket=$BUCKET" \
  -backend-config="prefix=env/$ENV" >/dev/null

CMD=${1:?missing terraform command}
shift
case "$CMD" in
  plan | apply | destroy | import | refresh | console)
    terraform "$CMD" -var-file="$VARS" "$@" ;;
  *)
    terraform "$CMD" "$@" ;;
esac
