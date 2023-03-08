import type { CommonTypes } from '@gms/common-model';
import React from 'react';

import type { OperationalTimePeriodConfigurationQuery } from '../api/processing-configuration/processing-configuration-api-slice';
import {
  useGetOperationalTimePeriodConfigurationQuery,
  useGetProcessingAnalystConfigurationQuery
} from '../api/processing-configuration/processing-configuration-api-slice';
import { useAppSelector } from './react-redux-hooks';

/**
 * A hook that provides the operational time range.
 *
 * @param operationalPeriodStart the operational period start duration
 * @param operationalPeriodEnd the operational period end duration
 * @returns the operational time range
 */
export const useOperationalTimePeriodTimeRange = (
  operationalPeriodStart: number,
  operationalPeriodEnd: number
): CommonTypes.TimeRange | undefined => {
  const nowSecs = useAppSelector(state => {
    return state.app.analyst.effectiveNowTime;
  });
  // ! memoize the time range so that the queries are not called multiple times
  return React.useMemo(
    () =>
      operationalPeriodStart !== undefined && operationalPeriodEnd !== undefined
        ? {
            startTimeSecs: nowSecs - operationalPeriodStart,
            endTimeSecs: nowSecs - operationalPeriodEnd
          }
        : undefined,
    [operationalPeriodStart, operationalPeriodEnd, nowSecs]
  );
};

/**
 * A hook that returns both the operational time period configuration query and the
 * calculated operational time range.
 *
 * @returns operational time period configuration query and the time range
 */
export const useOperationalTimePeriodConfiguration = (): {
  timeRange: CommonTypes.TimeRange | undefined;
  operationalTimePeriodConfigurationQuery: OperationalTimePeriodConfigurationQuery;
} => {
  const operationalTimePeriodConfigurationQuery = useGetOperationalTimePeriodConfigurationQuery();
  const timeRange = useOperationalTimePeriodTimeRange(
    operationalTimePeriodConfigurationQuery.data?.operationalPeriodStart,
    operationalTimePeriodConfigurationQuery.data?.operationalPeriodEnd
  );

  return {
    timeRange,
    operationalTimePeriodConfigurationQuery
  };
};

/**
 * @returns the effective time, determined by the current interval, or the operational
 * time range, if no interval is open.
 */
export const useEffectiveTime = (): number => {
  const { current: initTime } = React.useRef(Date.now() / 1000);
  const effectiveNow = useAppSelector(state => state.app.analyst.effectiveNowTime);
  // current interval and effective time
  const currentInterval = useAppSelector(state => state.app.workflow.timeRange);
  return currentInterval?.startTimeSecs ?? effectiveNow ?? initTime;
};

/**
 * returns the current interval with the lead and lag buffer duration applied
 * returns a TimeRange with undefined for the values if there is no current interval
 *
 * @returns CommonTypes.TimeRange
 */
export const useCurrentIntervalWithBuffer = (): CommonTypes.TimeRange => {
  const currentInterval = useAppSelector(state => state.app.workflow.timeRange);
  const processingAnalystConfigurationQuery = useGetProcessingAnalystConfigurationQuery();
  const leadBufferDuration = processingAnalystConfigurationQuery.data?.leadBufferDuration ?? 0;
  const lagBufferDuration = processingAnalystConfigurationQuery.data?.lagBufferDuration ?? 0;

  let startTime;
  let endTime;
  if (
    !currentInterval ||
    currentInterval.startTimeSecs == null ||
    currentInterval.endTimeSecs == null
  ) {
    startTime = null;
    endTime = null;
  } else {
    startTime = currentInterval.startTimeSecs - leadBufferDuration;
    endTime = currentInterval.endTimeSecs + lagBufferDuration;
  }

  return { startTimeSecs: startTime, endTimeSecs: endTime };
};
