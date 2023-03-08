ARG DOCKER_REGISTRY
ARG DOCKER_IMAGE_TAG
FROM $DOCKER_REGISTRY/gms-common/java:$DOCKER_IMAGE_TAG

USER 0

WORKDIR /opt/gms

EXPOSE 8080

COPY ui-processing-configuration-service-LATEST.jar ./ui-processing-configuration-service/ui-processing-configuration-service-LATEST.jar

RUN set -ex && \
    chown -R gms:gms /opt/gms

# gms uid
USER 1001

WORKDIR ui-processing-configuration-service

HEALTHCHECK --interval=5s --timeout=10s --retries=10 \
 CMD curl -sfm 30 localhost:8080/ui-processing-configuration-service/alive || exit 1

# Define default command.
CMD ["java", "-jar", "/opt/gms/ui-processing-configuration-service/ui-processing-configuration-service-LATEST.jar"]

