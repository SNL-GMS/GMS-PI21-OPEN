import isEqual from 'lodash/isEqual';
import { batch } from 'react-redux';

import type { AppDispatch, AppState } from '../store';
import * as Actions from './actions';
import type { AuthenticationStatus } from './user-session/types';
import { userSessionSlice } from './user-session/user-session-slice';

/**
 * Redux operation for setting the authentication status.
 *
 * @param event the event to set
 * @param authStatus
 */
export const setAppAuthenticationStatus = (authStatus: AuthenticationStatus) => (
  dispatch: AppDispatch,
  getState: () => AppState
): void => {
  const state: AppState = getState();
  if (!isEqual(state.app.userSession.authenticationStatus, authStatus)) {
    if (!authStatus.userName && !authStatus.authenticated) {
      batch(() => {
        // reset the application state
        dispatch(Actions.reset());

        // update the authentication status
        dispatch(userSessionSlice.actions.setAuthenticationStatus(authStatus));
      });
    } else {
      batch(() => {
        // update the authentication status
        dispatch(userSessionSlice.actions.setAuthenticationStatus(authStatus));
      });
    }
  }
};
