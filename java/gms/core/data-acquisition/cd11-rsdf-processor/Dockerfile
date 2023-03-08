# Run CD1.1 RSDF processing topology

ARG DOCKER_REGISTRY
ARG DOCKER_IMAGE_TAG
FROM $DOCKER_REGISTRY/gms-common/java:$DOCKER_IMAGE_TAG

USER 0

WORKDIR /cd11-rsdf-processor/

COPY bin ./cd11-rsdf-processor/bin
COPY lib ./cd11-rsdf-processor/lib

RUN set -ex && \
    chown -R gms:gms /cd11-rsdf-processor

# gms uid
USER 1001

# Start topology
CMD cd11-rsdf-processor/bin/cd11-rsdf-processor

HEALTHCHECK --interval=30s --retries=10 \
 CMD curl -f localhost:8080/cd11-rsdf-processor/alive || exit 1

