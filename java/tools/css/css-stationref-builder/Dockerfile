ARG DOCKER_REGISTRY
ARG DOCKER_IMAGE_TAG
FROM $DOCKER_REGISTRY/gms-common/java:$DOCKER_IMAGE_TAG

USER 0

WORKDIR /opt/gms

COPY bin ./tools-station-ref-builder/bin
COPY lib ./tools-station-ref-builder/lib

RUN set -ex && \
    chown -R gms:gms /opt/gms

# gms uid
USER 1001

WORKDIR tools-station-ref-builder

# Define default command.
CMD ["./bin/tools-station-ref-builder"]

