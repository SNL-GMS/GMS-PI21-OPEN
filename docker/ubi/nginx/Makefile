NAME = nginx

BUILD_ARGS = --build-arg NGINX_YUM_URL='$${NGINX_YUM_URL}' \
             --build-arg NGINX_VERSION='$${NGINX_VERSION}'

include $(shell CI-MAKE-CONFIG-PATH)/Makefile.docker
