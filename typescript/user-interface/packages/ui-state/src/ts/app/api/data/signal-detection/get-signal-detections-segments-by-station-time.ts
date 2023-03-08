import type { CommonTypes } from '@gms/common-model';
import { UILogger } from '@gms/ui-util';
import type { ActionReducerMapBuilder } from '@reduxjs/toolkit';
import { createAsyncThunk } from '@reduxjs/toolkit';
import { produce } from 'immer';

import { fetchSignalDetectionsWithSegmentsByStationsAndTime } from '../../../../workers';
import type { SignalDetectionWithSegmentsFetchResults } from '../../../../workers/waveform-worker/operations/fetch-signal-detections-segments-by-stations-time';
import { AsyncActionStatus } from '../../../query';
import { hasAlreadyBeenRequested } from '../../../query/async-fetch-util';
import { waveformActions } from '../../../state/waveform/waveform-slice';
import type { AppDispatch, AppState } from '../../../store';
import { getUiTheme } from '../../processing-configuration';
import { createRecipeToMutateUiChannelSegmentsRecord } from '../channel-segment/mutate-channel-segment-record';
import type { DataState } from '../types';
import { config } from './endpoint-configuration';
import type {
  GetSignalDetectionsAndSegmentsByStationsAndTimeQueryArgs,
  GetSignalDetectionsWithSegmentsByStationAndTimeQueryArgs
} from './types';

const logger = UILogger.create(
  'GMS_LOG_FETCH_SIGNAL_DETECTION',
  process.env.GMS_LOG_FETCH_SIGNAL_DETECTION
);

/**
 * Helper function used to determine if the getSignalDetectionsAndSegmentsByStationAndTime query should be skipped.
 *
 * @returns returns true if the arguments are valid; false otherwise.
 */
export const shouldSkipGetSignalDetectionsWithSegmentsByStationAndTime = (
  args: GetSignalDetectionsWithSegmentsByStationAndTimeQueryArgs
): boolean =>
  !args ||
  args.startTime == null ||
  args.endTime == null ||
  args.station == null ||
  args.stageId?.name == null;

/**
 * Async thunk action that fetches (requests) signal detections with segments by station and time.
 */
export const getSignalDetectionsAndSegmentsByStationAndTime = createAsyncThunk<
  SignalDetectionWithSegmentsFetchResults,
  GetSignalDetectionsWithSegmentsByStationAndTimeQueryArgs,
  {
    readonly rejectValue: Error;
  }
>(
  'signalDetection/getSignalDetectionsAndSegmentsByStationAndTime',
  async (
    arg: GetSignalDetectionsWithSegmentsByStationAndTimeQueryArgs,
    { getState, dispatch, rejectWithValue }
  ) => {
    const state = (getState as () => AppState)();
    const appDispatch = dispatch as AppDispatch;
    const currentInterval: CommonTypes.TimeRange = state.app.workflow.timeRange;
    const theme = getUiTheme(getState as () => AppState);

    const data: GetSignalDetectionsAndSegmentsByStationsAndTimeQueryArgs = {
      stations: [arg.station],
      startTime: arg.startTime,
      endTime: arg.endTime,
      stageId: arg.stageId,
      excludedSignalDetections: arg.excludedSignalDetections
    };
    const requestConfig = {
      ...config.signalDetection.services.getDetectionsWithSegmentsByStationsAndTime.requestConfig,
      data
    };

    appDispatch(waveformActions.incrementLoadingTotal());
    return fetchSignalDetectionsWithSegmentsByStationsAndTime(requestConfig, currentInterval, {
      waveformColor: theme.colors.waveformRaw,
      labelTextColor: theme.colors.waveformFilterLabel
    })
      .catch(error => {
        if (error.message !== 'canceled') {
          logger.error(`Failed getSignalDetectionsAndSegmentsByStationAndTime (rejected)`, error);
        }
        return rejectWithValue(error);
      })
      .finally(() => {
        appDispatch(waveformActions.incrementLoadingCompleted());
      });
  },
  {
    condition: (arg: GetSignalDetectionsWithSegmentsByStationAndTimeQueryArgs, { getState }) => {
      const state = (getState as () => AppState)();

      // determine if the query should be skipped based on the provided args; check if valid
      if (shouldSkipGetSignalDetectionsWithSegmentsByStationAndTime(arg)) {
        return false;
      }

      // check if the query has been executed already
      const requests =
        state.data.queries.getSignalDetectionWithSegmentsByStationAndTime[arg.station.name] ?? {};
      return !hasAlreadyBeenRequested(requests, arg);
    }
  }
);

/**
 * Injects the getSignalDetectionsAndSegmentsByStationAndTime reducers to the provided builder.
 *
 * @param builder the action reducer map builder
 */
export const addGetSignalDetectionsWithSegmentsByStationAndTimeReducers = (
  builder: ActionReducerMapBuilder<DataState>
): void => {
  builder
    /**
     * getSignalDetectionsAndSegmentsByStationAndTime PENDING action
     * Updates the signal detection query state to indicate that the query status is pending.
     * Note: Mutating the state maintains immutability because it uses immer under the hood.
     */
    .addCase(getSignalDetectionsAndSegmentsByStationAndTime.pending, (state, action) => {
      const history = state.queries.getSignalDetectionWithSegmentsByStationAndTime;
      const { name } = action.meta.arg.station;
      if (!history[name]) {
        history[name] = {};
      }
      history[name][action.meta.requestId] = {
        arg: action.meta.arg,
        status: AsyncActionStatus.pending,
        error: undefined
      };
    })

    /**
     * getSignalDetectionsAndSegmentsByStationAndTime FULFILLED action
     * Updates the signal detection query state to indicate that the query status is fulfilled.
     * Stores the retrieved signal detections in the signal detection redux state.
     * Stores the retrieved channel segments in the channel segment redux state.
     * Note: Mutating the state maintains immutability because it uses immer under the hood.
     */
    .addCase(getSignalDetectionsAndSegmentsByStationAndTime.fulfilled, (state, action) => {
      const history = state.queries.getSignalDetectionWithSegmentsByStationAndTime;
      const { name } = action.meta.arg.station;
      // make sure this is set in case a signal detection is returned from the worker after the
      // interval has changed
      if (!history[name]) {
        history[name] = {};
      }
      // If we don't have a request matching this ID, that means that it was cleared out
      // (for example, when an interval is closed), and so we don't need to process this.
      if (!Object.hasOwnProperty.call(history[name], action.meta.requestId)) {
        return;
      }
      history[name][action.meta.requestId] = {
        arg: action.meta.arg,
        status: AsyncActionStatus.fulfilled,
        error: undefined
      };

      action.payload.signalDetections.forEach(sd => {
        state.signalDetections[sd.id] = sd;
      });

      action.payload.uiChannelSegments.forEach(cs => {
        state.uiChannelSegments = produce(
          state.uiChannelSegments,
          createRecipeToMutateUiChannelSegmentsRecord(name, [cs])
        );
      });
    })

    /**
     * getSignalDetectionsAndSegmentsByStationAndTime REJECTED action
     * Updates the signal detection query state to indicate that the query status is rejected,
     * and adds the error message.
     * Note: Mutating the state maintains immutability because it uses immer under the hood.
     */
    .addCase(getSignalDetectionsAndSegmentsByStationAndTime.rejected, (state, action) => {
      const history = state.queries.getSignalDetectionWithSegmentsByStationAndTime;
      const { name } = action.meta.arg.station;
      // don't update if the history has been cleared before this promise rejected
      if (!Object.hasOwnProperty.call(history, name)) {
        return;
      }
      history[name][action.meta.requestId] = {
        arg: action.meta.arg,
        status: AsyncActionStatus.rejected,
        error: action.error
      };
    });
};
