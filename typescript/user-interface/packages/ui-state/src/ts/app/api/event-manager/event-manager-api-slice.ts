import type { ChannelTypes, EventTypes, SignalDetectionTypes } from '@gms/common-model';
import type { FeaturePrediction } from '@gms/common-model/lib/event';
import { chunkRange, Logger, uuid } from '@gms/common-util';
import type { AxiosBaseQueryFn } from '@gms/ui-workers';
import { axiosBaseQuery } from '@gms/ui-workers';
import type { MaybeDrafted } from '@reduxjs/toolkit/dist/query/core/buildThunks';
import { createApi } from '@reduxjs/toolkit/query/react';
import flatMap from 'lodash/flatMap';

import type { UseQueryStateResult } from '../../query';
import { useProduceAndHandleSkip } from '../../query/util';
import { Subscription } from '../../subscription';
import { config } from './endpoint-configuration';

const logger = Logger.create('GMS_LOG_EVENTS', process.env.GMS_LOG_EVENTS);

// TODO: Test this number when we are no longer using the canned end point
const MAX_CHANNELS_PER_PREDICTION = 100;
const MAX_SDS_PER_INTERVAL = 50;

const subscriberId = `find-event-status-info-by-stage-id-event-ids-${uuid.asString()}`;

export interface PredictFeaturesForLocationSolutionProps {
  locationSolution: EventTypes.LocationSolution;
  channels: ChannelTypes.Channel[];
  phases: string[];
}

export interface ReceiverCollection {
  receiverDataType: string;
  receiverBandType: string;
  receiverLocationsByName: Record<string, ChannelTypes.Location>;
}

export interface PredictFeaturesForEventLocationProps {
  receivers: ReceiverCollection[];
  sourceLocation: EventTypes.EventLocation;
  phases: string[];
}

export interface ReceiverLocationResponse {
  featurePredictions: FeaturePrediction[];
}

export interface PredictFeaturesForEventLocationResponse {
  receiverLocationsByName: Record<string, ReceiverLocationResponse>;
  isRequestingDefault?: boolean;
}

export interface EventStatusResponse {
  eventStatusInfoMap: Record<string, EventTypes.EventStatusInfo>;
  stageId: { name: string };
}

export interface EventStatus {
  stageId: { name: string };
  eventId: string;
  eventStatusInfo: EventTypes.EventStatusInfo;
}

export interface FindEventStatusInfoByStageIdAndEventIdsProps {
  stageId: { name: string };
  eventIds: string[];
}

export interface FindEventsByAssociatedSignalDetectionHypothesesProps {
  signalDetectionHypotheses: SignalDetectionTypes.SignalDetectionHypothesis[];
  stageId: { name: string };
}

export type FindEventsByAssociatedSignalDetectionHypothesesQuery = UseQueryStateResult<
  EventTypes.Event[]
>;

/**
 * Transforms response from the event status initial query to match the format of the subscription
 *
 * @param data the response from the event status base query
 * @returns
 */
export const eventStatusTransform = (data: EventStatusResponse): Record<string, EventStatus> => {
  // The initial query data is formatted differently then the subscription
  // So this rebuilds it into a Record of EventStatus to match what the subscription gets
  const newRecord: Record<string, EventStatus> = {};
  Object.entries(data.eventStatusInfoMap).forEach(params => {
    newRecord[params[0]] = {
      stageId: data.stageId,
      eventId: params[0],
      eventStatusInfo: params[1]
    };
  });
  return newRecord;
};

/**
 * Updates the events in the store.
 *
 * @param events the events to update or add
 * @returns a mutation function that is used with immer
 */
export const updateEventStatus = (eventStatus: EventStatus[]) => (
  draft: MaybeDrafted<Record<string, EventStatus>>
): void => {
  eventStatus.forEach(status => {
    // Update the dictionary with thew new data
    draft[status.eventId] = status;
  });
};

/**
 * The event manager api reducer slice.
 */
export const eventManagerApiSlice = createApi({
  reducerPath: 'eventManagerApi',
  baseQuery: axiosBaseQuery({
    baseUrl: config.eventManager.baseUrl
  }),
  endpoints(build) {
    return {
      /**
       * defines query for events status
       */
      findEventStatusInfoByStageIdAndEventIds: build.query<
        Record<string, EventStatus>,
        FindEventStatusInfoByStageIdAndEventIdsProps
      >({
        query: (data: FindEventStatusInfoByStageIdAndEventIdsProps) => ({
          requestConfig: {
            ...config.eventManager.services.findEventStatusInfoByStageIdAndEventIds.requestConfig,
            data
          }
        }),
        transformResponse: eventStatusTransform,
        async onCacheEntryAdded(data, { updateCachedData, cacheDataLoaded, cacheEntryRemoved }) {
          // Callback from the subscription list of Events
          const onMessage: (eventStatus: EventStatus[]) => void = eventStatus => {
            if (!eventStatus) {
              return;
            }
            updateCachedData(updateEventStatus(eventStatus));
          };

          try {
            // wait for the initial query to resolve before proceeding
            await cacheDataLoaded;

            Subscription.addSubscriber(subscriberId, 'events', onMessage);
            logger.debug(`Events subscription subscribed ${subscriberId}`);

            // cacheEntryRemoved will resolve when the cache subscription is no longer active
            await cacheEntryRemoved;
          } catch (e) {
            Subscription.removeSubscriber(subscriberId, 'events');
            logger.error(`Failed to establish websocket connection ${subscriberId}`, e);
          }
        }
      }),

      /**
       * defines query for predict features for location solution
       */
      predictFeaturesForLocationSolution: build.query<
        EventTypes.LocationSolution,
        PredictFeaturesForLocationSolutionProps
      >({
        queryFn: async ({ locationSolution, channels, phases }) => {
          const chunkedRanges = chunkRange(
            { start: 0, end: channels.length },
            MAX_CHANNELS_PER_PREDICTION
          );

          const baseQuery: AxiosBaseQueryFn<EventTypes.LocationSolution> = axiosBaseQuery({
            baseUrl: config.eventManager.baseUrl
          });

          const predictions = flatMap(
            await Promise.all<FeaturePrediction[]>(
              chunkedRanges.map(async range => {
                const { data } = await baseQuery(
                  {
                    requestConfig: {
                      ...config.eventManager.services.predictFeaturesForLocationSolution
                        .requestConfig,
                      data: {
                        locationSolution,
                        channels: channels.slice(range.start, range.end),
                        phases
                      }
                    }
                  },
                  undefined,
                  undefined
                );
                if (data) {
                  return Promise.resolve(data.featurePredictions.featurePredictions);
                }
                return Promise.resolve([] as FeaturePrediction[]);
              })
            )
          );

          const merged = [].concat(...predictions);

          const populatedLocationSolution: EventTypes.LocationSolution = {
            ...locationSolution,
            featurePredictions: { featurePredictions: merged }
          };
          return Promise.resolve({ data: populatedLocationSolution });
        }
      }),
      /**
       * defines query for predict features for location solution
       */
      predictFeaturesForEventLocation: build.query<
        PredictFeaturesForEventLocationResponse,
        PredictFeaturesForEventLocationProps
      >({
        queryFn: async ({ sourceLocation, receivers, phases }) => {
          const baseQuery: AxiosBaseQueryFn<PredictFeaturesForEventLocationResponse> = axiosBaseQuery(
            {
              baseUrl: config.eventManager.baseUrl
            }
          );

          const responses: Record<string, ReceiverLocationResponse>[] = flatMap(
            await Promise.all<Record<string, ReceiverLocationResponse>>(
              receivers.map(async receiver => {
                const { data } = await baseQuery(
                  {
                    requestConfig: {
                      ...config.eventManager.services.predictFeaturesForEventLocation.requestConfig,
                      data: {
                        sourceLocation,
                        receivers: [receiver],
                        phases
                      }
                    }
                  },
                  undefined,
                  undefined
                );
                if (data) {
                  return Promise.resolve(data.receiverLocationsByName);
                }
                return Promise.resolve(null as Record<string, ReceiverLocationResponse>);
              })
            )
          );
          let merged: Record<string, ReceiverLocationResponse> = {};
          responses.forEach(results => {
            merged = { ...merged, ...results };
          });
          return Promise.resolve({ data: { receiverLocationsByName: merged } });
        }
      }),

      /**
       * Defines the mutation for updating event status
       */
      updateEventStatus: build.mutation<void, EventStatus>({
        query: (data: EventStatus) => ({
          requestConfig: {
            ...config.eventManager.services.updateEventStatus.requestConfig,
            data
          }
        })
      }),

      /**
       * defines query for find events by assoc signal detection hypotheses
       */
      findEventsByAssociatedSignalDetectionHypotheses: build.query<
        EventTypes.Event[],
        FindEventsByAssociatedSignalDetectionHypothesesProps
      >({
        queryFn: async ({ signalDetectionHypotheses, stageId }) => {
          const chunkedRanges = chunkRange(
            { start: 0, end: signalDetectionHypotheses.length },
            MAX_SDS_PER_INTERVAL
          );

          const baseQuery: AxiosBaseQueryFn<EventTypes.Event[]> = axiosBaseQuery({
            baseUrl: config.eventManager.baseUrl
          });

          const events = flatMap(
            await Promise.all<EventTypes.Event[]>(
              chunkedRanges.map(async range => {
                const { data } = await baseQuery(
                  {
                    requestConfig: {
                      ...config.eventManager.services
                        .findEventsByAssociatedSignalDetectionHypotheses.requestConfig,
                      data: {
                        signalDetectionHypotheses: signalDetectionHypotheses.slice(
                          range.start,
                          range.end
                        ),
                        stageId
                      }
                    }
                  },
                  undefined,
                  undefined
                );

                if (data) {
                  return Promise.resolve(data);
                }
                return Promise.resolve([] as EventTypes.Event[]);
              })
            )
          );
          return Promise.resolve({ data: events });
        }
      })
    };
  }
});

export const { useUpdateEventStatusMutation } = eventManagerApiSlice;
export type UpdateEventMutation = ReturnType<
  typeof eventManagerApiSlice.useUpdateEventStatusMutation
>;

export type PredictFeaturesForLocationSolutionQuery = UseQueryStateResult<
  EventTypes.LocationSolution
>;
export type PredictFeaturesForEventLocationQuery = UseQueryStateResult<
  PredictFeaturesForEventLocationResponse
>;

export type FindEventStatusInfoByStageIdAndEventIdsQuery = UseQueryStateResult<
  Record<string, EventStatus>
>;

export interface PredictFeaturesForLocationSolutionQueryProps {
  featurePredictionQuery: PredictFeaturesForLocationSolutionQuery;
}

export interface PredictFeaturesForEventLocationQueryProps {
  featurePredictionQuery: PredictFeaturesForEventLocationQuery;
}

/**
 * The usePredictFeaturesForLocationSolutionQuery hook.
 * Wraps the hook from the slice to allow for reuse of
 * configuration, i.e. specifying when to skip the query.
 *
 * @param data the query props data
 * @returns the predict features for location solution query. If the query is skipped, returns `null` for the data.
 */
export const usePredictFeaturesForLocationSolutionQuery = (
  data: PredictFeaturesForLocationSolutionProps
): PredictFeaturesForLocationSolutionQuery => {
  const skip = (data?.channels?.length ?? 0) === 0 || data?.locationSolution == null;

  return useProduceAndHandleSkip<EventTypes.LocationSolution>(
    eventManagerApiSlice.usePredictFeaturesForLocationSolutionQuery(data, { skip }),
    skip
  );
};

/**
 * The usePredictFeaturesForLocationSolutionQuery hook.
 * Wraps the hook from the slice to allow for reuse of
 * configuration, i.e. specifying when to skip the query.
 *
 * @param data the query props data
 * @returns the predict features for location solution query. If the query is skipped, returns `null` for the data.
 */
export const usePredictFeaturesForEventLocationQuery = (
  data: PredictFeaturesForEventLocationProps
): PredictFeaturesForEventLocationQuery => {
  const skip =
    (data?.receivers.length ?? 0) === 0 ||
    data?.sourceLocation == null ||
    data?.phases == null ||
    data?.phases[0] == null ||
    (data?.phases.length ?? 0) === 0;

  return useProduceAndHandleSkip<PredictFeaturesForEventLocationResponse>(
    eventManagerApiSlice.usePredictFeaturesForEventLocationQuery(data, { skip }),
    skip
  );
};

export type UpdateEventStatusMutation = ReturnType<
  typeof eventManagerApiSlice.useUpdateEventStatusMutation
>;

const updateEventStatusMutation: UpdateEventStatusMutation = undefined;
// eslint-disable-next-line @typescript-eslint/no-magic-numbers
export type UpdateEventStatusMutationFunc = typeof updateEventStatusMutation[0];

/**
 * The useFindEventsByAssociatedSignalDetectionHypothesesQuery hook
 * Wraps the hook from the slice to allow skipping the query
 */

export const useFindEventsByAssociatedSignalDetectionHypothesesQuery = (
  data: FindEventsByAssociatedSignalDetectionHypothesesProps
): FindEventsByAssociatedSignalDetectionHypothesesQuery => {
  const skip = data.signalDetectionHypotheses.length === 0 || data.stageId == null;

  return useProduceAndHandleSkip<EventTypes.Event[]>(
    eventManagerApiSlice.useFindEventsByAssociatedSignalDetectionHypothesesQuery(data, {
      skip
    }),
    skip
  );
};
