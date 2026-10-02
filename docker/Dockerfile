# SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
#
# SPDX-License-Identifier: AGPL-3.0-only

ARG MONGOOSEIM_IMAGE=registry.dev.zextras.com/dev/mongooseim-ce:6.6.0-2

# Only read for its dpkg status DB.
FROM --platform=$TARGETPLATFORM ${MONGOOSEIM_IMAGE} AS baserootfs

# Unpack the JRE with dpkg -x so no target-arch code runs on the builder.
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

# Resolve against the target image's dpkg DB and arch, so both arches ship the same closure.
COPY --from=baserootfs /var/lib/dpkg/status /tmp/target-status

RUN set -e; \
    mkdir -p /staging /tmp/debs; \
    apt-get install -y --no-install-recommends --print-uris \
        -o Dir::State::status=/tmp/target-status \
        -o APT::Architecture=${TARGETARCH} \
        -o APT::Architectures=${TARGETARCH} \
        openjdk-21-jre-headless curl \
      | grep -oP "^'\K[^']+" > /tmp/uris.txt; \
    [ -s /tmp/uris.txt ] || { echo "no URIs resolved for TARGETARCH=${TARGETARCH}" >&2; exit 1; }

# cacerts is arch-independent; build it natively since dpkg -x skips postinst.
RUN apt-get install -y --no-install-recommends curl ca-certificates-java

RUN set -e; \
    cd /tmp/debs && while read -r url; do curl -fsSLO "$url"; done < /tmp/uris.txt; \
    for deb in /tmp/debs/*.deb; do dpkg -x "$deb" /staging; done; \
    rm -rf /tmp/debs /tmp/uris.txt /var/lib/apt/lists/*

# A real /lib dir would shadow the usr-merge /lib symlink.
RUN if [ -d /staging/lib ] && [ ! -L /staging/lib ]; then rm -rf /staging/lib; fi

# Not /var/log/carbonio/message-dispatcher: the base ships it chowned, a root-owned copy breaks MongooseIM.
RUN mkdir -p /staging/opt/carbonio-message-dispatcher \
             /staging/etc/carbonio/message-dispatcher

# The JRE dir is arch-suffixed and update-alternatives never ran.
RUN jvmdir="$(basename "$(ls -d /staging/usr/lib/jvm/java-21-openjdk-*)")" \
 && ln -s "${jvmdir}" /staging/usr/lib/jvm/default-jvm

RUN mkdir -p /staging/etc/ssl/certs/java \
 && cp /etc/ssl/certs/java/cacerts /staging/etc/ssl/certs/java/cacerts

FROM --platform=$TARGETPLATFORM ${MONGOOSEIM_IMAGE}

COPY --from=installer /staging/ /

COPY docker/carbonio-message-dispatcher-auth.jar /opt/carbonio-message-dispatcher/carbonio-message-dispatcher-auth.jar

COPY docker/mongooseim.toml.in /etc/carbonio/message-dispatcher/mongooseim.toml.in
COPY docker/logback.xml /etc/carbonio/message-dispatcher/logback.xml

ENV JAVA_HOME="/usr/lib/jvm/default-jvm" \
    PATH="/usr/lib/jvm/default-jvm/bin:${PATH}"

COPY --chmod=755 docker/entrypoint.sh /entrypoint.sh

EXPOSE 10000 10001

ENTRYPOINT ["/entrypoint.sh"]
