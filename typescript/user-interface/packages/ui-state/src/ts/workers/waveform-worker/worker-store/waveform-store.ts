import { UILogger } from '@gms/ui-util';

import { initStoragePersistence } from '../util/storage-util';
import { WorkerStore } from './worker-store';

export const WaveformStore = new WorkerStore<Float32Array>('waveform-store');

const logger = UILogger.create('GMS_LOG_WAVEFORM_STORE', process.env.GMS_LOG_WAVEFORM_STORE);

initStoragePersistence().catch(e => {
  logger.error(e);
});
