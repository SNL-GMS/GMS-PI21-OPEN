NAME = etcd

BUILD_ARGS = --build-arg 'ETCD_VERSION=$${ETCD_VERSION}' \
             --build-arg 'GITHUB_URL=$${GITHUB_URL}'

CLEAN = rm -rf src/_python src/_config

include $(shell CI-MAKE-CONFIG-PATH)/Makefile.docker
