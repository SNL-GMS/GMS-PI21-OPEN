ARG DOCKER_REGISTRY
ARG DOCKER_IMAGE_TAG
ARG PROJECT_NAME

FROM ${DOCKER_REGISTRY}/${PROJECT_NAME}/python:${DOCKER_IMAGE_TAG}

ARG GOOGLEAPIS_URL
ARG KUBECTL_VERSION
ARG HELM_URL
ARG HELM_VERSION

# Run python with unbuffered stdout
ENV PYTHONUNBUFFERED=1
# Set the path to the CA bundle for python requests
ENV REQUESTS_CA_BUNDLE=/etc/pki/tls/certs/ca-bundle.crt
# Set the path to the kubeconfig used by kubectl
ENV KUBECONFIG=/kubeconfig/config

# gmskube initially runs as USER 0, updates the gms uid, then switches to the gms user
# in the entrypoint script
USER 0

RUN set -ex && \
    umask 007 && \
    mkdir -p /kubeconfig && \
    chown gms:gms /kubeconfig && \
    # Install kubectl
    curl -OL ${GOOGLEAPIS_URL}/kubernetes-release/release/v${KUBECTL_VERSION}/bin/linux/amd64/kubectl && \
    mv ./kubectl /usr/local/bin/kubectl && \
    chmod a+rx /usr/local/bin/kubectl && \
    # Install helm
    curl -OL ${HELM_URL}/helm-v${HELM_VERSION}-linux-amd64.tar.gz && \
    tar -xvf helm-v${HELM_VERSION}-linux-amd64.tar.gz --no-same-owner && \
    mv ./linux-amd64/helm /usr/local/bin/helm && \
    chmod a+rx /usr/local/bin/helm && \
    rm -rf helm-v${HELM_VERSION}-linux-amd64.tar.gz ./linux-amd64

COPY ./gmskube /install/gmskube/gmskube
COPY ./setup.py /install/gmskube/setup.py
COPY ./environment.lock.yml /install/gmskube/environment.lock.yml

RUN set -ex && \
    umask 027 && \
    cd /install/gmskube && \
    micromamba install -y --name base --file environment.lock.yml && \
    pip install . && \
    micromamba clean --all -y

COPY --chown=gms:gms ./_deploy /deploy
COPY ./docker-entrypoint.sh /

WORKDIR /
ENTRYPOINT ["/docker-entrypoint.sh"]
CMD ["--help"]
