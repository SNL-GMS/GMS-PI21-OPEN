import type { WeavessTypes } from '@gms/weavess-core';
import type { AxiosRequestConfig } from 'axios';

import type { UiChannelSegment } from '../../types';
import { WorkerOperations } from '../waveform-worker/operations';
import { waveformWorkerRpc } from '../worker-rpcs';

/**
 * The Worker API for fetching channel segments by channel and time.
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
export const fetchChannelSegmentsByChannel = async (
  requestConfig: AxiosRequestConfig,
  currentInterval: WeavessTypes.TimeRange,
  colors: {
    waveformColor: string;
    labelTextColor: string;
  }
): Promise<UiChannelSegment[]> =>
  waveformWorkerRpc.rpc(WorkerOperations.FETCH_CHANNEL_SEGMENTS_BY_CHANNEL, {
    originalDomain: currentInterval,
    requestConfig,
    colors
  });
