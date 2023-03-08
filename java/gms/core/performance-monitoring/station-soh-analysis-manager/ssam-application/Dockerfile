ARG DOCKER_REGISTRY
ARG DOCKER_IMAGE_TAG
FROM $DOCKER_REGISTRY/gms-common/java:$DOCKER_IMAGE_TAG

USER 0

WORKDIR /opt/gms

EXPOSE 8080

COPY bin ./ssam-application/bin
COPY lib ./ssam-application/lib

RUN set -ex && \
    chown -R gms:gms /opt/gms

# gms uid
USER 1001

WORKDIR ssam-application

# Define default command.
CMD ["./bin/ssam-application"]

HEALTHCHECK --interval=30s --start-period=300s --retries=10 \
  CMD curl -f localhost:8080/ssam-control/alive || exit 1
