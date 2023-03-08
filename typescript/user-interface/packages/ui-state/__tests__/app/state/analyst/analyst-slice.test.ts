import { CommonTypes } from '@gms/common-model';
import { MILLISECONDS_IN_SECOND } from '@gms/common-util';
import { createAction } from '@reduxjs/toolkit';
import type * as Redux from 'redux';

import {
  analystInitialState,
  analystSlice
} from '../../../../src/ts/app/state/analyst/analyst-slice';
import type { AnalystState } from '../../../../src/ts/app/state/analyst/types';
import { WaveformDisplayMode, WaveformSortType } from '../../../../src/ts/app/state/analyst/types';

const MOCK_TIME = 1606818240000;
Date.now = jest.fn(() => MOCK_TIME);
Date.constructor = jest.fn(() => new Date(MOCK_TIME));

describe('state user session slice', () => {
  it('defined', () => {
    expect(analystInitialState).toBeDefined();
    expect(analystSlice).toBeDefined();
  });

  it('should return the initial state', () => {
    expect(analystSlice.reducer(undefined, createAction(undefined))).toMatchSnapshot();
    expect(analystSlice.reducer(undefined, createAction(''))).toMatchSnapshot();
    expect(analystSlice.reducer(analystInitialState, createAction(undefined))).toMatchSnapshot();
    expect(analystSlice.reducer(analystInitialState, createAction(''))).toMatchSnapshot();
  });

  it('should decrement history', () => {
    const action: Redux.AnyAction = {
      type: analystSlice.actions.decrementHistoryAction.type
    };
    const expectedState: AnalystState = {
      ...analystInitialState,
      historyActionInProgress: -1
    };
    expect(analystSlice.reducer(analystInitialState, action)).toEqual(expectedState);
  });

  it('should increment history', () => {
    const action: Redux.AnyAction = {
      type: analystSlice.actions.incrementHistoryAction.type
    };
    const expectedState: AnalystState = {
      ...analystInitialState,
      historyActionInProgress: 1
    };
    expect(analystSlice.reducer(analystInitialState, action)).toEqual(expectedState);
  });

  it('should setChannelFilters', () => {
    const action: Redux.AnyAction = {
      type: analystSlice.actions.setChannelFilters.type,
      payload: {
        chan: {
          description: 'my description',
          name: 'chan',
          filterCausality: undefined,
          filterPassBandType: undefined,
          filterSource: undefined,
          filterType: undefined,
          groupDelaySecs: undefined,
          highFrequencyHz: undefined,
          id: '12345',
          lowFrequencyHz: 3,
          order: 3,
          sampleRate: 40,
          sampleRateTolerance: 10,
          validForSampleRate: true,
          zeroPhase: undefined,
          aCoefficients: [1, 2, 3],
          bCoefficients: [1, 2, 3]
        }
      }
    };
    const expectedState: AnalystState = {
      ...analystInitialState,
      channelFilters: action.payload
    };
    expect(analystSlice.reducer(analystInitialState, action)).toEqual(expectedState);
  });

  it('should setDefaultSignalDetectionPhase', () => {
    const action: Redux.AnyAction = {
      type: analystSlice.actions.setDefaultSignalDetectionPhase.type,
      payload: CommonTypes.PhaseType.Lg
    };
    const expectedState: AnalystState = {
      ...analystInitialState,
      defaultSignalDetectionPhase: action.payload
    };
    expect(analystSlice.reducer(analystInitialState, action)).toEqual(expectedState);
  });

  it('should setEffectiveNowTime', () => {
    const action: Redux.AnyAction = {
      type: analystSlice.actions.setEffectiveNowTime.type,
      payload: 1234
    };
    const expectedState: AnalystState = {
      ...analystInitialState,
      effectiveNowTime: action.payload
    };
    expect(analystSlice.reducer(analystInitialState, action)).toEqual(expectedState);

    const action2: Redux.AnyAction = {
      type: analystSlice.actions.setEffectiveNowTime.type
    };
    const expectedState2: AnalystState = {
      ...analystInitialState,
      effectiveNowTime: MOCK_TIME / MILLISECONDS_IN_SECOND
    };
    expect(analystSlice.reducer(analystInitialState, action2)).toEqual(expectedState2);
  });

  it('should setMeasurementModeEntries', () => {
    const action: Redux.AnyAction = {
      type: analystSlice.actions.setMeasurementModeEntries.type,
      payload: {
        test: true
      }
    };
    const expectedState: AnalystState = {
      ...analystInitialState,
      measurementMode: {
        entries: action.payload,
        mode: analystInitialState.measurementMode.mode
      }
    };
    expect(analystSlice.reducer(analystInitialState, action)).toEqual(expectedState);
  });

  it('should setMode', () => {
    const action: Redux.AnyAction = {
      type: analystSlice.actions.setMode.type,
      payload: WaveformDisplayMode.MEASUREMENT
    };
    const expectedState: AnalystState = {
      ...analystInitialState,
      measurementMode: {
        entries: analystInitialState.measurementMode.entries,
        mode: WaveformDisplayMode.MEASUREMENT
      }
    };
    expect(analystSlice.reducer(analystInitialState, action)).toEqual(expectedState);
  });

  it('should setOpenEventId', () => {
    const action: Redux.AnyAction = {
      type: analystSlice.actions.setOpenEventId.type,
      payload: 'event 1'
    };
    const expectedState: AnalystState = {
      ...analystInitialState,
      openEventId: action.payload
    };
    expect(analystSlice.reducer(analystInitialState, action)).toEqual(expectedState);
  });

  it('should setOpenLayoutName', () => {
    const action: Redux.AnyAction = {
      type: analystSlice.actions.setOpenLayoutName.type,
      payload: 'my layout'
    };
    const expectedState: AnalystState = {
      ...analystInitialState,
      openLayoutName: action.payload
    };
    expect(analystSlice.reducer(analystInitialState, action)).toEqual(expectedState);
  });

  it('should setSdIdsToShowFk', () => {
    const action: Redux.AnyAction = {
      type: analystSlice.actions.setSdIdsToShowFk.type,
      payload: ['12345']
    };
    const expectedState: AnalystState = {
      ...analystInitialState,
      sdIdsToShowFk: action.payload
    };
    expect(analystSlice.reducer(analystInitialState, action)).toEqual(expectedState);
  });

  it('should setSelectedEventIds', () => {
    const action: Redux.AnyAction = {
      type: analystSlice.actions.setSelectedEventIds.type,
      payload: ['12345']
    };
    const expectedState: AnalystState = {
      ...analystInitialState,
      selectedEventIds: action.payload
    };
    expect(analystSlice.reducer(analystInitialState, action)).toEqual(expectedState);
  });

  it('should setSelectedLocationSolutionId', () => {
    const action: Redux.AnyAction = {
      type: analystSlice.actions.setSelectedLocationSolutionId.type,
      payload: '12345'
    };
    const expectedState: AnalystState = {
      ...analystInitialState,
      location: {
        ...analystInitialState.location,
        selectedLocationSolutionId: action.payload
      }
    };
    expect(analystSlice.reducer(analystInitialState, action)).toEqual(expectedState);
  });

  it('should setSelectedLocationSolutionSetId', () => {
    const action: Redux.AnyAction = {
      type: analystSlice.actions.setSelectedLocationSolutionSetId.type,
      payload: '12345'
    };
    const expectedState: AnalystState = {
      ...analystInitialState,
      location: {
        ...analystInitialState.location,
        selectedLocationSolutionSetId: action.payload
      }
    };
    expect(analystSlice.reducer(analystInitialState, action)).toEqual(expectedState);
  });

  it('should setSelectedPreferredLocationSolutionId', () => {
    const action: Redux.AnyAction = {
      type: analystSlice.actions.setSelectedPreferredLocationSolutionId.type,
      payload: '12345'
    };
    const expectedState: AnalystState = {
      ...analystInitialState,
      location: {
        ...analystInitialState.location,
        selectedPreferredLocationSolutionId: action.payload
      }
    };
    expect(analystSlice.reducer(analystInitialState, action)).toEqual(expectedState);
  });

  it('should setSelectedPreferredLocationSolutionSetId', () => {
    const action: Redux.AnyAction = {
      type: analystSlice.actions.setSelectedPreferredLocationSolutionSetId.type,
      payload: '12345'
    };
    const expectedState: AnalystState = {
      ...analystInitialState,
      location: {
        ...analystInitialState.location,
        selectedPreferredLocationSolutionSetId: action.payload
      }
    };
    expect(analystSlice.reducer(analystInitialState, action)).toEqual(expectedState);
  });

  it('should setSelectedSdIds', () => {
    const action: Redux.AnyAction = {
      type: analystSlice.actions.setSelectedSdIds.type,
      payload: ['12345']
    };
    const expectedState: AnalystState = {
      ...analystInitialState,
      selectedSdIds: action.payload
    };
    expect(analystSlice.reducer(analystInitialState, action)).toEqual(expectedState);
  });

  it('should setSelectedSortType', () => {
    const action: Redux.AnyAction = {
      type: analystSlice.actions.setSelectedSortType.type,
      payload: WaveformSortType.distance
    };
    const expectedState: AnalystState = {
      ...analystInitialState,
      selectedSortType: action.payload
    };
    expect(analystSlice.reducer(analystInitialState, action)).toEqual(expectedState);
  });
  it('should setEventListOpenTriggered', () => {
    const action: Redux.AnyAction = {
      type: analystSlice.actions.setEventListOpenTriggered.type,
      payload: true
    };
    const expectedState: AnalystState = {
      ...analystInitialState,
      eventListOpenEventTriggered: action.payload
    };
    expect(analystSlice.reducer(analystInitialState, action)).toEqual(expectedState);
  });
  it('should setMapOpenTriggered', () => {
    const action: Redux.AnyAction = {
      type: analystSlice.actions.setMapOpenTriggered.type,
      payload: true
    };
    const expectedState: AnalystState = {
      ...analystInitialState,
      mapOpenEventTriggered: action.payload
    };
    expect(analystSlice.reducer(analystInitialState, action)).toEqual(expectedState);
  });
  it('should throw when calling setIsFilterWithinHotkeyCycle if there is no selectedFilterList set', () => {
    const action: Redux.AnyAction = {
      type: analystSlice.actions.setIsFilterWithinHotkeyCycle.type,
      payload: { index: 0, isWithinCycle: true }
    };
    expect(() => analystSlice.reducer(analystInitialState, action)).toThrow(
      'cannot override hotkey cycle with no filter list selected'
    );
  });
  it('should setIsFilterWithinHotkeyCycle if there is a selectedFilterList set', () => {
    const startingState = {
      ...analystInitialState,
      selectedFilterList: 'test'
    };
    const action: Redux.AnyAction = {
      type: analystSlice.actions.setIsFilterWithinHotkeyCycle.type,
      payload: { index: 0, isWithinCycle: true }
    };
    const expectedState: AnalystState = {
      ...startingState,
      hotkeyCycleOverrides: {
        test: {
          [action.payload.index]: action.payload.isWithinCycle
        }
      }
    };
    expect(analystSlice.reducer(startingState, action)).toEqual(expectedState);
  });
});
