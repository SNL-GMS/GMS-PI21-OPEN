import { isDataBySampleRate, isDataByTime, isFloat32Array } from '../../src/ts/types';
import type { WeavessTypes } from '../../src/ts/weavess-core';

const dataBySampleRate: WeavessTypes.DataBySampleRate = {
  sampleRate: 60,
  startTimeSecs: 123456,
  endTimeSecs: 123457,
  values: [1, 2, 3, 4]
};

const dataBySampleRateTyped: WeavessTypes.DataBySampleRate = {
  sampleRate: 60,
  startTimeSecs: 123456,
  endTimeSecs: 123457,
  values: new Float32Array([1, 2, 3, 4])
};

const dataByTime: WeavessTypes.DataByTime = {
  values: [
    {
      timeSecs: 123456,
      value: 1
    }
  ]
};

describe('Waveform Renderer Utils', () => {
  test('functions should be defined', () => {
    expect(isFloat32Array).toBeDefined();
    expect(isDataBySampleRate).toBeDefined();
    expect(isDataByTime).toBeDefined();
  });

  test('isFloat32Array', () => {
    expect(isFloat32Array([1, 2, 3, 4])).not.toBeTruthy();
    expect(isFloat32Array(new Float32Array([1, 2, 3, 4]))).toBeTruthy();
  });

  test('isDataBySampleRate', () => {
    expect(isDataBySampleRate(dataBySampleRate)).toBeTruthy();
    expect(isDataBySampleRate(dataBySampleRateTyped)).toBeTruthy();
    expect(isDataBySampleRate(dataByTime)).not.toBeTruthy();
  });

  test('isDataByTime', () => {
    expect(isDataByTime(dataBySampleRate)).not.toBeTruthy();
    expect(isDataByTime(dataBySampleRateTyped)).not.toBeTruthy();
    expect(isDataByTime(dataByTime)).toBeTruthy();
  });
});
