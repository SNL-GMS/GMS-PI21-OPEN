NAME = bitnami-kafka
SOURCE_IMAGE = $${IRONBANK_REGISTRY}/ironbank/bitnami/kafka
SOURCE_TAG = $${BITNAMI_KAFKA_TAG}

include $(shell CI-MAKE-CONFIG-PATH)/Makefile.docker

build:
	$(MAKE) tag
