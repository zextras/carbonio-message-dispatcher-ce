# SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
#
# SPDX-License-Identifier: AGPL-3.0-only

ARG MONGOOSEIM_IMAGE=registry.dev.zextras.com/dev/mongooseim-ce:6.6.0-2
FROM --platform=$TARGETPLATFORM ${MONGOOSEIM_IMAGE}

# openssl (needed by config-setup, package/carbonio-message-dispatcher-config-setup:46,
# to mint the API password) is already bundled in the base image: it ships
# carbonio-openssl, which installs its CLI at /opt/zextras/common/bin/openssl,
# and puts that dir on PATH via ENV (see docker/mongooseim-base/Dockerfile).
# Nothing to install here, so this stage has zero RUN instructions and the
# arm64 leg of the multi-platform build needs no QEMU emulation.

# No USER root either: the base image (docker/mongooseim-base/Dockerfile)
# sets no non-root USER, so the container already runs as root by default.

# Consul CLI (needed by config-setup to read KV)
COPY --from=docker.io/hashicorp/consul:2.0 /bin/consul /usr/local/bin/consul

# MongooseIM config template (package version has sidecar addresses pre-filled)
COPY package/mongooseim.toml.in /etc/carbonio/message-dispatcher/mongooseim.toml.in

# Config-setup script (reads DB creds from Consul KV and renders mongooseim.toml)
COPY --chmod=755 package/carbonio-message-dispatcher-config-setup /usr/local/bin/carbonio-message-dispatcher-config-setup

# Entrypoint
COPY --chmod=755 docker/mongoose/entrypoint.sh /entrypoint.sh

# Expose ports:
# 10000 - GraphQL Admin API / XMPP HTTP
# 10001 - XMPP C2S
EXPOSE 10000 10001

ENTRYPOINT ["/entrypoint.sh"]
