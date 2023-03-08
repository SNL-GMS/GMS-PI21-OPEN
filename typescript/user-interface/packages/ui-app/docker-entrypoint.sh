#! /bin/bash
set -eu
export GMS_UI_MODE=${GMS_UI_MODE:-soh} #Default the variable to soh if it is not set

if [ "$GMS_UI_MODE" = "ian" ]; then
  cat /etc/nginx/nginx-ian.nginx | envsubst '$URL_PATH $GMS_UI_MODE $NODE_ENV' > /etc/nginx/nginx.conf
else
  cat /etc/nginx/nginx-soh-all.nginx | envsubst '$URL_PATH $GMS_UI_MODE $NODE_ENV' > /etc/nginx/nginx.conf
fi

export GMS_KEYCLOAK_REALM=${GMS_KEYCLOAK_REALM}
export GMS_KEYCLOAK_URL=${GMS_KEYCLOAK_URL}
export GMS_KEYCLOAK_CLIENT_ID=${GMS_KEYCLOAK_CLIENT_ID}
export GMS_DISABLE_KEYCLOAK_AUTH=${GMS_DISABLE_KEYCLOAK_AUTH}
cat /opt/interactive-analysis-ui/${GMS_UI_MODE}/${NODE_ENV}/env-inject-template.js | envsubst '$GMS_KEYCLOAK_REALM $GMS_KEYCLOAK_URL $GMS_KEYCLOAK_CLIENT_ID $GMS_DISABLE_KEYCLOAK_AUTH'  > /opt/interactive-analysis-ui/${GMS_UI_MODE}/${NODE_ENV}/env-inject.js

exec nginx -g 'daemon off;'
