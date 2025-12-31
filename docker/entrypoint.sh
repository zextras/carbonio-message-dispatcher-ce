#!/bin/sh
# SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
#
# SPDX-License-Identifier: AGPL-3.0-only

set -e

CONFIG_FILE="/etc/carbonio/message-dispatcher/config.properties"
MONGOOSEIM_TOML="/usr/lib/mongooseim/etc/mongooseim.toml"

# Create config.properties for the auth service
# Note: db-name, db-username, db-password are read from Consul, not from properties
echo "" > "${CONFIG_FILE}"

addEnvToProperties() {
  if [ -n "$2" ]; then
    echo "$1=$2" >> "${CONFIG_FILE}"
  else
    echo "$1 is not set. Skipping it."
  fi
}

# Auth service host/port configuration
addEnvToProperties "carbonio.message-dispatcher.auth.host" "${CARBONIO_MESSAGE_DISPATCHER_AUTH_HOST}"
addEnvToProperties "carbonio.message-dispatcher.auth.port" "${CARBONIO_MESSAGE_DISPATCHER_AUTH_PORT}"

# Database host/port configuration (name/user/pass come from Consul)
addEnvToProperties "carbonio.postgres.host" "${CARBONIO_POSTGRES_HOST}"
addEnvToProperties "carbonio.postgres.port" "${CARBONIO_POSTGRES_PORT}"

# User management configuration (optional, for external auth)
addEnvToProperties "carbonio.user-management.host" "${CARBONIO_USER_MANAGEMENT_HOST}"
addEnvToProperties "carbonio.user-management.port" "${CARBONIO_USER_MANAGEMENT_PORT}"

# Service discover (Consul) configuration
addEnvToProperties "carbonio.service-discover.host" "${CARBONIO_SERVICE_DISCOVER_HOST}"
addEnvToProperties "carbonio.service-discover.port" "${CARBONIO_SERVICE_DISCOVER_PORT}"

# Update mongooseim.toml with environment values
# MongooseIM doesn't use Consul, so we need to pass all DB config via ENV
if [ -n "${CARBONIO_POSTGRES_HOST}" ]; then
  sed -i "s/<db-host>/${CARBONIO_POSTGRES_HOST}/" "${MONGOOSEIM_TOML}"
fi

if [ -n "${CARBONIO_POSTGRES_PORT}" ]; then
  sed -i "s/<db-port>/${CARBONIO_POSTGRES_PORT}/" "${MONGOOSEIM_TOML}"
fi

if [ -n "${CARBONIO_POSTGRES_DB_NAME}" ]; then
  sed -i "s/<db-name>/${CARBONIO_POSTGRES_DB_NAME}/" "${MONGOOSEIM_TOML}"
fi

if [ -n "${CARBONIO_POSTGRES_DB_USERNAME}" ]; then
  sed -i "s/<db-username>/${CARBONIO_POSTGRES_DB_USERNAME}/" "${MONGOOSEIM_TOML}"
fi

if [ -n "${CARBONIO_POSTGRES_DB_PASSWORD}" ]; then
  sed -i "s/<db-password>/${CARBONIO_POSTGRES_DB_PASSWORD}/" "${MONGOOSEIM_TOML}"
fi

if [ -n "${CARBONIO_GRAPHQL_API_USERNAME}" ]; then
  sed -i "s/<api-username>/${CARBONIO_GRAPHQL_API_USERNAME}/" "${MONGOOSEIM_TOML}"
fi

if [ -n "${CARBONIO_GRAPHQL_API_PASSWORD}" ]; then
  sed -i "s/<api-password>/${CARBONIO_GRAPHQL_API_PASSWORD}/" "${MONGOOSEIM_TOML}"
fi

# Start the auth service in the background
# The auth service:
# - Reads host/port config from config.properties
# - Reads db-name/username/password from Consul
# - Handles database migrations via Flyway at startup
AUTH_HOST="${CARBONIO_MESSAGE_DISPATCHER_AUTH_HOST:-127.0.0.1}"
AUTH_PORT="${CARBONIO_MESSAGE_DISPATCHER_AUTH_PORT:-10002}"

echo "Starting carbonio-message-dispatcher-auth service on ${AUTH_HOST}:${AUTH_PORT}..."
java -Djava.net.preferIPv4Stack=true \
     -jar /opt/carbonio-message-dispatcher/carbonio-message-dispatcher-auth.jar &

# Wait for auth service to be ready (check health endpoint)
echo "Waiting for auth service to be ready..."
MAX_RETRIES=30
RETRY_COUNT=0

while [ $RETRY_COUNT -lt $MAX_RETRIES ]; do
    if curl -s "http://${AUTH_HOST}:${AUTH_PORT}/health/ready" > /dev/null 2>&1; then
        echo "Auth service is ready (database migrations completed)"
        break
    fi
    RETRY_COUNT=$((RETRY_COUNT + 1))
    echo "Waiting for auth service... ($RETRY_COUNT/$MAX_RETRIES)"
    sleep 2
done

if [ $RETRY_COUNT -eq $MAX_RETRIES ]; then
    echo "Warning: Auth service health check timed out, proceeding anyway..."
fi

# Start MongooseIM in foreground
echo "Starting MongooseIM..."
exec mongooseimctl foreground
