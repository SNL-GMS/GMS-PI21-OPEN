ARG DOCKER_REGISTRY
ARG DOCKER_IMAGE_TAG
ARG PROJECT_NAME

FROM ${DOCKER_REGISTRY}/${PROJECT_NAME}/ubi:${DOCKER_IMAGE_TAG}

ARG GITHUB_URL
ARG JAVA_VERSION
ARG JAVA_MAJOR_VERSION
ENV JAVA_HOME /usr/local/openjdk-${JAVA_MAJOR_VERSION}
ENV PATH=${JAVA_HOME}/bin:${PATH}

USER 0

RUN set -ex && \
    # install java jre (steps are derived from the official openjdk dockerfile)
    # JAVA_VERSION is the full version number with update (i.e 11.0.4.1-1)
    # JAVA_VERSION%%-* is the version without update number (i.e 11.0.4.1)
    # JAVA_VERSION##*- is the update number after the dash (i.e 1)
    mkdir -p ${JAVA_HOME} && \
    curl -fOL $GITHUB_URL/AdoptOpenJDK/openjdk${JAVA_MAJOR_VERSION}-upstream-binaries/releases/download/jdk-${JAVA_VERSION%%-*}+${JAVA_VERSION##*-}/OpenJDK${JAVA_MAJOR_VERSION}U-jre_x64_linux_${JAVA_VERSION%%-*}_${JAVA_VERSION##*-}.tar.gz && \
    tar -xvf OpenJDK${JAVA_MAJOR_VERSION}U-jre_x64_linux_${JAVA_VERSION%%-*}_${JAVA_VERSION##*-}.tar.gz --directory ${JAVA_HOME} --strip-components 1 --no-same-owner && \
    chmod a+rx ${JAVA_HOME} && \
    rm -f OpenJDK*.tar.gz && \
    find "${JAVA_HOME}/lib" -name '*.so' -exec dirname '{}' ';' | sort -u > /etc/ld.so.conf.d/docker-openjdk.conf && \
    ldconfig && \
    java -Xshare:dump && \
    java --version

# gms uid
USER 1001

CMD ["/bin/bash"]
