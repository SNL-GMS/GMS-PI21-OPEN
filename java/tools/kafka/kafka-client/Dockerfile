ARG DOCKER_REGISTRY
ARG DOCKER_IMAGE_TAG
FROM $DOCKER_REGISTRY/gms-common/java:$DOCKER_IMAGE_TAG

USER 0

WORKDIR /opt/gms

EXPOSE 8080

COPY bin ./kafka-client/bin
COPY lib ./kafka-client/lib

RUN set -ex && \
    chown -R gms:gms /opt/gms

# gms uid
USER 1001

WORKDIR kafka-client

#Running the jar in the docker container
ENTRYPOINT ["./bin/kafka-client"]
