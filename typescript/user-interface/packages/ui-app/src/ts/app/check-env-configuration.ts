import {
  CESIUM_OFFLINE,
  GATEWAY_HTTP_PROXY_URI,
  GMS_UI_MODE,
  IS_MODE_IAN,
  IS_MODE_LEGACY,
  IS_MODE_SOH,
  IS_NODE_ENV_DEVELOPMENT,
  IS_NODE_ENV_PRODUCTION,
  NODE_ENV,
  SUBSCRIPTIONS_PROXY_URI,
  UI_URL
} from '@gms/common-util';
import { UILogger } from '@gms/ui-util';

import {
  GMS_DISABLE_KEYCLOAK_AUTH,
  GMS_KEYCLOAK_CLIENT_ID,
  GMS_KEYCLOAK_REALM,
  GMS_KEYCLOAK_URL
} from '~env';

const logger = UILogger.create('GMS_LOG_CONFIG', process.env.GMS_LOG_CONFIG);

/**
 * Checks the ENV Configuration.
 * Logs the current environment configuration.
 */
export const checkEnvConfiguration = (): void => {
  logger.debug(
    `Environment (process.env): ` +
      `\n   process.env.NODE_ENV=${process.env.NODE_ENV}` +
      `\n   process.env.GMS_UI_MODE=${process.env.GMS_UI_MODE}` +
      `\n   process.env.GATEWAY_HTTP_PROXY_URI=${process.env.GATEWAY_HTTP_PROXY_URI}` +
      `\n   process.env.WAVEFORMS_PROXY_URI=${process.env.WAVEFORMS_PROXY_URI}` +
      `\n   process.env.SUBSCRIPTIONS_PROXY_URI=${process.env.SUBSCRIPTIONS_PROXY_URI}` +
      `\n   process.env.CESIUM_OFFLINE=${process.env.CESIUM_OFFLINE}`
  );

  logger.debug(
    `App Environment: ` +
      `\n   NODE_ENV=${NODE_ENV}` +
      `\n   GMS_UI_MODE=${GMS_UI_MODE}` +
      `\n   IS_MODE_LEGACY=${IS_MODE_LEGACY}` +
      `\n   IS_MODE_IAN=${IS_MODE_IAN}` +
      `\n   IS_MODE_SOH=${IS_MODE_SOH}` +
      `\n   IS_NODE_ENV_DEVELOPMENT=${IS_NODE_ENV_DEVELOPMENT}` +
      `\n   IS_NODE_ENV_PRODUCTION=${IS_NODE_ENV_PRODUCTION}` +
      `\n   GATEWAY_HTTP_PROXY_URI=${GATEWAY_HTTP_PROXY_URI}` +
      `\n   SUBSCRIPTIONS_PROXY_URI=${SUBSCRIPTIONS_PROXY_URI}` +
      `\n   UI_URL=${UI_URL}` +
      `\n   CESIUM_OFFLINE=${CESIUM_OFFLINE}` +
      `\n   GMS_KEYCLOAK_URL=${GMS_KEYCLOAK_URL}` +
      `\n   GMS_KEYCLOAK_CLIENT_ID=${GMS_KEYCLOAK_CLIENT_ID}` +
      `\n   GMS_KEYCLOAK_REALM=${GMS_KEYCLOAK_REALM}` +
      `\n   GMS_DISABLE_KEYCLOAK_AUTH=${GMS_DISABLE_KEYCLOAK_AUTH}`
  );
};
