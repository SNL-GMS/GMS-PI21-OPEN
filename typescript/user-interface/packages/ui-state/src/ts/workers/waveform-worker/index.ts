import { Timer } from '@gms/common-util';
import { RpcProvider } from 'worker-rpc';

import { cancelWorkerRequests } from './operations/cancel-worker-requests';
import { clearWaveforms } from './operations/clear-waveforms';
import { fetchChannelSegmentsByChannel } from './operations/fetch-channel-segments-by-channel';
import { fetchEventsAndDetectionsWithSegments } from './operations/fetch-events-detections-segments-by-time';
import { fetchSignalDetectionsWithSegments } from './operations/fetch-signal-detections-segments-by-stations-time';
import { getBoundaries } from './operations/get-boundaries';
import { getWaveform } from './operations/get-waveform';
import { WorkerOperations } from './operations/operations';

/**
 * Set this to true to use transfer objects (this will cause the worker to lose access to that data).
 * Not yet supported.
 */
const SHOULD_USE_TRANSFER = false;

/**
 * Create the rpcProvider and tell it how to send messages.
 */
const rpcProvider = new RpcProvider((message, transfer: any[]) => {
  Timer.end('[Waveform worker]', 1000);

  // Get the reference to self so that we can post messages to the main thread
  const workerSelf = globalThis as typeof globalThis & {
    postMessage: (message: RpcProvider.Message, transfer?: any[]) => void;
  };

  if (SHOULD_USE_TRANSFER && ArrayBuffer.isView(message.payload)) {
    // We can only use transferObjects for ArrayBuffers and their Views (such as Float32Array)
    workerSelf.postMessage(message, [message.payload.buffer]);
  } else {
    // Copy message data via structured cloning
    workerSelf.postMessage(message, transfer);
  }
});

/**
 * Handle the message passed to the worker. Dispatch the requested operation.
 */
onmessage = e => {
  Timer.start('[Waveform worker]');
  rpcProvider.dispatch(e.data);
};

/** RPC Handler Registration */
rpcProvider.registerRpcHandler(
  WorkerOperations.FETCH_SIGNAL_DETECTIONS_WITH_SEGMENTS_BY_STATIONS_TIME,
  fetchSignalDetectionsWithSegments
);
rpcProvider.registerRpcHandler(
  WorkerOperations.FETCH_CHANNEL_SEGMENTS_BY_CHANNEL,
  fetchChannelSegmentsByChannel
);
rpcProvider.registerRpcHandler(
  WorkerOperations.FETCH_EVENTS_WITH_DETECTIONS_AND_SEGMENTS_BY_TIME,
  fetchEventsAndDetectionsWithSegments
);
rpcProvider.registerRpcHandler(WorkerOperations.GET_WAVEFORM, getWaveform);
rpcProvider.registerRpcHandler(WorkerOperations.GET_BOUNDARIES, getBoundaries);
rpcProvider.registerRpcHandler(WorkerOperations.CLEAR_WAVEFORMS, clearWaveforms);
rpcProvider.registerRpcHandler(WorkerOperations.CANCEL_WORKER_REQUESTS, cancelWorkerRequests);
