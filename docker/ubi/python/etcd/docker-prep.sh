#!/bin/bash

set -eux

# ----------------------------------------------------------------------
# Docker build will use the directory containing this script as the
# build context for the etcd container.
#
# This script must be run prior to docker build to collect the various
# files that needed by the etcd container to this context.
# ----------------------------------------------------------------------

# Get the path to the directory containing this bash script.
# Then define other paths relative to this script directory.
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" > /dev/null && pwd )"
DOCKER_PYTHON_DIR="${SCRIPT_DIR}/src/_python"
DOCKER_CONFIG_DIR="${SCRIPT_DIR}/src/_config"
GMS_HOME="$( cd "${SCRIPT_DIR}/../../../.." > /dev/null && pwd )"

rm -rf ${DOCKER_PYTHON_DIR}
mkdir -p ${DOCKER_PYTHON_DIR}

rm -rf ${DOCKER_CONFIG_DIR}
mkdir -p ${DOCKER_CONFIG_DIR}

# Copy over the config/system directory with the configuration files we need to load into etcd
cp -rf ${GMS_HOME}/config/system ${DOCKER_CONFIG_DIR}

# Copy over Python code needed to load etcd 
cp -rf ${GMS_HOME}/python/gms-sysconfig ${DOCKER_PYTHON_DIR}

# Everything needed to do the `docker build` should now be in the
# `_config` and `_python` subdirectories.
