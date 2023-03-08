# This image pulls the internet swagger-ui that is based on alpine, then copies
# the original files out of it into our ubi/nginx image
#
ARG FROM_IMAGE
ARG DOCKER_REGISTRY
ARG DOCKER_IMAGE_TAG
ARG PROJECT_NAME
FROM ${FROM_IMAGE} as base
FROM $DOCKER_REGISTRY/$PROJECT_NAME/nginx:$DOCKER_IMAGE_TAG

# env vars from swagger image
ENV API_KEY="**NONE**"
ENV SWAGGER_JSON="/"
ENV PORT=8080
ENV BASE_URL=""

USER 0

# copy files out of the base swagger image
COPY --from=base /etc/nginx /etc/nginx
COPY --from=base /usr/share/nginx /usr/share/nginx

RUN set -ex && \
    # install node 14
    dnf -y module enable nodejs:14 && \
    dnf -y install nodejs && \
    # modify nginx.conf
    sed -i '/gzip_types/a \ \ map $remote_addr $log_ip {\"127.0.0.1" 0;\ default 1;}' /etc/nginx/nginx.conf && \
    sed -i '/index/a \ \ \ \ access_log /var/log/nginx/access.log combined if=$log_ip;' /etc/nginx/nginx.conf && \
    sed -i '/index/a \ \ \ \ absolute_redirect off;' /etc/nginx/nginx.conf && \
    chown -R nginx:nginx /etc/nginx /usr/share/nginx && \
    # clean up
    dnf clean all && \
    rm -rf /var/cache/dnf

# copy gms items
COPY --chown=nginx:nginx src/configure-spec-files.sh /configure-spec-files.sh
COPY --chown=nginx:nginx src/spec /usr/share/nginx/html/spec

HEALTHCHECK --interval=30s --timeout=30s \
  CMD wget --quiet --spider http://localhost:8080 \
    || exit 1

# nginx uid
USER 1002

# The `swaggerapi/swagger-ui` image had a CMD of `sh /usr/share/nginx/run.sh`.
# Here we just stick our `configure-spec-files.sh` script in front of that so
# that it can setup the $URLS environment variable based on the files that it
# finds in the `/usr/share/nginx/html/spec` directory at run time.
ENTRYPOINT ["sh", "/configure-spec-files.sh", "/usr/share/nginx/run.sh"]
