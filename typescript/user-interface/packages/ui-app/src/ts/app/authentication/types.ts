import type { AuthenticationStatus } from '@gms/ui-state';

export interface Authenticator {
  authenticateWith(userName: string): Promise<AuthenticationStatus>;
  checkIsAuthenticated(): Promise<AuthenticationStatus>;
  unAuthenticateWith(): Promise<AuthenticationStatus>;
  logout(setAppAuthenticationStatus: (status: AuthenticationStatus) => void): void;
}

export interface LoginRequestParams {
  userName: string;
}
