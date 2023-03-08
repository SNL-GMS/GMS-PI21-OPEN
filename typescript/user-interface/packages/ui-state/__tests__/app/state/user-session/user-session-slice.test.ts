import { createAction } from '@reduxjs/toolkit';
import type * as Redux from 'redux';

import type * as Types from '../../../../src/ts/app/state/user-session/types';
import {
  userSessionInitialState,
  userSessionSlice
} from '../../../../src/ts/app/state/user-session/user-session-slice';

describe('state user session slice', () => {
  it('defined', () => {
    expect(userSessionSlice).toBeDefined();
    expect(userSessionInitialState).toBeDefined();
  });

  it('should return the initial state', () => {
    expect(userSessionSlice.reducer(undefined, createAction(undefined))).toMatchSnapshot();
    expect(userSessionSlice.reducer(undefined, createAction(''))).toMatchSnapshot();
    expect(
      userSessionSlice.reducer(userSessionInitialState, createAction(undefined))
    ).toMatchSnapshot();
    expect(userSessionSlice.reducer(userSessionInitialState, createAction(''))).toMatchSnapshot();
  });

  it('should set connected', () => {
    const action: Redux.AnyAction = {
      type: userSessionSlice.actions.setConnected.type,
      payload: true
    };
    const expectedState: Types.UserSessionState = {
      ...userSessionInitialState,
      connected: true
    };
    expect(userSessionSlice.reducer(userSessionInitialState, action)).toEqual(expectedState);
  });

  it('should set authentication status', () => {
    const action: Redux.AnyAction = {
      type: userSessionSlice.actions.setAuthenticationStatus.type,
      payload: {
        userName: 'test',
        authenticated: true,
        authenticationCheckComplete: true,
        failedToConnect: false
      }
    };
    const expectedState: Types.UserSessionState = {
      ...userSessionInitialState,
      authenticationStatus: {
        userName: 'test',
        authenticated: true,
        authenticationCheckComplete: true,
        failedToConnect: false
      }
    };
    expect(userSessionSlice.reducer(userSessionInitialState, action)).toEqual(expectedState);
  });
});
