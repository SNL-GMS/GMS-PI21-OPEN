NAME = java

BUILD_ARGS = --build-arg 'GITHUB_URL=$${GITHUB_URL}' \
             --build-arg 'JAVA_VERSION=$${JAVA_VERSION}' \
             --build-arg 'JAVA_MAJOR_VERSION=$${JAVA_VERSION%%.*}'

include $(shell CI-MAKE-CONFIG-PATH)/Makefile.docker
