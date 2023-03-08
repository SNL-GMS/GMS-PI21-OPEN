import { createAction } from '@reduxjs/toolkit';
import type * as Redux from 'redux';

import { mapInitialState, mapSlice } from '../../../../src/ts/app/state/map/map-slice';
import type { MapState } from '../../../../src/ts/app/state/map/types';

describe('state user session slice', () => {
  it('defined', () => {
    expect(mapInitialState).toBeDefined();
    expect(mapSlice).toBeDefined();
  });

  it('should return the initial state', () => {
    expect(mapSlice.reducer(undefined, createAction(undefined))).toMatchSnapshot();
    expect(mapSlice.reducer(undefined, createAction(''))).toMatchSnapshot();
    expect(mapSlice.reducer(mapInitialState, createAction(undefined))).toMatchSnapshot();
    expect(mapSlice.reducer(mapInitialState, createAction(''))).toMatchSnapshot();
  });

  it('should setIsMapSyncedWithWaveformZoom', () => {
    const action: Redux.AnyAction = {
      type: mapSlice.actions.setIsMapSyncedWithWaveformZoom.type,
      payload: true
    };
    const expectedState: MapState = {
      ...mapInitialState,
      isSyncedWithWaveformZoom: action.payload
    };
    expect(mapSlice.reducer(mapInitialState, action)).toEqual(expectedState);
  });
});
