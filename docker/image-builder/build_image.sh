#!/bin/sh

# This script builds the image without using the bash/make based CI scripts
# used for the other images. This allows it to build inside the alpine-based
# docker image from dockerhub. It is intended to be run from gitlab CI.

# source the repository.env and versions.env file to set some variables
. ${CI_PROJECT_DIR}/ci/repository.env
. ${CI_PROJECT_DIR}/ci/versions.env

SOURCE_IMAGE=${IRONBANK_REGISTRY}/ironbank/redhat/ubi/ubi8
SOURCE_TAG=$${UBI_TAG}

if [ -n "${CI_USE_PROXY}" ]; then
    docker_proxy_args="--build-arg http_proxy --build-arg https_proxy --build-arg no_proxy"
fi

docker build \
    --build-arg "SOURCE_IMAGE=${SOURCE_IMAGE}" \
    --build-arg "SOURCE_TAG=${SOURCE_TAG}${UPSTREAM_TAG_SUFFIX}" \
    --build-arg "UBI_RPM_URL=${UBI_RPM_URL}" \
    --build-arg "DOCKER_YUM_URL=${DOCKER_YUM_URL}" \
    ${docker_proxy_args} \
    --tag "${CI_DOCKER_REGISTRY}/gms-common/image-builder:${CI_COMMIT_SHA}" \
    ${CI_PROJECT_DIR}/docker/image-builder && \
docker push ${CI_DOCKER_REGISTRY}/gms-common/image-builder:${CI_COMMIT_SHA}