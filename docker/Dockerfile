# SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
#
# SPDX-License-Identifier: AGPL-3.0-only

ARG MONGOOSEIM_IMAGE=registry.dev.zextras.com/dev/mongooseim-ce:6.6.0-2

# Stage 0: the target-platform base image, pulled only so the installer can
# read its dpkg status DB. No RUN, so nothing target-arch ever executes.
FROM --platform=$TARGETPLATFORM ${MONGOOSEIM_IMAGE} AS baserootfs

# Stage 1: runs natively on BUILDPLATFORM (amd64 CI builder) -- no QEMU.
# openjdk-21-jre-headless and curl are stock Ubuntu packages, so instead of
# hand-guessing their (sizeable, version-drifting) dependency closure we
# resolve it mechanically with `apt-get install --print-uris`, which is
# pure APT metadata resolution -- it prints the download URL/filename/size
# for every package APT would pull in, without unpacking anything, running
# any script, or touching the filesystem outside its own cache. We then
# fetch each URI ourselves and unpack with `dpkg -x` -- never `dpkg -i` or
# `apt-get install <pkg>:${TARGETARCH}`, both of which execute target-arch
# maintainer scripts (postinst runs update-alternatives, ldconfig, and
# ca-certificates-java's cacerts rebuild) that QEMU would have to emulate.
FROM --platform=$BUILDPLATFORM docker.io/library/ubuntu:jammy AS installer

ARG TARGETARCH
ARG BUILDARCH=amd64

RUN apt-get update \
 && apt-get install -y --no-install-recommends ca-certificates \
 # Cross-arch setup when TARGETARCH != BUILDARCH
 && if [ "${TARGETARCH}" != "${BUILDARCH}" ]; then \
      dpkg --add-architecture ${TARGETARCH} \
      && sed -i 's|^deb |deb [arch='"${BUILDARCH}"'] |' /etc/apt/sources.list \
      && printf 'Types: deb\nURIs: http://ports.ubuntu.com/ubuntu-ports/\nSuites: jammy jammy-updates jammy-security\nComponents: main restricted universe multiverse\nArchitectures: %s\nSigned-By: /usr/share/keyrings/ubuntu-archive-keyring.gpg\n' "${TARGETARCH}" \
         > /etc/apt/sources.list.d/ports-${TARGETARCH}.sources; \
    fi \
 && printf 'deb [arch=%s trusted=yes] https://repo.zextras.tools/repository/ubuntu-devel-jammy jammy main\n' "${TARGETARCH}" \
    > /etc/apt/sources.list.d/zextras.list \
 && apt-get update

# Resolve the closure against the FINAL IMAGE's package set, not this
# builder's. Pointing Dir::State::status at the target rootfs's dpkg DB is
# what makes the result both minimal and arch-symmetric:
#   - minimal: apt omits everything ubuntu:jammy already ships, so we do not
#     unpack a second libc6/zlib1g/... over the base image's own copies
#     without ldconfig ever running.
#   - symmetric: with the builder's native status DB, an amd64 target would
#     see libc6 etc. as satisfied while an arm64 target would not, so the two
#     legs of the same multi-arch manifest would ship different content.
# APT::Architecture/Architectures must ALSO be flipped to TARGETARCH: with
# the builder's native arch still amd64, apt reads an arm64 status DB as a
# pile of foreign-arch packages and declares half of it broken
# ("debconf : PreDepends: perl-base ... but it is not going to be
# installed"), because Multi-Arch: no packages cannot satisfy foreign deps.
# Flipping all three options turns this into a pure "what would a TARGETARCH
# ubuntu:jammy still need?" query. Nothing here reads or executes
# target-arch code -- it is APT metadata only.
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

# Native `curl` fetches the .debs listed above. `ca-certificates-java` is
# installed NATIVELY (no :${TARGETARCH}, so its postinst actually runs on
# BUILDPLATFORM) purely to generate /etc/ssl/certs/java/cacerts. That file
# is a plain Java keystore -- arch-independent data, not a binary -- so
# building it here and copying it into /staging below is equivalent to
# letting the target-arch package's own postinst build it, which dpkg -x
# never does. Without this, the unpacked JRE would have an empty trust
# store and every outbound TLS call would fail to validate any CA.
RUN apt-get install -y --no-install-recommends curl ca-certificates-java

RUN set -e; \
    cd /tmp/debs && while read -r url; do curl -fsSLO "$url"; done < /tmp/uris.txt; \
    for deb in /tmp/debs/*.deb; do dpkg -x "$deb" /staging; done; \
    rm -rf /tmp/debs /tmp/uris.txt /var/lib/apt/lists/*

# Defensive, as in docker/mongooseim-base/Dockerfile: Ubuntu's /lib is a
# usr-merge SYMLINK to /usr/lib, and dpkg -x turns any /lib/* entry in a
# .deb's file list into a real directory, which would shadow /usr/lib
# (including the dynamic linker's own libs) when COPYed onto /. Verified
# that none of the openjdk-21-jre-headless / curl closure resolved above
# ships anything under /lib today (checked `dpkg -L` for every package),
# but keep the guard so a future dependency bump can't silently break exec.
RUN if [ -d /staging/lib ] && [ ! -L /staging/lib ]; then rm -rf /staging/lib; fi

# Create runtime directories for the auth service (BUILDPLATFORM mkdir,
# so no emulation involved).
#
# /var/log/carbonio/message-dispatcher is deliberately NOT created here: the
# base image already ships it chowned to carbonio-message-dispatcher (the
# user mongooseimctl re-execs as). Re-creating it in /staging would land a
# root-owned directory on top of that one, and MongooseIM would then die at
# boot with eacces opening its own log file.
RUN mkdir -p /staging/opt/carbonio-message-dispatcher \
             /staging/etc/carbonio/message-dispatcher

# openjdk-21-jre-headless normally relies on `update-alternatives` (run by
# its postinst, which never runs here) to symlink /usr/bin/java etc. Its
# real install dir is arch-suffixed (java-21-openjdk-amd64 vs. -arm64), so
# a hardcoded path would break one arch; resolve it with a glob instead
# and point an arch-independent `default-jvm` symlink at it. The final
# stage puts $JAVA_HOME/bin on PATH so `java` (invoked bare by
# docker/entrypoint.sh) resolves on both architectures.
RUN jvmdir="$(basename "$(ls -d /staging/usr/lib/jvm/java-21-openjdk-*)")" \
 && ln -s "${jvmdir}" /staging/usr/lib/jvm/default-jvm

RUN mkdir -p /staging/etc/ssl/certs/java \
 && cp /etc/ssl/certs/java/cacerts /staging/etc/ssl/certs/java/cacerts

# Stage 2: final image -- TARGETPLATFORM, zero RUN steps, no QEMU needed.
FROM --platform=$TARGETPLATFORM ${MONGOOSEIM_IMAGE}

COPY --from=installer /staging/ /

# Copy the auth service JAR
COPY docker/carbonio-message-dispatcher-auth.jar /opt/carbonio-message-dispatcher/carbonio-message-dispatcher-auth.jar

# Copy configuration files
# The release bakes ETC_DIR=/etc/carbonio/message-dispatcher, so mongooseimctl
# reads mongooseim.toml from there — not from the release's own etc dir that
# the old upstream-built base image used.
COPY docker/mongooseim.toml.in /etc/carbonio/message-dispatcher/mongooseim.toml.in
COPY docker/logback.xml /etc/carbonio/message-dispatcher/logback.xml

ENV JAVA_HOME="/usr/lib/jvm/default-jvm" \
    PATH="/usr/lib/jvm/default-jvm/bin:${PATH}"

# Copy entrypoint script
COPY --chmod=755 docker/entrypoint.sh /entrypoint.sh

# Expose ports:
# 10000 - GraphQL Admin API
# 10001 - XMPP C2S
EXPOSE 10000 10001

ENTRYPOINT ["/entrypoint.sh"]
