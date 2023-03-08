NAME = logging-elasticsearch
# note: kibana and elasticsearch versions must match
SOURCE_IMAGE = $${IRONBANK_REGISTRY}/ironbank/elastic/elasticsearch/elasticsearch
SOURCE_TAG = $${ELASTICSEARCH_TAG}

include $(shell CI-MAKE-CONFIG-PATH)/Makefile.docker

build:
	$(MAKE) tag
