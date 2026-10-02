# SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
#
# SPDX-License-Identifier: AGPL-3.0-only

ARG MONGOOSEIM_IMAGE=registry.dev.zextras.com/dev/mongooseim-ce:6.6.0-2
FROM --platform=$TARGETPLATFORM ${MONGOOSEIM_IMAGE}

COPY --from=docker.io/hashicorp/consul:2.0 /bin/consul /usr/local/bin/consul

# The package/ template has the sidecar addresses pre-filled.
COPY package/mongooseim.toml.in /etc/carbonio/message-dispatcher/mongooseim.toml.in

COPY --chmod=755 package/carbonio-message-dispatcher-config-setup /usr/local/bin/carbonio-message-dispatcher-config-setup

COPY --chmod=755 docker/mongoose/entrypoint.sh /entrypoint.sh

EXPOSE 10000 10001

ENTRYPOINT ["/entrypoint.sh"]
