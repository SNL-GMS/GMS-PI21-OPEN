ARG DOCKER_REGISTRY
ARG DOCKER_IMAGE_TAG
ARG PROJECT_NAME

FROM ${DOCKER_REGISTRY}/${PROJECT_NAME}/ubi:${DOCKER_IMAGE_TAG}

COPY --chown=65534:65534 src/register.sh /register.sh
COPY --chown=65534:65534 src/unregister.sh /unregister.sh

# prometheus pods run as the nobody 65534 user
USER 65534

ENTRYPOINT ["/register.sh"]
