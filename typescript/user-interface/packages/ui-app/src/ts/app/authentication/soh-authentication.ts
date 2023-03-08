import { API_LOGIN_CHECK_URI, API_LOGIN_URI, API_LOGOUT_URI } from '@gms/common-util';
import type { AuthenticationStatus } from '@gms/ui-state';
import { UILogger } from '@gms/ui-util';
import Axios from 'axios';
import { toast } from 'react-toastify';

import type { Authenticator } from './types';

const logger = UILogger.create('GMS_LOG_AUTH', process.env.GMS_LOG_AUTH);

/**
 * Returns the AuthState from the network result.
 *
 * @param result the result to process
 */
const getAuthStatusFromResult = (result): AuthenticationStatus => {
  // If username is defined, but was not authenticated - bad username
  if (result.data.userName && !result.data.authenticated) {
    toast.warn(`User name contained invalid characters.`);
  }
  return {
    authenticated: result.data.authenticated,
    userName: result.data.userName,
    authenticationCheckComplete: true,
    failedToConnect: false
  };
};

/**
 * Returns a failed connection AuthStatus.
 */
const getFailedToConnectAuthStatus = (): AuthenticationStatus => ({
  authenticated: false,
  userName: undefined,
  authenticationCheckComplete: false,
  failedToConnect: true
});

/**
 * Attempts to login to the server with the given credentials
 *
 * @param userName Plaintext username
 */
async function authenticateWith(userName: string): Promise<AuthenticationStatus> {
  return Axios.get(API_LOGIN_URI, {
    params: {
      userName
    }
  })
    .then(getAuthStatusFromResult)
    .catch(getFailedToConnectAuthStatus);
}

/**
 * Checks if the user is logged in.
 */
async function checkIsAuthenticated(): Promise<AuthenticationStatus> {
  return Axios.get(API_LOGIN_CHECK_URI)
    .then(getAuthStatusFromResult)
    .catch(getFailedToConnectAuthStatus);
}

/**
 * Checks if the user is logged in.
 *
 * @param callback redux action that updates the authorization status
 */
async function unAuthenticateWith(): Promise<AuthenticationStatus> {
  return Axios.get(API_LOGOUT_URI)
    .then(getAuthStatusFromResult)
    .catch(getFailedToConnectAuthStatus);
}

/**
 * Logs the user out of the system
 */
const logout = (setAppAuthenticationStatus: (status: AuthenticationStatus) => void): void => {
  unAuthenticateWith()
    .then(result => {
      setAppAuthenticationStatus(result);
    })
    .catch(error => logger.error(`Failed to un-authenticate: ${error}`));
};

export const sohAuthenticator: Authenticator = {
  authenticateWith,
  checkIsAuthenticated,
  unAuthenticateWith,
  logout
};
