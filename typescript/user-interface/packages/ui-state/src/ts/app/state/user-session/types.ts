/**
 * The authentication status.
 */
export interface AuthenticationStatus {
  userName: string;
  authenticated: boolean;
  authenticationCheckComplete: boolean;
  failedToConnect: boolean;
}

/**
 * The system message state.
 */
export interface UserSessionState {
  authenticationStatus: AuthenticationStatus;
  connected: boolean;
}
