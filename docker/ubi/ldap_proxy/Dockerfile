ARG DOCKER_REGISTRY
ARG DOCKER_IMAGE_TAG
ARG PROJECT_NAME

FROM ${DOCKER_REGISTRY}/${PROJECT_NAME}/ubi:${DOCKER_IMAGE_TAG}

ARG HTTPD_VERSION
ENV LOG_LEVEL=debug

USER 0

RUN set -ex && \
    dnf -y install httpd-${HTTPD_VERSION} mod_ldap mod_ssl && \
    rm /etc/httpd/conf.d/*.conf && \
    sed -i 's/Listen\ 80/Listen\ 8080/' /etc/httpd/conf/httpd.conf && \
    chmod 0770 /run/httpd/ && \
    chown -R apache:apache /etc/httpd/ && \
    chown -R apache:apache /var/log/httpd/ && \
    # clean up
    dnf clean all && \
    rm -rf /var/cache/dnf

COPY --chown=apache:apache src/docker-entrypoint.sh /docker-entrypoint.sh
COPY --chown=apache:apache src/default-site.conf /etc/httpd/conf.d/

# apache uid
USER 48

ENTRYPOINT ["/docker-entrypoint.sh"]
CMD [ "/usr/sbin/apachectl", "-DFOREGROUND" ]
EXPOSE 8080

HEALTHCHECK --interval=30s --retries=10 \
  CMD ps -ef | grep 'apache.*httpd' | grep -v grep \
  || exit 1
