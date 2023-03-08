#!/bin/bash

set -eux

# --------------------------------------------------------
# Convert the gms-common CSS data to COI JSON in place
# 
# The source CSS files live under this directory in:
#   station-reference/stationdata
#
# The destination COI JSON files are created in:
#   station-reference/data
# --------------------------------------------------------

SCRIPT_PATH=$( cd $( dirname "${BASH_SOURCE[0]}" ) > /dev/null && pwd)

# build gradle proxy args because it doesn't use system environment variables - used by opensource build
gradle_proxy_args=()
if [ -n "${CI_USE_PROXY:-}" ]; then
    pattern='^(([[:alnum:]]+)://)?(([[:alnum:]]+)@)?([^:^@]+)(:([[:digit:]]+))?$'
    if [[ "${http_proxy}" =~ $pattern ]]; then
        gradle_proxy_args+=("-Dhttp.proxyHost=${BASH_REMATCH[5]}")
        gradle_proxy_args+=("-Dhttp.proxyPort=${BASH_REMATCH[7]}")
    fi

    if [[ "${https_proxy}" =~ $pattern ]]; then
        gradle_proxy_args+=("-Dhttps.proxyHost=${BASH_REMATCH[5]}")
        gradle_proxy_args+=("-Dhttps.proxyPort=${BASH_REMATCH[7]}")
    fi

    gradle_proxy_args+=("-Dhttp.nonProxyHosts=${no_proxy//,/|}")
    gradle_proxy_args+=("-Dhttps.nonProxyHosts=${no_proxy//,/|}")
fi

# Assume this is being called from the parent directory
STATION_DATA_PATH="${SCRIPT_PATH}/station-reference"
CSS_FULL_PATH="$STATION_DATA_PATH/data"
JSON_FULL_PATH="$STATION_DATA_PATH/stationdata"
NETWORK_FILE_NAME="network.dat"

# Set full paths to the source CSS data and the destination JSON data

# Set environment variable needed by the script that converts CSS to JSON
export GMS_HOME=${SCRIPT_PATH}/../java

# Ensure that the Java `css-stationref-converter` program is built.
time gradle ${gradle_proxy_args[@]} -g ${GMS_HOME} -p ${GMS_HOME}/tools  --no-daemon :css-stationref-converter:build -x test

# If the JSON_FULL_PATH subdirectory exists remove it.
if [ -d "${JSON_FULL_PATH}" ]; then
    rm -rf ${JSON_FULL_PATH};  # The css-stationref-converter requires this be missing when running outside a container
fi

# If running in CI then make sure JSON_FULL_PATH exists, since CI runs in a container
if [[ -z "${CI-}" ]]; then
    echo "Running outside of CI";
else
    echo "Running in CI, creating ${JSON_FULL_PATH}";
    mkdir -p ${JSON_FULL_PATH};
fi

# Run the java code to generate the COI JSON data from the CSS source
cd ${STATION_DATA_PATH}

echo "*** Running station reference converter ***"
time gradle ${gradle_proxy_args[@]} -g ${GMS_HOME} -p ${GMS_HOME}/tools --no-daemon :css-stationref-converter:run -Dexec.args="-data ${CSS_FULL_PATH} -outputDir ${JSON_FULL_PATH} -network ${NETWORK_FILE_NAME}"
echo "*** Done running station reference converter ***"

# The previous command will not exit with an error code when errors happen, so check for output
if [ ! -d "${JSON_FULL_PATH}/responses" ]; then
  echo "${JSON_FULL_PATH}/responses missing, check for errors in the station reference converter."
  exit 1
fi
if [ ! -f "${JSON_FULL_PATH}/processing-response.json" ]; then
  echo "${JSON_FULL_PATH}/processing-response.json missing, check for errors in the station reference converter."
  exit 1
fi
