# SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
#
# SPDX-License-Identifier: AGPL-3.0-only

services {
  check {
    tcp      = "127.78.0.11:10000"
    timeout  = "1s"
    interval = "5s"
  }

  connect {
    sidecar_service {
      proxy {
        local_service_address = "127.78.0.11"
      }
    }
  }

  name = "carbonio-chats-messaging-xmpp"
  port = 10000
}