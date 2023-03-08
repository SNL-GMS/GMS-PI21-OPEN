#
# Dockerfile for running node applications as part of the GMS project
#

# Start with the GMS node base image
ARG DOCKER_REGISTRY
ARG DOCKER_IMAGE_TAG
ARG PROJECT_NAME

FROM ${DOCKER_REGISTRY}/${PROJECT_NAME}/typescript:${DOCKER_IMAGE_TAG}

USER 0

# Copy in the application and fix permissions.
COPY ./resources /app/resources
COPY ./dist /app
COPY ./config/ /app/config/

RUN chown -R node:node /app

# node uid
USER 1000
WORKDIR /app

# Create a volume for the externally-mounted waveform binary test data files
VOLUME /opt/app-root/src/Test_Data_Sets

# Expose the HTTP (3000) websockets (4000) ports
EXPOSE 3000 4000

# CI Commit REF and SHA are displayed on the UI
ARG CI_COMMIT_REF_NAME=NA
ARG CI_COMMIT_SHA=NA
ENV CI_COMMIT_REF_NAME=${CI_COMMIT_REF_NAME} \
    CI_COMMIT_SHA=${CI_COMMIT_SHA}

# Set the default container startup command.
ENTRYPOINT ["node", "--max-old-space-size=8142"]

CMD ["./api-gateway-server.js"]

HEALTHCHECK --interval=30s --retries=10 \
 CMD curl -f localhost:3000/alive || exit 1
