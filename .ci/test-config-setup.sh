#!/bin/bash

# SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
#
# SPDX-License-Identifier: AGPL-3.0-only

set -e

REPO_ROOT=$(cd "$(dirname "$0")/.." && pwd)
TEST_DIR=$(mktemp -d)
trap 'rm -rf "$TEST_DIR"' EXIT
mkdir "$TEST_DIR/bin"
cp "$REPO_ROOT/package/mongooseim.toml.in" "$TEST_DIR/mongooseim.toml.in"
echo stale > "$TEST_DIR/mongooseim.toml"

cat > "$TEST_DIR/bin/id" <<'EOF'
#!/bin/sh
echo 0
EOF
cat > "$TEST_DIR/bin/consul" <<'EOF'
#!/bin/sh
case "$3" in
  carbonio-message-dispatcher-db/db-password) printf '%s\n' 'db\&pass|word' ;;
  carbonio-message-dispatcher/api/username) printf '%s\n' 'api"user' ;;
  carbonio-message-dispatcher/api/password) printf '%s\n' 'api&pass|word' ;;
  *) exit 1 ;;
esac
EOF
chmod +x "$TEST_DIR/bin/id" "$TEST_DIR/bin/consul"

PATH="$TEST_DIR/bin:$PATH" MONGOOSEIM_TOML="$TEST_DIR/mongooseim.toml" \
  "$REPO_ROOT/package/carbonio-message-dispatcher-config-setup"

grep -Fq 'password = "db\\&pass|word"' "$TEST_DIR/mongooseim.toml"
grep -Fq 'username = "api\"user"' "$TEST_DIR/mongooseim.toml"
grep -Fq 'password = "api&pass|word"' "$TEST_DIR/mongooseim.toml"
grep -qxF '[modules.mod_pin_message]' "$TEST_DIR/mongooseim.toml"
! grep -q 'rdbms_server_type\|mod_websockets\|<db-password>\|<api-username>\|<api-password>' "$TEST_DIR/mongooseim.toml"
