services {
  checks = [
    {
      id       = "ready",
      http     = "http://127.78.0.10:10000/admin/commands",
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
            destination_name   = "carbonio-messaging-db"
            local_bind_address = "127.78.0.10"
            local_bind_port    = 20000
          }
        ]
      }
    }
  }

  name = "carbonio-messaging-http"
  port = 10000
}