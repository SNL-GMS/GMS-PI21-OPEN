import type { CommonTypes, WorkflowTypes } from '@gms/common-model';
import type { PayloadAction } from '@reduxjs/toolkit';
import { createSlice } from '@reduxjs/toolkit';

import type { WorkflowState } from './types';

/**
 * The initial state for the workflow state.
 */
export const workflowInitialState: WorkflowState = {
  timeRange: {
    startTimeSecs: null,
    endTimeSecs: null
  },
  stationGroup: null,
  openIntervalName: null,
  openActivityNames: [],
  analysisMode: null
};

/**
 * The workflow reducer slice.
 */
export const workflowSlice = createSlice({
  name: 'workflow',
  initialState: workflowInitialState,
  reducers: {
    /**
     * Sets time range of the workflow state
     *
     * @param state the state
     * @param action the action
     */
    setTimeRange(state, action: PayloadAction<CommonTypes.TimeRange>) {
      state.timeRange = action.payload;
    },

    /**
     * Sets station group of the workflow state
     *
     * @param state the state
     * @param action the action
     */
    setStationGroup(state, action: PayloadAction<WorkflowTypes.StationGroup>) {
      state.stationGroup = action.payload;
    },

    /**
     * Sets the open interval name of the workflow state
     *
     * @param state the state
     * @param action the action
     */
    setOpenIntervalName(state, action: PayloadAction<string>) {
      state.openIntervalName = action.payload;
    },

    /**
     * Sets the open activity names of the workflow state
     *
     * @param state the state
     * @param action the action
     */
    setOpenActivityNames(state, action: PayloadAction<string[]>) {
      state.openActivityNames = action.payload;
    },

    /**
     * Sets the open activity names of the workflow state
     *
     * @param state the state
     * @param action the action
     */
    setAnalysisMode(state, action: PayloadAction<WorkflowTypes.AnalysisMode>) {
      state.analysisMode = action.payload;
    }
  }
});

/**
 * The workflow actions.
 */
export const workflowActions = workflowSlice.actions;
