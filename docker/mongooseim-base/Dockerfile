# SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
#
# SPDX-License-Identifier: AGPL-3.0-only

# Unpack the per-arch .deb with dpkg -x so no maintainer scripts or target-arch code run.
FROM --platform=$BUILDPLATFORM docker.io/library/ubuntu:jammy AS installer

ARG TARGETARCH
ARG BUILDARCH=amd64

RUN apt-get update \
 && apt-get install -y --no-install-recommends ca-certificates \
 && if [ "${TARGETARCH}" != "${BUILDARCH}" ]; then \
      dpkg --add-architecture ${TARGETARCH} \
      && sed -i 's|^deb |deb [arch='"${BUILDARCH}"'] |' /etc/apt/sources.list \
      && printf 'Types: deb\nURIs: http://ports.ubuntu.com/ubuntu-ports/\nSuites: jammy jammy-updates jammy-security\nComponents: main restricted universe multiverse\nArchitectures: %s\nSigned-By: /usr/share/keyrings/ubuntu-archive-keyring.gpg\n' "${TARGETARCH}" \
         > /etc/apt/sources.list.d/ports-${TARGETARCH}.sources; \
    fi \
 && printf 'deb [arch=%s trusted=yes] https://repo.zextras.tools/repository/ubuntu-devel-jammy jammy main\n' "${TARGETARCH}" \
    > /etc/apt/sources.list.d/zextras.list \
 && apt-get update

COPY artifacts/ /tmp/artifacts/

# Pin jammy: artifacts/ also holds the noble and rocky packages.
RUN set -e; \
	pkg=$(ls /tmp/artifacts/carbonio-message-dispatcher-ce_*jammy*_${TARGETARCH}.deb); \
	[ "$(printf '%s\n' "$pkg" | wc -l)" -eq 1 ] || { echo "ambiguous deb match: $pkg" >&2; exit 1; }; \
	mkdir -p /staging /tmp/debs && cd /tmp/debs && \
	apt-get download \
		carbonio-erlang:${TARGETARCH} \
		carbonio-openssl:${TARGETARCH} && \
	cd / && for deb in /tmp/debs/*.deb "$pkg"; do dpkg -x "$deb" /staging; done && \
	rm -rf /tmp/debs /tmp/artifacts /var/lib/apt/lists/*

# Drop host-only payload; a real /lib dir would shadow the usr-merge /lib symlink.
RUN rm -rf /staging/lib \
           /staging/etc/zextras/pending-setups.d \
           /staging/etc/zextras/service-discover \
           /staging/etc/carbonio/message-dispatcher/service-discover

# Compat path only: the release hard-codes LIB_DIR=/opt/zextras/common/lib/mongooseim.
RUN mkdir -p /staging/usr/lib && \
    ln -s /opt/zextras/common/lib/mongooseim /staging/usr/lib/mongooseim

# mongooseimctl re-execs as this user, normally created by the postinst.
RUN groupadd -r carbonio-message-dispatcher \
 && useradd -r -M \
      -g carbonio-message-dispatcher \
      -d /var/lib/carbonio/message-dispatcher \
      -s /sbin/nologin carbonio-message-dispatcher \
 && cp /etc/passwd /etc/group /etc/shadow /etc/gshadow /staging/etc/

RUN mkdir -p /staging/var/lib/carbonio/message-dispatcher \
             /staging/var/lock/carbonio/message-dispatcher \
             /staging/var/log/carbonio/message-dispatcher \
 && chmod -R 755 /staging/opt/zextras/common/lib/mongooseim/log \
 && chown -R carbonio-message-dispatcher:carbonio-message-dispatcher \
      /staging/opt/zextras/common/lib/mongooseim/log \
      /staging/var/lib/carbonio/message-dispatcher \
      /staging/var/lock/carbonio/message-dispatcher \
      /staging/var/log/carbonio/message-dispatcher

FROM --platform=$TARGETPLATFORM docker.io/library/ubuntu:jammy

COPY --from=installer /staging/ /

ENV PATH="/opt/zextras/common/bin:${PATH}"

EXPOSE 10001

ENTRYPOINT ["mongooseimctl", "foreground"]
