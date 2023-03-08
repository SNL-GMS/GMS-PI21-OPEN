import type { WeavessTypes } from '@gms/weavess-core';
import type { AxiosRequestConfig } from 'axios';

import { WorkerOperations } from '../waveform-worker/operations';
import type { EventsWithDetectionsAndSegmentsFetchResults } from '../waveform-worker/operations/fetch-events-detections-segments-by-time';
import { waveformWorkerRpc } from '../worker-rpcs';

/**
 * The Worker API for fetching events with detections and segments by time.
 *
 * @param requestConfig the request config
 * @param currentInterval the current interval
 * @param colors the color settings
 *
 * @throws {@link Error} any exceptions
 * @throws {@link Error} any Axios request/response failures
 *
 * @returns the fetch result containing events with detections and segments
 */
export const fetchEventsWithDetectionsAndSegmentsByTime = async (
  requestConfig: AxiosRequestConfig,
  currentInterval: WeavessTypes.TimeRange,
  colors: {
    waveformColor: string;
    labelTextColor: string;
  }
): Promise<EventsWithDetectionsAndSegmentsFetchResults> =>
  waveformWorkerRpc.rpc(WorkerOperations.FETCH_EVENTS_WITH_DETECTIONS_AND_SEGMENTS_BY_TIME, {
    originalDomain: currentInterval,
    requestConfig,
    colors
  });
