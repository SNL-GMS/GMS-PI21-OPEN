#
#  Dockerfile to make a 'cypress-test' image for GMS.
#

ARG DOCKER_REGISTRY
ARG DOCKER_IMAGE_TAG
ARG PROJECT_NAME

FROM ${DOCKER_REGISTRY}/${PROJECT_NAME}/builder:${DOCKER_IMAGE_TAG}

USER 0

RUN set -ex && \
    mkdir -p /user-interface && \
    chown -R gms:gms /user-interface && \
    # yum requirements
    dnf -y install GConf2 libXScrnSaver gtk2-devel gtk3-devel libnotify-devel xorg-x11-server-Xvfb alsa-lib && \
    # cleanup
    dnf clean all && \
    rm -rf /var/cache/dnf

# gms uid
USER 1001

RUN set -ex && \
    # install cypress - note: the download mirror is configured inside the builder image
    yarn global add cypress@10.8.0 -W && \
    yarn global add mochawesome-merge@4.2.1 -W

COPY --chown=gms:gms ./_user-interface /user-interface
COPY --chown=gms:gms ./scripts/run-cypress-tests /user-interface/packages/cypress-tests/run-cypress-tests
COPY --chown=gms:gms ./_scripts/pre-test /user-interface/packages/cypress-tests/pre-test
COPY --chown=gms:gms ./_scripts/post-test /user-interface/packages/cypress-tests/post-test

WORKDIR /user-interface/packages/cypress-tests

ARG SORRY_CYPRESS_AUTHOR
ARG SORRY_CYPRESS_BRANCH
ARG SORRY_CYPRESS_EMAIL
ARG SORRY_CYPRESS_MESSAGE
ARG SORRY_CYPRESS_REMOTE
ARG SORRY_CYPRESS_SHA
ARG SORRY_CYPRESS_TIMESTAMP
ENV COMMIT_INFO_AUTHOR=${SORRY_CYPRESS_AUTHOR}
ENV COMMIT_INFO_BRANCH=${SORRY_CYPRESS_BRANCH}
ENV COMMIT_INFO_EMAIL=${SORRY_CYPRESS_EMAIL}
ENV COMMIT_INFO_MESSAGE=${SORRY_CYPRESS_MESSAGE}
ENV COMMIT_INFO_REMOTE=${SORRY_CYPRESS_REMOTE}
ENV COMMIT_INFO_SHA=${SORRY_CYPRESS_SHA}
ENV COMMIT_INFO_TIMESTAMP=${SORRY_CYPRESS_TIMESTAMP}

CMD ["./run-cypress-tests"]
