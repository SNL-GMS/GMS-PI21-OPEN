import { Displays } from '@gms/common-model';
import type * as Redux from 'redux';
import type { MockStore, MockStoreCreator } from 'redux-mock-store';
import createMockStore from 'redux-mock-store';
import thunk from 'redux-thunk';

import {
  commonActions,
  GLDisplayState,
  setCommandPaletteVisibility,
  setGlDisplayState,
  setSelectedStationIds
} from '../../../../src/ts/app/state/common';
import { initialState } from '../../../../src/ts/app/state/reducer';
import type { AppState } from '../../../../src/ts/app/store';

const middlewares = [thunk];
const mockStoreCreator: MockStoreCreator<AppState, Redux.AnyAction> = createMockStore(middlewares);

describe('state common-workspace operations', () => {
  describe('operations', () => {
    describe('setCommandPaletteVisibility', () => {
      it('should set the command palette visibility', () => {
        const store: MockStore<AppState, any> = mockStoreCreator({ app: initialState } as any);
        store.dispatch(setCommandPaletteVisibility(true));
        let actions = store.getActions();
        expect(actions[actions.length - 1].payload).toEqual(true);

        store.dispatch(setCommandPaletteVisibility(true));
        actions = store.getActions();
        expect(actions[actions.length - 1].payload).toEqual(true);

        store.dispatch(commonActions.setCommandPaletteVisibility(false));
        store.dispatch(setCommandPaletteVisibility(false));
        actions = store.getActions();
        expect(actions[actions.length - 1].payload).toEqual(false);
      });
    });

    describe('setSelectedStationIds', () => {
      it('should set the selected station ids', () => {
        const store: MockStore<AppState, any> = mockStoreCreator({ app: initialState } as any);
        store.dispatch(setSelectedStationIds(['1', '2', '3']));
        let actions = store.getActions();
        expect(actions[actions.length - 1].payload).toEqual(['1', '2', '3']);

        store.dispatch(setSelectedStationIds(['1']));
        actions = store.getActions();
        expect(actions[actions.length - 1].payload).toEqual(['1']);

        store.dispatch(setSelectedStationIds(['1']));
        actions = store.getActions();
        expect(actions[actions.length - 1].payload).toEqual(['1']);

        store.dispatch(commonActions.setCommandPaletteVisibility(undefined));
        store.dispatch(setSelectedStationIds(undefined));
        actions = store.getActions();
        expect(actions[actions.length - 1].payload).toEqual(undefined);
      });
    });

    describe('setGlDisplayState', () => {
      it('should set the display layout to "open" for a display', () => {
        const store: MockStore<AppState, any> = mockStoreCreator({ app: initialState } as any);
        store.dispatch(
          setGlDisplayState(Displays.CommonDisplays.SYSTEM_MESSAGES, GLDisplayState.OPEN)
        );
        const actions = store.getActions();
        expect(
          actions.find(action => action.type === commonActions.setGlLayoutState.type).payload[
            Displays.CommonDisplays.SYSTEM_MESSAGES
          ]
        ).toEqual(GLDisplayState.OPEN);
      });

      it('throws when given an invalid display name', () => {
        const store: MockStore<AppState, any> = mockStoreCreator({ app: initialState } as any);
        expect(() =>
          store.dispatch(setGlDisplayState('GARBAGE', GLDisplayState.CLOSED))
        ).toThrowErrorMatchingSnapshot();
      });
    });
  });
});
