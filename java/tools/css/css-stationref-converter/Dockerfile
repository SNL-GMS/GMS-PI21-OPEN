ARG DOCKER_REGISTRY
ARG DOCKER_IMAGE_TAG
FROM $DOCKER_REGISTRY/gms-common/java:$DOCKER_IMAGE_TAG

USER 0

WORKDIR /css-stationref-converter/

COPY Metadata ./css-root
COPY bin ./css-stationref-converter/bin
COPY lib ./css-stationref-converter/lib

RUN set -ex && \
    chown -R gms:gms /css-stationref-converter

# gms uid
USER 1001

ENTRYPOINT ["./css-stationref-converter/bin/css-stationref-converter"]
