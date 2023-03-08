import { Logger, Timer } from '@gms/common-util';
import type { QueryReturnValue } from '@reduxjs/toolkit/dist/query/baseQueryTypes';
import type { BaseQueryFn } from '@reduxjs/toolkit/query/react';
import type { AxiosRequestConfig } from 'axios';
import Axios from 'axios';
import PQueue from 'p-queue';

import { defaultRequestTransformers, defaultResponseTransformers } from './axios-transformers';

const logger = Logger.create('GMS_LOG_AXIOS', process.env.GMS_LOG_AXIOS);

// TODO: Plumb in the max requests as part of the request
const maxParallelRequests = 10;

/**
 * The singleton promise queue that limits the number of in-flight requests. Requests made after the
 * limit is hit will be queued up and requested based on their priorities.
 */
const promiseQueue: PQueue = new PQueue({ concurrency: maxParallelRequests });

/**
 * Defines the arguments that are required and used by the
 * custom Axios base query for RTK.
 */
export interface Args {
  requestConfig: AxiosRequestConfig;
}

/**
 * Defines the Error type that is used by the
 * custom Axios base query for RTK.
 */
export interface Error {
  data: unknown;
  status: number;
}

/**
 * Defines any extra option arguments that are defined and used
 * by the custom Axios base query for RTK.
 *
 * ! currently the extra options are empty and not used
 */
export type ExtraOptions = Record<string, unknown>;

/**
 * Defines any meta data arguments that are defined and used
 * by the custom Axios base query for RTK.
 *
 * ! currently the meta data is empty and not used
 */
export type Meta = Record<string, unknown>;

/**
 * Defines the typed custom Axios base query function for RTK.
 *
 * ? ResultType specifies the type of the data returned by the query
 */
export type AxiosBaseQueryFn<ResultType = unknown> = BaseQueryFn<
  Args,
  ResultType,
  Error,
  ExtraOptions,
  Meta
>;

/**
 * Defines the typed result object that is returned from the
 * custom Axios base query for RTK.
 */
export type AxiosBaseQueryResult<ResultType = unknown> = Promise<
  QueryReturnValue<ResultType, Error, Meta>
>;

/**
 * A custom base base query implementation using Axios for RTK.
 *
 * @param baseUrl for the query function
 * @returns an axios base query function
 */
export function axiosBaseQuery<ResultType = unknown>(
  { baseUrl }: { baseUrl: string } = { baseUrl: '' }
): AxiosBaseQueryFn<ResultType> {
  return async ({ requestConfig }): AxiosBaseQueryResult<ResultType> => {
    const url = `${baseUrl}${requestConfig.url}`;
    return promiseQueue.add(async () => {
      try {
        Timer.start(`[axios]: query ${url} ${JSON.stringify(requestConfig)}`);
        const result = await Axios.request({
          ...requestConfig,
          url,
          // apply the default response transformers; unless the request config specifies its own
          transformResponse: requestConfig.transformResponse ?? defaultResponseTransformers,
          // apply the default request transformers; unless the request config specifies its own
          transformRequest: requestConfig.transformRequest ?? defaultRequestTransformers
        });
        Timer.end(`[axios]: query ${url} ${JSON.stringify(requestConfig)}`);
        return { data: result.data as ResultType };
      } catch (error) {
        if (error && error.message !== 'canceled') {
          logger.error('Failed Axios request:', error, requestConfig);
        }
        throw error;
      }
    });
  };
}
