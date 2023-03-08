#
#  Dockerfile to make a configured etcd image for GMS.
#
ARG DOCKER_REGISTRY
ARG DOCKER_IMAGE_TAG
ARG PROJECT_NAME
FROM $DOCKER_REGISTRY/${PROJECT_NAME}/python:$DOCKER_IMAGE_TAG

ARG ETCD_VERSION
ARG GITHUB_URL

# Configure a single-node etcd server.  ETCD_NAME is definied
# in a separate ENV statement so that it can be interpolated
# later when setting ETCD_INITIAL_CLUSTER.
ENV ETCD_NAME=etcd1
ENV USER_NAME=etcd \
    ETCDCTL_API=3 \
    ETCD_DATA_DIR=/var/lib/etcd \
    ETCD_INITIAL_ADVERTISE_PEER_URLS=http://127.0.0.1:2380 \
    ETCD_LISTEN_PEER_URLS=http://0.0.0.0:2380 \
    ETCD_ADVERTISE_CLIENT_URLS=http://127.0.0.1:2379 \
    ETCD_LISTEN_CLIENT_URLS=http://0.0.0.0:2379 \
    ETCD_INITIAL_CLUSTER="${ETCD_NAME}=http://127.0.0.1:2380"

USER 0

# Copy in the scripts
COPY src/*.sh /usr/local/bin/
# copy gms-sysconfig Python interface
COPY src/_python/gms-sysconfig /setup/gms-sysconfig

RUN set -ex && \
    # install etcd
    curl -fOL ${GITHUB_URL}/etcd-io/etcd/releases/download/${ETCD_VERSION}/etcd-${ETCD_VERSION}-linux-amd64.tar.gz && \
    tar -xzf etcd-${ETCD_VERSION}-linux-amd64.tar.gz --no-same-owner && \
    mv etcd-${ETCD_VERSION}-linux-amd64/etcd etcd-${ETCD_VERSION}-linux-amd64/etcdctl /usr/bin && \
    rm -rf etcd-${ETCD_VERSION}-linux-amd64 && \
    # install gms-sysconfig
    umask 027 && \
    micromamba install -y --name base --file /setup/gms-sysconfig/environment.lock.yml && \
    cd /setup/gms-sysconfig && \
    pip install . && \
    # Setup permissions
    mkdir -p ${ETCD_DATA_DIR} && \
    chown -R gms:gms ${ETCD_DATA_DIR} && \
    # cleanup
    micromamba clean --all -y

WORKDIR /

# Copy in the the system configuration files
COPY src/_config/system/* /setup/config/system/

# gms uid
USER 1001

RUN set -ex && \
    # run setup script - unset the build arg ETCD_VERSION since the script does not like it
    unset ETCD_VERSION && \
    /usr/local/bin/etcd-setup.sh

HEALTHCHECK --interval=30s --timeout=30s --retries=5 CMD etcd-healthcheck.sh || exit 1

CMD ["etcd-entrypoint.sh", "etcd"]
