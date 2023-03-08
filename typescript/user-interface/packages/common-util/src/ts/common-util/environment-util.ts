import type { CommonTypes } from '@gms/common-model';
import { UserProfileTypes } from '@gms/common-model';

import { isWindowDefined } from './window-util';

const windowIsDefined = isWindowDefined();

// !The environment utils currently depends on the common-model for UserMode
// TODO determine if the util library should only use primitive types
// TODO determine if the environment utils should be moved to a different package

/**
 * The NODE_ENV environment variable.
 */
export const { NODE_ENV } = process.env;

/**
 * True if NODE_ENV is set to development.
 */
export const IS_NODE_ENV_DEVELOPMENT = NODE_ENV === 'development';

/**
 * True if NODE_ENV is set to production.
 */
export const IS_NODE_ENV_PRODUCTION = NODE_ENV === 'production';

/**
 * True if NODE_ENV is set to test.
 */
export const IS_NODE_ENV_TEST = NODE_ENV === 'test';

/**
 * The UI Mode ['ian', 'soh' or 'legacy']
 */
export const GMS_UI_MODE = process.env.GMS_UI_MODE || undefined;

/**
 * True if configured for ALL; false otherwise.
 */
export const IS_MODE_LEGACY = GMS_UI_MODE === UserProfileTypes.UserMode.LEGACY;

/**
 * True if configured for IAN; false otherwise.
 */
export const IS_MODE_IAN = GMS_UI_MODE === UserProfileTypes.UserMode.IAN;

/**
 * True if configured for SOH; false otherwise.
 */
export const IS_MODE_SOH = GMS_UI_MODE === UserProfileTypes.UserMode.SOH;

/**
 * The current user mode, which defines which layouts are supported
 */
export const CURRENT_USER_MODE =
  (IS_MODE_LEGACY && UserProfileTypes.UserMode.LEGACY) ||
  (IS_MODE_IAN && UserProfileTypes.UserMode.IAN) ||
  (IS_MODE_SOH && UserProfileTypes.UserMode.SOH) ||
  '';

/** Useful because unit tests can easily override functions. */
export const isIanMode = (): boolean => IS_MODE_IAN;

/** Useful because unit tests can easily override functions. */
export const isSohMode = (): boolean => IS_MODE_SOH;

/**
 * Returns the supported modes based on the current user mode.
 */
export const SUPPORTED_MODES: UserProfileTypes.UserMode[] =
  (CURRENT_USER_MODE === UserProfileTypes.UserMode.SOH && [UserProfileTypes.UserMode.SOH]) ||
  (CURRENT_USER_MODE === UserProfileTypes.UserMode.IAN && [UserProfileTypes.UserMode.IAN]) ||
  Object.keys(UserProfileTypes.UserMode).map(mode => UserProfileTypes.UserMode[mode]);

/**
 * The UI_URL endpoint. This is the URL from which the UI content is served.
 */
export const UI_URL = windowIsDefined
  ? `${window.location.protocol}//${window.location.host}`
  : 'http://localhost:8080';

/**
 * The UI_BASE_PATH endpoint. This is the base path for the URL in a deployment
 */
export const UI_BASE_PATH =
  windowIsDefined && !window.location.host.includes('localhost') ? '/interactive-analysis-ui' : '';

/**
 * The SUBSCRIPTION protocol.
 */
export const UI_SUBSCRIPTION_PROTOCOL =
  windowIsDefined && window.location.protocol === 'https:' ? 'wss' : 'ws';

/**
 * The SUBSCRIPTION URL endpoint.
 */
export const UI_SUBSCRIPTION_URL = windowIsDefined
  ? `${UI_SUBSCRIPTION_PROTOCOL}://${window.location.host}`
  : 'ws://localhost';

/**
 * The GATEWAY_HTTP_PROXY_URI environment variable (or the default value if not set).
 */
export const GATEWAY_HTTP_PROXY_URI = process.env.GATEWAY_HTTP_PROXY_URI || UI_URL;

/**
 * The SUBSCRIPTIONS_PROXY_URI environment variable (or the default value if not set).
 */
export const SUBSCRIPTIONS_PROXY_URI = process.env.SUBSCRIPTIONS_PROXY_URI || UI_SUBSCRIPTION_URL;

/**
 * The API_GATEWAY_URI environment variable (or the default value if not set).
 */
export const API_GATEWAY_URI = GATEWAY_HTTP_PROXY_URI;

/**
 * The API_GATEWAY_URI environment variable for checking a user's login status.
 */
export const API_LOGIN_CHECK_URI = `${GATEWAY_HTTP_PROXY_URI}/interactive-analysis-api-gateway/auth/checkLogIn`;

/**
 * The API_GATEWAY_URI environment variable for accessing the login endpoint.
 */
export const API_LOGIN_URI = `${GATEWAY_HTTP_PROXY_URI}/interactive-analysis-api-gateway/auth/logInUser`;

/**
 * The API_GATEWAY_URI environment variable for accessing the logout endpoint.
 */
export const API_LOGOUT_URI = `${GATEWAY_HTTP_PROXY_URI}/interactive-analysis-api-gateway/auth/logOutUser`;

/**
 * The CESIUM_OFFLINE environment variable.
 */
export const CESIUM_OFFLINE = process.env.CESIUM_OFFLINE
  ? !(
      process.env.CESIUM_OFFLINE === 'null' ||
      process.env.CESIUM_OFFLINE === 'undefined' ||
      process.env.CESIUM_OFFLINE === 'false'
    )
  : false;

/**
 * The `AVAILABLE_SOUND_FILES` environment variable.
 * The available configured sound files for the system.
 */
export const AVAILABLE_SOUND_FILES: string[] =
  process.env.AVAILABLE_SOUND_FILES &&
  process.env.AVAILABLE_SOUND_FILES !== 'undefined' &&
  process.env.AVAILABLE_SOUND_FILES !== 'null'
    ? process.env.AVAILABLE_SOUND_FILES.split(';')
    : [];

export const VERSION_INFO: CommonTypes.VersionInfo = {
  versionNumber: process.env.VERSION_NUMBER ?? 'Unknown version',
  commitSHA: process.env.COMMIT_SHA ?? 'Unknown commit'
};

/**
 * Turns on timing points for UI. Set to 'verbose' to see log timing points.
 * Set to any other string to see warnings level timing only.
 */
export const GMS_PERFORMANCE_MONITORING_ENABLED =
  process.env.GMS_PERFORMANCE_MONITORING_ENABLED?.toLocaleLowerCase() ?? false;
