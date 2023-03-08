ARG DOCKER_REGISTRY
ARG DOCKER_IMAGE_TAG
ARG PROJECT_NAME

FROM ${DOCKER_REGISTRY}/${PROJECT_NAME}/ubi:${DOCKER_IMAGE_TAG}

ARG NGINX_YUM_URL
ARG NGINX_VERSION

USER 0

COPY src/nginx.repo /etc/yum.repos.d/nginx.repo

RUN set -ex && \
    # explicitly add the nginx user to prevent the uid/gid from changing
    groupadd --gid 1002 nginx && \
    useradd --gid 1002 --uid 1002 --comment "nginx user" --home-dir /var/cache/nginx --shell /sbin/nologin --system nginx && \
    # install nginx
    sed -i -e "s~#NGINX_YUM_URL#~$NGINX_YUM_URL~g" /etc/yum.repos.d/nginx.repo && \
    dnf -y install nginx-${NGINX_VERSION} && \
    # fix nginx user permissions
    touch /var/run/nginx.pid && \
    mkdir -p /var/cache/nginx && \
    chown -R nginx:nginx /var/cache/nginx && \
    chown -R nginx:nginx /var/log/nginx && \
    chown -R nginx:nginx /etc/nginx && \
    chown -R nginx:nginx /var/run/nginx.pid && \
    # forward nginx logs to stdout and stderr
    ln -sf /dev/stdout /var/log/nginx/access.log && \
    ln -sf /dev/stderr /var/log/nginx/error.log && \
    # clean up
    dnf clean all && \
    rm -rf /var/cache/dnf

# Expose NGINX ports
EXPOSE 8080 8443

# nginx uid
USER 1002

ENTRYPOINT ["nginx", "-g", "daemon off;"]

STOPSIGNAL SIGQUIT
