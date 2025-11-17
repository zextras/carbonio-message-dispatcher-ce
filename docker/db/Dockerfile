FROM --platform=$BUILDPLATFORM postgres:16-alpine

ARG TARGETPLATFORM
ARG BUILDPLATFORM

COPY docker/db/init.sql /docker-entrypoint-initdb.d/init.sql
