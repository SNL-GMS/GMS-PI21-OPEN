ARG DOCKER_REGISTRY
ARG DOCKER_IMAGE_TAG
FROM $DOCKER_REGISTRY/gms-common/java:$DOCKER_IMAGE_TAG

USER 0

WORKDIR /opt/gms

EXPOSE 8080

COPY prometheus-jmx-config.yaml ./frameworks-osd-service/prometheus-jmx-config.yaml
COPY bin ./frameworks-osd-service/bin
COPY lib ./frameworks-osd-service/lib

RUN set -ex && \
    chown -R gms:gms /opt/gms

# gms uid
USER 1001

HEALTHCHECK --interval=30s \
  CMD curl -f localhost:8080/frameworks-osd-service/alive \
    || exit 1

WORKDIR frameworks-osd-service

#Running the jar in the docker container
CMD ["./bin/frameworks-osd-service"]

