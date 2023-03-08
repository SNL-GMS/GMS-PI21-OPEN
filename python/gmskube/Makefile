NAME       = gmskube
BUILD_ARGS = --build-arg 'GOOGLEAPIS_URL=$${GOOGLEAPIS_URL}' \
             --build-arg 'HELM_URL=$${HELM_URL}' \
             --build-arg 'HELM_VERSION=$${HELM_VERSION}' \
             --build-arg 'KUBECTL_VERSION=$${KUBECTL_VERSION}'

CLEAN = rm -rf _deploy

include $(shell CI-MAKE-CONFIG-PATH)/Makefile.docker
