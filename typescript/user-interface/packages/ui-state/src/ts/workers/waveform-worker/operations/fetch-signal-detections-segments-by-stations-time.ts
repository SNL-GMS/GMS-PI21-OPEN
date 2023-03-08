import type { ChannelSegmentTypes, SignalDetectionTypes, WaveformTypes } from '@gms/common-model';
import { UILogger } from '@gms/ui-util';
import { axiosBaseQuery } from '@gms/ui-workers';
import type { WeavessTypes } from '@gms/weavess-core';
import type { AxiosRequestConfig } from 'axios';

import type { UiChannelSegment } from '../../../types';
import type { ChannelSegmentColorOptions } from '../types';
import { convertChannelSegmentsToWeavessTypedArrays } from '../util/channel-segment-util';
import { addController, removeController } from './cancel-worker-requests';

const logger = UILogger.create(
  'GMS_LOG_FETCH_SIGNAL_DETECTION',
  process.env.GMS_LOG_FETCH_SIGNAL_DETECTION
);

/**
 * Signal detection fetch type returned by worker fetch signal detection[] and ChannelSegment[]
 */
export interface SignalDetectionWithSegmentsFetchResults {
  signalDetections: SignalDetectionTypes.SignalDetection[];
  uiChannelSegments: UiChannelSegment[];
}

export interface FetchSignalDetectionWithSegmentsParameters {
  originalDomain: WeavessTypes.TimeRange;
  requestConfig: AxiosRequestConfig;
  colors: ChannelSegmentColorOptions;
}

/**
 * Sends a request to the server using the provided request configuration.
 * Converts the returned data to the TypedArray format Weavess requires.
 *
 * @param requestConfig the request configuration
 * @throws {@link Error} any exceptions
 * @throws {@link Error} any Axios request/response failures
 */
export const requestSignalDetectionsWithSegments = async (
  requestConfig: AxiosRequestConfig,
  originalDomain: WeavessTypes.TimeRange,
  colors: ChannelSegmentColorOptions
): Promise<SignalDetectionWithSegmentsFetchResults> => {
  if (!requestConfig.baseURL) {
    return Promise.reject(
      new Error('Cannot make a request on the worker without a baseUrl in the config')
    );
  }
  const controller = new AbortController();
  try {
    const queryFn = axiosBaseQuery<{
      signalDetections: SignalDetectionTypes.SignalDetection[];
      channelSegments: ChannelSegmentTypes.ChannelSegment<WaveformTypes.Waveform>[];
    }>({
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
    return {
      signalDetections: result.data?.signalDetections ?? [],
      uiChannelSegments: await convertChannelSegmentsToWeavessTypedArrays(
        result.data?.channelSegments ?? [],
        originalDomain,
        colors
      )
    };
  } catch (error) {
    if (error.message !== 'canceled') {
      logger.error(`[Worker] Error fetching/loading signal detections with segments`, error);
      removeController(controller);
    }
    return Promise.reject(error);
  }
};

/**
 * Fetches signal detections with segments.
 *
 * @param params the request parameters
 * @throws {@link Error} any exceptions
 * @throws {@link Error} any Axios request/response failures
 */
export const fetchSignalDetectionsWithSegments = async (
  params: FetchSignalDetectionWithSegmentsParameters
): Promise<SignalDetectionWithSegmentsFetchResults> => {
  return requestSignalDetectionsWithSegments(
    params.requestConfig,
    params.originalDomain,
    params.colors
  );
};
