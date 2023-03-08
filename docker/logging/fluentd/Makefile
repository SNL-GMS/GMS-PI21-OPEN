NAME = logging-fluentd
SOURCE_IMAGE = $${IRONBANK_REGISTRY}/ironbank/opensource/fluentd/fluentd-kubernetes-daemonset
SOURCE_TAG = $${FLUENTD_TAG}

include $(shell CI-MAKE-CONFIG-PATH)/Makefile.docker

build:
	$(MAKE) tag
