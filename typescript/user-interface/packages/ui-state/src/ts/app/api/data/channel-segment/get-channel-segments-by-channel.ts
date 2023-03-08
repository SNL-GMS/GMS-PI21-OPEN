import { UILogger } from '@gms/ui-util';
import type { ActionReducerMapBuilder } from '@reduxjs/toolkit';
import { createAsyncThunk } from '@reduxjs/toolkit';
import { produce } from 'immer';

import type { UiChannelSegment } from '../../../../types';
import { fetchChannelSegmentsByChannel } from '../../../../workers';
import { AsyncActionStatus } from '../../../query';
import { hasAlreadyBeenRequested } from '../../../query/async-fetch-util';
import { waveformActions } from '../../../state/waveform/waveform-slice';
import type { AppDispatch, AppState } from '../../../store';
import { getUiTheme } from '../../processing-configuration';
import type { DataState } from '../types';
import { config } from './endpoint-configuration';
import { createRecipeToMutateUiChannelSegmentsRecord } from './mutate-channel-segment-record';
import type { GetChannelSegmentsByChannelQueryArgs } from './types';

const logger = UILogger.create('GMS_LOG_FETCH_WAVEFORMS', process.env.GMS_LOG_FETCH_WAVEFORMS);

/**
 * Helper function used to determine if the getChannelSegmentsByChannel query should be skipped.
 *
 * @returns returns true if the arguments are valid; false otherwise.
 */
export const shouldSkipGetChannelSegmentsByChannel = (
  args: GetChannelSegmentsByChannelQueryArgs
): boolean => !args || args.startTime == null || args.endTime == null || args.channel == null;

/**
 * Async thunk action that fetches (requests) channel segments by channel.
 */
export const getChannelSegmentsByChannel = createAsyncThunk<
  UiChannelSegment[],
  GetChannelSegmentsByChannelQueryArgs
>(
  'channelSegment/getChannelSegmentsByChannel',
  async (arg: GetChannelSegmentsByChannelQueryArgs, { getState, dispatch, rejectWithValue }) => {
    const state = (getState as () => AppState)();
    const appDispatch = dispatch as AppDispatch;
    const currentInterval = state.app.workflow.timeRange;
    const theme = getUiTheme(getState as () => AppState);

    const requestConfig = {
      ...config.waveform.services.getChannelSegment.requestConfig,
      data: {
        startTime: arg.startTime,
        endTime: arg.endTime,
        channels: [arg.channel]
      }
    };

    appDispatch(waveformActions.incrementLoadingTotal());
    return fetchChannelSegmentsByChannel(requestConfig, currentInterval, {
      waveformColor: theme.colors.waveformRaw,
      labelTextColor: theme.colors.waveformFilterLabel
    })
      .catch(error => {
        if (error.message !== 'canceled') {
          logger.error(`Failed getChannelSegmentsByChannel (rejected)`, error);
        }
        return rejectWithValue(error);
      })
      .finally(() => {
        appDispatch(waveformActions.incrementLoadingCompleted());
      });
  },
  {
    condition: (arg: GetChannelSegmentsByChannelQueryArgs, { getState }) => {
      const state = (getState as () => AppState)();

      // determine if the query should be skipped based on the provided args; check if valid
      if (shouldSkipGetChannelSegmentsByChannel(arg)) {
        return false;
      }

      // check if the query has been executed already
      const requests = state.data.queries.getChannelSegmentsByChannel[arg.channel.name] ?? {};
      return !hasAlreadyBeenRequested(requests, arg);
    }
  }
);

/**
 * Injects the getChannelSegmentsByChannel reducers to the provided builder.
 *
 * @param builder the action reducer map builder
 */
export const addGetChannelSegmentsByChannelReducers = (
  builder: ActionReducerMapBuilder<DataState>
): void => {
  builder
    /**
     * getChannelSegmentsByChannel PENDING action
     * Updates the channel segment query state to indicate that the query status is pending.
     * Note: Mutating the state maintains immutability because it uses immer under the hood.
     */
    .addCase(getChannelSegmentsByChannel.pending, (state, action) => {
      const history = state.queries.getChannelSegmentsByChannel;
      const { name } = action.meta.arg.channel;
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
     * getChannelSegmentsByChannel FULFILLED action
     * Updates the channel segment query state to indicate that the query status is fulfilled.
     * Stores the retrieved channel segments in the channel segment redux state.
     * Note: Mutating the state maintains immutability because it uses immer under the hood.
     */
    .addCase(getChannelSegmentsByChannel.fulfilled, (state, action) => {
      const history = state.queries.getChannelSegmentsByChannel;
      const { name } = action.meta.arg.channel;
      // make sure this is set in case a channelSegment is returned from the worker after the interval has changed
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

      action.payload.forEach(cs => {
        state.uiChannelSegments = produce(
          state.uiChannelSegments,
          createRecipeToMutateUiChannelSegmentsRecord(name, [cs])
        );
      });
    })

    /**
     * getChannelSegmentsByChannel REJECTED action
     * Updates the channel segment query state to indicate that the query status is rejected,
     * and adds the error message.
     * Note: Mutating the state maintains immutability because it uses immer under the hood.
     */
    .addCase(getChannelSegmentsByChannel.rejected, (state, action) => {
      const history = state.queries.getChannelSegmentsByChannel;
      const { name } = action.meta.arg.channel;
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
