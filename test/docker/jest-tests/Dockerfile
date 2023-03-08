#
#  Dockerfile to make a 'jest-test' image for GMS.
#

ARG DOCKER_REGISTRY
ARG DOCKER_IMAGE_TAG
ARG PROJECT_NAME

FROM ${DOCKER_REGISTRY}/${PROJECT_NAME}/builder:${DOCKER_IMAGE_TAG}

USER 0

RUN set -ex && \
    mkdir -p /user-interface && \
    chown -R gms:gms /user-interface

# gms uid
USER 1001

COPY --chown=gms:gms ./_user-interface /user-interface
COPY --chown=gms:gms ./scripts/run-jest-tests /user-interface/packages/integration-tests/run-jest-tests
COPY --chown=gms:gms ./_scripts/pre-test /user-interface/packages/integration-tests/pre-test
COPY --chown=gms:gms ./_scripts/post-test /user-interface/packages/integration-tests/post-test

WORKDIR /user-interface/packages/integration-tests

CMD ["./run-jest-tests"]
