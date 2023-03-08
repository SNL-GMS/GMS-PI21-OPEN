import type {
  ChannelSegmentTypes,
  EventTypes,
  SignalDetectionTypes,
  WaveformTypes
} from '@gms/common-model';
import type { AxiosResponse } from 'axios';
import Axios from 'axios';

import type { FetchEventsWithDetectionsAndSegmentsParameters } from '../../../src/ts/workers/waveform-worker/operations/fetch-events-detections-segments-by-time';
import {
  fetchEventsAndDetectionsWithSegments,
  requestEventsAndDetectionsWithSegments
} from '../../../src/ts/workers/waveform-worker/operations/fetch-events-detections-segments-by-time';

describe('Events Query', () => {
  it('has defined exports', () => {
    expect(requestEventsAndDetectionsWithSegments).toBeDefined();
    expect(fetchEventsAndDetectionsWithSegments).toBeDefined();
  });
  it('fetchEventsAndDetectionsWithSegments returns events, signalDetection, and channelSegments', async () => {
    const response: AxiosResponse<{
      events: EventTypes.Event[];
      signalDetections: SignalDetectionTypes.SignalDetection[];
      channelSegments: ChannelSegmentTypes.ChannelSegment<WaveformTypes.Waveform>[];
    }> = {
      status: 200,
      config: {},
      headers: {},
      statusText: '',
      data: { events: [], signalDetections: [], channelSegments: [] }
    };
    Axios.request = jest.fn().mockImplementation(async () => Promise.resolve(response));
    const params: FetchEventsWithDetectionsAndSegmentsParameters = {
      requestConfig: { baseURL: undefined },
      originalDomain: { endTimeSecs: 6, startTimeSecs: 4 },
      colors: { labelTextColor: 'color', waveformColor: 'color' }
    };
    // TODO might be flaky
    // eslint-disable-next-line jest/no-conditional-expect
    await fetchEventsAndDetectionsWithSegments(params).catch(e => expect(e).toMatchSnapshot());
    params.requestConfig.baseURL = '/baseURL';
    const result = await fetchEventsAndDetectionsWithSegments(params);
    expect(result).toMatchInlineSnapshot(`
Object {
  "events": Array [],
  "signalDetections": Array [],
  "uiChannelSegments": Array [],
}
`);
  });
});
