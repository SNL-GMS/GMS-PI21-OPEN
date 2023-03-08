import type { PayloadAction } from '@reduxjs/toolkit';
import { createSlice } from '@reduxjs/toolkit';

import type {
  DisplayedSignalDetectionConfigurationEnum,
  SignalDetectionColumn,
  SignalDetectionsState
} from './types';

/**
 * The initial state for the signal detections panel.
 */
export const signalDetectionsInitialState: SignalDetectionsState = {
  displayedSignalDetectionConfiguration: {
    syncWaveform: false,
    signalDetectionBeforeInterval: true,
    signalDetectionAfterInterval: true,
    signalDetectionAssociatedToOpenEvent: true,
    signalDetectionAssociatedToCompletedEvent: true,
    signalDetectionAssociatedToOtherEvent: true,
    signalDetectionUnassociated: true
  },
  signalDetectionsColumns: {
    unsavedChanges: true,
    assocStatus: true,
    conflict: true,
    station: true,
    channel: true,
    phase: true,
    phaseConfidence: false,
    time: true,
    timeStandardDeviation: true,
    azimuth: true,
    azimuthStandardDeviation: true,
    slowness: true,
    slownessStandardDeviation: true,
    amplitude: true,
    period: true,
    sNR: true,
    rectilinearity: false,
    emergenceAngle: false,
    shortPeriodFirstMotion: false,
    longPeriodFirstMotion: false,
    rejected: true
  }
};

/**
 * The signal detections panel state reducer slice
 */
export const signalDetectionsSlice = createSlice({
  name: 'signalDetections',
  initialState: signalDetectionsInitialState,
  reducers: {
    /**
     * Sets the boolean that determines if the signal detections panel should sync visible signal detections to the waveform
     * panel's zoom interval
     */
    updateDisplayedSignalDetectionConfiguration(
      state,
      action: PayloadAction<Record<DisplayedSignalDetectionConfigurationEnum, boolean>>
    ) {
      state.displayedSignalDetectionConfiguration = action.payload;
    },
    /**
     * Sets the boolean that determines if a signal detections column should be displayed
     */
    updateSignalDetectionColumns: (
      state,
      action: PayloadAction<Record<SignalDetectionColumn, boolean>>
    ) => {
      state.signalDetectionsColumns = action.payload;
    }
  }
});
export const signalDetectionsActions = signalDetectionsSlice.actions;
