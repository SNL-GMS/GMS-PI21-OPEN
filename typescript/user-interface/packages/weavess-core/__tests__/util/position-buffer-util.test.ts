/* eslint-disable @typescript-eslint/no-magic-numbers */
/* eslint-disable @typescript-eslint/no-floating-promises */

import type { DataBySampleRate } from '../../src/ts/types';
import type {
  CreatePositionBufferBySampleRateParams,
  CreatePositionBufferByTimeParams
} from '../../src/ts/util/position-buffer-util';
import {
  convertToPositionBuffer,
  createPositionBufferForDataBySampleRate,
  createPositionBufferForDataByTime,
  getBoundsForPositionBuffer
} from '../../src/ts/util/position-buffer-util';
import type { WeavessTypes } from '../../src/ts/weavess-core';

describe('WEAVESS Core: Position Buffer Utils', () => {
  test('functions should be defined', () => {
    expect(createPositionBufferForDataByTime).toBeDefined();
    expect(createPositionBufferForDataBySampleRate).toBeDefined();
  });

  test('createPositionBufferForDataByTime', () => {
    const data: CreatePositionBufferByTimeParams = {
      displayStartTimeSecs: 0,
      displayEndTimeSecs: 10,
      glMin: 0,
      glMax: 100,
      values: [
        {
          timeSecs: 0,
          value: 5
        },
        {
          timeSecs: 5,
          value: 8
        },
        {
          timeSecs: 9,
          value: 2
        }
      ]
    };
    const buffer = createPositionBufferForDataByTime(data);
    expect(buffer[0]).toEqual(0);
    expect(buffer[1]).toEqual(5);
    expect(buffer[2]).toEqual(50);
    expect(buffer[3]).toEqual(8);
    expect(buffer[4]).toEqual(90);
    expect(buffer[5]).toEqual(2);
  });

  test('createPositionBufferForDataBySampleRate', () => {
    const data: CreatePositionBufferBySampleRateParams = {
      startTimeSecs: 0,
      endTimeSecs: 100,
      sampleRate: 5,
      displayStartTimeSecs: 0,
      displayEndTimeSecs: 10,
      glMin: 0,
      glMax: 100,
      values: [4, 5, 7]
    };
    const buffer = createPositionBufferForDataBySampleRate(data);
    expect(buffer[0]).toEqual(0);
    expect(buffer[1]).toEqual(4);
    expect(buffer[2]).toEqual(2);
    expect(buffer[3]).toEqual(5);
    expect(buffer[4]).toEqual(4);
    expect(buffer[5]).toEqual(7);
  });

  const testArray = new Array(100).fill(0, 0, 100).map((val: number, index: number) => index);

  const sampleWave: DataBySampleRate = {
    /** Epoch start time in seconds */
    startTimeSecs: 0,
    endTimeSecs: 100,
    sampleRate: 40,
    values: testArray
  };

  const visibleDomain: WeavessTypes.TimeRange = {
    startTimeSecs: 0,
    endTimeSecs: 1000
  };

  it('can convert an array of numbers into typed array', () => {
    const convertedWave = convertToPositionBuffer(sampleWave, visibleDomain);
    // eslint-disable-next-line @typescript-eslint/no-magic-numbers
    expect(convertedWave.BYTES_PER_ELEMENT).toBe(4);
  });

  it('creates a TypedArray with the correct length', () => {
    const convertedWave = convertToPositionBuffer(sampleWave, visibleDomain);
    expect(convertedWave).toHaveLength(sampleWave.values.length * 2);
  });

  it('can convert an array of numbers into an equivalent typed array', () => {
    const convertedWave = convertToPositionBuffer(sampleWave, visibleDomain);
    const yValues = convertedWave.filter((v, i) => i % 2 === 1);
    expect(Array.from(yValues)).toMatchObject(Array.from(sampleWave.values));
  });

  it('gives an empty TypedArray when given an empty set of values', () => {
    const emptyWave = {
      ...sampleWave,
      values: []
    };
    const convertedWave = convertToPositionBuffer(emptyWave, visibleDomain);
    expect(convertedWave).toHaveLength(0);
  });

  it('throws when values is undefined', () => {
    const wipeoutWave = {
      ...sampleWave,
      values: undefined
    };
    expect(() =>
      convertToPositionBuffer(wipeoutWave.values, visibleDomain)
    ).toThrowErrorMatchingSnapshot();
  });

  it('throws when waveform is undefined', () => {
    expect(() => convertToPositionBuffer(undefined, visibleDomain)).toThrowErrorMatchingSnapshot();
  });

  it('throws when visibleDomain is undefined', () => {
    expect(() => convertToPositionBuffer(sampleWave, undefined)).toThrowErrorMatchingSnapshot();
  });

  describe('getBoundsForPositionBuffer', () => {
    //                             index =>    0   1   2  3   4    5  6  7   8   9
    const positionBuffer = Float32Array.from([-10, 0, -5, 100, 0, -5, 5, 10, 10, -5]);

    it('exists', () => {
      expect(getBoundsForPositionBuffer).toBeDefined();
    });

    it('throws when it is given an odd number of elements', () => {
      expect(() => getBoundsForPositionBuffer(Float32Array.from([0, 0, 1, 100, 2]))).toThrow();
    });

    it('finds the min and max with no start or end indices', () => {
      const { min, max } = getBoundsForPositionBuffer(positionBuffer);
      expect(min).toBe(-5);
      expect(max).toBe(100);
    });

    it('finds the min and max with given start index', () => {
      const { min, max } = getBoundsForPositionBuffer(positionBuffer, 3);
      expect(min).toBe(-5);
      expect(max).toBe(100);
    });

    it('finds the min and max with given start and end index', () => {
      const { min, max } = getBoundsForPositionBuffer(positionBuffer, 3, 5);
      expect(min).toBe(-5);
      expect(max).toBe(100);
    });
  });
});
