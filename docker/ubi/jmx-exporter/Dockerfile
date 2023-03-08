# build bitnami jmx-exporter on ubi
ARG JMX_EXPORTER_IMAGE
ARG JMX_EXPORTER_TAG
ARG DOCKER_REGISTRY
ARG DOCKER_IMAGE_TAG
ARG PROJECT_NAME
FROM ${JMX_EXPORTER_IMAGE}:${JMX_EXPORTER_TAG} as jmx
FROM ${DOCKER_REGISTRY}/${PROJECT_NAME}/ubi:${DOCKER_IMAGE_TAG}

ARG BITNAMI_HOME=/opt/bitnami
ENV PATH="${BITNAMI_HOME}/java/bin:$PATH"

USER 0

# copy files out of jmx image into our ubi image
COPY --from=jmx ${BITNAMI_HOME} ${BITNAMI_HOME}

RUN set -ex && \
    dnf -y install procps && \
    chown -R gms:gms ${BITNAMI_HOME} && \
    # clean up
    dnf clean all && \
    rm -rf /var/cache/dnf

EXPOSE 5556
WORKDIR ${BITNAMI_HOME}/jmx-exporter

# gms uid
USER 1001

ENTRYPOINT [ "java", "-jar", "jmx_prometheus_httpserver.jar" ]
CMD [ "5556", "example_configs/httpserver_sample_config.yml" ]