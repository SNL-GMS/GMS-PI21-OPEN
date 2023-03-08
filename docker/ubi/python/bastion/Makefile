NAME = bastion

BUILD_ARGS = --build-arg 'ETCD_VERSION=$${ETCD_VERSION}' \
             --build-arg 'GITHUB_URL=$${GITHUB_URL}' \
             --build-arg 'KCAT_VERSION=$${KCAT_VERSION}' \
             --build-arg 'LIBRDKAFKA_VERSION=$${LIBRDKAFKA_VERSION}' \
             --build-arg 'MINIO_URL=$${MINIO_URL}' \
             --build-arg 'ORACLE_URL=$${ORACLE_URL}'

include $(shell CI-MAKE-CONFIG-PATH)/Makefile.docker
