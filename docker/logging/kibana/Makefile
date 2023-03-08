NAME = logging-kibana
# note: kibana and elasticsearch versions must match
SOURCE_IMAGE = $${IRONBANK_REGISTRY}/ironbank/elastic/kibana/kibana
SOURCE_TAG = $${KIBANA_TAG}

include $(shell CI-MAKE-CONFIG-PATH)/Makefile.docker

build:
	$(MAKE) tag
