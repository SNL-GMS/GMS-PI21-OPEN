#
# Dockerfile for running an NGINX instance serving up the GMS analyst user interface.
#

# Start with the project NGINX base image
ARG DOCKER_REGISTRY
ARG DOCKER_IMAGE_TAG
ARG PROJECT_NAME

FROM ${DOCKER_REGISTRY}/${PROJECT_NAME}/nginx:${DOCKER_IMAGE_TAG}

USER 0

WORKDIR /opt/interactive-analysis-ui/

# Copy files into the container
COPY ./nginx/ /etc/nginx
COPY ./docker-entrypoint.sh /docker-entrypoint.sh
COPY ./dist/ /opt/interactive-analysis-ui

# Since we're running as non-root, change the owner of places where NGINX writes
RUN set -ex && \
    chown -R nginx:nginx /opt/interactive-analysis-ui /etc/nginx /docker-entrypoint.sh

# nginx uid
USER 1002

# Expose NGINX ports
EXPOSE 8080 8443

ARG CI_COMMIT_REF_NAME=NA
ARG CI_COMMIT_SHA=NA

ENV CI_COMMIT_REF_NAME=${CI_COMMIT_REF_NAME} \
    CI_COMMIT_SHA=${CI_COMMIT_SHA}

# Set the run command to:
# 1. Create the ngninx conf file based on a template file and environment variables
# 2. Run nginx in the foreground ('deamon off')
ENTRYPOINT ["/bin/bash", "-c", "/docker-entrypoint.sh"]

HEALTHCHECK --interval=30s --retries=10 \
 CMD curl -f localhost:8080/alive || exit 1
