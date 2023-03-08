NAME = bitnami-jmx-exporter

BUILD_ARGS = --build-arg 'JMX_EXPORTER_IMAGE=$${CI_THIRD_PARTY_DOCKER_REGISTRY}/bitnami/jmx-exporter' \
             --build-arg 'JMX_EXPORTER_TAG=$${BITNAMI_JMX_EXPORTER_TAG}'

include $(shell CI-MAKE-CONFIG-PATH)/Makefile.docker
