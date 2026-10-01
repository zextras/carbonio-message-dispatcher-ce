# SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
#
# SPDX-License-Identifier: AGPL-3.0-only

# Stage 1: runs natively on BUILDPLATFORM (amd64 CI builder) — no QEMU.
# carbonio-message-dispatcher is prebuilt by the "Build deb/rpm" stage
# (MongooseIM's PKGBUILD cross-compiles the erlang release for aarch64,
# see package/PKGBUILD arch=('aarch64' 'x86_64')) and unstashed into
# artifacts/ before this build runs (see Jenkinsfile). We fetch the
# actual runtime libs the release needs for TARGETARCH via an explicit
# `apt-get download` list (pure network fetch, no execution) and unpack
# every package with `dpkg -x` — never `dpkg -i`/`apt-get install`, so
# no maintainer scripts run (postinst does groupadd/useradd/systemctl
# calls that are meaningless in a container) and no target-arch code
# ever executes on the BUILDPLATFORM host.
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

COPY artifacts/ /tmp/artifacts/

# The codename must be pinned: carbonio-message-dispatcher builds amd64 for
# jammy/noble/rocky-8/rocky-9 and aarch64 for jammy only, and the "Upload
# artifacts" stage unstashes every distro into this same workspace before the
# docker build, so artifacts/ also holds the noble (and rocky) packages. An
# unpinned *_${TARGETARCH}.deb glob would then match two files and dpkg -x
# would die with exit 2. This image is jammy-based, so it must unpack the
# jammy build (package__ubuntu_jammy => _package_legacy in the PKGBUILD).
RUN set -e; \
	pkg=$(ls /tmp/artifacts/carbonio-message-dispatcher-ce_*jammy*_${TARGETARCH}.deb); \
	[ "$(printf '%s\n' "$pkg" | wc -l)" -eq 1 ] || { echo "ambiguous deb match: $pkg" >&2; exit 1; }; \
	mkdir -p /staging /tmp/debs && cd /tmp/debs && \
	# MongooseIM's erlang release (carbonio-erlang) and its TLS NIF
	# (carbonio-openssl, PKGBUILD build() links against
	# /opt/zextras/common/lib) are the only two `depends=()` entries this
	# container actually executes. jq, pending-setups, service-discover and
	# service-discover-base are deliberately NOT fetched: they belong to
	# the host-provisioning chain (pending-setups drags in carbonio-core,
	# service-discover-base drags in Consul agent tooling) and nothing the
	# entrypoints (docker/entrypoint.sh, docker/mongoose/entrypoint.sh) or
	# `mongooseimctl foreground` invokes at container runtime uses them.
	# No extra system libs (libc6, zlib1g, libssl3, ...) are downloaded:
	# the final stage is ubuntu:jammy for TARGETPLATFORM natively, so it
	# already ships matching-arch libc/zlib/libssl, and carbonio-erlang /
	# carbonio-openssl are self-contained under /opt/zextras/common/lib.
	apt-get download \
		carbonio-erlang:${TARGETARCH} \
		carbonio-openssl:${TARGETARCH} && \
	cd / && for deb in /tmp/debs/*.deb "$pkg"; do dpkg -x "$deb" /staging; done && \
	rm -rf /tmp/debs /tmp/artifacts /var/lib/apt/lists/*

# Strip host-only payload the .deb installs purely for a systemd/Consul host
# install; none of it is read by mongooseimctl or the container entrypoints:
#  - /lib/systemd/system/*.service: no init system runs in the container.
#    The whole /staging/lib dir is removed (not just its systemd subdir):
#    Ubuntu's /lib is a usr-merge SYMLINK to /usr/lib, and dpkg -x created
#    /staging/lib as a plain directory (its only content). Copying that
#    plain directory onto / in the final stage would replace the /lib
#    symlink with a real (near-empty) directory, hiding /usr/lib's actual
#    contents — including the dynamic linker's own libs — from every path
#    that resolves through /lib, breaking exec entirely.
#  - /etc/zextras/pending-setups.d: only consumed by the host `pending-setups`
#    tool, which we deliberately did not fetch above.
#  - /etc/zextras/service-discover: Consul Connect intentions/HCL registered
#    by the (unfetched) service-discover agent, not by MongooseIM itself.
#  - /etc/carbonio/message-dispatcher/service-discover: the same Consul
#    intentions/policies JSON, just mirrored under the app's own etc dir.
RUN rm -rf /staging/lib \
           /staging/etc/zextras/pending-setups.d \
           /staging/etc/zextras/service-discover \
           /staging/etc/carbonio/message-dispatcher/service-discover

# Expose the release under /usr/lib/mongooseim, the path downstream images
# already use. The PKGBUILD build() bakes
# LIB_DIR=/opt/zextras/common/lib/mongooseim into the compiled release
# (mongooseimctl, releases/*/vm.args, RELX_* all hard-code that absolute
# path), so the real tree MUST stay there — /usr/lib/mongooseim is only a
# compat symlink, not the reverse. mongooseimctl itself lives in
# BIN_DIR=/opt/zextras/common/bin, put on PATH via ENV in the final stage.
RUN mkdir -p /staging/usr/lib && \
    ln -s /opt/zextras/common/lib/mongooseim /staging/usr/lib/mongooseim

# NOTE for downstream images: this build reads its config from
# /etc/carbonio/message-dispatcher, NOT from the release's own etc dir. The
# PKGBUILD bakes ETC_DIR=/etc/carbonio/message-dispatcher, so mongooseimctl
# has RUNNER_ETC_DIR set there and only ever loads mongooseim.toml from that
# path. The previous, source-built base image (from the mongooseim-docker
# fork) used the upstream default /usr/lib/mongooseim/etc, so any downstream
# Dockerfile still writing there is now silently ignored and MongooseIM will
# boot with no config at all, the .deb shipping only mongooseim.toml.in. A compat symlink
# does NOT fix this: buildah/docker COPY replaces a destination symlink with
# a regular file rather than writing through it (verified). Downstream must
# target /etc/carbonio/message-dispatcher/mongooseim.toml explicitly.

# The compiled release hard-codes RUNNER_USER="carbonio-message-dispatcher"
# (see /opt/zextras/common/bin/mongooseimctl), and re-execs itself through
# `runuser -s /bin/bash -l "$RUNNER_USER"` whenever it is not already that
# user. The account is normally created by the .deb's postinst
# (package/PKGBUILD:343-357), which `dpkg -x` never runs — without it every
# mongooseimctl invocation dies with "user carbonio-message-dispatcher does
# not exist". Recreate it here with the postinst's exact flags, then ship
# the resulting account databases in /staging.
#
# Shipping whole /etc/{passwd,group,shadow,gshadow} (rather than appending,
# which the zero-RUN final stage cannot do) is safe: this installer and the
# final stage are the SAME docker.io/library/ubuntu:jammy tag, and those
# four files are arch-independent, so the copy is byte-identical to what it
# replaces apart from the account we just added.
RUN groupadd -r carbonio-message-dispatcher \
 && useradd -r -M \
      -g carbonio-message-dispatcher \
      -d /var/lib/carbonio/message-dispatcher \
      -s /sbin/nologin carbonio-message-dispatcher \
 && cp /etc/passwd /etc/group /etc/shadow /etc/gshadow /staging/etc/

# Runtime dirs the postinst creates/chowns (PKGBUILD:352-357). chown resolves
# the name through this stage's own passwd, which is the one we just shipped,
# so the numeric ids match in the final image.
RUN mkdir -p /staging/var/lib/carbonio/message-dispatcher \
             /staging/var/lock/carbonio/message-dispatcher \
             /staging/var/log/carbonio/message-dispatcher \
 && chmod -R 755 /staging/opt/zextras/common/lib/mongooseim/log \
 && chown -R carbonio-message-dispatcher:carbonio-message-dispatcher \
      /staging/opt/zextras/common/lib/mongooseim/log \
      /staging/var/lib/carbonio/message-dispatcher \
      /staging/var/lock/carbonio/message-dispatcher \
      /staging/var/log/carbonio/message-dispatcher

# Stage 2: final image — TARGETPLATFORM, zero RUN steps, no QEMU needed.
FROM --platform=$TARGETPLATFORM docker.io/library/ubuntu:jammy

COPY --from=installer /staging/ /

ENV PATH="/opt/zextras/common/bin:${PATH}"

# 10001 - XMPP C2S (see docker/Dockerfile / docker/mongoose/Dockerfile)
EXPOSE 10001

ENTRYPOINT ["mongooseimctl", "foreground"]
