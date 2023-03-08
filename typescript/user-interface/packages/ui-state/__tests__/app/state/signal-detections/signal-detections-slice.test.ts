import { createAction } from '@reduxjs/toolkit';
import type * as Redux from 'redux';

import {
  signalDetectionsInitialState,
  signalDetectionsSlice
} from '../../../../src/ts/app/state/signal-detections/signal-detections-slice';
import type { SignalDetectionsState } from '../../../../src/ts/app/state/signal-detections/types';

describe('state signal detections slice', () => {
  it('is defined', () => {
    expect(signalDetectionsInitialState).toBeDefined();
    expect(signalDetectionsSlice).toBeDefined();
  });

  it('should return the initial state', () => {
    expect(signalDetectionsSlice.reducer(undefined, createAction(undefined))).toMatchSnapshot();
    expect(signalDetectionsSlice.reducer(undefined, createAction(''))).toMatchSnapshot();
    expect(
      signalDetectionsSlice.reducer(signalDetectionsInitialState, createAction(undefined))
    ).toMatchSnapshot();
    expect(
      signalDetectionsSlice.reducer(signalDetectionsInitialState, createAction(''))
    ).toMatchSnapshot();
  });

  it('should update displayed signal detections configuration', () => {
    const action: Redux.AnyAction = {
      type: signalDetectionsSlice.actions.updateDisplayedSignalDetectionConfiguration.type,
      payload: {
        displayedSignalDetectionConfiguration: {
          ...signalDetectionsInitialState.displayedSignalDetectionConfiguration,
          syncWaveform: true
        }
      }
    };
    const expectedState: SignalDetectionsState = {
      ...signalDetectionsInitialState,
      displayedSignalDetectionConfiguration: action.payload
    };
    expect(signalDetectionsSlice.reducer(signalDetectionsInitialState, action)).toEqual(
      expectedState
    );
  });
  it('should update signal detections columns', () => {
    const action: Redux.AnyAction = {
      type: signalDetectionsSlice.actions.updateSignalDetectionColumns.type,
      payload: {
        signalDetectionsColumns: {
          ...signalDetectionsInitialState.signalDetectionsColumns,
          unsavedChanges: false
        }
      }
    };
    const expectedState: SignalDetectionsState = {
      ...signalDetectionsInitialState,
      signalDetectionsColumns: action.payload
    };
    expect(signalDetectionsSlice.reducer(signalDetectionsInitialState, action)).toEqual(
      expectedState
    );
  });
});
