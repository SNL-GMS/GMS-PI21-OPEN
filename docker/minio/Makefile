NAME = minio
SOURCE_IMAGE = $${IRONBANK_REGISTRY}/ironbank/opensource/minio/minio
SOURCE_TAG = $${MINIO_TAG}

include $(shell CI-MAKE-CONFIG-PATH)/Makefile.docker

build:
	$(MAKE) tag
