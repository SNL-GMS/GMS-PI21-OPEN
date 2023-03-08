/* eslint-disable @typescript-eslint/dot-notation */
/* eslint-disable dot-notation */
import { createAction } from '@reduxjs/toolkit';
import type * as Redux from 'redux';

import { dataInitialState, dataSlice } from '../../../../src/ts/app/api/data/data-slice';
import type * as Types from '../../../../src/ts/app/api/data/types';
import { signalDetectionsData, uiChannelSegment } from '../../../__data__/signal-detections-data';

const clearWaveformsFunc = jest.fn();

jest.mock('../../../../src/ts/workers/api/clear-waveforms', () => {
  return {
    clearWaveforms: async (): Promise<void> => {
      await clearWaveformsFunc();
    }
  };
});

describe('data slice', () => {
  it('defined', () => {
    expect(dataInitialState).toBeDefined();
    expect(dataSlice).toBeDefined();
  });

  it('should return the initial state', () => {
    expect(dataSlice.reducer(undefined, createAction(undefined))).toMatchSnapshot();
    expect(dataSlice.reducer(undefined, createAction(''))).toMatchSnapshot();
    expect(dataSlice.reducer(dataInitialState, createAction(undefined))).toMatchSnapshot();
    expect(dataSlice.reducer(dataInitialState, createAction(''))).toMatchSnapshot();
  });

  it('should add signal detections', () => {
    const action: Redux.AnyAction = {
      type: dataSlice.actions.addSignalDetections.type,
      payload: [signalDetectionsData[0]]
    };
    const signalDetections = {};
    // eslint-disable-next-line prefer-destructuring
    signalDetections[signalDetectionsData[0].id] = signalDetectionsData[0];
    const expectedState: Types.DataState = {
      ...dataInitialState,
      signalDetections
    };
    expect(dataSlice.reducer(dataInitialState, action)).toEqual(expectedState);
  });

  it('should clear signal detections', () => {
    const action: Redux.AnyAction = {
      type: dataSlice.actions.clearSignalDetectionsAndHistory.type
    };
    const expectedState: Types.DataState = {
      ...dataInitialState
    };
    expect(dataSlice.reducer(dataInitialState, action)).toEqual(expectedState);
  });

  it('should add channel segments', () => {
    const data = [
      {
        name: 'sample',
        startTimeSecs: 400,
        endTimeSecs: 500,
        channelSegments: [uiChannelSegment]
      }
    ];

    const action: Redux.AnyAction = {
      type: dataSlice.actions.addChannelSegments.type,
      payload: data
    };
    expect(dataSlice.reducer(dataInitialState, action)).toMatchSnapshot();
  });

  it('should clear channel segments', () => {
    clearWaveformsFunc.mockClear();
    const action: Redux.AnyAction = {
      type: dataSlice.actions.clearChannelSegmentsAndHistory.type
    };
    const expectedState: Types.DataState = {
      ...dataInitialState
    };
    expect(dataSlice.reducer(dataInitialState, action)).toEqual(expectedState);
    expect(clearWaveformsFunc).toHaveBeenCalledTimes(1);
  });

  it('should add events', () => {
    const data: any[] = [
      {
        id: 'eventID'
      }
    ];

    const action: Redux.AnyAction = {
      type: dataSlice.actions.addEvents.type,
      payload: data
    };
    expect(dataSlice.reducer(dataInitialState, action)).toMatchSnapshot();
  });

  it('should clear events', () => {
    const action: Redux.AnyAction = {
      type: dataSlice.actions.clearEventsAndHistory.type
    };
    const expectedState: Types.DataState = {
      ...dataInitialState
    };
    expect(dataSlice.reducer(dataInitialState, action)).toEqual(expectedState);
  });

  it('should clear everything', () => {
    clearWaveformsFunc.mockClear();
    const action: Redux.AnyAction = {
      type: dataSlice.actions.clearAll.type
    };

    const expectedState: Types.DataState = {
      ...dataInitialState
    };
    expect(dataSlice.reducer(dataInitialState, action)).toEqual(expectedState);
    expect(clearWaveformsFunc).toHaveBeenCalledTimes(1);
  });
});
