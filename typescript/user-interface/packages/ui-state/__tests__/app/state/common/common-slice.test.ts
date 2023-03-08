import { createAction } from '@reduxjs/toolkit';
import type * as Redux from 'redux';
import type { MockStore, MockStoreCreator } from 'redux-mock-store';
import createMockStore from 'redux-mock-store';
import thunk from 'redux-thunk';

import { commonInitialState, commonSlice } from '../../../../src/ts/app/state/common';
import * as Types from '../../../../src/ts/app/state/common/types';
import { initialState } from '../../../../src/ts/app/state/reducer';
import type { AppState } from '../../../../src/ts/app/store';

const middlewares = [thunk];
const mockStoreCreator: MockStoreCreator<AppState, Redux.AnyAction> = createMockStore(middlewares);
let store: MockStore<AppState, Redux.AnyAction>;

describe('state workspace common', () => {
  describe('reducer', () => {
    it('should return the initial state', () => {
      expect(commonSlice.reducer(undefined, createAction(undefined))).toEqual(commonInitialState);
      expect(commonSlice.reducer(undefined, createAction(''))).toEqual(commonInitialState);
      expect(commonSlice.reducer(commonInitialState, createAction(undefined))).toEqual(
        commonInitialState
      );
      expect(commonSlice.reducer(commonInitialState, createAction(''))).toEqual(commonInitialState);
    });

    it('should set the command palette visibility', () => {
      const action: Redux.AnyAction = {
        type: commonSlice.actions.setCommandPaletteVisibility.type,
        payload: true
      };
      const expectedState: Types.CommonState = {
        ...commonInitialState,
        commandPaletteIsVisible: true
      };
      expect(commonSlice.reducer(commonInitialState, action)).toEqual(expectedState);
    });

    it('should set the key press action queue', () => {
      const action: Redux.AnyAction = {
        type: commonSlice.actions.setKeyPressActionQueue.type,
        payload: {
          1: 'action1',
          2: 'action2'
        }
      };
      const expectedState: Types.CommonState = {
        ...commonInitialState,
        keyPressActionQueue: action.payload
      };
      expect(commonSlice.reducer(commonInitialState, action)).toEqual(expectedState);
    });

    it('should set the selected station ids', () => {
      const action: Redux.AnyAction = {
        type: commonSlice.actions.setSelectedStationIds.type,
        payload: ['station1', 'station2']
      };
      const expectedState: Types.CommonState = {
        ...commonInitialState,
        selectedStationIds: action.payload
      };
      expect(commonSlice.reducer(commonInitialState, action)).toEqual(expectedState);
    });

    it('should set the GL layout state', () => {
      const action: Redux.AnyAction = {
        type: commonSlice.actions.setGlLayoutState.type,
        payload: {
          test1: Types.GLDisplayState.OPEN,
          test2: Types.GLDisplayState.CLOSED
        }
      };
      const expectedState: Types.CommonState = {
        ...commonInitialState,
        glLayoutState: action.payload
      };
      expect(commonSlice.reducer(commonInitialState, action)).toEqual(expectedState);
    });
  });

  describe('actions', () => {
    beforeEach(() => {
      store = mockStoreCreator({ app: initialState } as any);
    });

    it('should set the command palette visibility', () => {
      const expectedAction: Redux.AnyAction = {
        type: commonSlice.actions.setCommandPaletteVisibility.type,
        payload: true
      };
      expect(commonSlice.actions.setCommandPaletteVisibility(true)).toEqual(expectedAction);
      store.dispatch(commonSlice.actions.setCommandPaletteVisibility(true));

      const actions = store.getActions();
      expect(actions).toEqual([expectedAction]);
    });

    it('should set the key press action queue', () => {
      const expectedAction: Redux.AnyAction = {
        type: commonSlice.actions.setKeyPressActionQueue.type,
        payload: {
          1: 'action1',
          2: 'action2'
        }
      };
      expect(commonSlice.actions.setKeyPressActionQueue(expectedAction.payload)).toEqual(
        expectedAction
      );
      store.dispatch(commonSlice.actions.setKeyPressActionQueue(expectedAction.payload));

      const actions = store.getActions();
      expect(actions).toEqual([expectedAction]);
    });

    it('should set the selected station ids', () => {
      const expectedAction: Redux.AnyAction = {
        type: commonSlice.actions.setSelectedStationIds.type,
        payload: ['station1', 'station2']
      };
      expect(commonSlice.actions.setSelectedStationIds(expectedAction.payload)).toEqual(
        expectedAction
      );
      store.dispatch(commonSlice.actions.setSelectedStationIds(expectedAction.payload));

      const actions = store.getActions();
      expect(actions).toEqual([expectedAction]);
    });

    it('should set the GL layout state', () => {
      const expectedAction: Redux.AnyAction = {
        type: commonSlice.actions.setGlLayoutState.type,
        payload: {
          test1: Types.GLDisplayState.OPEN,
          test2: Types.GLDisplayState.CLOSED
        }
      };
      expect(commonSlice.actions.setGlLayoutState(expectedAction.payload)).toEqual(expectedAction);
      store.dispatch(commonSlice.actions.setGlLayoutState(expectedAction.payload));

      const actions = store.getActions();
      expect(actions).toEqual([expectedAction]);
    });
  });
});
