NAME = ubi
SOURCE_IMAGE = $${IRONBANK_REGISTRY}/ironbank/redhat/ubi/ubi8
SOURCE_TAG = $${UBI_TAG}

BUILD_ARGS = --build-arg 'SOURCE_IMAGE=$(SOURCE_IMAGE)' \
             --build-arg 'SOURCE_TAG=$(SOURCE_TAG)$${UPSTREAM_TAG_SUFFIX}' \
             --build-arg 'UBI_RPM_URL=$${UBI_RPM_URL}' \
             --build-arg 'ALMALINUX_RPM_URL=$${ALMALINUX_RPM_URL}' \
             --build-arg 'EPEL_YUM_URL=$${EPEL_YUM_URL}'

SUBDIRS := java \
           jmx-exporter \
           ldap_proxy \
	   nginx \
	   prometheus-sidecar \
	   python \
	   typescript

include $(shell CI-MAKE-CONFIG-PATH)/Makefile.docker
