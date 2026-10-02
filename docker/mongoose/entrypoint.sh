#!/bin/bash
set -e

echo "[mongoose] Waiting for consul agent..."
until consul members >/dev/null 2>&1; do sleep 1; done
echo "[mongoose] Consul agent ready."

echo "[mongoose] Waiting for DB credentials in Consul KV..."
until consul kv get carbonio-message-dispatcher-db/db-password >/dev/null 2>&1; do sleep 2; done
echo "[mongoose] DB credentials found."

echo "[mongoose] Running config-setup..."
carbonio-message-dispatcher-config-setup

ulimit -n 65536 2>/dev/null || echo "Warning: Could not set ulimit"

# Without the DB proxy MongooseIM starts with no DB pool and rooms break.
echo "[mongoose] Waiting for DB proxy (127.78.0.10:20000)..."
until echo > /dev/tcp/127.78.0.10/20000 2>/dev/null; do sleep 2; done
echo "[mongoose] DB proxy ready."

echo "[mongoose] Starting MongooseIM..."
exec mongooseimctl foreground
