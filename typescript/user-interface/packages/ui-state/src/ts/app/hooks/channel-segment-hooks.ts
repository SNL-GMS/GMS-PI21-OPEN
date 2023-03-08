import type { CommonTypes, StationTypes } from '@gms/common-model';
import { determineExcludedRanges } from '@gms/common-util';
import produce from 'immer';
import flatMap from 'lodash/flatMap';
import React from 'react';

import type { UiChannelSegment } from '../../types';
import {
  getChannelSegmentsByChannel,
  shouldSkipGetChannelSegmentsByChannel
} from '../api/data/channel-segment/get-channel-segments-by-channel';
import type {
  GetChannelSegmentsByChannelHistory,
  GetChannelSegmentsByChannelQueryArgs,
  GetChannelSegmentsByChannelsQueryArgs
} from '../api/data/channel-segment/types';
import { UIStateError } from '../error-handling/ui-state-error';
import type { AsyncFetchResult } from '../query';
import { getNamesOfAllDisplayedChannels } from '../state/waveform/util';
import { useFetchHistoryStatus } from './fetch-history-hooks';
import { useEffectiveTime } from './operational-time-period-configuration-hooks';
import { useOldQueryDataIfReloading } from './query-util-hooks';
import { useAppDispatch, useAppSelector } from './react-redux-hooks';
import { useGetSignalDetections } from './signal-detection-hooks';
import { useGetAllStationsQuery } from './station-definition-hooks';
import { useViewableInterval } from './waveform-hooks';

/**
 * Defines async fetch result for the channel segments by channel history.
 *
 * @see {@link AsyncFetchResult}
 */
export type ChannelSegmentsByChannelHistoryFetchResult = AsyncFetchResult<
  GetChannelSegmentsByChannelHistory
>;

/**
 * Defines async fetch result for the channel segments. It contains flags indicating
 * the status of the request.
 *
 * @see {@link AsyncFetchResult}
 */
export type ChannelSegmentFetchResult = AsyncFetchResult<
  Record<string, Record<string, UiChannelSegment[]>>
>;

/**
 * Helper function that filters a Record<string, Record<string, UiChannelSegment[]>> for the provided unique names.
 *
 *
 * @returns a filtered Record<string, Record<string, UiChannelSegment[]>>
 */
const filterChannelSegmentsByNames = (
  channelSegments: Record<string, Record<string, UiChannelSegment[]>>,
  names: string[]
): Record<string, Record<string, UiChannelSegment[]>> => {
  const filteredChannelSegments: Record<string, Record<string, UiChannelSegment[]>> = {};
  if (names) {
    names.forEach(name => {
      if (channelSegments[name]) {
        filteredChannelSegments[name] = channelSegments[name];
      }
    });
  }
  return filteredChannelSegments;
};

/**
 * A hook that can be used to return the current history of the channel segments by channel query.
 * This includes the following information:
 *  - the async fetch status of all the async requests
 *  - the `data`: the history of the `getChannelSegmentsByChannel` queries
 *
 * @see {@link ChannelSegmentsByChannelHistoryFetchResult}
 *
 * @returns the current history of the channel segments by channel query.
 */
export const useGetChannelSegmentsByChannelHistory = (): ChannelSegmentsByChannelHistoryFetchResult => {
  const history = useAppSelector(state => state.data.queries.getChannelSegmentsByChannel);
  return useFetchHistoryStatus<GetChannelSegmentsByChannelQueryArgs>(history);
};

/**
 * @returns the skipped result for the get channel segments by channels query
 */
const useGetChannelSegmentsByChannelsSkippedResult = (): ChannelSegmentFetchResult => {
  const result = React.useRef({
    data: {},
    pending: 0,
    fulfilled: 0,
    rejected: 0,
    isLoading: false,
    isError: false
  });
  return result.current;
};

/**
 * A hook that issues the requests for the channel segments by channels query.
 *
 * @param args the channel segments by channels query arguments
 */
const useFetchChannelSegmentsByChannelsQuery = (
  args: GetChannelSegmentsByChannelsQueryArgs
): void => {
  const dispatch = useAppDispatch();
  const history = useGetChannelSegmentsByChannelHistory();

  React.useEffect(() => {
    args.channels.forEach(channel => {
      const ranges = determineExcludedRanges(
        Object.values(history.data[channel.name] ?? []).map(v => ({
          start: v.arg.startTime,
          end: v.arg.endTime
        })),
        { start: args.startTime, end: args.endTime }
      );

      ranges.forEach(r => {
        dispatch(
          getChannelSegmentsByChannel({
            channel,
            startTime: r.start,
            endTime: r.end
          })
        ).catch(error => {
          throw new UIStateError(error);
        });
      });
    });
  }, [dispatch, args, history.data]);
};

/**
 * A hook that can be used to retrieve channel segments by channels.
 * Makes an individual async request for each channel.
 *
 * This includes the following information:
 *  - the async fetch status of all the async requests
 *  - the `data`: the channel segments from all requests
 *
 * ! the returned results are filtered so that the results only match what the query args requested
 *
 * @param args the channel segments by channels query arguments
 *
 * @returns the channel segments fetch result.
 */
export const useGetChannelSegmentsByChannels = (
  args: GetChannelSegmentsByChannelsQueryArgs
): ChannelSegmentFetchResult => {
  const history = useGetChannelSegmentsByChannelHistory();

  // issue any new fetch requests
  useFetchChannelSegmentsByChannelsQuery(args);

  // retrieve all channel segments from the state
  const channelSegments = useAppSelector(state => state.data.uiChannelSegments);

  const skippedReturnValue = useGetChannelSegmentsByChannelsSkippedResult();

  const data = React.useMemo(
    () =>
      // filter out the channel segments based on the query parameters
      // TODO filter channel segments based on the time range requested
      filterChannelSegmentsByNames(
        channelSegments,
        args.channels.map(c => c.name)
      ),
    [args, channelSegments]
  );

  return React.useMemo(() => {
    if (
      shouldSkipGetChannelSegmentsByChannel({
        ...args,
        channel: args.channels && args.channels.length > 1 ? args.channels[0] : undefined
      })
    ) {
      return skippedReturnValue;
    }

    return { ...history, data };
  }, [args, data, history, skippedReturnValue]);
};

/**
 * A hook that can be used to retrieve channel segments for the signal detections
 * that were received by stations and time.
 *
 * This includes the following information:
 *  - the async fetch status of all the async requests
 *  - the `data`: the channel segments from all requests
 *
 * ! the returned results are filtered so that the results only match what the query args requested
 *
 * @param args the channel segments by channels query arguments
 *
 * @returns the channel segments fetch result.
 */
export const useGetChannelSegmentsForSignalDetections = (
  interval: CommonTypes.TimeRange
): ChannelSegmentFetchResult => {
  const signalDetections = useGetSignalDetections(interval);
  const channelSegments = useAppSelector(state => state.data.uiChannelSegments);

  // get the unique station names for each signal detection
  const stations: string[] = React.useMemo(
    () => flatMap(signalDetections.data.map(sd => sd.station.name)),
    [signalDetections.data]
  );

  // filter out out the channel segments for stations
  const data = React.useMemo(() => filterChannelSegmentsByNames(channelSegments, stations), [
    stations,
    channelSegments
  ]);

  const status = React.useMemo(
    () => ({
      fulfilled: signalDetections.fulfilled,
      isError: signalDetections.isError,
      isLoading: signalDetections.isLoading,
      pending: signalDetections.pending,
      rejected: signalDetections.rejected
    }),
    [
      signalDetections.fulfilled,
      signalDetections.isError,
      signalDetections.isLoading,
      signalDetections.pending,
      signalDetections.rejected
    ]
  );

  return React.useMemo(() => {
    return {
      ...status,
      data
    };
  }, [status, data]);
};

/**
 * A hook that can be used to retrieve query arguments based on the current state.
 * Accounts for the current interval and visible stations.
 *
 * @returns the channel segments by channels query args.
 */
export const useQueryArgsForGetChannelSegmentsByChannels = (): GetChannelSegmentsByChannelsQueryArgs => {
  const effectiveAt = useEffectiveTime();
  const stationResult = useGetAllStationsQuery(effectiveAt);
  const stationData = useOldQueryDataIfReloading<StationTypes.Station[]>(stationResult);
  const [viewableInterval] = useViewableInterval();
  const stationsVisibility = useAppSelector(state => state.app.waveform.stationsVisibility);

  const channels = React.useMemo(
    () =>
      getNamesOfAllDisplayedChannels(stationsVisibility, stationData || []).map(channelName => {
        return {
          name: channelName,
          effectiveAt
        };
      }),
    [stationsVisibility, stationData, effectiveAt]
  );

  return React.useMemo(
    () => ({
      channels,
      startTime: viewableInterval?.startTimeSecs,
      endTime: viewableInterval?.endTimeSecs
    }),
    [channels, viewableInterval]
  );
};

/**
 * A hook that can be used to retrieve channel segments for the current interval and visible channels/stations.
 *
 * @returns the channel segments result.
 */
export const useGetChannelSegments = (
  interval: CommonTypes.TimeRange
): ChannelSegmentFetchResult => {
  const channelArgs = useQueryArgsForGetChannelSegmentsByChannels();
  const channelSegmentsResult = useGetChannelSegmentsByChannels(channelArgs);
  const channelSegmentsForSdsResult = useGetChannelSegmentsForSignalDetections(interval);

  return React.useMemo(() => {
    // combine the data (channel segments) and statuses from both results
    return {
      pending: channelSegmentsResult.pending + channelSegmentsForSdsResult.pending,
      fulfilled: channelSegmentsResult.fulfilled + channelSegmentsForSdsResult.fulfilled,
      rejected: channelSegmentsResult.rejected + channelSegmentsForSdsResult.rejected,
      isLoading: channelSegmentsResult.isLoading || channelSegmentsForSdsResult.isLoading,
      isError: channelSegmentsResult.isError || channelSegmentsForSdsResult.isError,
      data: produce(channelSegmentsResult.data, draft => {
        Object.keys(channelSegmentsForSdsResult.data).forEach(key => {
          draft[key] = channelSegmentsForSdsResult.data[key];
        });
      })
    };
  }, [channelSegmentsResult, channelSegmentsForSdsResult]);
};
