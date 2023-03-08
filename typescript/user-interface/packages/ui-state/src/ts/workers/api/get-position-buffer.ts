import type { WeavessTypes } from '@gms/weavess-core';

import { WorkerOperations } from '../waveform-worker/operations/operations';
import { waveformWorkerRpc } from '../worker-rpcs';

/**
 * Requests the Weavess formatted Float32Array position buffer data from the WaveformWorker
 * which will either calculate and return the buffer, or will return a cached version.
 *
 * @param id the id corresponding to this position buffer
 * @returns a promise for a Float32Array formatted for Weavess' consumption using the
 * position buffer format: x y x y x y...
 */
export const getPositionBuffer = async (
  id: string,
  startTime: number,
  endTime: number,
  domainTimeRange: WeavessTypes.TimeRange
): Promise<Float32Array> => {
  return waveformWorkerRpc.rpc(WorkerOperations.GET_WAVEFORM, {
    id,
    startTime,
    endTime,
    domainTimeRange
  });
};
