import produce from 'immer';

import { createRecipeToMutateUiChannelSegmentsRecord } from '../../../../../src/ts/app/api/data/channel-segment/mutate-channel-segment-record';
import {
  uiChannelSegment,
  uiChannelSegmentData
} from '../../../../__data__/signal-detections-data';

describe('Waveform Data Cache', () => {
  it('can exercise immer produce method to add channel segment', () => {
    const waveformCache = produce(
      {},
      createRecipeToMutateUiChannelSegmentsRecord('ASAR', [uiChannelSegment])
    );
    expect(waveformCache).toEqual(uiChannelSegmentData);
  });

  it('can exercise immer produce method with undefined channel segment', () => {
    const waveformCache = produce(
      {},
      createRecipeToMutateUiChannelSegmentsRecord('ASAR', undefined)
    );
    expect(waveformCache).toEqual({});
  });
});
