import type * as Redux from 'redux';
import type { MockStore, MockStoreCreator } from 'redux-mock-store';
import createMockStore from 'redux-mock-store';
import thunk from 'redux-thunk';

import { setAppAuthenticationStatus } from '../../../src/ts/app/state/operations';
import { initialState } from '../../../src/ts/app/state/reducer';
import type { AppState } from '../../../src/ts/app/store';

const middlewares = [thunk];
const mockStoreCreator: MockStoreCreator<AppState, Redux.AnyAction> = createMockStore(middlewares);

describe('state operations', () => {
  describe('operations', () => {
    describe('setAppAuthenticationStatus', () => {
      it('should set app authentication status', () => {
        const store: MockStore<AppState, any> = mockStoreCreator({ app: initialState } as any);

        store.dispatch(setAppAuthenticationStatus(initialState.userSession.authenticationStatus));

        store.dispatch(
          setAppAuthenticationStatus({
            authenticated: false,
            authenticationCheckComplete: true,
            failedToConnect: false,
            userName: undefined
          })
        );
        let actions = store.getActions();
        expect(actions[actions.length - 1].payload).toEqual({
          authenticated: false,
          authenticationCheckComplete: true,
          failedToConnect: false,
          userName: undefined
        });

        store.dispatch(
          setAppAuthenticationStatus({
            authenticated: true,
            authenticationCheckComplete: true,
            failedToConnect: false,
            userName: 'tester'
          })
        );
        actions = store.getActions();
        expect(actions[actions.length - 1].payload).toEqual({
          authenticated: true,
          authenticationCheckComplete: true,
          failedToConnect: false,
          userName: 'tester'
        });
      });
    });
  });
});
