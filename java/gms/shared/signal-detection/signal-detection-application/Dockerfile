ARG DOCKER_REGISTRY
ARG DOCKER_IMAGE_TAG
FROM $DOCKER_REGISTRY/gms-common/java:$DOCKER_IMAGE_TAG

USER 0

WORKDIR /opt/gms

EXPOSE 8080

#COPY bin ./signal-detection-application/bin
#COPY lib ./signal-detection-application/lib
COPY signal-detection-application-LATEST.jar ./signal-detection-application/signal-detection-application-LATEST.jar

RUN set -ex && \
    chown -R gms:gms /opt/gms

# gms uid
USER 1001

WORKDIR signal-detection-application

# Disabling Ignite's quiet mode to clean up console logs
ENV JAVA_TOOL_OPTIONS "-DIGNITE_QUIET=false"

# Define default command.
CMD ["java", "-jar", "/opt/gms/signal-detection-application/signal-detection-application-LATEST.jar"]

HEALTHCHECK --interval=5s --timeout=10s --retries=10 \
  CMD curl -sfm 30 localhost:8080/signal-detection-application/alive || exit 1

