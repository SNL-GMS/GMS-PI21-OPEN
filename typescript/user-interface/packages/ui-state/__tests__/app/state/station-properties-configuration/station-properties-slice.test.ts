import { createAction } from '@reduxjs/toolkit';
import type * as Redux from 'redux';

import {
  stationPropertiesConfigurationInitialState,
  stationPropertiesConfigurationSlice
} from '../../../../src/ts/app/state/station-properties-configuration/station-properties-slice';
import type { StationPropertiesConfigurationState } from '../../../../src/ts/app/state/station-properties-configuration/types';

describe('station properties configuration slice', () => {
  it('defined', () => {
    expect(stationPropertiesConfigurationInitialState).toBeDefined();
    expect(stationPropertiesConfigurationSlice).toBeDefined();
  });

  it('should return the initial state', () => {
    expect(
      stationPropertiesConfigurationSlice.reducer(undefined, createAction(undefined))
    ).toMatchSnapshot();
    expect(
      stationPropertiesConfigurationSlice.reducer(undefined, createAction(''))
    ).toMatchSnapshot();
    expect(
      stationPropertiesConfigurationSlice.reducer(
        stationPropertiesConfigurationInitialState,
        createAction(undefined)
      )
    ).toMatchSnapshot();
    expect(
      stationPropertiesConfigurationSlice.reducer(
        stationPropertiesConfigurationInitialState,
        createAction('')
      )
    ).toMatchSnapshot();
  });

  it('should update channel configuration columns', () => {
    const action: Redux.AnyAction = {
      type: stationPropertiesConfigurationSlice.actions.updateChannelConfigurationColumns.type,
      payload: {
        channelConfigurationColumns: {
          ...stationPropertiesConfigurationInitialState.channelConfigurationColumns,
          name: false
        }
      }
    };
    const expectedState: StationPropertiesConfigurationState = {
      ...stationPropertiesConfigurationInitialState,
      channelConfigurationColumns: action.payload
    };
    expect(
      stationPropertiesConfigurationSlice.reducer(
        stationPropertiesConfigurationInitialState,
        action
      )
    ).toEqual(expectedState);
  });

  it('should update channel group configuration columns', () => {
    const action: Redux.AnyAction = {
      type: stationPropertiesConfigurationSlice.actions.updateChannelGroupConfigurationColumns.type,
      payload: {
        channelGroupConfigurationColumns: {
          ...stationPropertiesConfigurationInitialState.channelGroupConfigurationColumns,
          name: false
        }
      }
    };
    const expectedState: StationPropertiesConfigurationState = {
      ...stationPropertiesConfigurationInitialState,
      channelGroupConfigurationColumns: action.payload
    };
    expect(
      stationPropertiesConfigurationSlice.reducer(
        stationPropertiesConfigurationInitialState,
        action
      )
    ).toEqual(expectedState);
  });
  it('should set the selected effective at', () => {
    const action: Redux.AnyAction = {
      type: stationPropertiesConfigurationSlice.actions.setSelectedEffectiveAt.type,
      payload: 1
    };
    const expectedState: StationPropertiesConfigurationState = {
      ...stationPropertiesConfigurationInitialState,
      selectedEffectiveAtIndex: action.payload
    };
    expect(
      stationPropertiesConfigurationSlice.reducer(
        stationPropertiesConfigurationInitialState,
        action
      )
    ).toEqual(expectedState);
  });
});
