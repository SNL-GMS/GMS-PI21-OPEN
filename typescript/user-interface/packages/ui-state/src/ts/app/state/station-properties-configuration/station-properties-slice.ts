import type { PayloadAction } from '@reduxjs/toolkit';
import { createSlice } from '@reduxjs/toolkit';

import type { StationPropertiesConfigurationState } from './types';

export const stationPropertiesConfigurationInitialState: StationPropertiesConfigurationState = {
  channelConfigurationColumns: {
    name: true,
    effectiveAt: true,
    effectiveUntil: true,
    latitudeDegrees: true,
    longitudeDegrees: true,
    depthKm: true,
    elevationKm: true,
    nominalSampleRateHz: true,
    units: true,
    orientationHorizontalDegrees: true,
    orientationVerticalDegrees: true,
    calibrationFactor: true,
    calibrationPeriod: true,
    calibrationEffectiveAt: true,
    calibrationTimeShift: true,
    calibrationStandardDeviation: true,
    northDisplacementKm: true,
    eastDisplacementKm: true,
    verticalDisplacementKm: true,
    description: true,
    channelDataType: false,
    channelBandType: false,
    channelInstrumentType: false,
    channelOrientationCode: false,
    channelOrientationType: false,
    calibrationResponseId: true,
    fapResponseId: true
  },
  channelGroupConfigurationColumns: {
    name: true,
    effectiveAt: true,
    effectiveUntil: true,
    latitudeDegrees: true,
    longitudeDegrees: true,
    depthKm: true,
    elevationKm: true,
    type: true,
    description: true
  },
  selectedEffectiveAtIndex: 0
};

export const stationPropertiesConfigurationSlice = createSlice({
  name: 'stationPropertiesConfiguration',
  initialState: stationPropertiesConfigurationInitialState,
  reducers: {
    updateChannelConfigurationColumns: (state, action: PayloadAction<Record<any, boolean>>) => {
      state.channelConfigurationColumns = action.payload;
    },
    updateChannelGroupConfigurationColumns: (
      state,
      action: PayloadAction<Record<any, boolean>>
    ) => {
      state.channelGroupConfigurationColumns = action.payload;
    },
    setSelectedEffectiveAt: (state, action: PayloadAction<number>) => {
      state.selectedEffectiveAtIndex = action.payload;
    }
  }
});

export const stationPropertiesConfigurationActions = stationPropertiesConfigurationSlice.actions;
