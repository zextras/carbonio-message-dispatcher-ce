# SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
#
# SPDX-License-Identifier: AGPL-3.0-only

FROM gcr.io/distroless/java21-debian12:debug

# Copy the auth service JAR
COPY docker/carbonio-message-dispatcher-auth.jar /app/carbonio-message-dispatcher-auth.jar

# Copy configuration files
COPY docker/logback.xml /etc/carbonio/message-dispatcher/logback.xml

# Expose auth service port
EXPOSE 10002

ENTRYPOINT ["java", "-Djava.net.preferIPv4Stack=true", "-jar", "/app/carbonio-message-dispatcher-auth.jar"]
