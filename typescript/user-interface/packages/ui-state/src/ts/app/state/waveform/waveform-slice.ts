import type { CommonTypes } from '@gms/common-model';
import type { PayloadAction } from '@reduxjs/toolkit';
import { createSlice } from '@reduxjs/toolkit';

import type {
  StationVisibilityChangesDictionary,
  WaveformLoadingState,
  WaveformState
} from './types';

// The default and initial waveform loading state
export const DEFAULT_INITIAL_WAVEFORM_LOADING_STATE: WaveformLoadingState = {
  isLoading: false,
  total: 0,
  completed: 0,
  percent: 0,
  description: 'Loading waveforms'
};

/**
 * the initial state for the waveform slice
 */
export const waveformInitialState: WaveformState = {
  loadingState: DEFAULT_INITIAL_WAVEFORM_LOADING_STATE,
  stationsVisibility: {},
  shouldShowTimeUncertainty: false,
  shouldShowPredictedPhases: true,
  zoomInterval: null,
  viewableInterval: null,
  minimumOffset: 0,
  maximumOffset: 0,
  baseStationTime: 0
};

/**
 * the waveform reducer slice
 */
export const waveformSlice = createSlice({
  name: 'waveform',
  initialState: waveformInitialState,
  reducers: {
    /**
     * Sets the stationsVisibility which tracks the changes to the default visibility
     */
    setStationsVisibility(state, action: PayloadAction<StationVisibilityChangesDictionary>) {
      state.stationsVisibility = action.payload;
    },
    /**
     * Sets the viewable interval, which is the interval of time that can be viewed in the waveform display
     * without loading more data from the server. This interval changes if the user pans out of the previous
     * viewable interval.
     */
    setViewableInterval(state, action: PayloadAction<CommonTypes.TimeRange>) {
      state.viewableInterval = action.payload;
    },
    /**
     * Sets the zoom interval, which is the interval of time currently displayed on the waveform display. This
     * amount of time is changed when the user zooms in or out, and when they pan.
     */
    setZoomInterval(state, action: PayloadAction<CommonTypes.TimeRange>) {
      state.zoomInterval = action.payload;
    },
    /**
     * Sets the minimum offset when not aligned by time
     */
    setMinimumOffset(state, action: PayloadAction<number>) {
      state.minimumOffset = action.payload;
    },
    /**
     * Sets the maximum offset when not aligned by time
     */
    setMaximumOffset(state, action: PayloadAction<number>) {
      state.maximumOffset = action.payload;
    },
    /**
     * Sets the base station time for offset calculations when not aligned by time
     */
    setBaseStationTime(state, action: PayloadAction<number>) {
      state.baseStationTime = action.payload;
    },
    /**
     * Sets the waveform client loading state, which is used to update the waveform loading spinner.
     */
    setWaveformClientLoadingState(state, action: PayloadAction<WaveformLoadingState>) {
      state.loadingState = action.payload;
    },
    /**
     * Sets whether or not to show the time uncertainty
     */
    setShouldShowTimeUncertainty(state, action: PayloadAction<boolean>) {
      state.shouldShowTimeUncertainty = action.payload;
    },

    /**
     * Sets whether or not to show the predicted phases
     */
    setShouldShowPredictedPhases(state, action: PayloadAction<boolean>) {
      state.shouldShowPredictedPhases = action.payload;
    },

    /**
     * Adds a new entry to the waveform loading state (the total).
     * Increments the waveform loading state total.
     * Updates the percent completed and is loading state as well;
     */
    incrementLoadingTotal(state) {
      state.loadingState.total += 1;
      state.loadingState.percent = state.loadingState.completed / state.loadingState.total;
      state.loadingState.isLoading = state.loadingState.completed !== state.loadingState.total;
    },

    /**
     * Marks the waveform client loading state
     * Increments the waveform loading state completed.
     * Updates the percent completed and is loading state as well;
     */
    incrementLoadingCompleted(state) {
      if (state.loadingState.total > state.loadingState.completed) {
        state.loadingState.completed += 1;
        state.loadingState.percent = state.loadingState.completed / state.loadingState.total;
      }
      state.loadingState.isLoading = state.loadingState.completed !== state.loadingState.total;
    },

    /**
     * Resets the waveform loading state
     */
    resetLoading(state) {
      state.loadingState = DEFAULT_INITIAL_WAVEFORM_LOADING_STATE;
    }
  }
});

/**
 * The waveform Redux actions from the waveform slice.
 */
export const waveformActions = waveformSlice.actions;
