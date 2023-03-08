import type { AuthenticationStatus } from '@gms/ui-state';

/** The login screen state */
export interface LoginScreenState {
  username: string;
  isDarkMode: boolean;
}

/**
 * The login screen redux props.
 * Note: these props are mapped in from Redux state
 */
export interface LoginScreenReduxProps {
  redirectPath: string;
  authenticated: boolean;
  authenticationCheckComplete: boolean;
  failedToConnect: boolean;
  setAppAuthenticationStatus(auth: AuthenticationStatus): void;
}

export interface Authenticator {
  authenticateWith(userName: string): Promise<AuthenticationStatus>;
  checkIsAuthenticated(): Promise<AuthenticationStatus>;
  unAuthenticateWith(): Promise<AuthenticationStatus>;
}

export interface LoginScreenBaseProps {
  authenticator: Authenticator;
}

export type LoginScreenProps = LoginScreenReduxProps & LoginScreenBaseProps;
