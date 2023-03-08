import type { FkTypes } from '@gms/common-model';
import { ChannelSegmentTypes } from '@gms/common-model';

export const fkSpectra: FkTypes.FkPowerSpectra = {
  id: undefined,
  spectrums: [],
  contribChannels: [],
  startTime: undefined,
  endTime: undefined,
  sampleRateHz: undefined,
  sampleCount: undefined,
  type: ChannelSegmentTypes.TimeSeriesType.WAVEFORM,
  windowLead: undefined,
  windowLength: undefined,
  stepSize: undefined,
  lowFrequency: undefined,
  highFrequency: undefined,
  metadata: {
    phaseType: undefined,
    slowStartX: -20,
    slowDeltaX: undefined,
    slowStartY: -20,
    slowDeltaY: undefined
  },
  slowCountX: undefined,
  slowCountY: undefined,
  reviewed: undefined,
  fstatData: undefined,
  configuration: undefined
};

export const incrementAmt = 10;

export const canvasDimension = 40;

export const sqrtOfFifty = 7.0710678118654755;
