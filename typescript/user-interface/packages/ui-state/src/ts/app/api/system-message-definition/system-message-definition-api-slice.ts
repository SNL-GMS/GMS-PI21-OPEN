import type { SystemMessageTypes } from '@gms/common-model';
import { axiosBaseQuery } from '@gms/ui-workers';
import { createApi } from '@reduxjs/toolkit/query/react';

import type { UseQueryStateResult } from '../../query';
import { config } from './endpoint-configuration';

/**
 * The system message definition api reducer slice.
 */
export const systemMessageDefinitionApiSlice = createApi({
  reducerPath: 'systemMessageDefinitionApi',
  baseQuery: axiosBaseQuery({
    baseUrl: config.systemMessageDefinition.baseUrl
  }),
  endpoints(build) {
    return {
      /**
       * defines the system message definition profile query
       */
      getSystemMessageDefinition: build.query<SystemMessageTypes.SystemMessageDefinition[], void>({
        query: () => ({
          requestConfig:
            config.systemMessageDefinition.services.getSystemMessageDefinitions.requestConfig
        })
      })
    };
  }
});

export const { useGetSystemMessageDefinitionQuery } = systemMessageDefinitionApiSlice;

export type SystemMessageDefinitionQuery = UseQueryStateResult<
  SystemMessageTypes.SystemMessageDefinition[]
>;

export interface SystemMessageDefinitionsQueryProps {
  systemMessageDefinitionsQuery: SystemMessageDefinitionQuery;
}
