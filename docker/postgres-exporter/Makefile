NAME = postgres-exporter
SOURCE_IMAGE = $${IRONBANK_REGISTRY}/ironbank/opensource/prometheus/postgres-exporter
SOURCE_TAG = $${POSTGRES_EXPORTER_TAG}

include $(shell CI-MAKE-CONFIG-PATH)/Makefile.docker

build:
	$(MAKE) tag
