#
#  Dockerfile to make a 'frameworks-osd-ttl-worker' image for GMS.
#
ARG DOCKER_REGISTRY
ARG DOCKER_IMAGE_TAG
ARG PROJECT_NAME
FROM ${DOCKER_REGISTRY}/${PROJECT_NAME}/postgres:${DOCKER_IMAGE_TAG}

USER 0

WORKDIR /ttl

COPY src/trigger-stored-procedure /ttl

RUN set -ex && \
    chown -R postgres:postgres /ttl

# postgres uid
USER 26

# If we are running, we are healthy
HEALTHCHECK --interval=30s CMD /bin/true

# Empty out the entrypoint that we inherit from the postgres image.
ENTRYPOINT [""]

CMD ["/ttl/trigger-stored-procedure"]
