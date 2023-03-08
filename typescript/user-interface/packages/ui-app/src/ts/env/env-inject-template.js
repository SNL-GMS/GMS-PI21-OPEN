/* eslint-disable no-template-curly-in-string */
window.ENV = Object.freeze({
  // inject ENVs onto the window object.
  // the string '${something}' will be replaced via envsubst just before runtime
  GMS_KEYCLOAK_REALM: '${GMS_KEYCLOAK_REALM}',
  GMS_KEYCLOAK_URL: '${GMS_KEYCLOAK_URL}',
  GMS_KEYCLOAK_CLIENT_ID: '${GMS_KEYCLOAK_CLIENT_ID}',
  GMS_DISABLE_KEYCLOAK_AUTH: '${GMS_DISABLE_KEYCLOAK_AUTH}'
});
