FROM --platform=$BUILDPLATFORM erlangsolutions/mongooseim:6.4.0

ARG TARGETPLATFORM
ARG BUILDPLATFORM

COPY docker/mongooseim.toml /usr/lib/mongooseim/etc/mongooseim.toml

EXPOSE 10000

CMD ["mongooseimctl", "foreground"]
