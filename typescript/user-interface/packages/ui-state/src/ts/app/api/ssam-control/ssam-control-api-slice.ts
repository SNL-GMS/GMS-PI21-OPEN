import type { ConfigurationTypes, SohTypes } from '@gms/common-model';
import type { AxiosBaseQueryFn } from '@gms/ui-workers';
import { axiosBaseQuery } from '@gms/ui-workers';
import { createApi } from '@reduxjs/toolkit/query/react';

import type { UseQueryStateResult } from '../../query';
import { useProduceAndHandleSkip } from '../../query/util';
import { config } from './endpoint-configuration';
import type {
  RetrieveDecimatedHistoricalStationSohQueryProps,
  UiHistoricalSohAsTypedArray
} from './retrieve-decimated-historical-station-soh';
import { retrieveDecimatedHistoricalStationSoh } from './retrieve-decimated-historical-station-soh';
import { transformConfigurationQueryResponse } from './transform-configuration-query-response';

/**
 * The decimated historical station SOH cache.
 * !This cache is maintained and persisted along side the Redux cache.
 */
const decimatedHistoricalStationSohCache: Record<string, UiHistoricalSohAsTypedArray> = {};

/**
 * The SSAM Control api reducer slice.
 */
export const ssamControlApiSlice = createApi({
  reducerPath: 'ssamControlApi',
  baseQuery: axiosBaseQuery({
    baseUrl: config.ssamControl.baseUrl
  }),
  endpoints(build) {
    return {
      /**
       * defines the configuration query for the SOH configuration
       */
      getSohConfiguration: build.query<ConfigurationTypes.UiSohConfiguration, void>({
        query: () => ({
          requestConfig: {
            ...config.ssamControl.services.getSohConfiguration.requestConfig
          }
        }),
        transformResponse: transformConfigurationQueryResponse
      }),

      /**
       * defines the retrieve decimated historical station soh query
       */
      retrieveDecimatedHistoricalStationSoh: build.query<
        string,
        RetrieveDecimatedHistoricalStationSohQueryProps
      >({
        queryFn: async data => {
          const baseQuery: AxiosBaseQueryFn<UiHistoricalSohAsTypedArray> = axiosBaseQuery({
            baseUrl: config.ssamControl.baseUrl
          });
          const result = await retrieveDecimatedHistoricalStationSoh(
            {
              ...config.ssamControl.services.retrieveDecimatedHistoricalStationSoh.requestConfig,
              data
            },
            baseQuery
          );

          const key = JSON.stringify(data);

          // store the data in the cache outside of Redux since Float32Arrays are not serializable
          decimatedHistoricalStationSohCache[key] = result.data;

          // store just the unique query data key as the result in redux for data look ups
          return {
            data: key
          };
        },
        async onCacheEntryAdded(data, { cacheEntryRemoved }) {
          await cacheEntryRemoved;
          // redux cache has been removed; manually remove the cached decimated data
          const key = JSON.stringify(data);
          delete decimatedHistoricalStationSohCache[key];
        }
      })
    };
  }
});

export const { useGetSohConfigurationQuery } = ssamControlApiSlice;

export type SohConfigurationQuery = UseQueryStateResult<ConfigurationTypes.UiSohConfiguration>;

export type RetrieveDecimatedHistoricalStationSohQuery = Omit<
  UseQueryStateResult<UiHistoricalSohAsTypedArray>,
  'currentData'
>;

export interface SohConfigurationQueryProps {
  sohConfigurationQuery: SohConfigurationQuery;
}

/**
 * The useRetrieveDecimatedHistoricalStationSohQuery hook. Returns the
 * RetrieveDecimatedHistoricalStationSohQuery.
 *
 * Wraps the original hook from the api slice to allow for reuse of
 * configuration, i.e. specifying when to skip the query.
 *
 * ! this query will be skipped (not executed) if the following is not provided
 * !   - a valid start time
 * !   - a valid end time
 * !   - a valid station name
 * !   - a valid monitor type
 * !   - a valid samples per channel
 * !   - a valid max query interval size
 *
 * !! NOTE the return data of this query is does not include the `currentData` object because of
 * !! how the data is converted from the claim check to the actual data
 *
 * @param data the query parameter arguments (RetrieveDecimatedHistoricalStationSohQueryProps)
 * @returns the results of the query, or if skipped, the returned data will be set to `null`.
 */
// eslint-disable-next-line complexity
export const useRetrieveDecimatedHistoricalStationSohQuery = (
  data: Omit<SohTypes.RetrieveDecimatedHistoricalStationSohInput, 'samplesPerChannel'>
): RetrieveDecimatedHistoricalStationSohQuery => {
  const sohConfigurationQuery = ssamControlApiSlice.useGetSohConfigurationQuery();
  const maxHistoricalQueryIntervalSizeMs =
    sohConfigurationQuery.data?.maxHistoricalQueryIntervalSizeMs ?? 0;
  const historicalSamplesPerChannel = sohConfigurationQuery.data?.historicalSamplesPerChannel ?? 0;

  const args = {
    data: { ...data, samplesPerChannel: historicalSamplesPerChannel },
    maxQueryIntervalSize: maxHistoricalQueryIntervalSizeMs
  };

  const skip =
    !data ||
    data.startTime == null ||
    data.endTime == null ||
    data.startTime > data.endTime ||
    data.stationName == null ||
    data.sohMonitorType == null ||
    historicalSamplesPerChannel == null ||
    historicalSamplesPerChannel < 1 ||
    maxHistoricalQueryIntervalSizeMs == null ||
    maxHistoricalQueryIntervalSizeMs < 1;

  const query = useProduceAndHandleSkip<string>(
    ssamControlApiSlice.useRetrieveDecimatedHistoricalStationSohQuery(args, { skip }),
    skip
  );
  return {
    ...query,
    data: decimatedHistoricalStationSohCache[JSON.stringify(args)]
  };
};
