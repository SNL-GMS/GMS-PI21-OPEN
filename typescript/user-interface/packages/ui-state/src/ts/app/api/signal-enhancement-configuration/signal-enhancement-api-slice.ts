import type { FilterListTypes } from '@gms/common-model';
import { axiosBaseQuery } from '@gms/ui-workers';
import { createApi } from '@reduxjs/toolkit/query/react';

import { config } from './endpoint-configuration';

/**
 * The signal Enhancement configuration api reducer slice.
 */
export const signalEnhancementConfigurationApiSlice = createApi({
  reducerPath: 'signalEnhancementConfigurationApi',
  baseQuery: axiosBaseQuery({
    baseUrl: config.signalEnhancementConfiguration.baseUrl
  }),
  endpoints(build) {
    return {
      /**
       * defines the signal enhancement configuration query for the filter lists definition
       */
      getFilterListsDefinition: build.query<FilterListTypes.FilterListsDefinition, void>({
        query: () => ({
          requestConfig: {
            ...config.signalEnhancementConfiguration.services.getSignalEnhancementConfiguration
              .requestConfig
          }
        })
      })
    };
  }
});

export const { useGetFilterListsDefinitionQuery } = signalEnhancementConfigurationApiSlice;
