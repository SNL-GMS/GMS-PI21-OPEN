import type { PayloadAction } from '@reduxjs/toolkit';
import { createSlice } from '@reduxjs/toolkit';

import type { AuthenticationStatus, UserSessionState } from './types';

/**
 * The initial state for the user session state.
 */
export const userSessionInitialState: UserSessionState = {
  authenticationStatus: {
    userName: null,
    authenticated: false,
    authenticationCheckComplete: false,
    failedToConnect: false
  },
  connected: true
};

/**
 * The system message reducer slice.
 */
export const userSessionSlice = createSlice({
  name: 'userSession',
  initialState: userSessionInitialState,
  reducers: {
    /**
     * Sets the authentication status
     *
     * @param state the state
     * @param action the action
     */
    setAuthenticationStatus(state, action: PayloadAction<AuthenticationStatus>) {
      state.authenticationStatus = action.payload;
    },

    /**
     * Sets the connection state
     *
     * @param state the state
     * @param action the action
     */
    setConnected(state, action: PayloadAction<boolean>) {
      state.connected = action.payload;
    }
  }
});

export const userSessionActions = userSessionSlice.actions;
