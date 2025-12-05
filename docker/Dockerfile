FROM --platform=$BUILDPLATFORM erlangsolutions/mongooseim:6.4.0@sha256:883e2c392f81f20b58d12f97f4ac3a359d8e92967b3e0de59beeb4c81fe138ec

ARG TARGETPLATFORM
ARG BUILDPLATFORM

COPY docker/mongooseim.toml /usr/lib/mongooseim/etc/mongooseim.toml

EXPOSE 10000

CMD ["mongooseimctl", "foreground"]
