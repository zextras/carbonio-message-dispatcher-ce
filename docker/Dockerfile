# SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
#
# SPDX-License-Identifier: AGPL-3.0-only

FROM --platform=$BUILDPLATFORM erlangsolutions/mongooseim:6.4.0

ARG TARGETPLATFORM
ARG BUILDPLATFORM

USER root

# Install OpenJDK 21 and curl for health checks
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        openjdk-21-jre-headless \
        curl \
    && rm -rf /var/lib/apt/lists/*

# Create directories for the auth service
RUN mkdir -p /opt/carbonio-message-dispatcher \
             /etc/carbonio/message-dispatcher \
             /var/log/carbonio/message-dispatcher

# Copy the auth service JAR
COPY docker/carbonio-message-dispatcher-auth.jar /opt/carbonio-message-dispatcher/carbonio-message-dispatcher-auth.jar

# Copy configuration files
COPY docker/mongooseim.toml /usr/lib/mongooseim/etc/mongooseim.toml
COPY docker/logback.xml /etc/carbonio/message-dispatcher/logback.xml

# Copy entrypoint script
COPY docker/entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

# Expose ports:
# 10000 - GraphQL Admin API
# 10001 - XMPP C2S
EXPOSE 10000 10001

ENTRYPOINT ["/entrypoint.sh"]
