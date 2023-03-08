import type { CommonTypes, WorkflowTypes } from '@gms/common-model';
import { UILogger } from '@gms/ui-util';
import includes from 'lodash/includes';
import isEqual from 'lodash/isEqual';
import { batch } from 'react-redux';

import { cancelWorkerRequests } from '../../../workers/api/cancel-worker-requests';
import { clearWaveforms } from '../../../workers/api/clear-waveforms';
import { dataSlice } from '../../api/data/data-slice';
import { eventManagerApiSlice } from '../../api/event-manager/event-manager-api-slice';
import type { AppDispatch, AppState } from '../../store';
import { AnalystWorkspaceOperations } from '../analyst';
import { analystSlice } from '../analyst/analyst-slice';
import { WaveformDisplayMode, WaveformSortType } from '../analyst/types';
import { commonActions } from '../common/common-slice';
import { AnalystWaveformOperations, waveformSlice } from '../waveform';
import { workflowSlice } from './workflow-slice';

const logger = UILogger.create(
  'GMS_LOG_WORKFLOW_REDUX_OPERATIONS',
  process.env.GMS_LOG_WORKFLOW_REDUX_OPERATIONS
);

/**
 * ! Always wrap this in a batch function to prevent render thrashing.
 * This is not calling batch internally to avoid nested batch calls, which causes double rendering.
 */
const resetToDefaultState = (dispatch: AppDispatch) => {
  batch(() => {
    dispatch(
      workflowSlice.actions.setTimeRange({ startTimeSecs: undefined, endTimeSecs: undefined })
    );
    dispatch(analystSlice.actions.setEffectiveNowTime());
    dispatch(analystSlice.actions.setSelectedFilterList(null));
    dispatch(analystSlice.actions.setSelectedSdIds([]));
    dispatch(analystSlice.actions.setOpenEventId(undefined));
    dispatch(analystSlice.actions.setSelectedEventIds([]));
    dispatch(analystSlice.actions.setSdIdsToShowFk([]));
    dispatch(analystSlice.actions.setMode(WaveformDisplayMode.DEFAULT));
    dispatch(analystSlice.actions.setMeasurementModeEntries({}));
    dispatch(analystSlice.actions.setSelectedSortType(WaveformSortType.stationNameAZ));
    dispatch(workflowSlice.actions.setStationGroup(undefined));
    dispatch(workflowSlice.actions.setOpenIntervalName(undefined));
    dispatch(workflowSlice.actions.setOpenActivityNames([]));
    dispatch(workflowSlice.actions.setAnalysisMode(undefined));
    dispatch(commonActions.setSelectedStationIds([]));

    AnalystWaveformOperations.resetStationsVisibility(dispatch);
    AnalystWaveformOperations.resetWaveformIntervals(dispatch);

    // clear out cached data
    dispatch(eventManagerApiSlice.util.resetApiState());
    dispatch(waveformSlice.actions.resetLoading());
    dispatch(dataSlice.actions.clearAll());
    clearWaveforms().catch(e => {
      logger.error('Failure cleaning up the WaveformStore', e);
      throw e;
    });
    cancelWorkerRequests().catch(e => {
      logger.error('Failure cancel worker request', e);
      throw e;
    });
  });
};

export const setOpenInterval = (
  timeRange: CommonTypes.TimeRange,
  stationGroup: WorkflowTypes.StationGroup,
  openIntervalName: string,
  openActivityNames: string[],
  analysisMode: WorkflowTypes.AnalysisMode
) => (dispatch: AppDispatch, getState: () => AppState): void => {
  let openedNewActivity = false;
  const currentOpenActivityNames = getState().app.workflow.openActivityNames;
  const hasCurrentIntervalChanged =
    getState().app.workflow.openIntervalName !== openIntervalName ||
    (getState().app.workflow.timeRange && !isEqual(getState().app.workflow.timeRange, timeRange));
  openActivityNames.forEach(openActivityName => {
    if (!includes(currentOpenActivityNames, openActivityName)) openedNewActivity = true;
  });
  batch(async () => {
    // clear out the following
    // if the processing stage interval id (or time interval) has changed
    if (hasCurrentIntervalChanged) {
      resetToDefaultState(dispatch);
    }
    if (openedNewActivity) {
      dispatch(analystSlice.actions.setSelectedFilterList(null));
    }
    dispatch(workflowSlice.actions.setTimeRange(timeRange));
    dispatch(workflowSlice.actions.setStationGroup(stationGroup));
    dispatch(workflowSlice.actions.setOpenIntervalName(openIntervalName));
    dispatch(workflowSlice.actions.setOpenActivityNames(openActivityNames));
    dispatch(workflowSlice.actions.setAnalysisMode(analysisMode));
    if (stationGroup && timeRange.startTimeSecs) {
      await dispatch(
        AnalystWaveformOperations.initializeStationVisibility(stationGroup, timeRange.startTimeSecs)
      );
    }
    AnalystWaveformOperations.initializeWaveformIntervals()(dispatch, getState);
    AnalystWorkspaceOperations.setPreferredFilterList()(dispatch, getState);
    await AnalystWorkspaceOperations.setDefaultFilter()(dispatch, getState);
  });
};

export const setClosedInterval = (activityName: string, isStageInterval: boolean) => (
  dispatch: AppDispatch,
  getState: () => AppState
): void => {
  batch(() => {
    // Only want to clear the state if they do not have multiple activities open
    if (getState().app.workflow.openActivityNames.length <= 1 || isStageInterval) {
      resetToDefaultState(dispatch);
    } else {
      const ids = getState().app.workflow.openActivityNames.filter(name => name !== activityName);
      dispatch(analystSlice.actions.setSelectedFilterList(null));
      dispatch(workflowSlice.actions.setOpenActivityNames(ids));
      AnalystWorkspaceOperations.setPreferredFilterList()(dispatch, getState);
    }
  });
};
