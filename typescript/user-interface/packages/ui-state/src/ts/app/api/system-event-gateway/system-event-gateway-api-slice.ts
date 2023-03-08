import type { CommonTypes, SohTypes } from '@gms/common-model';
import { axiosBaseQuery } from '@gms/ui-workers';
import { createApi } from '@reduxjs/toolkit/query/react';

import { config } from './endpoint-configuration';

/**
 * The client log api reducer slice.
 */
export const systemEventGatewayApiSlice = createApi({
  reducerPath: 'systemEventGatewayApi',
  baseQuery: axiosBaseQuery({
    baseUrl: config.gateway.baseUrl
  }),
  endpoints(build) {
    return {
      /**
       * defines the post method to send client logs
       */
      clientLog: build.mutation<void, CommonTypes.ClientLogInput[]>({
        query: (data: CommonTypes.ClientLogInput[]) => ({
          requestConfig: {
            ...config.gateway.services.sendClientLogs.requestConfig,
            data
          }
        })
      }),
      acknowledgeSohStatus: build.mutation<void, SohTypes.AcknowledgedSohStatusChange[]>({
        query: (data: SohTypes.AcknowledgedSohStatusChange[]) => ({
          requestConfig: {
            ...config.gateway.services.acknowledgeSohStatus.requestConfig,
            data
          }
        })
      }),
      quietSohStatus: build.mutation<void, SohTypes.QuietedSohStatusChange[]>({
        query: (data: SohTypes.QuietedSohStatusChange[]) => ({
          requestConfig: {
            ...config.gateway.services.quietSohStatus.requestConfig,
            data
          }
        })
      })
    };
  }
});

export const { useClientLogMutation } = systemEventGatewayApiSlice;
export type ClientLogMutation = ReturnType<typeof systemEventGatewayApiSlice.useClientLogMutation>;
export const { useQuietSohStatusMutation } = systemEventGatewayApiSlice;
export type QuietSohStatuMutation = ReturnType<
  typeof systemEventGatewayApiSlice.useQuietSohStatusMutation
>;
export const { useAcknowledgeSohStatusMutation } = systemEventGatewayApiSlice;
export type AcknowledgeSohStatusMutation = ReturnType<
  typeof systemEventGatewayApiSlice.useAcknowledgeSohStatusMutation
>;
