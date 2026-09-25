FROM registry.dev.zextras.com/dev/carbonio-sidecar:devel

# Service registration
COPY package/carbonio-message-dispatcher-auth.hcl /consul/config/

# Setup files
COPY package/service-protocol-auth.json /etc/carbonio/message-dispatcher/service-discover/service-protocol.json
COPY package/service-protocol-auth.json /etc/carbonio/message-dispatcher/service-discover/service-protocol-auth.json
COPY package/service-protocol-http.json /etc/carbonio/message-dispatcher/service-discover/service-protocol-http.json
COPY package/service-protocol-xmpp.json /etc/carbonio/message-dispatcher/service-discover/service-protocol-xmpp.json
COPY package/intentions-auth.json       /etc/carbonio/message-dispatcher/service-discover/intentions.json
COPY package/intentions-auth.json       /etc/carbonio/message-dispatcher/service-discover/intentions-auth.json
COPY package/intentions-http.json       /etc/carbonio/message-dispatcher/service-discover/intentions-http.json
COPY package/intentions-xmpp.json       /etc/carbonio/message-dispatcher/service-discover/intentions-xmpp.json
COPY package/policies.json              /etc/carbonio/message-dispatcher/service-discover/policies.json

# Setup script
COPY --chmod=755 package/carbonio-message-dispatcher-setup /usr/local/bin/carbonio-message-dispatcher-setup

ENV SERVICE_NAME=carbonio-message-dispatcher-auth
ENV SETUP_SCRIPT="carbonio-message-dispatcher-setup"
ENV TOKEN_FILE=/etc/carbonio/message-dispatcher/service-discover/token
