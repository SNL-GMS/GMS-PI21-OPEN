ARG EMSCRIPTEN_IMAGE
ARG EMSCRIPTEN_TAG
ARG SOURCE_IMAGE
ARG SOURCE_TAG
FROM ${EMSCRIPTEN_IMAGE}:${EMSCRIPTEN_TAG} as emscripten
FROM ${SOURCE_IMAGE}:${SOURCE_TAG}

# ubi base
ARG UBI_RPM_URL
ARG ALMALINUX_RPM_URL
ARG EPEL_YUM_URL
# mamba
ARG MICROMAMBA_URL
ARG ANACONDA_REPO_URL
ARG MICROMAMBA_VERSION
ENV MAMBA_ROOT_PREFIX=/opt/conda
# pip
ARG PYPI_URL
ENV PIP_INDEX_URL=${PYPI_URL}
ENV PIP_NO_CACHE_DIR=1
ENV PIP_NO_COMPILE=1
# python
ENV PYTHONUNBUFFERED=1
ENV PYTHONDONOTWRITEBYTECODE=1
ENV REQUESTS_CA_BUNDLE=/etc/pki/tls/certs/ca-bundle.crt
# gradle
ARG GRADLE_BASE_URL
ARG GRADLE_VERSION
# java
ARG JAVA_VERSION
ARG JAVA_MAJOR_VERSION
ENV JAVA_HOME /usr/local/openjdk-${JAVA_MAJOR_VERSION}
# sonarscanner
ARG SONAR_URL
ARG SONAR_SCANNER_VERSION
# node
ARG NODEJS_FILES_URL
ARG NODE_YUM_URL
ARG NODEJS_VERSION
ARG NPM_REGISTRY
ARG NODE_GYP_PYTHON_VERSION
ENV NODE_GYP_FORCE_PYTHON=/opt/conda/envs/gyp/bin/python
# yarn
ARG YARN_VERSION
ARG YARN_REGISTRY
ENV YARN_NPM_REGISTRY_SERVER=${YARN_REGISTRY}
# docker
ARG DOCKER_YUM_URL
# electron
ARG ELECTRON_BUILDER_BINARIES_MIRROR
ENV ELECTRON_BUILDER_BINARIES_MIRROR=${ELECTRON_BUILDER_BINARIES_MIRROR}
ARG ELECTRON_MIRROR
ENV ELECTRON_MIRROR=${ELECTRON_MIRROR}
# cypress
ARG CYPRESS_DOWNLOAD_MIRROR
ENV CYPRESS_DOWNLOAD_MIRROR=${CYPRESS_DOWNLOAD_MIRROR}
ENV CYPRESS_CRASH_REPORTS=0
# docker-compose
ARG GITHUB_URL
ARG DOCKER_COMPOSE_VERSION
# kubectl
ARG GOOGLEAPIS_URL
ARG KUBECTL_VERSION
# helm
ARG HELM_URL
ARG HELM_VERSION
# minio client
ARG MINIO_URL
# gcc-toolset
ARG GCC_TOOLSET_MAJOR_VERSION
# enscripten
ENV EMSDK=/emsdk
ENV EM_CONFIG=/emsdk/.emscripten

# path
ENV PATH=${MAMBA_ROOT_PREFIX}/bin:${EMSDK}:${JAVA_HOME}/bin:${PATH}
# locale
ENV LANG="en_US.UTF-8"
ENV LANGUAGE="en_US.UTF-8"
ENV LC_ALL="en_US.UTF-8"

USER 0

# Reset shell to source environment files and always use bash.
SHELL [ "/bin/bash", "-lc" ]

# copy all the things
COPY src/_ubi/*.repo /etc/yum.repos.d/
COPY src/_python/pydistutils.cfg /root/.pydistutils.cfg
COPY src/environment.lock.yml /root/environment.lock.yml
COPY src/_typescript/nodesource.repo /etc/yum.repos.d/nodesource.repo
COPY src/docker-ce.repo /etc/yum.repos.d/docker-ce.repo
COPY --from=emscripten /emsdk /emsdk

RUN set -ex && \
    umask 027 && \
    # add gms user
    groupadd --gid 1001 gms && \
    useradd --gid 1001 --uid 1001 -d /opt/gms gms && \
    # configure yum
    echo "skip_missing_names_on_install=0" >> /etc/yum.conf && \
    # disable docs
    echo "tsflags=nodocs" >> /etc/yum.conf && \
    echo "tsflags=nodocs" >> /etc/dnf.conf && \
    # remove the ironbank repo and replace with a gms version
    rm -f /etc/yum.repos.d/ironbank.repo && \
    # setup repos
    sed -i -e "s~#UBI_RPM_URL#~${UBI_RPM_URL}~" /etc/yum.repos.d/ubi.repo && \
    sed -i -e "s~#ALMALINUX_RPM_URL#~${ALMALINUX_RPM_URL}~" /etc/yum.repos.d/almalinux.repo && \
    sed -i -e "s~#EPEL_YUM_URL#~${EPEL_YUM_URL}~" /etc/yum.repos.d/epel.repo && \
    sed -i -e "s~#DOCKER_YUM_URL#~${DOCKER_YUM_URL}~g" /etc/yum.repos.d/docker-ce.repo && \
    sed -i -e "s~#NODE_YUM_URL#~${NODE_YUM_URL}~g" /etc/yum.repos.d/nodesource.repo && \
    sed -i -e "s~#NODE_MAJOR_VERSION#~${NODEJS_VERSION%%.*}~g" /etc/yum.repos.d/nodesource.repo && \
    # install requirements
    # disable the appstream version of nodejs - it conflicts with nodesource
    dnf -y module disable nodejs && \
    dnf install -y bzip2 \
                   cmake \
                   docker-ce-cli \
                   gcc-c++ \
                   gcc-toolset-${GCC_TOOLSET_MAJOR_VERSION}-gcc-c++ \
                   gettext \
                   git \
                   glibc-locale-source \
                   glibc-langpack-en \
                   jq \
                   less \
                   make \
                   procps \
                   nodejs-${NODEJS_VERSION} \
                   perl \
                   postgresql-devel \
                   rsync \
                   unzip \
                   which && \
    # set locale
    localedef -i en_US -f UTF-8 en_US.UTF-8 && \
    echo "LANG=\"en_US.UTF-8\"" > /etc/locale.conf && \
    # install java jdk (steps are derived from the offical openjdk dockerfile)
    # JAVA_VERSION is the full version number with update (i.e 11.0.4.1-1)
    # JAVA_VERSION%%-* is the version without update number (i.e 11.0.4.1)
    # JAVA_VERSION##*- is the update number after the dash (i.e 1)
    mkdir -p ${JAVA_HOME} && \
    curl -fOL ${GITHUB_URL}/AdoptOpenJDK/openjdk${JAVA_MAJOR_VERSION}-upstream-binaries/releases/download/jdk-${JAVA_VERSION%%-*}+${JAVA_VERSION##*-}/OpenJDK${JAVA_MAJOR_VERSION}U-jdk_x64_linux_${JAVA_VERSION%%-*}_${JAVA_VERSION##*-}.tar.gz && \
    tar -xvf OpenJDK${JAVA_MAJOR_VERSION}U-jdk_x64_linux_${JAVA_VERSION%%-*}_${JAVA_VERSION##*-}.tar.gz --directory ${JAVA_HOME} --strip-components 1 --no-same-owner && \
    rm -f OpenJDK*.tar.gz && \
    chmod a+rx ${JAVA_HOME} && \
    find "${JAVA_HOME}/lib" -name '*.so' -exec dirname '{}' ';' | sort -u > /etc/ld.so.conf.d/docker-openjdk.conf && \
    ldconfig && \
    java -Xshare:dump && \
    java --version && \
    # install micromamba
    curl -fOL ${MICROMAMBA_URL}/conda-forge/micromamba/${MICROMAMBA_VERSION}/download/linux-64/micromamba-${MICROMAMBA_VERSION}-0.tar.bz2 && \
    tar -xvjf micromamba-${MICROMAMBA_VERSION}-0.tar.bz2 bin/micromamba --no-same-owner && \
    mv ./bin/micromamba /usr/local/bin/micromamba && \
    rm -rf micromamba-${MICROMAMBA_VERSION}-0.tar.bz2 && \
    micromamba shell hook -s bash > /etc/profile.d/micromamba.sh && \
    chmod a+rx /etc/profile.d/micromamba.sh && \
    mkdir -p ${MAMBA_ROOT_PREFIX} && \
    chmod a+rwx ${MAMBA_ROOT_PREFIX} && \
    mkdir -p /etc/conda && \
    # configure micromamba and pip
    micromamba config append --system default_channels ${ANACONDA_REPO_URL}/conda-forge && \
    micromamba config set --system channel_alias ${ANACONDA_REPO_URL} && \
    chmod -R a+rx /etc/conda && \
    sed -i -e "s~#PYPI_URL#~${PYPI_URL}~g" /root/.pydistutils.cfg && \
    cp /root/.pydistutils.cfg /opt/gms/.pydistutils.cfg && \
    chown gms:gms /opt/gms/.pydistutils.cfg && \
    echo ". /etc/profile.d/micromamba.sh" >> /etc/bashrc && \
    echo "micromamba activate base" >> /etc/bashrc && \
    # install builder python environment
    micromamba install -y --name base --file /root/environment.lock.yml && \
    # install node gyp python environment - this decouples the node gyp version from the gms python version
    micromamba create -y --name gyp -c conda-forge "python=${NODE_GYP_PYTHON_VERSION}" && \
    # change mamba permissions to enable gms group read-write. Note that in the builder we allow the gms group to install python packages
    chgrp -R gms ${MAMBA_ROOT_PREFIX} && \
    chmod -R g=u ${MAMBA_ROOT_PREFIX} && \
    find ${MAMBA_ROOT_PREFIX} -type d -exec chmod g+s {} \; && \
    # install node headers so we can build local libraries if necessary with node-gyp, tarball path is set in /usr/etc/npmrc file
    curl -fOL ${NODEJS_FILES_URL}/download/release/v${NODEJS_VERSION}/node-v${NODEJS_VERSION}-headers.tar.gz && \
    mkdir -p /usr/share/node && \
    mv node-v${NODEJS_VERSION}-headers.tar.gz /usr/share/node/node-v${NODEJS_VERSION}-headers.tar.gz && \
    # configure node
    npm config set registry ${NPM_REGISTRY} --global && \
    npm config set tarball /usr/share/node/node-v${NODEJS_VERSION}-headers.tar.gz --global && \
    chmod a+rx /usr/etc && \
    chgrp -R gms /usr/lib/node_modules && \
    chmod g+s /usr/lib/node_modules && \
    # install and configure yarn
    npm install yarn@${YARN_VERSION} --global && \
    yarn config set disable-self-update-check true --global && \
    yarn config set registry ${YARN_REGISTRY} --global && \
    yarn global add multiple-cucumber-html-reporter@1.10.1 && \
    chmod a+r /usr/local/share/.yarnrc && \
    ln -s /usr/local/share/.yarnrc /usr/etc/yarnrc && \
    # install gradle
    curl -fOL ${GRADLE_BASE_URL}/distributions/gradle-${GRADLE_VERSION}-bin.zip && \
    unzip -d /opt gradle-${GRADLE_VERSION}-bin.zip && \
    ln -s /opt/gradle-${GRADLE_VERSION}/bin/gradle /usr/bin/gradle && \
    rm -f gradle-${GRADLE_VERSION}-bin.zip && \
    # install sonarscanner
    curl -fOL ${SONAR_URL}/Distribution/sonar-scanner-cli/sonar-scanner-cli-${SONAR_SCANNER_VERSION}-linux.zip && \
    unzip -d /usr/local sonar-scanner-cli-${SONAR_SCANNER_VERSION}-linux.zip && \
    ln -s /usr/local/sonar-scanner-${SONAR_SCANNER_VERSION}-linux/bin/sonar-scanner /usr/local/bin/sonar-scanner && \
    rm -f sonar-scanner-cli-${SONAR_SCANNER_VERSION}-linux.zip && \
    # install docker-compose
    curl -fOL ${GITHUB_URL}/docker/compose/releases/download/${DOCKER_COMPOSE_VERSION}/docker-compose-Linux-x86_64 && \
    mv docker-compose-Linux-x86_64 /usr/local/bin/docker-compose && \
    chmod a+rx /usr/local/bin/docker-compose && \
    # install kubectl
    curl -fOL ${GOOGLEAPIS_URL}/kubernetes-release/release/v${KUBECTL_VERSION}/bin/linux/amd64/kubectl && \
    mv ./kubectl /usr/local/bin/kubectl && \
    chmod a+rx /usr/local/bin/kubectl && \
    # install helm
    curl -fOL ${HELM_URL}/helm-v${HELM_VERSION}-linux-amd64.tar.gz && \
    tar -xvf helm-v${HELM_VERSION}-linux-amd64.tar.gz --no-same-owner && \
    mv ./linux-amd64/helm /usr/local/bin/helm && \
    chmod a+rx /usr/local/bin/helm && \
    rm -rf helm-v${HELM_VERSION}-linux-amd64.tar.gz ./linux-amd64 && \
    # install minio client
    curl -fOL ${MINIO_URL}/client/mc/release/linux-amd64/mc && \
    mv ./mc /usr/local/bin/mc && \
    chmod a+rx /usr/local/bin/mc && \
    # setup emscripten - use system node and remove embedded node
    sed -i -e "s~NODE_JS = .*~NODE_JS = '/usr/bin/node'~" ${EM_CONFIG} && \
    rm -rf ${EMSDK}/node/* && \
    # cleanup
    chown -R gms:gms /opt/gms && \
    dnf clean all && \
    rm -rf /var/cache/dnf && \
    micromamba clean --all -y && \
    rm -rf /tmp/* /usr/src/*

# to use emscripten: source ${EMSDK}/emsdk_env.sh
# to use gcc-toolset-10: source /opt/rh/gcc-toolset-10/enable

# Note we don't switch users here due to oddities when running within the gitlab docker executor, so for now run as root.
# Downstream images that run outside of ci/cd should make every effort to switch to the gms user.
# USER 1001

CMD ["/bin/bash"]
