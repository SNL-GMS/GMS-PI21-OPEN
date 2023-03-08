ARG DOCKER_REGISTRY
ARG DOCKER_IMAGE_TAG
ARG PROJECT_NAME

FROM ${DOCKER_REGISTRY}/${PROJECT_NAME}/python:${DOCKER_IMAGE_TAG}

EXPOSE 8080
WORKDIR /opt/gms

USER 0

COPY src/login.patch login.patch
COPY src/environment.lock.yml environment.lock.yml

# install the environment and patch the login page with gms-specific information
RUN set -ex && \
    dnf install -y gcc-c++ \
                   glibc-locale-source \
                   glibc-langpack-en \
                   patch && \
    umask 027 && \
    micromamba install -y --name base --file environment.lock.yml && \
    patch -b -f ${MAMBA_ROOT_PREFIX}/lib/python3.10/site-packages/notebook/templates/login.html /opt/gms/login.patch && \
    # clean up
    dnf clean all && \
    rm -rf /var/cache/dnf && \
    micromamba clean --all -y && \
    rm -f /opt/gms/login.patch

COPY --chown=1001:1001 src/jupyter_notebook_config.py /opt/gms/.jupyter/jupyter_notebook_config.py
COPY --chown=1001:1001 notebooks /opt/gms/notebooks

# gms uid
USER 1001

CMD ["jupyter", "notebook"]
