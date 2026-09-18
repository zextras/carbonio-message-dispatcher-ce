#!/bin/bash

# SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
#
# SPDX-License-Identifier: AGPL-3.0-only

set -e

REPO_ROOT=$(cd "$(dirname "$0")/.." && pwd)
CONFIG_SETUP="$REPO_ROOT/package/carbonio-message-dispatcher-config-setup"
TEST_DIR=$(mktemp -d)
trap 'rm -rf "$TEST_DIR"' EXIT
mkdir "$TEST_DIR/bin"
cp "$REPO_ROOT/package/mongooseim.toml.in" "$TEST_DIR/mongooseim.toml.in"

# CO-4228: the config dpkg preserved as a "locally modified" conffile. Every key
# below is one MongooseIM 6.6.0 refuses to start with.
cat > "$TEST_DIR/mongooseim.toml" <<'EOF'
[outgoing_pools.rdbms.default.connection]
  rdbms_server_type = "pgsql"

[[listen.http.handlers.mongoose_admin_api]]
  host = "_"
  path = "/api"

[[listen.http.handlers.mod_websockets]]
  host = "_"
  path = "/ws-xmpp"
EOF

cat > "$TEST_DIR/bin/id" <<'EOF'
#!/bin/sh
echo 0
EOF
cat > "$TEST_DIR/bin/consul" <<'EOF'
#!/bin/sh
case "$3" in
  carbonio-message-dispatcher-db/db-password) printf '%s\n' "$STUB_DB_PASSWORD" ;;
  carbonio-message-dispatcher/api/username) printf '%s\n' "$STUB_API_USERNAME" ;;
  carbonio-message-dispatcher/api/password) printf '%s\n' "$STUB_API_PASSWORD" ;;
  *) exit 1 ;;
esac
EOF
chmod +x "$TEST_DIR/bin/id" "$TEST_DIR/bin/consul"

export PATH="$TEST_DIR/bin:$PATH"
export MONGOOSEIM_TOML="$TEST_DIR/mongooseim.toml"
export STUB_DB_PASSWORD='db\&pass|word'
export STUB_API_USERNAME='api"user'
export STUB_API_PASSWORD='api&pass|word'

assert_absent() {
  if grep -q "$1" "$MONGOOSEIM_TOML"; then
    echo "rendered config still contains: $1" >&2
    exit 1
  fi
}

"$CONFIG_SETUP"

grep -Fq 'password = "db\\&pass|word"' "$MONGOOSEIM_TOML"
grep -Fq 'username = "api\"user"' "$MONGOOSEIM_TOML"
grep -Fq 'password = "api&pass|word"' "$MONGOOSEIM_TOML"
grep -qxF '[modules.mod_pin_message]' "$MONGOOSEIM_TOML"
assert_absent 'mongoose_admin_api\|rdbms_server_type\|mod_websockets'
assert_absent '<db-password>\|<api-username>\|<api-password>'

file_mode() {
  stat -c '%a' "$MONGOOSEIM_TOML" 2>/dev/null || stat -f '%Lp' "$MONGOOSEIM_TOML"
}

# A mode an operator tightened on a file full of credentials is not reset.
chmod 600 "$MONGOOSEIM_TOML"
"$CONFIG_SETUP"
if [[ "$(file_mode)" != "600" ]]; then
  echo "re-render reset the mode to $(file_mode)" >&2
  exit 1
fi

rm "$MONGOOSEIM_TOML"
"$CONFIG_SETUP"
if [[ "$(file_mode)" != "644" ]]; then
  echo "fresh install produced mode $(file_mode)" >&2
  exit 1
fi

# A missing template fails without touching the config already in place.
mv "$TEST_DIR/mongooseim.toml.in" "$TEST_DIR/template.away"
if "$CONFIG_SETUP" >/dev/null 2>&1; then
  echo "config-setup succeeded with no template installed" >&2
  exit 1
fi
grep -Fq 'password = "db\\&pass|word"' "$MONGOOSEIM_TOML"
mv "$TEST_DIR/template.away" "$TEST_DIR/mongooseim.toml.in"

# A CR renders as invalid TOML, so it is refused before anything is replaced.
STUB_DB_PASSWORD=$'db\rpass'
if "$CONFIG_SETUP" >/dev/null 2>&1; then
  echo "config-setup accepted a control character in a Consul value" >&2
  exit 1
fi
grep -Fq 'password = "db\\&pass|word"' "$MONGOOSEIM_TOML"
