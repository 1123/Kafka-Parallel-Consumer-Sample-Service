FROM docker.io/alpine:3.16

LABEL org.opencontainers.image.authors="rschmitz@confluent.io"
LABEL org.opencontainers.image.description="Kafka Parallel Consumer Container based on Alpine Linux"
LABEL org.opencontainers.image.title="pc-example/Alpine"

RUN apk update \
  && apk upgrade \
  && apk add --update bash openjdk17-jre-headless kcat openssl3 \
  && rm -rf /var/cache/apk/*

COPY maven/lib /app/lib
COPY config /app/config
COPY scripts /app/scripts

RUN chmod 755 /app/scripts/*.sh

WORKDIR /app

CMD ["/app/scripts/run_application.sh", "${project.artifactId}-${project.version}"]
