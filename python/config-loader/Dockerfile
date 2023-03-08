ARG DOCKER_REGISTRY
ARG DOCKER_IMAGE_TAG
ARG PROJECT_NAME

FROM ${DOCKER_REGISTRY}/${PROJECT_NAME}/python:${DOCKER_IMAGE_TAG}

USER 0

EXPOSE 8080

WORKDIR /opt/gms

COPY ./setup.py ./config-loader/setup.py
COPY ./app ./config-loader/app
COPY ./gunicorn-config.py ./config-loader/gunicorn-config.py
COPY ./_python/gms-data-loader ./install/gms-data-loader
COPY ./environment.lock.yml ./config-loader/environment.lock.yml

RUN set -ex && \
    mkdir ./override && \
    mkdir ./config-loader/app/state && \
    mkdir ./base && \
    chown -R gms:gms /opt/gms && \
    umask 027 && \
    micromamba install -y --name base --file /opt/gms/config-loader/environment.lock.yml && \
    cd /opt/gms/install/gms-data-loader && \
    pip install . && \
    cd /opt/gms/config-loader && \
    pip install . && \
    micromamba clean --all -y

# copy the config data last to maximize docker cache hits
COPY --chown=gms:gms ./_config ./base

# gms uid
USER 1001

WORKDIR /opt/gms/config-loader

HEALTHCHECK --interval=30s CMD curl -f localhost:8080/alive || exit 1

ENTRYPOINT ["gunicorn", "--config=gunicorn-config.py", "app"]
