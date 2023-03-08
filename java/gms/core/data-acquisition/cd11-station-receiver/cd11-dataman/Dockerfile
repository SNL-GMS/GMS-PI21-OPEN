# Docker build arguments
ARG DOCKER_REGISTRY
ARG DOCKER_IMAGE_TAG
FROM $DOCKER_REGISTRY/gms-common/java:$DOCKER_IMAGE_TAG

USER 0

WORKDIR /data-receiver/

COPY bin ./cd11-dataman/bin
COPY lib ./cd11-dataman/lib

RUN set -ex && \
    mkdir -p /data-receiver/shared-volume/logs/stations && \
    mkdir -p /data-receiver/shared-volume/dataframes && \
    chown -R gms:gms /data-receiver

# gms uid
USER 1001

# Start cd11-dataman
CMD cd11-dataman/bin/cd11-dataman

HEALTHCHECK --interval=30s --retries=10 \
 CMD curl -f localhost:8080/da-dataman/alive || exit 1

