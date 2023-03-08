NAME = builder
SOURCE_IMAGE = $${IRONBANK_REGISTRY}/ironbank/redhat/ubi/ubi8
SOURCE_TAG = $${UBI_TAG}
BUILD_ARGS = --build-arg 'SOURCE_IMAGE=$(SOURCE_IMAGE)' \
             --build-arg 'SOURCE_TAG=$(SOURCE_TAG)$${UPSTREAM_TAG_SUFFIX}' \
             --build-arg 'ALMALINUX_RPM_URL=$${ALMALINUX_RPM_URL}' \
             --build-arg 'ANACONDA_REPO_URL=$${ANACONDA_REPO_URL}' \
             --build-arg 'CYPRESS_DOWNLOAD_MIRROR=$${CYPRESS_DOWNLOAD_MIRROR}' \
             --build-arg 'DOCKER_YUM_URL=$${DOCKER_YUM_URL}' \
             --build-arg 'DOCKER_COMPOSE_VERSION=$${DOCKER_COMPOSE_VERSION}' \
             --build-arg 'ELECTRON_BUILDER_BINARIES_MIRROR=$${ELECTRON_BUILDER_BINARIES_MIRROR}' \
             --build-arg 'ELECTRON_MIRROR=$${ELECTRON_MIRROR}' \
             --build-arg 'EMSCRIPTEN_IMAGE=$${CI_THIRD_PARTY_DOCKER_REGISTRY}/emscripten/emsdk' \
             --build-arg 'EMSCRIPTEN_TAG=$${EMSCRIPTEN_TAG}' \
             --build-arg 'EPEL_YUM_URL=$${EPEL_YUM_URL}' \
             --build-arg 'GCC_TOOLSET_MAJOR_VERSION=$${GCC_TOOLSET_MAJOR_VERSION}' \
             --build-arg 'GITHUB_URL=$${GITHUB_URL}' \
             --build-arg 'GOOGLEAPIS_URL=$${GOOGLEAPIS_URL}' \
             --build-arg 'GRADLE_BASE_URL=$${GRADLE_BASE_URL}' \
             --build-arg 'GRADLE_VERSION=$${GRADLE_VERSION}' \
             --build-arg 'HELM_URL=$${HELM_URL}' \
             --build-arg 'HELM_VERSION=$${HELM_VERSION}' \
             --build-arg 'JAVA_VERSION=$${JAVA_VERSION}' \
             --build-arg 'JAVA_MAJOR_VERSION=$${JAVA_VERSION%%.*}' \
             --build-arg 'KUBECTL_VERSION=$${KUBECTL_VERSION}' \
             --build-arg 'MICROMAMBA_URL=$${MICROMAMBA_URL}' \
             --build-arg 'MICROMAMBA_VERSION=$${MICROMAMBA_VERSION}' \
             --build-arg 'MINIO_URL=$${MINIO_URL}' \
             --build-arg 'NODE_YUM_URL=$${NODE_YUM_URL}' \
             --build-arg 'NODEJS_FILES_URL=$${NODEJS_FILES_URL}' \
             --build-arg 'NODEJS_VERSION=$${NODEJS_VERSION}' \
             --build-arg 'NODE_GYP_PYTHON_VERSION=$${NODE_GYP_PYTHON_VERSION}' \
             --build-arg 'NPM_REGISTRY=$${NPM_REGISTRY}' \
             --build-arg 'PYPI_URL=$${PYPI_URL}' \
             --build-arg 'SONAR_URL=$${SONAR_URL}' \
             --build-arg 'SONAR_SCANNER_VERSION=$${SONAR_SCANNER_VERSION}' \
             --build-arg 'UBI_RPM_URL=$${UBI_RPM_URL}' \
             --build-arg 'YARN_REGISTRY=$${YARN_REGISTRY}' \
             --build-arg 'YARN_VERSION=$${YARN_VERSION}'

CLEAN = rm -rf src/_python src/_ubi src/_typescript

include $(shell CI-MAKE-CONFIG-PATH)/Makefile.docker
