NAME = instance-prometheus
SOURCE_IMAGE = $${IRONBANK_REGISTRY}/ironbank/opensource/prometheus/prometheus
SOURCE_TAG = $${PROMETHEUS_TAG}

include $(shell CI-MAKE-CONFIG-PATH)/Makefile.docker

build:
	$(MAKE) tag
