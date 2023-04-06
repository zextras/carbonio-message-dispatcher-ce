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

  name = "carbonio-message-dispatcher-xmpp"
  port = 10000
}