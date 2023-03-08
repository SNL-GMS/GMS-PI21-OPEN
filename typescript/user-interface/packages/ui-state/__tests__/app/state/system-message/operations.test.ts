import { SystemMessageTypes } from '@gms/common-model';
import type * as Redux from 'redux';
import type { MockStore, MockStoreCreator } from 'redux-mock-store';
import createMockStore from 'redux-mock-store';
import thunk from 'redux-thunk';

import { initialState } from '../../../../src/ts/app/state/reducer';
import * as Operations from '../../../../src/ts/app/state/system-message/operations';
import { systemMessageSlice } from '../../../../src/ts/app/state/system-message/system-message-slice';
import type { AppState } from '../../../../src/ts/app/store';

// eslint-disable-next-line @typescript-eslint/no-magic-numbers
Date.now = jest.fn().mockReturnValue(1575410988600);

const middlewares = [thunk];
const mockStoreCreator: MockStoreCreator<AppState, Redux.AnyAction> = createMockStore(middlewares);
// eslint-disable-next-line @typescript-eslint/no-explicit-any
let store: MockStore<AppState, any>;

describe('state system message operations', () => {
  describe('operations', () => {
    const lastUpdated = 123456789;

    const systemMessages1: SystemMessageTypes.SystemMessage[] = [
      {
        id: '1',
        type: SystemMessageTypes.SystemMessageType.CHANNEL_MONITOR_TYPE_QUIETED,
        severity: SystemMessageTypes.SystemMessageSeverity.CRITICAL,
        category: SystemMessageTypes.SystemMessageCategory.SOH,
        subCategory: SystemMessageTypes.SystemMessageSubCategory.CAPABILITY,
        time: lastUpdated,
        message: 'sample message'
      }
    ];

    const systemMessages2: SystemMessageTypes.SystemMessage[] = [
      {
        id: '2',
        type: SystemMessageTypes.SystemMessageType.STATION_CAPABILITY_STATUS_CHANGED,
        severity: SystemMessageTypes.SystemMessageSeverity.WARNING,
        category: SystemMessageTypes.SystemMessageCategory.SOH,
        subCategory: SystemMessageTypes.SystemMessageSubCategory.STATUS,
        time: lastUpdated,
        message: 'sample message'
      }
    ];

    const systemMessages3: SystemMessageTypes.SystemMessage[] = [
      {
        id: '3',
        type: SystemMessageTypes.SystemMessageType.STATION_NEEDS_ATTENTION,
        severity: SystemMessageTypes.SystemMessageSeverity.INFO,
        category: SystemMessageTypes.SystemMessageCategory.SOH,
        subCategory: SystemMessageTypes.SystemMessageSubCategory.STATUS,
        time: lastUpdated,
        message: 'sample message'
      }
    ];

    beforeEach(() => {
      store = mockStoreCreator({ app: initialState } as any);
    });

    it('should be able to add system messages (undefined)', () => {
      const expectedActions = [];
      store.dispatch(Operations.addSystemMessages(undefined));

      const actions = store.getActions();
      expect(actions).toEqual(expectedActions);
    });

    it('should be able to add system messages (empty)', () => {
      const expectedActions = [];
      store.dispatch(Operations.addSystemMessages([]));

      const actions = store.getActions();
      expect(actions).toEqual(expectedActions);
    });

    it('should be able to add system messages', () => {
      const expectedActions = [
        {
          type: systemMessageSlice.actions.setSystemMessages.type,
          payload: [...systemMessages1, ...systemMessages2]
        },
        {
          type: systemMessageSlice.actions.setLatestSystemMessages.type,
          payload: [...systemMessages1, ...systemMessages2]
        },
        {
          type: systemMessageSlice.actions.setLastUpdated.type,
          payload: Date.now()
        }
      ];
      store.dispatch(Operations.addSystemMessages([...systemMessages1, ...systemMessages2]));
      const actions = store.getActions();
      expect(actions).toEqual(expectedActions);
    });

    it('should be able to add system messages with a limit', () => {
      const expectedActions = [
        {
          type: systemMessageSlice.actions.setSystemMessages.type,
          payload: [...systemMessages1, ...systemMessages2]
        },
        {
          type: systemMessageSlice.actions.setLatestSystemMessages.type,
          payload: [...systemMessages1, ...systemMessages2]
        },
        {
          type: systemMessageSlice.actions.setLastUpdated.type,
          payload: Date.now()
        },
        {
          type: systemMessageSlice.actions.setSystemMessages.type,
          payload: [...systemMessages3]
        },
        {
          type: systemMessageSlice.actions.setLatestSystemMessages.type,
          payload: [...systemMessages3]
        },
        {
          type: systemMessageSlice.actions.setLastUpdated.type,
          payload: Date.now()
        }
      ];
      store.dispatch(Operations.addSystemMessages([...systemMessages1, ...systemMessages2], 2));

      store.dispatch(Operations.addSystemMessages([...systemMessages3], 2));

      const actions = store.getActions();
      expect(actions).toEqual(expectedActions);
    });

    it('should be able to clear all system', () => {
      const expectedActions = [
        {
          type: systemMessageSlice.actions.setSystemMessages.type,
          payload: []
        }
      ];
      store.dispatch(Operations.clearAllSystemMessages());

      const actions = store.getActions();
      expect(actions).toEqual(expectedActions);
    });
  });
});
