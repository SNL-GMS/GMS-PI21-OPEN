import type { CommonTypes, EventTypes } from '@gms/common-model';
import { SignalDetectionTypes } from '@gms/common-model';
import {
  findPreferredEventHypothesis,
  findPreferredLocationSolution
} from '@gms/common-model/lib/event';
import { chunkRanges, determineExcludedRanges } from '@gms/common-util';
import includes from 'lodash/includes';
import * as React from 'react';

import type {
  FindEventStatusInfoByStageIdAndEventIdsProps,
  FindEventStatusInfoByStageIdAndEventIdsQuery
} from '../api';
import {
  eventManagerApiSlice,
  useGetProcessingAnalystConfigurationQuery,
  useWorkflowQuery
} from '../api';
import {
  getEventsWithDetectionsAndSegmentsByTime,
  shouldSkipGetEventsWithDetectionsAndSegmentsByTime
} from '../api/data/event/get-events-detections-segments-by-time';
import type {
  GetEventsWithDetectionsAndSegmentsByTimeHistory,
  GetEventsWithDetectionsAndSegmentsByTimeQueryArgs
} from '../api/data/event/types';
import { UIStateError } from '../error-handling/ui-state-error';
import type { AsyncFetchResult } from '../query';
import { useProduceAndHandleSkip } from '../query/util';
import { eventsActions } from '../state';
import { useFetchHistoryStatus } from './fetch-history-hooks';
import { useAppDispatch, useAppSelector } from './react-redux-hooks';
import { useStationsVisibility, useViewableInterval } from './waveform-hooks';

/**
 * Defines async fetch result for the events by time history.
 *
 * @see {@link AsyncFetchResult}
 */
export type EventsWithDetectionsAndSegmentsByTimeHistoryFetchResult = AsyncFetchResult<
  GetEventsWithDetectionsAndSegmentsByTimeHistory
>;

/**
 * Defines async fetch result for the events. It contains flags indicating
 * the status of the request.
 *
 * @see {@link AsyncFetchResult}
 */
export type EventsFetchResult = AsyncFetchResult<EventTypes.Event[]>;

/**
 * A hook that can be used to return the current history of the events by time query.
 * This includes the following information:
 *  - the async fetch status of all the async requests
 *  - the `data`: the history of the `getEventsWithDetectionsAndSegmentsByTime` queries
 *
 * @returns returns the current history of the events by time query.
 */
export const useGetEventsWithDetectionsAndSegmentsByTimeHistory = (): EventsWithDetectionsAndSegmentsByTimeHistoryFetchResult => {
  const history = useAppSelector(
    state => state.data.queries.getEventsWithDetectionsAndSegmentsByTime
  );
  return useFetchHistoryStatus<GetEventsWithDetectionsAndSegmentsByTimeQueryArgs>(history);
};

/**
 * @returns the skipped result for the get signal detections by stations query
 */
const useGetEventsWithDetectionsAndSegmentsByTimeSkippedResult = (): EventsFetchResult => {
  const result = React.useRef({
    data: [],
    pending: 0,
    fulfilled: 0,
    rejected: 0,
    isLoading: false,
    isError: false
  });
  return result.current;
};

/**
 * A hook that issues the requests for the events by time query. This hooks will call out to the events
 * service and if there is new data for the specified TimeRange will update the Events, Signal detections,
 * and channel segments associated with the events in the given TimeRange.
 *
 * You will need to subscribe to the useGetEvents, useGetSignalDetection, and useGetChannelSegments hooks
 * to get the data resulting from the any updated data.
 *
 * ! the fetches will be chunked based on the processing configuration
 *
 * @param args the events by time query arguments
 */
const useFetchEventsWithDetectionsAndSegmentsByTime = (
  args: GetEventsWithDetectionsAndSegmentsByTimeQueryArgs
): void => {
  const dispatch = useAppDispatch();
  const processingAnalystConfiguration = useGetProcessingAnalystConfigurationQuery();
  const history = useGetEventsWithDetectionsAndSegmentsByTimeHistory();

  const maxTimeRangeRequestInSeconds =
    processingAnalystConfiguration.data?.endpointConfigurations
      ?.getEventsWithDetectionsAndSegmentsByTime?.maxTimeRangeRequestInSeconds;

  React.useEffect(() => {
    const ranges = determineExcludedRanges(
      Object.values(history.data.eventsWithDetectionsAndSegmentsByTime ?? []).map(v => ({
        start: v.arg.startTime,
        end: v.arg.endTime
      })),
      { start: args.startTime, end: args.endTime }
    );

    // chunk up the data requests based on the `maxTimeRangeRequestInSeconds`
    const chunkedRanges = chunkRanges(ranges, maxTimeRangeRequestInSeconds);

    chunkedRanges?.forEach(r => {
      dispatch(
        getEventsWithDetectionsAndSegmentsByTime({
          stageId: args.stageId,
          startTime: r.start,
          endTime: r.end
        })
      ).catch(error => {
        throw new UIStateError(error);
      });
    });
  }, [dispatch, args, history.data, maxTimeRangeRequestInSeconds]);
};

/**
 * A hook thar returns the fetch results for events by time.
 *
 *  This includes the following information:
 *  - the async fetch status of all the async requests
 *  - the `data`: the events from all requests
 *
 * ! the returned results are filtered so that the results only match what the query args requested
 *
 * @param args the query props data
 * @returns the events with segments and signal detections by time query. If skipped, the returned data will be set to `null`
 */
export const useGetEventsByTime = (
  args: GetEventsWithDetectionsAndSegmentsByTimeQueryArgs
): EventsFetchResult => {
  const history = useGetEventsWithDetectionsAndSegmentsByTimeHistory();

  useFetchEventsWithDetectionsAndSegmentsByTime(args);

  const workflowQuery = useWorkflowQuery();

  const openIntervalName = useAppSelector(state => state.app.workflow.openIntervalName);

  const stageNames = React.useMemo(
    () => (workflowQuery.isSuccess ? workflowQuery.data?.stages?.map(stage => stage.name) : []),
    [workflowQuery.isSuccess, workflowQuery.data?.stages]
  );

  // retrieve all events from the state
  const events = useAppSelector(state => state.data.events);

  const skippedReturnValue = useGetEventsWithDetectionsAndSegmentsByTimeSkippedResult();

  const shouldSkip = shouldSkipGetEventsWithDetectionsAndSegmentsByTime(args);

  const emptyArray = React.useRef<EventTypes.Event[]>([]);

  const data = React.useMemo(
    () => (shouldSkip ? emptyArray.current : Object.values(events).map(e => e)),
    [events, shouldSkip]
  );

  // filter the events based on the query args using the preferred hypothesis
  const filteredData = React.useMemo(
    () =>
      data.filter(event => {
        const preferredEventHypothesis = findPreferredEventHypothesis(
          event,
          openIntervalName,
          stageNames
        );
        const locationSolution = findPreferredLocationSolution(
          preferredEventHypothesis.id.hypothesisId,
          event.eventHypotheses
        );

        const time = locationSolution?.location?.time;
        // check that the event time falls between the start and end time
        return time >= args.startTime && time <= args.endTime;
      }),
    [args.endTime, args.startTime, data, openIntervalName, stageNames]
  );

  return React.useMemo(() => {
    if (shouldSkipGetEventsWithDetectionsAndSegmentsByTime(args)) {
      return skippedReturnValue;
    }
    return { ...history, data: filteredData };
  }, [args, history, filteredData, skippedReturnValue]);
};

/**
 * A hook that can be used to retrieve query arguments based on the current state.
 * Accounts for the current interval and visible stations.
 *
 * @param interval interval of time to use as the start and end time
 * @returns the events with detections and segments by time query args.
 */
export const useQueryArgsForGetEventsWithDetectionsAndSegmentsByTime = (
  interval: CommonTypes.TimeRange
): GetEventsWithDetectionsAndSegmentsByTimeQueryArgs => {
  const stageName = useAppSelector(state => state.app.workflow.openIntervalName);
  return React.useMemo(
    () => ({
      startTime: interval?.startTimeSecs,
      endTime: interval?.endTimeSecs,
      stageId: {
        name: stageName
      }
    }),
    [interval, stageName]
  );
};

/**
 * A hook that can be used to retrieve events for the current interval.
 *
 * @returns the events results for the viewable interval.
 */
export const useGetEvents = (): EventsFetchResult => {
  const [viewableInterval] = useViewableInterval();
  const args = useQueryArgsForGetEventsWithDetectionsAndSegmentsByTime(viewableInterval);
  return useGetEventsByTime(args);
};

/**
 * Wraps the hook from the event manager api slice to allow for reuse of
 * Returns the query result for the event status by stage and event ids query.
 *
 * The useEventStatusQuery hook wraps the RTK query hook
 * useFindEventStatusInfoByStageIdAndEventIdsQuery; to allow for reuse of
 * configuration, i.e. specifying when to skip the query.
 *
 * @returns the event status by stage and event ids. If skipped, the return will be null
 */
export const useEventStatusQuery = (): FindEventStatusInfoByStageIdAndEventIdsQuery => {
  const stageName = useAppSelector(state => state.app.workflow.openIntervalName);
  const eventData = useGetEvents();
  const data: FindEventStatusInfoByStageIdAndEventIdsProps = {
    stageId: { name: stageName },
    eventIds: eventData.data?.map(event => event.id)
  };
  const skip =
    data.stageId?.name == null ||
    data.eventIds == null ||
    data.eventIds.length < 1 ||
    eventData.pending > 0;

  return useProduceAndHandleSkip(
    eventManagerApiSlice.useFindEventStatusInfoByStageIdAndEventIdsQuery(data, { skip }),
    skip
  );
};

/**
 * Hook for managing the stations associated with an open event
 *
 * @returns a tuple consisting of the stations associated with an open event and a setter to update
 * the associated stations
 */
export const useStationsAssociatedWithCurrentOpenEvent = (): [
  string[],
  (newValue: string[]) => void
] => {
  const stationsAssociatedWithCurrentOpenEvent = useAppSelector(
    state => state.app.events.stationsAssociatedWithCurrentOpenEvent
  );
  const dispatch = useAppDispatch();
  const setStationsAssociatedWithCurrentOpenEvent = React.useCallback(
    (newValue: string[]) =>
      dispatch(eventsActions.setStationsAssociatedWithCurrentOpenEvent(newValue)),
    [dispatch]
  );
  return [stationsAssociatedWithCurrentOpenEvent, setStationsAssociatedWithCurrentOpenEvent];
};

/**
 * Get the station names from the signal detections associated with the current open event
 *
 * @param currentOpenEventId current open event ID
 * @param eventResults events
 * @param signalDetectionsFromStore signal detections
 * @returns station names as an array of strings
 */
export function getStationNamesFromAssociatedSignalDetections(
  currentOpenEventId: string,
  eventResults: EventsFetchResult,
  signalDetectionsFromStore: Record<string, SignalDetectionTypes.SignalDetection>
): string[] {
  const stationNames = [];
  const currentOpenEvent = eventResults.data?.find(event => event.id === currentOpenEventId);
  const associatedSignalDetectionHypothesisIds = currentOpenEvent.overallPreferred?.associatedSignalDetectionHypotheses.map(
    hypothesis => hypothesis.id.id
  );
  const signalDetections = Object.values(signalDetectionsFromStore);

  signalDetections.forEach(sd => {
    if (
      !SignalDetectionTypes.Util.getCurrentHypothesis(sd.signalDetectionHypotheses).rejected &&
      includes(
        associatedSignalDetectionHypothesisIds,
        SignalDetectionTypes.Util.getCurrentHypothesis(sd.signalDetectionHypotheses).id.id
      )
    ) {
      stationNames.push(sd.station.name);
    }
  });

  return stationNames;
}

/**
 * Hook to display additional stations on the Waveform Display that may be outside of
 * the default station group with detections associated to the open event
 *
 * @returns a callback that accepts an event ID
 */
export const useUpdateVisibleStationsForOpenEvent = (): ((openEventId: string) => void) => {
  const eventResults = useGetEvents();
  const [
    stationsAssociatedWithCurrentOpenEvent,
    setStationsAssociatedWithCurrentOpenEvent
  ] = useStationsAssociatedWithCurrentOpenEvent();
  const { stationsVisibility, setStationVisibility } = useStationsVisibility();
  const signalDetectionsFromStore = useAppSelector(state => state.data.signalDetections);
  return React.useCallback(
    currentOpenEventId => {
      const updates = [];
      const stationsInStationsVisibility = [];
      const stationNames = getStationNamesFromAssociatedSignalDetections(
        currentOpenEventId,
        eventResults,
        signalDetectionsFromStore
      );
      if (stationsAssociatedWithCurrentOpenEvent.length > 0) {
        // if we've opened another event without closing a previously opened event
        Object.entries(stationsVisibility).forEach(([, stationVisibilityChanges]) => {
          const { stationName } = stationVisibilityChanges;
          if (includes(stationsAssociatedWithCurrentOpenEvent, stationName)) {
            setStationVisibility(stationName, false);
          }
        });
      }
      Object.entries(stationsVisibility).forEach(([, stationVisibilityChanges]) => {
        stationsInStationsVisibility.push(stationVisibilityChanges.stationName);
      });
      stationNames.forEach(name => {
        if (stationsInStationsVisibility.includes(name) && !stationsVisibility[name].visibility) {
          // visibility was updated
          updates.push(name);
          setStationVisibility(name, true);
        }
        if (!stationsInStationsVisibility.includes(name)) {
          // station was added
          updates.push(name);
          setStationVisibility(name, true);
        }
      });
      if (updates.length > 0) {
        setStationsAssociatedWithCurrentOpenEvent(updates);
      }
    },
    [
      eventResults,
      setStationVisibility,
      setStationsAssociatedWithCurrentOpenEvent,
      signalDetectionsFromStore,
      stationsAssociatedWithCurrentOpenEvent,
      stationsVisibility
    ]
  );
};

/**
 * Hook to stop displaying additional stations on the Waveform Display that may have
 * been added when opening an event
 *
 * @returns a callback that requires no arguments
 */
export const useUpdateVisibleStationsForCloseEvent = (): (() => void) => {
  const [
    stationsAssociatedWithCurrentOpenEvent,
    setStationsAssociatedWithCurrentOpenEvent
  ] = useStationsAssociatedWithCurrentOpenEvent();
  const { stationsVisibility, setStationVisibility } = useStationsVisibility();
  return React.useCallback(() => {
    if (stationsAssociatedWithCurrentOpenEvent.length > 0) {
      Object.entries(stationsVisibility).forEach(key => {
        const { stationName } = key[1];
        if (includes(stationsAssociatedWithCurrentOpenEvent, stationName)) {
          setStationVisibility(stationName, false);
        }
      });
      setStationsAssociatedWithCurrentOpenEvent([]);
    }
  }, [
    setStationVisibility,
    setStationsAssociatedWithCurrentOpenEvent,
    stationsAssociatedWithCurrentOpenEvent,
    stationsVisibility
  ]);
};
