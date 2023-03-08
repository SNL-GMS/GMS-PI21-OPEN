ARG DOCKER_REGISTRY
ARG DOCKER_IMAGE_TAG
FROM $DOCKER_REGISTRY/gms-common/java:$DOCKER_IMAGE_TAG

USER 0

WORKDIR /opt/gms

EXPOSE 8080

COPY bin ./frameworks-configuration-service/bin
COPY lib ./frameworks-configuration-service/lib

RUN set -ex && \
    chown -R gms:gms /opt/gms

# gms uid
USER 1001

HEALTHCHECK --interval=30s \
  CMD curl -f localhost:8080/frameworks-configuration-service/alive \
    || exit 1

WORKDIR frameworks-configuration-service

#Running the jar in the docker container
CMD ["./bin/frameworks-configuration-service"]

