#!/bin/sh

set -ue

# Directory where we will look for spec files.  This directory must also be
# accessible via HTTP, which is why the default is down here under
# /usr/share/nginx/html.  See $SPEC_URL below.
SPEC_DIR=/usr/share/nginx/html/spec

# The BASE_URL environment variable is used to optionally tell the swagger-ui
# container to serve up the Swagger UI from a non-/ route.
if [ -z "${BASE_URL}" ]; then
    BASE_URL=""
fi

# URL where files in $SPEC_DIR will be served up by the embedded nginx.
SPEC_URL=${BASE_URL}/spec

# Start with an empty $URLS.
URLS=""

# For each spec file found in $SPEC_DIR...
for spec_path in $(ls ${SPEC_DIR}/*.json ${SPEC_DIR}/*.yaml ${SPEC_DIR}/*.yml); do
    # Compute a few derivatives of $spec_path for use when building up $URLS.
    spec_file=$(basename ${spec_path})
    spec_url="${SPEC_URL}/${spec_file}"
    spec_name=$(echo ${spec_file} | sed 's/\.[a-z]*$//')

    echo CONFIGURING SPEC FILE: PATH=${spec_path} FILE=${spec_file} URL=${spec_url} NAME=${spec_name}

    # Build up the body of the $URLS environment variable to be the JSON array
    # described on this page:
    # https://github.com/swagger-api/swagger-ui/blob/master/docs/usage/installation.md
    URLS="${URLS} {url: \"${spec_url}\", name: \"${spec_name}\"},"
done

# Trim off the trailing `,` and add array brackets `[]` around the $URLS value
# to make valid JSON that swagger-ui expects.
URLS=$(echo ${URLS} | sed 's/,$//')
URLS="[${URLS}]"
export URLS

echo FINAL VALUE IN URLS: ${URLS}

# Exec over to whatever the "real" CMD is supposed to be.
exec "$@"
