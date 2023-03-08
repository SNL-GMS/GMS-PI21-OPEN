# Run cd11-data-provider

ARG DOCKER_REGISTRY
ARG DOCKER_IMAGE_TAG
FROM $DOCKER_REGISTRY/gms-common/java:$DOCKER_IMAGE_TAG

USER 0

WORKDIR /cd11-data-provider/

# Copy in the CD1.1 RSDF data and configuration
COPY conf /rsdf/cd11/

COPY bin ./cd11-data-provider/bin
COPY lib ./cd11-data-provider/lib

RUN set -ex && \
    chown -R gms:gms /cd11-data-provider /rsdf

# gms uid
USER 1001
# This script will set the CD11_INJECTOR_CONFIG_PATH and run the cd11-data-provider
CMD export GMS_CONFIG_CD11_DATA_PROVIDER__CONFIG_PATH=/rsdf/cd11/${CD11_INJECTOR_CONFIG_NAME}-config.json && cd11-data-provider/bin/cd11-data-provider

