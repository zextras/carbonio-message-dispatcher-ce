ARG SIDECAR_IMAGE=registry.dev.zextras.com/dev/carbonio-sidecar:main
FROM --platform=$BUILDPLATFORM docker.io/library/alpine:3.23 AS consul-template
ARG TARGETARCH
ARG CONSUL_TEMPLATE_VERSION=0.42.1
RUN apk add --no-cache curl unzip && \
    curl -fsSL -o /tmp/ct.zip "https://releases.hashicorp.com/consul-template/${CONSUL_TEMPLATE_VERSION}/consul-template_${CONSUL_TEMPLATE_VERSION}_linux_${TARGETARCH}.zip" && \
    curl -fsSL -o /tmp/ct.sums "https://releases.hashicorp.com/consul-template/${CONSUL_TEMPLATE_VERSION}/consul-template_${CONSUL_TEMPLATE_VERSION}_SHA256SUMS" && \
    echo "$(grep "_linux_${TARGETARCH}.zip" /tmp/ct.sums | cut -d' ' -f1)  /tmp/ct.zip" | sha256sum -c - && \
    unzip -j /tmp/ct.zip consul-template -d /out && \
    chmod +x /out/consul-template

FROM ${SIDECAR_IMAGE}

RUN useradd -r -U carbonio-message-dispatcher

COPY --from=consul-template /out/consul-template /usr/local/bin/consul-template
COPY package/carbonio-message-dispatcher-http-template.hcl /consul/templates/carbonio-message-dispatcher-http-template.hcl

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

COPY --chmod=755 docker/http-sidecar/carbonio-message-dispatcher-http-hcl-entrypoint /usr/local/bin/carbonio-message-dispatcher-http-hcl-entrypoint
ENTRYPOINT ["carbonio-message-dispatcher-http-hcl-entrypoint"]
