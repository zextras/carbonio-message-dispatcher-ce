ARG SIDECAR_IMAGE=registry.dev.zextras.com/dev/carbonio-sidecar:main
FROM --platform=$BUILDPLATFORM docker.io/library/alpine:3.20 AS hcl
COPY package/carbonio-message-dispatcher-http-template.hcl /carbonio-message-dispatcher-http-template.hcl
RUN sed -E 's/\{\{ or \(env "[A-Z0-9_]+"\) "([^"]*)" \}\}/\1/g' \
    /carbonio-message-dispatcher-http-template.hcl > /carbonio-message-dispatcher-http.hcl \
    && ! grep -q '{{' /carbonio-message-dispatcher-http.hcl

FROM ${SIDECAR_IMAGE}

RUN useradd -r -U carbonio-message-dispatcher

COPY --from=hcl /carbonio-message-dispatcher-http.hcl /consul/config/

COPY package/service-protocol-http.json /etc/carbonio/message-dispatcher/service-discover/service-protocol.json
COPY package/service-protocol-auth.json /etc/carbonio/message-dispatcher/service-discover/service-protocol-auth.json
COPY package/service-protocol-http.json /etc/carbonio/message-dispatcher/service-discover/service-protocol-http.json
COPY package/service-protocol-xmpp.json /etc/carbonio/message-dispatcher/service-discover/service-protocol-xmpp.json
COPY package/intentions-http.json       /etc/carbonio/message-dispatcher/service-discover/intentions.json
COPY package/intentions-auth.json       /etc/carbonio/message-dispatcher/service-discover/intentions-auth.json
COPY package/intentions-http.json       /etc/carbonio/message-dispatcher/service-discover/intentions-http.json
COPY package/intentions-xmpp.json       /etc/carbonio/message-dispatcher/service-discover/intentions-xmpp.json
COPY package/policies.json              /etc/carbonio/message-dispatcher/service-discover/policies.json

COPY --chmod=755 package/carbonio-message-dispatcher-setup /usr/local/bin/carbonio-message-dispatcher-setup

ENV SERVICE_NAME=carbonio-message-dispatcher-http
ENV SETUP_SCRIPT="carbonio-message-dispatcher-setup"
ENV TOKEN_FILE=/etc/carbonio/message-dispatcher/service-discover/token
