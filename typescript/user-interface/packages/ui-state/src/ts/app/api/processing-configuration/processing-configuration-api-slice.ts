import { ConfigurationTypes } from '@gms/common-model';
import { axiosBaseQuery } from '@gms/ui-workers';
import { createApi } from '@reduxjs/toolkit/query/react';

import type { UseQueryStateResult } from '../../query/types';
import { config } from './endpoint-configuration';

/**
 * The processing configuration api reducer slice.
 */
export const processingConfigurationApiSlice = createApi({
  reducerPath: 'processingConfigurationApi',
  baseQuery: axiosBaseQuery({
    baseUrl: config.processingConfiguration.baseUrl
  }),
  endpoints(build) {
    return {
      /**
       * defines the processing configuration query for the common configuration
       */
      getProcessingCommonConfiguration: build.query<
        ConfigurationTypes.ProcessingCommonConfiguration,
        void
      >({
        query: () => ({
          requestConfig: {
            ...config.processingConfiguration.services.getProcessingConfiguration.requestConfig,
            data: {
              configName: ConfigurationTypes.CommonConfigs.DEFAULT,
              selectors: []
            }
          }
        })
      }),

      /**
       * defines the processing configuration query for the operational time period configuration
       */
      getOperationalTimePeriodConfiguration: build.query<
        ConfigurationTypes.OperationalTimePeriodConfiguration,
        void
      >({
        query: () => ({
          requestConfig: {
            ...config.processingConfiguration.services.getProcessingConfiguration.requestConfig,
            data: {
              configName: ConfigurationTypes.OperationalTimePeriodConfigs.DEFAULT,
              selectors: []
            }
          }
        })
      }),

      /**
       * defines the processing configuration query for the analyst configuration
       */
      getProcessingAnalystConfiguration: build.query<
        ConfigurationTypes.ProcessingAnalystConfiguration,
        void
      >({
        query: () => ({
          requestConfig: {
            ...config.processingConfiguration.services.getProcessingConfiguration.requestConfig,
            data: {
              configName: ConfigurationTypes.AnalystConfigs.DEFAULT,
              selectors: []
            }
          }
        })
      }),

      /**
       * defines the processing configuration query for the station group names
       */
      getProcessingStationGroupNamesConfiguration: build.query<
        ConfigurationTypes.StationGroupNamesConfiguration,
        void
      >({
        query: () => ({
          requestConfig: {
            ...config.processingConfiguration.services.getProcessingConfiguration.requestConfig,
            data: {
              configName: ConfigurationTypes.StationGroupNamesConfig.DEFAULT,
              selectors: []
            }
          }
        })
      }),
      /**
       * defines the processing configuration query for the SohControl station group names
       */
      getProcessingSohControlStationGroupNamesConfiguration: build.query<
        ConfigurationTypes.StationGroupNamesConfiguration,
        void
      >({
        query: () => ({
          requestConfig: {
            ...config.processingConfiguration.services.getProcessingConfiguration.requestConfig,
            data: {
              configName: ConfigurationTypes.SohControlStationGroupNamesConfig.DEFAULT,
              selectors: []
            }
          }
        })
      })
    };
  }
});

export const {
  useGetProcessingCommonConfigurationQuery,
  useGetOperationalTimePeriodConfigurationQuery,
  useGetProcessingAnalystConfigurationQuery,
  useGetProcessingStationGroupNamesConfigurationQuery,
  useGetProcessingSohControlStationGroupNamesConfigurationQuery
} = processingConfigurationApiSlice;

export type ProcessingCommonConfigurationQuery = UseQueryStateResult<
  ConfigurationTypes.ProcessingCommonConfiguration
>;

export interface ProcessingCommonConfigurationQueryProps {
  processingCommonConfigurationQuery: ProcessingCommonConfigurationQuery;
}

export type OperationalTimePeriodConfigurationQuery = UseQueryStateResult<
  ConfigurationTypes.OperationalTimePeriodConfiguration
>;

export interface OperationalTimePeriodConfigurationQueryProps {
  operationalTimePeriodConfigurationQuery: OperationalTimePeriodConfigurationQuery;
}

export type ProcessingAnalystConfigurationQuery = UseQueryStateResult<
  ConfigurationTypes.ProcessingAnalystConfiguration
>;

export interface ProcessingAnalystConfigurationQueryProps {
  processingAnalystConfigurationQuery: ProcessingAnalystConfigurationQuery;
}

export type ProcessingStationGroupNamesConfigurationQuery = UseQueryStateResult<
  ConfigurationTypes.StationGroupNamesConfiguration
>;

export interface ProcessingStationGroupNamesConfigurationQueryProps {
  processingStationGroupNamesConfigurationQuery: ProcessingStationGroupNamesConfigurationQuery;
}

export type ProcessingSohControlStationGroupNamesConfigurationQuery = UseQueryStateResult<
  ConfigurationTypes.StationGroupNamesConfiguration
>;

export interface ProcessingSohControlStationGroupNamesConfigurationQueryProps {
  processingSohControlStationGroupNamesConfigurationQuery: ProcessingSohControlStationGroupNamesConfigurationQuery;
}
