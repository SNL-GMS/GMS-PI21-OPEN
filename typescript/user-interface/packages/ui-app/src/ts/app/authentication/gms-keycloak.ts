import { UI_BASE_PATH, UI_URL } from '@gms/common-util';
import type { AppDispatch, AppState, AuthenticationStatus } from '@gms/ui-state';
import { AppOperations } from '@gms/ui-state';
import { UILogger } from '@gms/ui-util';
import type { KeycloakConfig } from 'keycloak-js';
import Keycloak from 'keycloak-js';

import { GMS_KEYCLOAK_CLIENT_ID, GMS_KEYCLOAK_REALM, GMS_KEYCLOAK_URL } from '~env';

const logger = UILogger.create('GMS_KEYCLOAK', process.env.GMS_KEYCLOAK);

const gmsKeycloakConfig: KeycloakConfig = {
  realm: GMS_KEYCLOAK_REALM,
  url: GMS_KEYCLOAK_URL,
  clientId: GMS_KEYCLOAK_CLIENT_ID
};

const gmsKeycloak = new Keycloak(gmsKeycloakConfig);

/**
 * Creates a function to dispatch for redux that updates the authentication status.
 * Calls keycloak to get he users information.
 *
 * @returns a function for redux dispatch
 */
const updateUserAuthenticationStatus = async (): Promise<
  (dispatch: AppDispatch, getState: () => AppState) => void
> => {
  try {
    const profile = await gmsKeycloak.loadUserProfile();
    // TODO: remove once we are satisfied everything works
    logger.debug('loadUserProfile', JSON.stringify(profile, null, '  '));
    const authenticationStatus: AuthenticationStatus = {
      userName: profile.username,
      authenticated: true,
      authenticationCheckComplete: true,
      failedToConnect: false
    };
    return AppOperations.setAppAuthenticationStatus(authenticationStatus);
  } catch {
    throw new Error('Keycloak loadUserProfile failed to load');
  }
};
/**
 * Initializes Keycloak instance and calls the provided callback function if successfully authenticated.
 *
 * @param onAuthenticatedCallback
 */
const callLogin = (onAuthenticatedCallback: () => void) => {
  gmsKeycloak
    .init({ onLoad: 'login-required', checkLoginIframe: false })
    .then(authenticated => {
      if (authenticated) {
        logger.info('User has been authenticated');
        onAuthenticatedCallback();
      } else {
        logger.error('User has not been authenticated');
      }
    })
    .catch(e => {
      logger.error(e);
      logger.error(`KEYCLOAK init exception: ${e?.error}`);
    });
};

const callLogout = (redirectUri = `${UI_URL}${UI_BASE_PATH}`) => {
  const logoutOptions = { redirectUri };

  gmsKeycloak
    .logout(logoutOptions)
    .then(success => {
      logger.debug('KEYCLOAK logout success ', success);
    })
    .catch(error => {
      logger.error('KEYCLOAK logout error ', error);
    });
};

export const KeyCloakService: {
  callLogin: (onAuthenticatedCallback: () => void) => void;
  callLogout: (redirectUri?: string) => void;
  updateUserAuthenticationStatus: () => Promise<
    (dispatch: AppDispatch, getState: () => AppState) => void
  >;
} = {
  callLogin,
  callLogout,
  updateUserAuthenticationStatus
};
