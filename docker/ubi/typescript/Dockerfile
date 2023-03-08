ARG DOCKER_REGISTRY
ARG DOCKER_IMAGE_TAG
ARG PROJECT_NAME

FROM $DOCKER_REGISTRY/$PROJECT_NAME/ubi:$DOCKER_IMAGE_TAG

ARG NODE_YUM_URL
ARG NODEJS_VERSION

USER 0

COPY src/nodesource.repo /etc/yum.repos.d/nodesource.repo

RUN set -ex && \
    sed -i -e "s~#NODE_YUM_URL#~${NODE_YUM_URL}~g" /etc/yum.repos.d/nodesource.repo && \
    sed -i -e "s~#NODE_MAJOR_VERSION#~${NODEJS_VERSION%%.*}~g" /etc/yum.repos.d/nodesource.repo && \
    # disable the appstream version of nodejs - it conflicts with nodesource
    dnf -y module disable nodejs && \
    dnf -y install nodejs-$NODEJS_VERSION && \
    # create node user
    groupadd --gid 1000 node && \
    useradd --uid 1000 --gid 1000 --shell /bin/bash --create-home node && \
    # clean up
    dnf clean all && \
    rm -rf /var/cache/dnf

# node uid
USER 1000

CMD ["/bin/bash"]