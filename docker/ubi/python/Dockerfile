ARG DOCKER_REGISTRY
ARG DOCKER_IMAGE_TAG
ARG PROJECT_NAME

FROM ${DOCKER_REGISTRY}/${PROJECT_NAME}/ubi:${DOCKER_IMAGE_TAG}

ARG MICROMAMBA_URL
ARG ANACONDA_REPO_URL
ARG PYPI_URL
ARG MICROMAMBA_VERSION

# Set pip index
ENV PIP_INDEX_URL=${PYPI_URL}
# Disable pip cache
ENV PIP_NO_CACHE_DIR=1
# Disable pip bytecode compile
ENV PIP_NO_COMPILE=1
# Run python with unbuffered stdout
ENV PYTHONUNBUFFERED=1
# Do not write python bytecode
ENV PYTHONDONOTWRITEBYTECODE=1
# Set the path to the CA bundle for python requests
ENV REQUESTS_CA_BUNDLE=/etc/pki/tls/certs/ca-bundle.crt

ENV MAMBA_ROOT_PREFIX=/opt/conda
ENV PATH=${MAMBA_ROOT_PREFIX}/bin:${PATH}

USER 0

COPY src/pydistutils.cfg /root/.pydistutils.cfg
COPY src/environment.lock.yml /root/environment.lock.yml

RUN set -ex && \
    dnf -y install bzip2 && \
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
    # install the base environment
    # note the default ironbank umask is 077 which causes python package to install with restricted permissions, change to 027 before install
    umask 027 && \
    micromamba install -y --name base --file /root/environment.lock.yml && \
    # change permissions to enable gms group read. Note that group write is removed, all python package installs should done as USER 0 in the production containers
    chgrp -R gms ${MAMBA_ROOT_PREFIX} && \
    chmod -R g=u ${MAMBA_ROOT_PREFIX} && \
    chmod -R g-w ${MAMBA_ROOT_PREFIX} && \
    find ${MAMBA_ROOT_PREFIX} -type d -exec chmod g+s {} \; && \
    # clean up
    micromamba clean --all -y && \
    dnf clean all && \
    rm -rf /var/cache/dnf


# gms uid
USER 1001

CMD ["/bin/bash"]