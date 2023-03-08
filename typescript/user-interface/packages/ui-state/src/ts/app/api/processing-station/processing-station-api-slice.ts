import type { ProcessingStationTypes } from '@gms/common-model';
import { axiosBaseQuery } from '@gms/ui-workers';
import { createApi } from '@reduxjs/toolkit/query/react';

import type { UseQueryStateResult } from '../../query';
import { useProduceAndHandleSkip } from '../../query/util';
import { config } from './endpoint-configuration';

/**
 * The processing station api reducer slice.
 */
export const processingStationApiSlice = createApi({
  reducerPath: 'processingStationApi',
  baseQuery: axiosBaseQuery({
    baseUrl: config.gateway.baseUrl
  }),
  endpoints(build) {
    return {
      /**
       * defines the post method to send processing station
       */
      getProcessingStationGroups: build.query<
        ProcessingStationTypes.ProcessingStationGroup[],
        string[]
      >({
        query: (data: string[]) => ({
          requestConfig: {
            ...config.gateway.services.getProcessingStationGroups.requestConfig,
            data
          }
        })
      })
    };
  }
});

export const useGetProcessingStationGroupsQuery = (data: string[]): ProcessingStationGroupQuery => {
  const skip = !data || data.length === 0;
  return useProduceAndHandleSkip<ProcessingStationTypes.ProcessingStationGroup[]>(
    processingStationApiSlice.useGetProcessingStationGroupsQuery(data, { skip }),
    skip
  );
};

export type getProcessingStationGroups = ReturnType<
  typeof processingStationApiSlice.useGetProcessingStationGroupsQuery
>;

export type ProcessingStationGroupQuery = UseQueryStateResult<
  ProcessingStationTypes.ProcessingStationGroup[]
>;
