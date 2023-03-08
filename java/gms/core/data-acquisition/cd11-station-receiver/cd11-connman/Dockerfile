# Docker build arguments
ARG DOCKER_REGISTRY
ARG DOCKER_IMAGE_TAG
FROM $DOCKER_REGISTRY/gms-common/java:$DOCKER_IMAGE_TAG

USER 0

WORKDIR /data-receiver/

COPY bin ./cd11-connman/bin
COPY lib ./cd11-connman/lib

RUN set -ex && \
    # connman needs the `host` command from bind-utils to resolve hostnames in a Fortify-friendly way
    dnf -y install bind-utils && \
    mkdir -p /data-receiver/shared-volume/logs && \
    chown -R gms:gms /data-receiver && \
    # clean up
    dnf clean all && \
    rm -rf /var/cache/dnf

# gms uid
USER 1001

CMD cd11-connman/bin/cd11-connman

HEALTHCHECK --interval=30s --retries=10 \
 CMD curl -f localhost:8080/da-connman/alive || exit 1
