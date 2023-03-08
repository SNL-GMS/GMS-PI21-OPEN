NAME = python

BUILD_ARGS = --build-arg 'MICROMAMBA_URL=$${MICROMAMBA_URL}' \
             --build-arg 'MICROMAMBA_VERSION=$${MICROMAMBA_VERSION}' \
             --build-arg 'PYPI_URL=$${PYPI_URL}' \
             --build-arg 'ANACONDA_REPO_URL=$${ANACONDA_REPO_URL}'

SUBDIRS := bastion \
           etcd

include $(shell CI-MAKE-CONFIG-PATH)/Makefile.docker
