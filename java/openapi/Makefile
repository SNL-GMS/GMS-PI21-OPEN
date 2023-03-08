NAME = swagger-gms

BUILD_ARGS = --build-arg 'FROM_IMAGE=$${CI_THIRD_PARTY_DOCKER_REGISTRY}/swaggerapi/swagger-ui:v3.52.0'

CLEAN = rm -rf src/spec

include $(shell CI-MAKE-CONFIG-PATH)/Makefile.docker

