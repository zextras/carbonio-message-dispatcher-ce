// Generated from the packaged template on every upgrade and setup:
// local edits to this file are overwritten.
// To change the values, set them in /etc/zextras/service-discover/carbonio-message-dispatcher-auth.env
// (KEY=value lines; keys and defaults are printed at the end of carbonio-message-dispatcher-setup),
// then run carbonio-message-dispatcher-setup.

services {
  check {
    http     = "http://127.78.0.23:10000/health/ready"
    method   = "GET"
    timeout  = "{{ or (env "CHECK_TIMEOUT") "1s" }}"
    interval = "{{ or (env "CHECK_INTERVAL") "5s" }}"
  }

  connect {
    sidecar_service {
      proxy {
        local_service_address = "127.78.0.23"
        upstreams = [
          {
            destination_name   = "carbonio-user-management"
            local_bind_address = "127.78.0.23"
            local_bind_port    = 20000
          },
          {
            destination_name   = "carbonio-message-dispatcher-db"
            local_bind_address = "127.78.0.23"
            local_bind_port    = 20001
          }
        ]
      }
    }
  }

  name = "carbonio-message-dispatcher-auth"
  port = 10000
}
