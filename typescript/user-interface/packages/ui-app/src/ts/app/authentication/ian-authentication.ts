import type { AuthenticationStatus } from '@gms/ui-state';
import { deleteCookie, getCookie, setCookie, UILogger } from '@gms/ui-util';
import { toast } from 'react-toastify';
import xssFilters from 'xss-filters';

import type { Authenticator, LoginRequestParams } from './types';

const logger = UILogger.create('GMS_LOG_AUTH', process.env.GMS_LOG_AUTH);

const AUTH_COOKIE_ID = 'IAN-user-auth';

/**
 * Returns a failed connection AuthStatus.
 */
const getFailedToConnectAuthStatus = (): AuthenticationStatus => ({
  authenticated: false,
  userName: undefined,
  authenticationCheckComplete: false,
  failedToConnect: true
});

const mockAuthRequest = async (
  params: LoginRequestParams,
  mockAuth = true
): Promise<AuthenticationStatus> =>
  new Promise<AuthenticationStatus>((resolve, reject) => {
    resolve({
      authenticated: mockAuth,
      userName: params.userName,
      authenticationCheckComplete: true,
      failedToConnect: false
    });
    reject();
  });

/**
 * TODO: Add validation/sanitization to whatever service eventually manages user session
 * ! IMPORTANT: This is not sufficient, and sanitization must happen on any service
 * ! that accepts user input
 * This is helpful for giving the user immediate feedback
 */
function checkUserName(userName: string): boolean {
  const safeUserName = xssFilters.inHTMLData(userName);
  if (!userName || !safeUserName) {
    toast.info(`Please input your username.`, { toastId: 'cy-toast-no-username' });
    return false;
  }
  if (userName !== safeUserName) {
    toast.warn(`User name contained invalid characters.`, {
      toastId: 'cy-toast-invalid-username'
    });
    return false;
  }
  return true;
}

/**
 * Attempts to login to the server with the given credentials
 *
 * @param userName Plaintext username
 */
async function authenticateWith(userName: string): Promise<AuthenticationStatus> {
  const userNameIsGood = checkUserName(userName);
  // If user name is not good don't set the cookie
  if (userNameIsGood) {
    setCookie(AUTH_COOKIE_ID, userName);
  }
  // Return authorized if userNameIsGood = true
  return mockAuthRequest(
    {
      userName
    },
    userNameIsGood
  ).catch(getFailedToConnectAuthStatus);
}

/**
 * Checks if the user is logged in.
 */
async function checkIsAuthenticated(): Promise<AuthenticationStatus> {
  const userName = getCookie(AUTH_COOKIE_ID);
  if (userName) {
    return mockAuthRequest({
      userName
    }).catch(getFailedToConnectAuthStatus);
  }
  return mockAuthRequest({ userName }, false);
}

/**
 * Checks if the user is logged in.
 *
 * @param callback redux action that updates the authorization status
 */
async function unAuthenticateWith(userName = 'default'): Promise<AuthenticationStatus> {
  deleteCookie(AUTH_COOKIE_ID, userName);
  return mockAuthRequest(
    {
      userName
    },
    false
  ).catch(getFailedToConnectAuthStatus);
}

/**
 * Logs the user out of the system
 */
const logout = (setAppAuthenticationStatus: (status: AuthenticationStatus) => void): void => {
  // eslint-disable-next-line @typescript-eslint/no-floating-promises
  unAuthenticateWith()
    .then(result => {
      setAppAuthenticationStatus(result);
    })
    .catch(error => logger.error(`Failed to un-authenticate: ${error}`));
};

export const ianAuthenticator: Authenticator = {
  authenticateWith,
  checkIsAuthenticated,
  unAuthenticateWith,
  logout
};
