NAME = postgres
SOURCE_IMAGE = $${IRONBANK_REGISTRY}/ironbank/opensource/postgres/postgresql12
SOURCE_TAG = $${POSTGRES_TAG}

BUILD_ARGS = --build-arg 'SOURCE_IMAGE=$(SOURCE_IMAGE)' \
             --build-arg 'SOURCE_TAG=$(SOURCE_TAG)$${UPSTREAM_TAG_SUFFIX}'

SUBDIRS := frameworks-osd-ttl-worker

include $(shell CI-MAKE-CONFIG-PATH)/Makefile.docker
