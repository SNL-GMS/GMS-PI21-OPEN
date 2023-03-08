/* eslint-disable @typescript-eslint/no-magic-numbers */
import type { WeavessTypes } from '@gms/weavess-core';

import {
  buildChannelSegmentBounds,
  calculateChannelSegBounds,
  calculateDataSegmentBounds,
  mergeBounds,
  scaleToPositionBufferIndex
} from '../../../src/ts/workers/waveform-worker/util/boundary-util';
import { WaveformStore } from '../../../src/ts/workers/waveform-worker/worker-store/waveform-store';

describe('boundary-util', () => {
  const newBounds: WeavessTypes.ChannelSegmentBoundaries = {
    bottomMax: -100,
    topMax: 1000,
    channelAvg: 250,
    channelSegmentId: 'newBoundsId',
    offset: 1000,
    samplesCount: 100
  };
  const dataSeg: WeavessTypes.DataSegment = {
    data: {
      id: 'abc123',
      startTimeSecs: 0,
      endTimeSecs: 50,
      sampleRate: 0.2,
      // min: -10, max: 100
      //          index =>>>     0    1    2   3    4  5   6   7  8   9   10  11
      values: Float32Array.from([-10, 100, -5, -10, 0, 50, 5, -5, 10, 25, 15, -2])
    },
    color: 'LemonChiffon',
    pointSize: 1
  };

  describe('mergeBounds', () => {
    it('exists', () => {
      expect(mergeBounds).toBeDefined();
    });
    const existingBounds: WeavessTypes.ChannelSegmentBoundaries = {
      bottomMax: -1000,
      topMax: 100,
      channelAvg: -500,
      channelSegmentId: 'existingBoundsId',
      offset: 1000,
      samplesCount: 100
    };
    it('should return the existing bounds if the newBounds are undefined', () => {
      const mergedBounds = mergeBounds(existingBounds, undefined as any);
      expect(mergedBounds).toBe(existingBounds);
    });
    it('should return the new bounds if the existing bounds are undefined', () => {
      const mergedBounds = mergeBounds(undefined as any, newBounds);
      expect(mergedBounds).toBe(newBounds);
    });
    it('returns the new bounds with the correct values', () => {
      const mergedBounds = mergeBounds(existingBounds, newBounds);
      expect(mergedBounds.bottomMax).toBe(Math.min(newBounds.bottomMax, existingBounds.bottomMax));
      expect(mergedBounds.topMax).toBe(Math.max(newBounds.topMax, existingBounds.topMax));
      expect(mergedBounds.offset).toBe(
        Math.max(
          newBounds.topMax,
          existingBounds.topMax,
          Math.abs(Math.min(Math.min(newBounds.bottomMax, existingBounds.bottomMax)))
        )
      );
      expect(mergedBounds.channelAvg).toBe(0);
      expect(mergedBounds.channelSegmentId).toBe(existingBounds.channelSegmentId);
    });
  });
  describe('buildChannelSegmentBounds', () => {
    it('exists', () => {
      expect(buildChannelSegmentBounds).toBeDefined();
    });
    it.skip('creates a set of channel segment bounds', async () => {
      await WaveformStore.store(
        (dataSeg.data as WeavessTypes.DataClaimCheck).id,
        Promise.resolve(dataSeg.data.values as Float32Array)
      );
      const ampBounds = await calculateDataSegmentBounds(
        dataSeg.data as WeavessTypes.DataClaimCheck,
        0,
        100
      );
      const chanSegBounds = buildChannelSegmentBounds(ampBounds);
      expect(chanSegBounds.topMax).toBe(ampBounds.amplitudeMax);
      expect(chanSegBounds.bottomMax).toBe(ampBounds.amplitudeMin);
      expect(chanSegBounds.channelAvg).toBe(0);
      expect(chanSegBounds.offset).toBe(
        Math.max(Math.abs(ampBounds.amplitudeMax), Math.abs(ampBounds.amplitudeMin))
      );
    });
    it('throws if given an invalid data type', async () => {
      await expect(
        calculateChannelSegBounds({
          ...dataSeg.data,
          id: undefined
        } as any)
      ).rejects.toThrow();
    });
  });

  describe('scaleToPositionBufferIndex', () => {
    //                          index =>>>     0   1     2   3    4  5   6   7   8   9   10   11
    const positionBuffer = Float32Array.from([-10, 100, -5, -100, 0, 50, 5, -50, 10, 25, 15, -25]);
    const domain = [-10, 15];
    const { length } = positionBuffer;
    it('Correct index selection', () => {
      const windowStart = -9;
      const windowEnd = 1;
      const expectedStartIdx = 1;
      const expectedEndIdx = 5;
      const [startIdx, endIdx] = scaleToPositionBufferIndex(length, domain, windowStart, windowEnd);
      expect(startIdx).toEqual(expectedStartIdx);
      expect(endIdx).toEqual(expectedEndIdx);
    });

    it('Proper clamping', () => {
      const windowStart = -1e3;
      const windowEnd = 1e3;
      const [startIdx, endIdx] = scaleToPositionBufferIndex(length, domain, windowStart, windowEnd);
      expect(startIdx).toEqual(1);
      expect(endIdx).toEqual(11);
    });

    it('Proper rounding', () => {
      const windowStart = positionBuffer[0] + (positionBuffer[2] - positionBuffer[0]) / 2;

      let [startIdx] = scaleToPositionBufferIndex(length, domain, windowStart, undefined);
      expect(startIdx).toEqual(3);

      const rightWindowStart = windowStart + 0.001; // move to the right
      [startIdx] = scaleToPositionBufferIndex(length, domain, rightWindowStart, undefined);
      expect(startIdx).toEqual(3);

      const leftWindowStart = windowStart - 0.001; // move to the right
      [startIdx] = scaleToPositionBufferIndex(length, domain, leftWindowStart, undefined);
      expect(startIdx).toEqual(1);
    });

    it('Proper ordering', () => {
      const windowStart = 5;
      const windowEnd = -5;
      expect(() => scaleToPositionBufferIndex(length, domain, windowStart, windowEnd)).toThrow();
    });
  });
});
