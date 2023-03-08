import type {
  ChannelSegmentTypes,
  EventTypes,
  SignalDetectionTypes,
  WaveformTypes
} from '@gms/common-model';
import { UILogger } from '@gms/ui-util';
import { axiosBaseQuery } from '@gms/ui-workers';
import type { WeavessTypes } from '@gms/weavess-core';
import type { AxiosRequestConfig } from 'axios';

import type { UiChannelSegment } from '../../../types';
import type { ChannelSegmentColorOptions } from '../types';
import { convertChannelSegmentsToWeavessTypedArrays } from '../util/channel-segment-util';
import { addController, removeController } from './cancel-worker-requests';

const logger = UILogger.create('GMS_LOG_FETCH_EVENTS', process.env.GMS_LOG_FETCH_EVENTS);

export interface FetchEventsWithDetectionsAndSegmentsParameters {
  originalDomain: WeavessTypes.TimeRange;
  requestConfig: AxiosRequestConfig;
  colors: ChannelSegmentColorOptions;
}

/**
 * Events fetch type returned by worker.
 */
export interface EventsWithDetectionsAndSegmentsFetchResults {
  events: EventTypes.Event[];
  signalDetections: SignalDetectionTypes.SignalDetection[];
  uiChannelSegments: UiChannelSegment[];
}

/**
 * Sends a request to the server using the provided request configuration.
 * Validates the returned data to ensure it is of the expected type
 * Converts the returned channelSegment data to the TypedArray format Weavess requires.
 *
 * @param requestConfig the request configuration
 * @throws {@link Error} any exceptions
 * @throws {@link Error} any Axios request/response failures
 */
export const requestEventsAndDetectionsWithSegments = async (
  requestConfig: AxiosRequestConfig,
  originalDomain: WeavessTypes.TimeRange,
  colors: ChannelSegmentColorOptions
): Promise<EventsWithDetectionsAndSegmentsFetchResults> => {
  if (!requestConfig.baseURL) {
    return Promise.reject(
      new Error('Cannot make a request on the worker without a baseUrl in the config')
    );
  }
  let result;
  const controller = new AbortController();
  try {
    const queryFn = axiosBaseQuery<{
      events: EventTypes.Event[];
      signalDetections: SignalDetectionTypes.SignalDetection[];
      channelSegments: ChannelSegmentTypes.ChannelSegment<WaveformTypes.Waveform>[];
    }>({
      baseUrl: requestConfig.baseURL
    });
    addController(controller);
    // ! pass undefined as the second and third args because our axios request doesn't use the api or extra options
    result = await queryFn(
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
      events: result?.data?.events ?? [],
      signalDetections: result?.data?.signalDetections ?? [],
      uiChannelSegments: await convertChannelSegmentsToWeavessTypedArrays(
        result?.data?.channelSegments ?? [],
        originalDomain,
        colors
      )
    };
  } catch (error) {
    if (error.message !== 'canceled') {
      logger.error(`[Worker] Error fetching/loading events and detections with segments`, error);
      removeController(controller);
    }
    return Promise.reject(error);
  }
};

/**
 * Fetches events and detections with segments.
 *
 * @param params the request parameters
 * @throws {@link Error} any exceptions
 * @throws {@link Error} any Axios request/response failures
 */
export const fetchEventsAndDetectionsWithSegments = async (
  params: FetchEventsWithDetectionsAndSegmentsParameters
): Promise<EventsWithDetectionsAndSegmentsFetchResults> => {
  return requestEventsAndDetectionsWithSegments(
    params.requestConfig,
    params.originalDomain,
    params.colors
  );
};
