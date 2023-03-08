import { RpcProvider } from 'worker-rpc';

// eslint-disable-next-line no-var, vars-on-top
declare var require;
// eslint-disable-next-line @typescript-eslint/no-var-requires, import/no-webpack-loader-syntax, @typescript-eslint/no-require-imports, import/no-extraneous-dependencies
const WaveformWorker = require('worker-loader?inline&fallback=false!./waveform-worker'); // eslint:disable-line

export const waveformWorker = new WaveformWorker();
export const waveformWorkerRpc = new RpcProvider((message, transfer) => {
  waveformWorker.postMessage(message, transfer);
});
waveformWorker.onmessage = e => {
  waveformWorkerRpc.dispatch(e.data);
};
