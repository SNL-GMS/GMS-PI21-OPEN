import { WorkerOperations } from '../waveform-worker/operations/operations';
import { waveformWorkerRpc } from '../worker-rpcs';

/**
 * Cancels all outbound request from worker thread.
 */
export const cancelWorkerRequests = async (): Promise<void> =>
  waveformWorkerRpc.rpc(WorkerOperations.CANCEL_WORKER_REQUESTS);
