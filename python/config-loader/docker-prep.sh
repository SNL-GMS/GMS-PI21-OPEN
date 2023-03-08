#!/bin/bash

set -ex

SCRIPT_PATH=$( cd $( dirname "${BASH_SOURCE[0]}" ) > /dev/null && pwd)
CONFIG_PATH="${SCRIPT_PATH}/../../config"
JSON_FULL_PATH="${CONFIG_PATH}/station-reference/stationdata"
PYTHON_PATH="${SCRIPT_PATH}/.."

# Run the css-to-coi conversion if the COI JSON directory is not available
if [ ! -d "${JSON_FULL_PATH}" ]; then
    ${CONFIG_PATH}/css-to-coi.sh
fi    

# Copy the contents of the config directory from gms-common to the current directory
if [ ! -d "_config" ]; then mkdir "_config"; fi
cp -r ${CONFIG_PATH}/* _config

# Copy dependent python libraries to a local python directory
if [ ! -d "_python" ]; then mkdir "_python"; fi
cp -r ${PYTHON_PATH}/gms-data-loader _python
