ARG DOCKER_REGISTRY
ARG DOCKER_IMAGE_TAG
FROM $DOCKER_REGISTRY/gms-common/java:$DOCKER_IMAGE_TAG

USER 0

WORKDIR /opt/gms

EXPOSE 8080

COPY station-definition-application-LATEST.jar ./station-definition-application/station-definition-application-LATEST.jar


RUN set -ex && \
    chown -R gms:gms /opt/gms

# gms uid
USER 1001

WORKDIR station-definition-application

HEALTHCHECK --interval=5s --timeout=10s --retries=10 \
 CMD curl -sfm 30 localhost:8080/station-definition-service/alive || exit 1

# Disabling Ignite's quiet mode to clean up console logs
ENV JAVA_TOOL_OPTIONS "-DIGNITE_QUIET=false"

# Define default command.
CMD ["java", "-jar", "/opt/gms/station-definition-application/station-definition-application-LATEST.jar"]

