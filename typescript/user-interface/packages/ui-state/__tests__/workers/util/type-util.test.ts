import { toEpochSeconds } from '@gms/common-util';
import cloneDeep from 'lodash/cloneDeep';

import type { GetChannelSegmentsByChannelsQueryArgs } from '../../../src/ts/app';
import { config } from '../../../src/ts/app/api/data/channel-segment/endpoint-configuration';
import { WorkerTypeUtil } from '../../../src/ts/workers/waveform-worker/util';

describe('Waveform Worker Type Util', () => {
  const mockWaveformQuery: GetChannelSegmentsByChannelsQueryArgs = {
    startTime: toEpochSeconds('2021-03-02T17:30:57.376Z'),
    endTime: toEpochSeconds('2021-03-02T23:30:57.376Z'),
    channels: [
      {
        name: 'AAK.AAK.BHN',
        effectiveAt: toEpochSeconds('2021-03-02T17:30:57.376Z')
      }
    ]
  };
  const waveformConfig = config.waveform.services.getChannelSegment.requestConfig;
  waveformConfig.data = mockWaveformQuery;
  waveformConfig.baseURL = 'localhost:8080/';
  it('check if is waveform request valid', () => {
    expect(WorkerTypeUtil.isWaveformRequest(waveformConfig)).toBeTruthy();
  });

  it('check if is no data waveform request is not valid', () => {
    const noDataRequest = cloneDeep(waveformConfig);
    noDataRequest.data = undefined;
    expect(WorkerTypeUtil.isWaveformRequest(noDataRequest)).toBeFalsy();
  });

  it('check if is string dates waveform request is not valid', () => {
    let badDateArgs = {
      ...mockWaveformQuery,
      startTime: '2021-03-02T17:30:57.376Z'
    } as unknown;
    const badDates = cloneDeep(waveformConfig);
    badDates.data = badDateArgs;
    expect(WorkerTypeUtil.isWaveformRequest(badDates)).toBeFalsy();
    badDateArgs = {
      ...mockWaveformQuery,
      endTime: '2021-03-02T23:30:57.376Z'
    };
    expect(WorkerTypeUtil.isWaveformRequest(badDates)).toBeFalsy();
    badDateArgs = {
      ...mockWaveformQuery,
      endTime: undefined,
      startTime: undefined
    };
    expect(WorkerTypeUtil.isWaveformRequest(badDates)).toBeFalsy();
  });
});
