import type { CommonTypes } from '@gms/common-model';
import { createAction } from '@reduxjs/toolkit';
import type * as Redux from 'redux';
import type { MockStore, MockStoreCreator } from 'redux-mock-store';
import createMockStore from 'redux-mock-store';
import thunk from 'redux-thunk';

import { initialState } from '../../../../src/ts/app/state/reducer';
import { waveformInitialState, waveformSlice } from '../../../../src/ts/app/state/waveform';
import type {
  StationVisibilityChangesDictionary,
  WaveformLoadingState,
  WaveformState
} from '../../../../src/ts/app/state/waveform/types';
import * as Util from '../../../../src/ts/app/state/waveform/util';
import type { AppState } from '../../../../src/ts/app/store';

const middlewares = [thunk];
const mockStoreCreator: MockStoreCreator<AppState, Redux.AnyAction> = createMockStore(middlewares);
let store: MockStore<AppState, Redux.AnyAction>;

describe('waveform slice', () => {
  describe('exists', () => {
    it('exists', () => {
      expect(waveformInitialState).toBeDefined();
      expect(waveformSlice).toBeDefined();
      expect(waveformInitialState).toMatchSnapshot();
    });
  });
  describe('reducer', () => {
    it('should return the initial state', () => {
      expect(waveformSlice.reducer(undefined, createAction(undefined))).toEqual(
        waveformInitialState
      );
      expect(waveformSlice.reducer(undefined, createAction(''))).toEqual(waveformInitialState);
      expect(waveformSlice.reducer(waveformInitialState, createAction(undefined))).toEqual(
        waveformInitialState
      );
      expect(waveformSlice.reducer(waveformInitialState, createAction(''))).toEqual(
        waveformInitialState
      );
    });
  });
  describe('actions', () => {
    beforeEach(() => {
      store = mockStoreCreator({ app: initialState } as any);
    });

    it('should set the stations visibility', () => {
      const sDict: StationVisibilityChangesDictionary = {
        stationName: Util.newStationVisibilityChangesObject('stationName')
      };
      const action: Redux.AnyAction = {
        type: waveformSlice.actions.setStationsVisibility.type,
        payload: sDict
      };
      expect(waveformSlice.reducer(waveformInitialState, action)).toEqual({
        ...waveformInitialState,
        stationsVisibility: sDict
      });
      expect(waveformSlice.actions.setStationsVisibility(sDict)).toEqual(action);
      store.dispatch(waveformSlice.actions.setStationsVisibility(sDict));
      const actions = store.getActions();
      expect(actions).toEqual([action]);
    });

    it('should set the viewable interval', () => {
      const viewableInterval: CommonTypes.TimeRange = {
        startTimeSecs: 1,
        endTimeSecs: 2
      };
      const action: Redux.AnyAction = {
        type: waveformSlice.actions.setViewableInterval.type,
        payload: viewableInterval
      };

      expect(waveformSlice.reducer(waveformInitialState, action)).toEqual({
        ...waveformInitialState,
        viewableInterval
      });

      expect(waveformSlice.actions.setViewableInterval(viewableInterval)).toEqual(action);

      store.dispatch(waveformSlice.actions.setViewableInterval(viewableInterval));
      const actions = store.getActions();
      expect(actions).toEqual([action]);
    });

    it('should set the zoom interval', () => {
      const zoomInterval: CommonTypes.TimeRange = {
        startTimeSecs: 1,
        endTimeSecs: 2
      };
      const action: Redux.AnyAction = {
        type: waveformSlice.actions.setZoomInterval.type,
        payload: zoomInterval
      };

      expect(waveformSlice.reducer(waveformInitialState, action)).toEqual({
        ...waveformInitialState,
        zoomInterval
      });

      expect(waveformSlice.actions.setZoomInterval(zoomInterval)).toEqual(action);

      store.dispatch(waveformSlice.actions.setZoomInterval(zoomInterval));
      const actions = store.getActions();
      expect(actions).toEqual([action]);
    });

    it('should set the waveform client loading state', () => {
      const waveformClientState: WaveformLoadingState = {
        isLoading: true,
        total: 2,
        completed: 1,
        percent: 50,
        description: 'Loading waveforms'
      };
      const action: Redux.AnyAction = {
        type: waveformSlice.actions.setWaveformClientLoadingState.type,
        payload: waveformClientState
      };

      expect(waveformSlice.reducer(waveformInitialState, action)).toEqual({
        ...waveformInitialState,
        loadingState: waveformClientState
      });

      expect(waveformSlice.actions.setWaveformClientLoadingState(waveformClientState)).toEqual(
        action
      );

      store.dispatch(waveformSlice.actions.setWaveformClientLoadingState(waveformClientState));
      const actions = store.getActions();
      expect(actions).toEqual([action]);
    });

    it('should set should show time uncertainty', () => {
      const shouldShowTimeUncertainty = false;
      const action: Redux.AnyAction = {
        type: waveformSlice.actions.setShouldShowTimeUncertainty.type,
        payload: shouldShowTimeUncertainty
      };

      expect(waveformSlice.reducer(waveformInitialState, action)).toEqual({
        ...waveformInitialState,
        shouldShowTimeUncertainty
      });

      expect(waveformSlice.actions.setShouldShowTimeUncertainty(shouldShowTimeUncertainty)).toEqual(
        action
      );

      store.dispatch(waveformSlice.actions.setShouldShowTimeUncertainty(shouldShowTimeUncertainty));
      const actions = store.getActions();
      expect(actions).toEqual([action]);
    });

    it('should increment loading total', () => {
      const action: Redux.AnyAction = {
        type: waveformSlice.actions.incrementLoadingTotal.type
      };
      const expectedState: WaveformState = {
        ...waveformInitialState,
        loadingState: {
          ...waveformInitialState.loadingState,
          total: 1,
          isLoading: true
        }
      };
      expect(waveformSlice.reducer(waveformInitialState, action)).toEqual(expectedState);
    });

    it('should increment completed total', () => {
      const action: Redux.AnyAction = {
        type: waveformSlice.actions.incrementLoadingCompleted.type
      };

      expect(waveformSlice.reducer(waveformInitialState, action)).toEqual(waveformInitialState);

      const expectedState: WaveformState = {
        ...waveformInitialState,
        loadingState: {
          ...waveformInitialState.loadingState,
          completed: 1,
          total: 1,
          percent: 1,
          isLoading: false
        }
      };
      expect(
        waveformSlice.reducer(
          {
            ...waveformInitialState,
            loadingState: { ...waveformInitialState.loadingState, total: 1 }
          },
          action
        )
      ).toEqual(expectedState);
    });

    it('should reset loading status', () => {
      const action: Redux.AnyAction = {
        type: waveformSlice.actions.resetLoading.type
      };
      expect(waveformSlice.reducer(waveformInitialState, action)).toEqual(waveformInitialState);
    });
  });
});
