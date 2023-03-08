ARG DOCKER_REGISTRY
ARG DOCKER_IMAGE_TAG
FROM $DOCKER_REGISTRY/gms-common/java:$DOCKER_IMAGE_TAG

USER 0

WORKDIR /opt/gms

EXPOSE 8080

#COPY bin ./signal-enhancement-configuration-application/bin
#COPY lib ./signal-enhancement-configuraiton-application/lib
COPY signal-enhancement-configuration-application-LATEST.jar ./signal-enhancement-configuration-application/signal-enhancement-configuration-application-LATEST.jar

RUN set -ex && \
    chown -R gms:gms /opt/gms

# gms uid
USER 1001

WORKDIR signal-enhancement-configuration-application

# Define default command.
CMD ["java", "-jar", "/opt/gms/signal-enhancement-configuration-application/signal-enhancement-configuration-application-LATEST.jar"]

HEALTHCHECK --interval=5s --timeout=10s --retries=10 \
  CMD curl -sfm 30 localhost:8080/signal-enhancement-configuration-application/alive || exit 1

