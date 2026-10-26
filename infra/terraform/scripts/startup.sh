#!/bin/bash
# Runs on every boot of the backend VM: install Docker, mount the data disk,
# then run the deploy script (which starts mysql, caddy and the app).
set -euo pipefail

if ! command -v docker >/dev/null; then
  apt-get update
  apt-get install -y docker.io
fi
systemctl enable --now docker

# Data disk: formatted once, mounted on every boot.
DISK=/dev/disk/by-id/google-data
MNT=/mnt/data
blkid "$DISK" >/dev/null || mkfs.ext4 -F "$DISK"
mkdir -p "$MNT"
mountpoint -q "$MNT" || mount -o discard,defaults "$DISK" "$MNT"
grep -q " $MNT " /etc/fstab || echo "$DISK $MNT ext4 discard,defaults,nofail 0 2" >> /etc/fstab

curl -sf -H "Metadata-Flavor: Google" \
  http://metadata.google.internal/computeMetadata/v1/instance/attributes/deploy-script | bash
