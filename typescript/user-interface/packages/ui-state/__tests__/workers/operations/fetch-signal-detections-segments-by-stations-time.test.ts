/* eslint-disable jest/expect-expect */
/* eslint-disable @typescript-eslint/no-magic-numbers */
import type { SignalDetectionTypes } from '@gms/common-model';
import { ChannelSegmentTypes, CommonTypes, WaveformTypes } from '@gms/common-model';
import { uuid } from '@gms/common-util';
import type { WeavessTypes } from '@gms/weavess-core';
import type { AxiosResponse } from 'axios';
import Axios from 'axios';
import { enableMapSet } from 'immer';

import { config } from '../../../src/ts/app/api/data/signal-detection/endpoint-configuration';
import type { GetSignalDetectionsAndSegmentsByStationsAndTimeQueryArgs } from '../../../src/ts/app/api/data/signal-detection/types';
import type { FetchSignalDetectionWithSegmentsParameters } from '../../../src/ts/workers/waveform-worker/operations/fetch-signal-detections-segments-by-stations-time';
import {
  fetchSignalDetectionsWithSegments,
  requestSignalDetectionsWithSegments
} from '../../../src/ts/workers/waveform-worker/operations/fetch-signal-detections-segments-by-stations-time';
import { asarFkbSegmentId, signalDetectionsData, uiChannelSegmentData } from '../../__data__';

enableMapSet();

// mock the uuid
uuid.asString = jest.fn().mockImplementation(() => '12345789');

const MOCK_TIME = 1606818240000;
global.Date.now = jest.fn(() => MOCK_TIME);
const dataSegmentData = (uiChannelSegmentData as any).ASAR[WaveformTypes.UNFILTERED][0]
  .channelSegment.dataSegments[0].data as WeavessTypes.DataBySampleRate;

const waveform: ChannelSegmentTypes.ChannelSegment<WaveformTypes.Waveform> = {
  units: CommonTypes.Units.NANOMETERS,
  id: asarFkbSegmentId,
  timeseriesType: ChannelSegmentTypes.TimeSeriesType.WAVEFORM,
  timeseries: [
    {
      endTime: dataSegmentData.endTimeSecs,
      type: ChannelSegmentTypes.TimeSeriesType.WAVEFORM,
      startTime: dataSegmentData.startTimeSecs,
      sampleRateHz: dataSegmentData.sampleRate,
      samples: dataSegmentData.values,
      sampleCount: dataSegmentData.values.length
    }
  ]
};

const channelSegments: ChannelSegmentTypes.ChannelSegment<WaveformTypes.Waveform>[] = [waveform];

describe('Signal Detection Query', () => {
  it('fetchSignalDetectionsWithSegments defined', () => {
    expect(fetchSignalDetectionsWithSegments).toBeDefined();
  });
  it('requestSignalDetectionsAndConvertWaveforms defined', () => {
    expect(requestSignalDetectionsWithSegments).toBeDefined();
  });

  describe('fetchSignalDetection', () => {
    const now = Date.now() / 1000;
    const timeRange: CommonTypes.TimeRange = {
      // eslint-disable-next-line @typescript-eslint/no-magic-numbers
      startTimeSecs: now - 3600,
      endTimeSecs: now
    };
    const aakStation = {
      name: 'AAK',
      effectiveAt: timeRange.startTimeSecs
    };
    const signalDetectionQueryArgs: GetSignalDetectionsAndSegmentsByStationsAndTimeQueryArgs = {
      stations: [aakStation],
      startTime: timeRange.startTimeSecs,
      endTime: timeRange.endTimeSecs,
      stageId: {
        name: 'AL1',
        effectiveTime: timeRange.startTimeSecs
      }
    };
    const response: AxiosResponse<{
      channelSegments: ChannelSegmentTypes.ChannelSegment<WaveformTypes.Waveform>[];
      signalDetections: SignalDetectionTypes.SignalDetection[];
    }> = {
      status: 200,
      config: {},
      headers: {},
      statusText: '',
      data: {
        channelSegments,
        signalDetections: signalDetectionsData
      }
    };

    Axios.request = jest.fn().mockImplementation(async () => Promise.resolve(response));

    const sdConfig =
      config.signalDetection.services.getDetectionsWithSegmentsByStationsAndTime.requestConfig;
    sdConfig.data = signalDetectionQueryArgs;
    sdConfig.baseURL = 'localhost:8080/';
    const params: FetchSignalDetectionWithSegmentsParameters = {
      requestConfig: sdConfig,
      originalDomain: timeRange,
      colors: {
        waveformColor: 'tomato', // it's a real css color
        labelTextColor: 'bisque' // we just need valid css colors for the test
      }
    };
    it('fetchSignalDetection returns the expected result with valid args', async () => {
      const result = await fetchSignalDetectionsWithSegments(params);
      expect(result).toMatchSnapshot();
    });

    it('fetchSignalDetection no baseURL', async () => {
      const badBaseURL = {
        ...params,
        requestConfig: {
          ...params.requestConfig,
          baseURL: undefined
        }
      };
      await expect(fetchSignalDetectionsWithSegments(badBaseURL)).rejects.toThrow();
    });
  });
});
