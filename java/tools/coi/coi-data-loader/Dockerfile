ARG DOCKER_REGISTRY
ARG DOCKER_IMAGE_TAG
FROM $DOCKER_REGISTRY/gms-common/java:$DOCKER_IMAGE_TAG

USER 0

WORKDIR /opt/gms

COPY bin ./coi-data-loader/bin
COPY lib ./coi-data-loader/lib

RUN set -ex && \
    chown -R gms:gms /opt/gms

# gms uid
USER 1001

WORKDIR coi-data-loader

# Define default command.
CMD ["./bin/coi-data-loader"]

