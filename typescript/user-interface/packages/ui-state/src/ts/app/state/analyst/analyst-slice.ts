/* eslint-disable @typescript-eslint/no-magic-numbers */
import type { WaveformTypes } from '@gms/common-model';
import { CommonTypes } from '@gms/common-model';
import { MILLISECONDS_IN_SECOND } from '@gms/common-util';
import type { PayloadAction } from '@reduxjs/toolkit';
import { createSlice } from '@reduxjs/toolkit';

import type { AnalystState } from './types';
import { AlignWaveformsOn, WaveformDisplayMode, WaveformSortType } from './types';

/**
 * The initial state for the analyst state.
 */
export const analystInitialState: AnalystState = {
  channelFilters: {},
  defaultSignalDetectionPhase: CommonTypes.PhaseType.P,
  effectiveNowTime: null,
  historyActionInProgress: 0,
  hotkeyCycleOverrides: {},
  location: {
    selectedLocationSolutionId: null,
    selectedLocationSolutionSetId: null,
    selectedPreferredLocationSolutionId: null,
    selectedPreferredLocationSolutionSetId: null
  },
  measurementMode: {
    entries: {},
    mode: WaveformDisplayMode.DEFAULT
  },
  openEventId: null,
  openLayoutName: null,
  sdIdsToShowFk: [],
  selectedEventIds: [],
  selectedFilterList: null,
  selectedFilterIndex: null,
  selectedSdIds: [],
  selectedSortType: WaveformSortType.stationNameAZ,
  eventListOpenEventTriggered: false,
  mapOpenEventTriggered: false,
  alignWaveformsOn: AlignWaveformsOn.TIME,
  phaseToAlignOn: null
};

/**
 * The analyst reducer slice.
 */
export const analystSlice = createSlice({
  name: 'analyst',
  initialState: analystInitialState,
  reducers: {
    /**
     * Decrements the history action is in progress
     *
     * @param state the state
     * @param action the action
     */
    decrementHistoryAction(state) {
      state.historyActionInProgress -= 1;
    },

    /**
     * Increments the history action is in progress
     *
     * @param state the state
     * @param action the action
     */
    incrementHistoryAction(state) {
      state.historyActionInProgress += 1;
    },

    /**
     * Sets the channel filters.
     * (selected waveform filter for a give channel id)
     *
     * @param state the state
     * @param action the action
     */
    setChannelFilters(state, action: PayloadAction<Record<string, WaveformTypes.WaveformFilter>>) {
      state.channelFilters = action.payload;
    },

    /**
     * Sets the signal detection default phase.
     * The selected phase type that will be used for the creation of
     * a new a signal detection.
     *
     * @param state the state
     * @param action the action
     */
    setDefaultSignalDetectionPhase(state, action: PayloadAction<CommonTypes.PhaseType>) {
      state.defaultSignalDetectionPhase = action.payload;
    },

    /**
     * Sets the effective now time. Sets it to Date.now if argument is not provided
     *
     * @param state the state
     * @param action the action
     */
    setEffectiveNowTime(state, action?: PayloadAction<number>) {
      state.effectiveNowTime = action.payload
        ? action.payload
        : Date.now() / MILLISECONDS_IN_SECOND;
    },

    /**
     * Set the current filter list which defines the filters available.
     */
    setSelectedFilterList(state, action: PayloadAction<string>) {
      state.selectedFilterList = action.payload;
    },

    /**
     * Set the current filter list which defines the filters available
     */
    setSelectedFilterIndex(state, action: PayloadAction<number | null>) {
      state.selectedFilterIndex = action.payload;
    },

    /**
     * Sets a single override to the default hotkey state for the filter at the provided index
     */
    setIsFilterWithinHotkeyCycle(
      state,
      action: PayloadAction<{ index: number; isWithinCycle: boolean }>
    ) {
      if (!state.selectedFilterList) {
        throw new Error('cannot override hotkey cycle with no filter list selected');
      }
      if (!state.hotkeyCycleOverrides[state.selectedFilterList]) {
        state.hotkeyCycleOverrides[state.selectedFilterList] = {};
      }
      state.hotkeyCycleOverrides[state.selectedFilterList][action.payload.index] =
        action.payload.isWithinCycle;
    },

    /**
     * Sets overrides to the default hotkey state, such that each key is an index, and the boolean
     * value is the user supplied override.
     */
    setFilterHotkeyCycleOverridesForCurrentFilterList(
      state,
      action: PayloadAction<Record<number, boolean>>
    ) {
      state.hotkeyCycleOverrides[state.selectedFilterList] = action.payload;
    },

    /**
     * Sets the measurement mode entries.
     * Record (dictionary) of signal detection ids to boolean value indicating if the
     * amplitude measurement should be displayed (visible).
     *
     * @param state the state
     * @param action the action
     */
    setMeasurementModeEntries(state, action: PayloadAction<Record<string, boolean>>) {
      state.measurementMode.entries = action.payload;
    },

    /**
     * Sets the waveform display mode
     *
     * @param state the state
     * @param action the action
     */
    setMode(state, action: PayloadAction<WaveformDisplayMode>) {
      state.measurementMode.mode = action.payload;
    },

    /**
     * Sets the open event id.
     *
     * @param state the state
     * @param action the action
     */
    setOpenEventId(state, action: PayloadAction<string>) {
      state.openEventId = action.payload;
    },

    /**
     * Sets the open layout name.
     *
     * @param state the state
     * @param action the action
     */
    setOpenLayoutName(state, action: PayloadAction<string>) {
      state.openLayoutName = action.payload;
    },

    /**
     * Sets the signal detection ids that have been marked to show FK.
     *
     * @param state the state
     * @param action the action
     */
    setSdIdsToShowFk(state, action: PayloadAction<string[]>) {
      state.sdIdsToShowFk = action.payload;
    },

    /**
     * Sets the selected event ids.
     *
     * @param state the state
     * @param action the action
     */
    setSelectedEventIds(state, action: PayloadAction<string[]>) {
      state.selectedEventIds = action.payload;
    },

    /**
     * Sets flag indicating an analyst attempted to open an event from the event list
     *
     * @param state the state
     * @param action the action
     */
    setEventListOpenTriggered(state, action: PayloadAction<boolean>) {
      state.eventListOpenEventTriggered = action.payload;
    },

    /**
     * Sets flag indicating an analyst attempted to open an event from the map
     *
     * @param state the state
     * @param action the action
     */
    setMapOpenTriggered(state, action: PayloadAction<boolean>) {
      state.mapOpenEventTriggered = action.payload;
    },

    /**
     * Sets the selected location solution id.
     *
     * @param state the state
     * @param action the action
     */
    setSelectedLocationSolutionId(state, action: PayloadAction<string>) {
      state.location.selectedLocationSolutionId = action.payload;
    },

    /**
     * Sets the selected location solution set id.
     *
     * @param state the state
     * @param action the action
     */
    setSelectedLocationSolutionSetId(state, action: PayloadAction<string>) {
      state.location.selectedLocationSolutionSetId = action.payload;
    },

    /**
     * Sets the  selected preferred location solution id.
     *
     * @param state the state
     * @param action the action
     */
    setSelectedPreferredLocationSolutionId(state, action: PayloadAction<string>) {
      state.location.selectedPreferredLocationSolutionId = action.payload;
    },

    /**
     * Sets the selected preferred location solution set id.
     *
     * @param state the state
     * @param action the action
     */
    setSelectedPreferredLocationSolutionSetId(state, action: PayloadAction<string>) {
      state.location.selectedPreferredLocationSolutionSetId = action.payload;
    },

    /**
     * Sets the selected signal detection ids.
     *
     * @param state the state
     * @param action the action
     */
    setSelectedSdIds(state, action: PayloadAction<string[]>) {
      state.selectedSdIds = action.payload;
    },

    /**
     * Sets the selected sort type.
     *
     * @param state the state
     * @param action the action
     */
    setSelectedSortType(state, action: PayloadAction<WaveformSortType>) {
      state.selectedSortType = action.payload;
    },
    /**
     * Sets the selected alignment.
     *
     * @param state the state
     * @param action the action
     */
    setAlignWaveformsOn(state, action: PayloadAction<AlignWaveformsOn>) {
      state.alignWaveformsOn = action.payload;
    },
    /**
     * Sets the selected sort type.
     *
     * @param state the state
     * @param action the action
     */
    setPhaseToAlignOn(state, action: PayloadAction<CommonTypes.PhaseType>) {
      state.phaseToAlignOn = action.payload;
    }
  }
});

/**
 * The analyst actions.
 */
export const analystActions = analystSlice.actions;
