import { SystemMessageTypes } from '@gms/common-model';
import { createAction } from '@reduxjs/toolkit';
import type * as Redux from 'redux';
import type { MockStore, MockStoreCreator } from 'redux-mock-store';
import createMockStore from 'redux-mock-store';
import thunk from 'redux-thunk';

import { initialState } from '../../../../src/ts/app/state/reducer';
import {
  systemMessageInitialState,
  systemMessageSlice
} from '../../../../src/ts/app/state/system-message/system-message-slice';
import type { SystemMessageState } from '../../../../src/ts/app/state/system-message/types';
import type { AppState } from '../../../../src/ts/app/store';

const middlewares = [thunk];
const mockStoreCreator: MockStoreCreator<AppState, Redux.AnyAction> = createMockStore(middlewares);
let store: MockStore<AppState, Redux.AnyAction>;

describe('state system message', () => {
  describe('reducer', () => {
    it('should return the initial state', () => {
      expect(systemMessageSlice.reducer(undefined, createAction(undefined))).toEqual(
        systemMessageInitialState
      );
      expect(systemMessageSlice.reducer(undefined, createAction(''))).toEqual(
        systemMessageInitialState
      );

      expect(
        systemMessageSlice.reducer(systemMessageInitialState, createAction(undefined))
      ).toEqual(systemMessageInitialState);
      expect(systemMessageSlice.reducer(systemMessageInitialState, createAction(''))).toEqual(
        systemMessageInitialState
      );
    });

    it('should set the last updated time', () => {
      const lastUpdated = 123456789;

      const action: Redux.AnyAction = {
        type: systemMessageSlice.actions.setLastUpdated.type,
        payload: lastUpdated
      };
      const expectedState: SystemMessageState = {
        ...systemMessageInitialState,
        lastUpdated
      };
      expect(systemMessageSlice.reducer(systemMessageInitialState, action)).toEqual(expectedState);
    });

    it('should set the system messages', () => {
      const systemMessages: SystemMessageTypes.SystemMessage[] = [
        {
          id: '1',
          type: SystemMessageTypes.SystemMessageType.CHANNEL_MONITOR_TYPE_QUIETED,
          severity: SystemMessageTypes.SystemMessageSeverity.CRITICAL,
          category: SystemMessageTypes.SystemMessageCategory.SOH,
          subCategory: SystemMessageTypes.SystemMessageSubCategory.CAPABILITY,
          time: 123456789,
          message: 'sample message'
        }
      ];
      const action: Redux.AnyAction = {
        type: systemMessageSlice.actions.setSystemMessages.type,
        payload: systemMessages
      };
      const expectedState: SystemMessageState = {
        ...systemMessageInitialState,
        systemMessages
      };
      expect(systemMessageSlice.reducer(systemMessageInitialState, action)).toEqual(expectedState);
    });

    it('should set the latest system messages', () => {
      const latestSystemMessages: SystemMessageTypes.SystemMessage[] = [
        {
          id: '4',
          type: SystemMessageTypes.SystemMessageType.CHANNEL_MONITOR_TYPE_STATUS_CHANGED,
          severity: SystemMessageTypes.SystemMessageSeverity.WARNING,
          category: SystemMessageTypes.SystemMessageCategory.SOH,
          subCategory: SystemMessageTypes.SystemMessageSubCategory.USER,
          time: 123456789,
          message: 'sample message'
        }
      ];
      const action: Redux.AnyAction = {
        type: systemMessageSlice.actions.setLatestSystemMessages.type,
        payload: latestSystemMessages
      };

      const expectedState: SystemMessageState = {
        ...systemMessageInitialState,
        latestSystemMessages
      };
      expect(systemMessageSlice.reducer(systemMessageInitialState, action)).toEqual(expectedState);
    });
  });

  describe('actions', () => {
    beforeEach(() => {
      store = mockStoreCreator({ app: initialState } as any);
    });

    it('should set the last updated time', () => {
      const lastUpdatedTime = 123456789;
      const expectedAction: Redux.AnyAction = {
        type: systemMessageSlice.actions.setLastUpdated.type,
        payload: lastUpdatedTime
      };
      expect(systemMessageSlice.actions.setLastUpdated(lastUpdatedTime)).toEqual(expectedAction);
      store.dispatch(systemMessageSlice.actions.setLastUpdated(lastUpdatedTime));

      const actions = store.getActions();
      expect(actions).toEqual([expectedAction]);
    });

    it('should set the system messages', () => {
      const systemMessages: SystemMessageTypes.SystemMessage[] = [
        {
          id: '1',
          type: SystemMessageTypes.SystemMessageType.CHANNEL_MONITOR_TYPE_QUIETED,
          severity: SystemMessageTypes.SystemMessageSeverity.CRITICAL,
          category: SystemMessageTypes.SystemMessageCategory.SOH,
          subCategory: SystemMessageTypes.SystemMessageSubCategory.CAPABILITY,
          time: 123456789,
          message: 'sample message'
        }
      ];
      const expectedAction: Redux.AnyAction = {
        type: systemMessageSlice.actions.setSystemMessages().type,
        payload: systemMessages
      };
      expect(systemMessageSlice.actions.setSystemMessages(systemMessages)).toEqual(expectedAction);
      store.dispatch(systemMessageSlice.actions.setSystemMessages(systemMessages));

      const actions = store.getActions();
      expect(actions).toEqual([expectedAction]);
    });
  });
});
