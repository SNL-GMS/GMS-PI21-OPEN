import type { SohTypes } from '@gms/common-model';
import { axiosBaseQuery } from '@gms/ui-workers';
import { createApi } from '@reduxjs/toolkit/query/react';

import type { UseQueryStateResult } from '../../query';
import { useProduceAndHandleSkip } from '../../query/util';
import { config } from './endpoint-configuration';

/**
 * The SOH ACEI api reducer slice.
 */
export const sohAceiApiSlice = createApi({
  reducerPath: 'sohAceiApi',
  baseQuery: axiosBaseQuery({
    baseUrl: config.sohAcei.baseUrl
  }),
  endpoints(build) {
    return {
      /**
       * defines the SOH historical acei data query
       */
      getHistoricalAceiData: build.query<
        SohTypes.UiHistoricalAcei[],
        SohTypes.UiHistoricalAceiInput
      >({
        query: (data: SohTypes.UiHistoricalAceiInput) => ({
          requestConfig: {
            ...config.sohAcei.services.getHistoricalAceiData.requestConfig,
            data
          }
        })
      })
    };
  }
});

/**
 * The useGetHistoricalAceiDataQuery hook. Returns the ACEI Historical data.
 *
 * Wraps the original hook from the api slice to allow for reuse of
 * configuration, i.e. specifying when to skip the query.
 *
 * ! this query will be skipped (not executed) if the station name, time range or monitor type are missing
 *
 * @param data the UiHistoricalAceiInput i.e. station name, start and end times and monitor type
 * @returns the results from the query
 */
export const useGetHistoricalAceiDataQuery = (
  data: SohTypes.UiHistoricalAceiInput
): HistoricalAceiDataQuery => {
  const skip =
    data.stationName === undefined ||
    data.startTime === undefined ||
    data.endTime === undefined ||
    data.type === undefined;

  return useProduceAndHandleSkip<SohTypes.UiHistoricalAcei[]>(
    sohAceiApiSlice.useGetHistoricalAceiDataQuery(data, { skip }),
    skip
  );
};

export type HistoricalAceiDataQuery = UseQueryStateResult<SohTypes.UiHistoricalAcei[]>;

export interface SohAceiQueryProps {
  historicalAceiDataQuery: HistoricalAceiDataQuery;
}
