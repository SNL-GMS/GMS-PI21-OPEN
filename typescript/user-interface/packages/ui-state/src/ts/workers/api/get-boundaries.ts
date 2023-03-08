import type { WeavessTypes } from '@gms/weavess-core';

import { WorkerOperations } from '../waveform-worker/operations/operations';
import { waveformWorkerRpc } from '../worker-rpcs';

/**
 * Gets the boundaries for the channel segment. Makes a request to the WaveformWorker for
 * these boundaries, which will be calculated if they are not yet cached.
 *
 * @param channelName the name of the channel for which to get the boundaries
 * @returns the computed boundaries for the channel segment (min, max, etc)
 */
export const getBoundaries = async (
  channelSegment?: WeavessTypes.ChannelSegment,
  startTimeSecs?: number,
  endTimeSecs?: number
): Promise<WeavessTypes.ChannelSegmentBoundaries> =>
  waveformWorkerRpc.rpc(WorkerOperations.GET_BOUNDARIES, {
    channelSegment,
    startTimeSecs,
    endTimeSecs
  });
