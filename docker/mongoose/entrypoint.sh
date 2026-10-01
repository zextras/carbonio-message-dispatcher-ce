#!/bin/bash
set -e

# Wait for consul agent
echo "[mongoose] Waiting for consul agent..."
until consul members >/dev/null 2>&1; do sleep 1; done
echo "[mongoose] Consul agent ready."

# Wait for DB credentials to be available in Consul KV
echo "[mongoose] Waiting for DB credentials in Consul KV..."
until consul kv get carbonio-message-dispatcher-db/db-password >/dev/null 2>&1; do sleep 2; done
echo "[mongoose] DB credentials found."

# Render mongooseim.toml from the packaged template and Consul values.
echo "[mongoose] Running config-setup..."
carbonio-message-dispatcher-config-setup

# mongooseimctl's RUNNER_ETC_DIR is /etc/carbonio/message-dispatcher,
# where config-setup wrote the rendered file.

# Increase file descriptor limit
ulimit -n 65536 2>/dev/null || echo "Warning: Could not set ulimit"

# Wait for DB to be reachable via sidecar proxy, else Mongoose starts without a db pool and rooms do not work
echo "[mongoose] Waiting for DB proxy (127.78.0.10:20000)..."
until echo > /dev/tcp/127.78.0.10/20000 2>/dev/null; do sleep 2; done
echo "[mongoose] DB proxy ready."

echo "[mongoose] Starting MongooseIM..."
exec mongooseimctl foreground
