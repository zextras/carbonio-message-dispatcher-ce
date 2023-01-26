# SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
#
# SPDX-License-Identifier: AGPL-3.0-only

services {
  checks = [
    {
      id       = "ready",
      http     = "http://127.78.0.10:10000/api/graphql",
      method   = "GET",
      timeout  = "1s",
      interval = "5s"
    }
  ],

  connect {
    sidecar_service {
      proxy {
        local_service_address = "127.78.0.10"
        upstreams             = [
          {
            destination_name   = "carbonio-chats-messaging-db"
            local_bind_address = "127.78.0.10"
            local_bind_port    = 20000
          }
        ]
      }
    }
  }

  name = "carbonio-chats-messaging-http"
  port = 10000
}