/* eslint-disable @typescript-eslint/no-magic-numbers */
import type { WaveformTypes } from '@gms/common-model';
import { ChannelSegmentTypes, CommonTypes } from '@gms/common-model';

export const rawWaveform: ChannelSegmentTypes.ChannelSegment<WaveformTypes.Waveform> = {
  units: CommonTypes.Units.NANOMETERS,
  id: {
    channel: {
      name: 'AAK.AAK.BH1',
      effectiveAt: 1274335200
    },
    startTime: 1274335200,
    endTime: 1274336200,
    creationTime: 1274335200
  },
  timeseriesType: ChannelSegmentTypes.TimeSeriesType.WAVEFORM,
  timeseries: [
    {
      endTime: 1274336200,
      type: ChannelSegmentTypes.TimeSeriesType.WAVEFORM,
      startTime: 1274335200,
      sampleRateHz: 40,
      samples: [
        223.633869,
        227.47485600000002,
        231.592173,
        226.811664,
        234.90813300000002,
        232.39353,
        228.55254300000001,
        231.757971,
        227.47485600000002,
        224.24179500000002,
        231.675072,
        234.687069,
        229.10520300000002,
        234.687069,
        224.68392300000002,
        223.164108
      ],
      sampleCount: 16
    }
  ]
};
