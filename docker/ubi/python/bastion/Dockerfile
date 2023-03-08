#
#  Dockerfile to make a 'bastion' image for GMS.
#
#  This is a container usable for system maintenance and diagnostics.
#
#  docker exec -it [ bastion image id ] /bin/bash
#

ARG DOCKER_REGISTRY
ARG DOCKER_IMAGE_TAG
ARG PROJECT_NAME

FROM ${DOCKER_REGISTRY}/${PROJECT_NAME}/python:${DOCKER_IMAGE_TAG}

ARG LIBRDKAFKA_VERSION
ARG KCAT_VERSION
ARG ETCD_VERSION
ARG GITHUB_URL
ARG ORACLE_URL
ARG MINIO_URL

ENV LD_LIBRARY_PATH="/usr/local/lib:/usr/lib/oracle/18.5/client64/lib"
ENV PATH="/usr/lib/oracle/18.5/client64/bin:${PATH}"

USER 0

WORKDIR /opt/gms

RUN set -ex && \
    dnf -y install gcc-c++ cmake make openssl bind-utils net-tools postgresql tcpdump libnsl zlib-devel libzstd-devel && \
    # build librdkafka
    curl -fOL ${GITHUB_URL}/edenhill/librdkafka/archive/v${LIBRDKAFKA_VERSION}.tar.gz && \
    tar -xzf v${LIBRDKAFKA_VERSION}.tar.gz --no-same-owner && \
    cd librdkafka-${LIBRDKAFKA_VERSION} && \
    ./configure && \
    make -j$(nproc)&& \
    make install && \
    cd .. && \
    rm -rf v${LIBRDKAFKA_VERSION}.tar.gz librdkafka-${LIBRDKAFKA_VERSION} && \
    # build kcat (formerly known as kafkacat)
    curl -fOL ${GITHUB_URL}/edenhill/kcat/archive/${KCAT_VERSION}.tar.gz && \
    tar -xzf ${KCAT_VERSION}.tar.gz --no-same-owner && \
    cd kcat-${KCAT_VERSION} && \
    LDFLAGS="-pthread -lm -ldl" ./configure --enable-static --enable-json && \
    make -j$(nproc)&& \
    make install && \
    cd .. && \
    rm -rf ${KCAT_VERSION}.tar.gz kcat-${KCAT_VERSION} && \
    ln -s /usr/local/bin/kcat /usr/local/bin/kafkacat && \
    # install etcdctl
    curl -fOL ${GITHUB_URL}/etcd-io/etcd/releases/download/${ETCD_VERSION}/etcd-${ETCD_VERSION}-linux-amd64.tar.gz && \
    tar -xzf etcd-${ETCD_VERSION}-linux-amd64.tar.gz --no-same-owner && \
    mv etcd-${ETCD_VERSION}-linux-amd64/etcdctl /usr/bin && \
    rm -rf etcd-${ETCD_VERSION}-linux-amd64 etcd-${ETCD_VERSION}-linux-amd64.tar.gz && \
    # install oracle instant client
    curl -fOL ${ORACLE_URL}/otn_software/linux/instantclient/185000/oracle-instantclient18.5-basic-18.5.0.0.0-3.x86_64.rpm && \
    curl -fOL ${ORACLE_URL}/otn_software/linux/instantclient/185000/oracle-instantclient18.5-sqlplus-18.5.0.0.0-3.x86_64.rpm && \
    dnf -y --nogpgcheck localinstall oracle-instantclient*.rpm && \
    rm -rf oracle-instantclient*.rpm && \
    # install minio client
    curl -fOL ${MINIO_URL}/client/mc/release/linux-amd64/mc && \
    mv ./mc /usr/local/bin/mc && \
    chmod a+rx /usr/local/bin/mc && \
    # cleanup
    dnf -y remove gcc-c++ cmake make && \
    dnf clean all && \
    rm -rf /var/cache/dnf

ENV ETCDCTL_API=3

# If we are running, we are healthy
HEALTHCHECK CMD ps -o args -p 1 | grep sleep

# gms uid
USER 1001

# Run in the background forever.
# System maintainers should `docker exec -it [ bastion image id ] /bin/bash` to access
CMD ["/bin/sleep", "infinity"]
