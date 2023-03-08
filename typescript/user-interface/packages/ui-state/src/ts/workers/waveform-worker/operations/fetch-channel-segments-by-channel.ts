import type { ChannelSegmentTypes, WaveformTypes } from '@gms/common-model';
import { UILogger } from '@gms/ui-util';
import { axiosBaseQuery } from '@gms/ui-workers';
import type { WeavessTypes } from '@gms/weavess-core';
import type { AxiosRequestConfig } from 'axios';

import type { UiChannelSegment } from '../../../types';
import type { ChannelSegmentColorOptions } from '../types';
import { convertChannelSegmentsToWeavessTypedArrays } from '../util/channel-segment-util';
import { addController, removeController } from './cancel-worker-requests';

const logger = UILogger.create('GMS_LOG_FETCH_WAVEFORMS', process.env.GMS_LOG_FETCH_WAVEFORMS);

export interface FetchWaveformParameters {
  originalDomain: WeavessTypes.TimeRange;
  requestConfig: AxiosRequestConfig;
  colors: ChannelSegmentColorOptions;
}

/**
 * Sends a request to the server using the provided request configuration and query key.
 * Uses the defaultQuery function to perform the request.
 * Validates the returned data to ensure it is of the expected type.
 * Converts the returned data to the TypedArray format Weavess requires.
 *
 * @param requestConfig the request configuration
 * @throws {@link Error} any exceptions
 * @throws {@link Error} any Axios request/response failures
 */
export const requestChannelSegments = async (
  requestConfig: AxiosRequestConfig,
  originalDomain: WeavessTypes.TimeRange,
  colors: ChannelSegmentColorOptions
): Promise<UiChannelSegment[]> => {
  if (!requestConfig.baseURL) {
    return Promise.reject(
      new Error('Cannot make a request on the worker without a baseUrl in the config')
    );
  }
  const controller = new AbortController();
  try {
    const queryFn = axiosBaseQuery<ChannelSegmentTypes.ChannelSegment<WaveformTypes.Waveform>[]>({
      baseUrl: requestConfig.baseURL
    });
    addController(controller);
    // ! pass undefined as the second and third args because our axios request doesn't use the api or extra options
    const result = await queryFn(
      {
        requestConfig: {
          ...requestConfig,
          signal: controller.signal
        }
      },
      undefined,
      undefined
    );
    removeController(controller);
    return convertChannelSegmentsToWeavessTypedArrays(result.data, originalDomain, colors);
  } catch (error) {
    if (error.message !== 'canceled') {
      logger.error(`[Worker] Error fetching/loading waveforms`, error);
      removeController(controller);
    }
    return Promise.reject(error);
  }
};

/**
 * Fetches channel segments.
 *
 * @param params the request parameters
 * @throws {@link Error} any exceptions
 * @throws {@link Error} any Axios request/response failures
 */
export const fetchChannelSegmentsByChannel = async (
  params: FetchWaveformParameters
): Promise<UiChannelSegment[]> => {
  return requestChannelSegments(params.requestConfig, params.originalDomain, params.colors);
};
