/* eslint-disable @typescript-eslint/no-magic-numbers */

import type { WeavessTypes } from '@gms/weavess-core';
import Axios from 'axios';

import type { GetWaveformParams } from '../../../src/ts/workers/waveform-worker/operations/get-waveform';
import { getWaveform } from '../../../src/ts/workers/waveform-worker/operations/get-waveform';
import { WaveformStore } from '../../../src/ts/workers/waveform-worker/worker-store/waveform-store';

const timeRange: WeavessTypes.TimeRange = {
  startTimeSecs: 0,
  endTimeSecs: 3
};
const params: GetWaveformParams = {
  id: 'TestId',
  startTime: timeRange.startTimeSecs,
  endTime: timeRange.endTimeSecs,
  domainTimeRange: timeRange
};

const positionBuffer = Float32Array.from([0, 100, 25, 102, 50, 104, 75, 105, 100, 130]);

describe('getWaveform', () => {
  it('has a WaveformStore it can access', () => {
    expect(WaveformStore).toBeDefined();
  });

  it('Can get boundaries that were previously set', async () => {
    const wavePromise = new Promise<Float32Array>(resolve => {
      resolve(positionBuffer);
    });
    await WaveformStore.store(params.id, wavePromise);
    expect(WaveformStore.retrieve(params.id)).toBeDefined();
    const result = await getWaveform(params);
    expect(result).toEqual(positionBuffer);
  });

  it('get Position Buffer with bad time range', async () => {
    const badParams = {
      ...params,
      startTime: 1,
      endTime: 0
    };
    const wavePromise = new Promise<Float32Array>(resolve => {
      resolve(positionBuffer);
    });
    await WaveformStore.store(badParams.id, wavePromise);
    expect(WaveformStore.retrieve(badParams.id)).toBeDefined();
    Axios.request = jest
      .fn()
      .mockRejectedValue(
        new Error(
          'Start index should never be greater than end index. Something is wrong with the logic.'
        )
      );
    await expect(getWaveform(badParams)).rejects.toThrow(
      'Start index should never be greater than end index. Something is wrong with the logic.'
    );
  });

  it('get Position Buffer should return partial buffer', async () => {
    const partialParams = {
      ...params,
      startTime: 0,
      endTime: 1
    };
    const wavePromise = new Promise<Float32Array>(resolve => {
      resolve(positionBuffer);
    });
    await WaveformStore.store(partialParams.id, wavePromise);
    expect(WaveformStore.retrieve(partialParams.id)).toBeDefined();
    const result = await getWaveform(partialParams);
    expect(result).toMatchSnapshot();
  });

  it('get Position Buffer should return empty buffer', async () => {
    const emptyParams = {
      ...params,
      startTime: 4,
      endTime: 5
    };
    const wavePromise = new Promise<Float32Array>(resolve => {
      resolve(positionBuffer);
    });
    await WaveformStore.store(emptyParams.id, wavePromise);
    expect(WaveformStore.retrieve(emptyParams.id)).toBeDefined();
    const result = await getWaveform(emptyParams);
    expect(result).toMatchSnapshot();
  });
});
