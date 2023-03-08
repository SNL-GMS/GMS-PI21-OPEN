ARG DOCKER_REGISTRY
ARG DOCKER_IMAGE_TAG
ARG PROJECT_NAME

FROM $DOCKER_REGISTRY/$PROJECT_NAME/nginx:$DOCKER_IMAGE_TAG

COPY --chown=nginx:nginx src/javadoc /usr/share/nginx/html/javadoc
COPY --chown=nginx:nginx src/nginx.conf /etc/nginx/nginx.conf

# nginx uid
USER 1002
