import { WorkerOperations } from '../waveform-worker/operations/operations';
import { waveformWorkerRpc } from '../worker-rpcs';

/**
 * Clears out all cached waveforms from the worker cache.
 */
export const clearWaveforms = async (): Promise<void> =>
  waveformWorkerRpc.rpc(WorkerOperations.CLEAR_WAVEFORMS);
