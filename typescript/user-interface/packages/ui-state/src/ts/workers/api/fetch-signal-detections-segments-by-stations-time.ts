import type { WeavessTypes } from '@gms/weavess-core';
import type { AxiosRequestConfig } from 'axios';

import { WorkerOperations } from '../waveform-worker/operations';
import type { SignalDetectionWithSegmentsFetchResults } from '../waveform-worker/operations/fetch-signal-detections-segments-by-stations-time';
import { waveformWorkerRpc } from '../worker-rpcs';

/**
 * The Worker API for fetching signal detections with segments by stations and time.
 *
 * @param requestConfig the request config
 * @param currentInterval the current interval
 * @param colors the color settings
 *
 * @throws {@link Error} any exceptions
 * @throws {@link Error} any Axios request/response failures
 *
 * @returns the fetch result containing signal detections with segments
 */
export const fetchSignalDetectionsWithSegmentsByStationsAndTime = async (
  requestConfig: AxiosRequestConfig,
  currentInterval: WeavessTypes.TimeRange,
  colors: {
    waveformColor: string;
    labelTextColor: string;
  }
): Promise<SignalDetectionWithSegmentsFetchResults> =>
  waveformWorkerRpc.rpc(WorkerOperations.FETCH_SIGNAL_DETECTIONS_WITH_SEGMENTS_BY_STATIONS_TIME, {
    originalDomain: currentInterval,
    requestConfig,
    colors
  });
