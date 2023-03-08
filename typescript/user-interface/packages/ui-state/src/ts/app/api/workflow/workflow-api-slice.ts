import type { CommonTypes, WorkflowTypes } from '@gms/common-model';
import { Logger, MILLISECONDS_IN_SECOND, uuid } from '@gms/common-util';
import { axiosBaseQuery } from '@gms/ui-workers';
import type { AnyAction } from '@reduxjs/toolkit';
import type { MaybeDrafted } from '@reduxjs/toolkit/dist/query/core/buildThunks';
import { createApi } from '@reduxjs/toolkit/query/react';
import React from 'react';
import { useDispatch } from 'react-redux';

import type { UseQueryStateResult } from '../../query';
import { useProduceAndHandleSkip } from '../../query/util';
import { analystActions } from '../../state/analyst/analyst-slice';
import { Subscription } from '../../subscription';
import { config } from './endpoint-configuration';

const logger = Logger.create('GMS_LOG_WORKFLOW', process.env.GMS_LOG_WORKFLOW);
const subscriberId = `stage-intervals-by-id-and-time-${uuid.asString()}`;

export type StageIntervalList = { name: string; value: WorkflowTypes.StageInterval[] }[];

/**
 * Defines the parameters for the StageIntervalsByIdAndTime query.
 */
export interface StageIntervalsByIdAndTimeParams {
  stageIds: {
    name: string;
  }[];
  startTime: number;
  endTime: number;
}

/**
 * Defines the parameters for the UpdateActivityIntervalStatus mutation.
 */
export interface UpdateActivityIntervalStatusParams {
  userName: string;
  time: number;
  activityIntervalId: WorkflowTypes.IntervalId;
  stageIntervalId: WorkflowTypes.IntervalId;
  status: WorkflowTypes.IntervalStatus;
}

/**
 * Defines the parameters for the UpdateStageIntervalStatus mutation.
 */
export interface UpdateStageIntervalStatusParams {
  userName: string;
  time: number;
  stageIntervalId: WorkflowTypes.IntervalId;
  status: WorkflowTypes.IntervalStatus;
}

const isStageIntervalMatched = (s1: WorkflowTypes.StageInterval, s2: WorkflowTypes.StageInterval) =>
  s1.name === s2.name &&
  s1.stageMode === s2.stageMode &&
  s1.startTime === s2.startTime &&
  s1.endTime === s2.endTime;

const mergeStageIntervals = (
  data: WorkflowTypes.StageInterval[],
  stageInterval: WorkflowTypes.StageInterval
) => {
  // Update the Stage Interval list with the updated/new Stage Interval
  let foundInCache = false;
  const newData: WorkflowTypes.StageInterval[] =
    data.map(si => {
      // if the name, stageMode, startTime and endTime match,
      // and modification time is after the cached entry, it is an update
      if (isStageIntervalMatched(si, stageInterval)) {
        foundInCache = true;
        if (si.modificationTime <= stageInterval.modificationTime) {
          return stageInterval;
        }
      }
      return si;
    }) ?? [];

  // If data was not an update then add as a new entry
  if (!foundInCache) {
    newData.push(stageInterval);
  }
  return newData;
};

/**
 * Updates the stage intervals in the store.
 *
 * @param stageIntervals the stage intervals to update or add
 * @returns a mutation function that is used with immer
 */
export const updateStageIntervals = (stageIntervals: WorkflowTypes.StageInterval[]) => (
  draft: MaybeDrafted<StageIntervalList>
): void => {
  if (draft && stageIntervals) {
    stageIntervals.forEach(stageInterval => {
      // If no map or map entry not found
      if (!draft.find(si => si.name === stageInterval.name)) {
        logger.warn(`Query cache does not have stage ${stageInterval.name}`);
        return;
      }

      // Get the Stage Interval list for the relevant Stage
      const cacheStageIntervals: WorkflowTypes.StageInterval[] = draft.find(
        si => si.name === stageInterval.name
      ).value;

      const newData = mergeStageIntervals(cacheStageIntervals, stageInterval);

      // update the data with the updated values
      draft.find(si => si.name === stageInterval.name).value = newData;
    });
  }
};

/**
 * The workflow api reducer slice.
 */
export const workflowApiSlice = createApi({
  reducerPath: 'workflowApi',
  baseQuery: axiosBaseQuery({
    baseUrl: config.workflow.baseUrl
  }),
  endpoints(build) {
    return {
      /**
       * defines the workflow query
       */
      workflow: build.query<WorkflowTypes.Workflow, void>({
        query: () => ({ requestConfig: config.workflow.services.workflow.requestConfig })
      }),

      /**
       * defines the stageIntervalsByIdAndTime query
       */
      stageIntervalsByIdAndTime: build.query<StageIntervalList, StageIntervalsByIdAndTimeParams>({
        query: (data: StageIntervalsByIdAndTimeParams) => ({
          requestConfig: {
            ...config.workflow.services.stageIntervalsByIdAndTime.requestConfig,
            data
          }
        }),
        transformResponse: (responseData: { [s: string]: WorkflowTypes.StageInterval[] }) => {
          const stageIntervals: StageIntervalList = [];

          Object.entries<WorkflowTypes.StageInterval[]>(responseData).forEach(([name, value]) => {
            stageIntervals.push({ name, value });
          });

          return stageIntervals;
        },
        async onCacheEntryAdded(
          data,
          { dispatch, updateCachedData, cacheDataLoaded, cacheEntryRemoved }
        ) {
          // Callback from the subscription list of StageInterval
          const onMessage: (
            stageIntervals: WorkflowTypes.StageInterval[]
          ) => void = stageIntervals => {
            if (!stageIntervals || stageIntervals.length === 0) {
              return;
            }
            updateCachedData(updateStageIntervals(stageIntervals));
          };

          const onOpen = (e: Event, isReconnect: boolean): void => {
            logger.debug(`Workflow subscription open (reconnected: ${isReconnect})`);
            if (isReconnect) {
              dispatch(analystActions.setEffectiveNowTime(Date.now() / MILLISECONDS_IN_SECOND));
            }
          };

          try {
            // wait for the initial query to resolve before proceeding
            await cacheDataLoaded;

            Subscription.addSubscriber(subscriberId, 'intervals', onMessage, onOpen);
            logger.debug(`Workflow subscription subscribed ${subscriberId}`);

            // cacheEntryRemoved will resolve when the cache subscription is no longer active
            await cacheEntryRemoved;
          } catch (e) {
            Subscription.removeSubscriber(subscriberId, 'intervals');
            logger.error(`Failed to establish websocket connection ${subscriberId}`, e);
          }
        }
      }),

      /**
       * defines the updateActivityIntervalStatus mutation
       */
      updateActivityIntervalStatus: build.mutation<void, UpdateActivityIntervalStatusParams>({
        query: (data: UpdateActivityIntervalStatusParams) => ({
          requestConfig: {
            ...config.workflow.services.updateActivityIntervalStatus.requestConfig,
            data
          }
        })
      }),

      /**
       * defines the updateStageIntervalStatus mutation
       */
      updateStageIntervalStatus: build.mutation<void, UpdateStageIntervalStatusParams>({
        query: (data: UpdateStageIntervalStatusParams) => ({
          requestConfig: {
            ...config.workflow.services.updateStageIntervalStatus.requestConfig,
            data
          }
        })
      })
    };
  }
});

export const {
  useWorkflowQuery,
  useUpdateActivityIntervalStatusMutation,
  useUpdateStageIntervalStatusMutation
} = workflowApiSlice;

/**
 * Workflow query type.
 */
export type WorkflowQuery = UseQueryStateResult<WorkflowTypes.Workflow>;

/**
 * StageIntervalsByIdAndTime query type.
 */
export type StageIntervalsByIdAndTimeQuery = UseQueryStateResult<StageIntervalList>;

/**
 * Helper function for formatting data for the StageIntervalsByIdAndTime query.
 *
 * @param stageNames the stage names array
 * @param timeRange the time range
 * @returns returns the parameters for the StageIntervalsByIdAndTime query
 */
export const useGetStageIntervalsByIdAndTimeParams = (
  stageNames: string[],
  timeRange: CommonTypes.TimeRange
): StageIntervalsByIdAndTimeParams =>
  React.useMemo(
    () => ({
      stageIds: stageNames.map(name => ({ name })).sort(),
      startTime: timeRange?.startTimeSecs,
      endTime: timeRange?.endTimeSecs
    }),
    [stageNames, timeRange?.endTimeSecs, timeRange?.startTimeSecs]
  );

/**
 * The useStageIntervalsByIdAndTimeQuery hook.
 * Wraps the hook from the workflow api slice to allow for reuse of
 * configuration, i.e. specifying when to skip the query.
 *
 * @param stageNames the stage names array
 * @param timeRange the time range
 * @returns the results from the StageIntervalsByIdAndTime query. If skipped, the returned data will be set to `null`.
 */
export const useStageIntervalsByIdAndTimeQuery = (
  stageNames: string[],
  timeRange: CommonTypes.TimeRange
): StageIntervalsByIdAndTimeQuery => {
  const skip =
    timeRange === undefined ||
    timeRange.startTimeSecs === undefined ||
    timeRange.endTimeSecs === undefined ||
    timeRange.startTimeSecs >= timeRange.endTimeSecs ||
    stageNames === undefined ||
    stageNames.length <= 0;
  const data = useGetStageIntervalsByIdAndTimeParams(stageNames, timeRange);
  return useProduceAndHandleSkip<StageIntervalList>(
    workflowApiSlice.useStageIntervalsByIdAndTimeQuery(data, { skip }),
    skip
  );
};

/**
 * Creates an immer draft producer for cleaning up stale interval data.
 *
 * @param staleCleanUpBoundary the stale boundary to clean up
 * @returns immer draft producer
 */
export const produceCleanUp = (staleCleanUpBoundary: number | undefined) => (
  draft: MaybeDrafted<StageIntervalList>
): void => {
  if (staleCleanUpBoundary !== undefined && draft) {
    draft.forEach((entry, index) => {
      if (entry?.value == null) {
        return;
      }
      entry.value.forEach(v => {
        if (v.startTime >= v.endTime) {
          logger.error(`Invalid stage interval data, start time is >= end time`, entry, v);
          throw new Error(`Invalid stage interval data, start time is greater than end time`);
        }
      });
      const updated = entry.value.filter(v => v.endTime > staleCleanUpBoundary);
      if (updated.length !== entry.value.length) {
        draft[index].value = updated;
      }
    });
  }
};

/**
 * The useCleanupStageIntervalsByIdAndTimeQuery hook that is used to clean up stage
 * intervals from the store over time.
 *
 * @param stageNames the stage names array
 * @param timeRange the time range
 * @returns returns a function that can be easily used to clean up the store
 */
export const useCleanupStageIntervalsByIdAndTimeQuery = (
  stageNames: string[],
  timeRange: CommonTypes.TimeRange
): ((staleCleanUpBoundary: number) => void) => {
  const dispatch = useDispatch();
  const data = useGetStageIntervalsByIdAndTimeParams(stageNames, timeRange);
  return React.useMemo(
    () => (staleCleanUpBoundary: number): void => {
      const patch = dispatch(
        (workflowApiSlice.util.updateQueryData(
          'stageIntervalsByIdAndTime',
          data,
          produceCleanUp(staleCleanUpBoundary)
        ) as unknown) as AnyAction
      );
      logger.debug('Cleaning up stale workflow data', patch);
    },
    [data, dispatch]
  );
};

export type UpdateStageIntervalStatusMutation = ReturnType<
  typeof workflowApiSlice.useUpdateStageIntervalStatusMutation
>;

const updateStageIntervalStatusMutation: UpdateStageIntervalStatusMutation = undefined;
// eslint-disable-next-line @typescript-eslint/no-magic-numbers
export type UpdateStageIntervalStatusMutationFunc = typeof updateStageIntervalStatusMutation[0];

export type UpdateActivityIntervalStatusMutation = ReturnType<
  typeof workflowApiSlice.useUpdateActivityIntervalStatusMutation
>;

const updateActivityIntervalStatusMutation: UpdateActivityIntervalStatusMutation = undefined;
// eslint-disable-next-line @typescript-eslint/no-magic-numbers
export type UpdateActivityIntervalStatusMutationFunc = typeof updateActivityIntervalStatusMutation[0];

export type CleanupStageIntervalsByIdAndTimeQuery = ReturnType<
  typeof useCleanupStageIntervalsByIdAndTimeQuery
>;
