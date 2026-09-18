#!/bin/bash

# SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
#
# SPDX-License-Identifier: AGPL-3.0-only

set -euo pipefail

DISTRO=${1:-ubuntu-jammy}
YAP_VERSION=${YAP_VERSION:-2.6.1}
ROOT=$(cd "$(dirname "$0")" && pwd)

case "$DISTRO" in
  ubuntu-jammy)
    REPO="name=zextras,url=https://repo.zextras.io/release/ubuntu,suite=jammy,components=main,format=deb,gpgCheck=false,distros=ubuntu"
    ;;
  rocky-8)
    REPO="name=zextras,url=https://repo.zextras.io/release/rhel8/,format=rpm,gpgCheck=false,distros=rocky"
    ;;
  *)
    echo "Unsupported distribution: $DISTRO (use ubuntu-jammy or rocky-8)"
    exit 1
    ;;
esac

if command -v podman >/dev/null 2>&1 && podman info >/dev/null 2>&1; then
  RUNTIME=podman
elif command -v docker >/dev/null 2>&1 && docker info >/dev/null 2>&1; then
  RUNTIME=docker
else
  echo "Docker or Podman must be installed and running"
  exit 1
fi

cd "$ROOT"
mvn clean package -DskipTests
cp carbonio-message-dispatcher-auth/target/carbonio-message-dispatcher-auth-*-fatjar.jar \
  package/carbonio-message-dispatcher-auth.jar
mkdir -p "artifacts/$DISTRO"

# $1/$2 in the command belong to the container shell.
# shellcheck disable=SC2016
"$RUNTIME" run --rm --platform linux/amd64 --user root \
  --entrypoint=bash \
  -e YAP_ALLOW_UNVERIFIED_REPOS=1 \
  -v "$ROOT/artifacts/$DISTRO:/artifacts" \
  -v "$ROOT:/tmp/staging:ro" \
  "docker.io/m0rf30/yap-$DISTRO:$YAP_VERSION" \
  -c 'yap prepare "$1" --repo "$2" && yap build "$1" /tmp/staging --repo "$2" -U' \
  -- "$DISTRO" "$REPO"
