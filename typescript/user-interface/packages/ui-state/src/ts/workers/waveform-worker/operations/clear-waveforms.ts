import { UILogger } from '@gms/ui-util';

import { WaveformStore } from '../worker-store';

const logger = UILogger.create('GMS_LOG_WAVEFORM_STORE', process.env.GMS_LOG_WAVEFORM_STORE);

export const clearWaveforms = (): void => {
  WaveformStore.cleanup().catch(e => {
    logger.error('Error cleaning up waveforms');
    throw e;
  });
};
