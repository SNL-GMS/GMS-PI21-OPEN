import { FeatureMeasurementType } from '@gms/common-model/lib/signal-detection';
import cloneDeep from 'lodash/cloneDeep';
import type { AnyAction } from 'redux';
import type { MockStoreCreator } from 'redux-mock-store';
import createMockStore from 'redux-mock-store';
import thunk from 'redux-thunk';

import {
  addGetEventsWithDetectionsAndSegmentsByTimeReducers,
  getEventsWithDetectionsAndSegmentsByTime,
  shouldSkipGetEventsWithDetectionsAndSegmentsByTime
} from '../../../../../src/ts/app/api/data/event/get-events-detections-segments-by-time';
import type { GetEventsWithDetectionsAndSegmentsByTimeQueryArgs } from '../../../../../src/ts/app/api/data/event/types';
import type { AppState } from '../../../../../src/ts/app/store';
import { appState } from '../../../../test-util';

jest.mock('../../../../../src/ts/workers', () => {
  const actual = jest.requireActual('../../../../../src/ts/workers');
  return {
    ...actual,
    getEvents: jest.fn(async () => {
      return Promise.resolve({
        signalDetections: ['signalDetections'],
        channelSegments: ['channelSegments'],
        events: ['events']
      });
    })
  };
});

const fiveMinutes = 300000;
const endTimeSecs = 123456789;
const startTimeSecs = (endTimeSecs - fiveMinutes) / 1000;

const eventsQueryInput: GetEventsWithDetectionsAndSegmentsByTimeQueryArgs = {
  startTime: startTimeSecs,
  endTime: endTimeSecs / 1000,
  stageId: { name: 'stageId' }
};

describe('Get events by time', () => {
  it('have defined exports', () => {
    expect(getEventsWithDetectionsAndSegmentsByTime).toBeDefined();
    expect(addGetEventsWithDetectionsAndSegmentsByTimeReducers).toBeDefined();
    expect(shouldSkipGetEventsWithDetectionsAndSegmentsByTime).toBeDefined();
  });

  it('build a builder using addGetChannelSegmentsByChannelReducers', () => {
    const mapKeys = [
      'events/getEventsWithDetectionsAndSegmentsByTime/pending',
      'events/getEventsWithDetectionsAndSegmentsByTime/fulfilled',
      'events/getEventsWithDetectionsAndSegmentsByTime/rejected'
    ];
    const builderMap = new Map();
    const builder: any = {
      addCase: (k, v) => {
        builderMap.set(k.type, v);
        return builder;
      }
    };
    addGetEventsWithDetectionsAndSegmentsByTimeReducers(builder);
    expect(builderMap).toMatchSnapshot();

    const state = {
      events: [{ id: 'eventID1' }],
      signalDetections: [{ id: 'sdID1', station: { name: 'stationName1' } }],
      channelSegments: [{ channelName: 'stationName1' }],
      queries: { getEventsWithDetectionsAndSegmentsByTime: {} }
    };
    const action = {
      meta: { requestId: 12345, arg: {} },
      payload: {
        events: [{ id: 'eventID2' }],
        signalDetections: [
          {
            station: { name: 'stationName1' },
            id: 'sdID2',
            signalDetectionHypotheses: [
              {
                featureMeasurements: [
                  {
                    featureMeasurementType: FeatureMeasurementType.ARRIVAL_TIME,
                    channel: { name: 'stationName1' }
                  }
                ]
              }
            ]
          }
        ],
        uiChannelSegments: [
          {
            channelSegment: {
              channelName: 'channelName2'
            },
            channelSegmentDescriptor: undefined
          }
        ]
      }
    };
    builderMap.get(mapKeys[0])(state, action);
    expect(state).toMatchSnapshot();
    builderMap.get(mapKeys[1])(state, action);
    expect(state).toMatchSnapshot();
    builderMap.get(mapKeys[2])(state, action);
    expect(state).toMatchSnapshot();
  });

  it('can determine when to skip query execution', () => {
    expect(shouldSkipGetEventsWithDetectionsAndSegmentsByTime(undefined)).toBeTruthy();
    expect(
      shouldSkipGetEventsWithDetectionsAndSegmentsByTime({ ...eventsQueryInput, startTime: null })
    ).toBeTruthy();
    expect(
      shouldSkipGetEventsWithDetectionsAndSegmentsByTime({ ...eventsQueryInput, endTime: null })
    ).toBeTruthy();
    expect(
      shouldSkipGetEventsWithDetectionsAndSegmentsByTime({ ...eventsQueryInput, stageId: null })
    ).toBeTruthy();
    expect(
      shouldSkipGetEventsWithDetectionsAndSegmentsByTime({
        ...eventsQueryInput,
        stageId: { name: null }
      })
    ).toBeTruthy();
    expect(shouldSkipGetEventsWithDetectionsAndSegmentsByTime(eventsQueryInput)).toBeFalsy();
  });

  it('will not execute query if the args are invalid', async () => {
    const mockStoreCreator: MockStoreCreator<AppState, AnyAction> = createMockStore([thunk]);

    const store = mockStoreCreator(appState);

    await store.dispatch(
      getEventsWithDetectionsAndSegmentsByTime({ ...eventsQueryInput, stageId: null }) as any
    );

    // results should have empty arrays since current interval is not set
    expect(store.getActions()).toHaveLength(0);
  });
  it('will not execute query if the current interval is not defined', async () => {
    const mockStoreCreator: MockStoreCreator<AppState, AnyAction> = createMockStore([thunk]);

    const mockAppState: AppState = cloneDeep(appState);
    mockAppState.processingConfigurationApi = {
      endpoints: {
        getProcessingAnalystConfiguration: {
          select: jest.fn(() =>
            jest.fn(() => ({
              data: {
                uiThemes: {
                  name: 'currentTheme',
                  colors: { waveformRaw: 'color', waveformFilterLabel: 'color' }
                }
              }
            }))
          )
        }
      }
    } as any;
    mockAppState.userManagerApi = {
      endpoints: {
        getUserProfile: {
          select: jest.fn(() => jest.fn(() => ({ data: { currentTheme: 'currentTheme' } })))
        }
      }
    } as any;
    const store = mockStoreCreator(mockAppState);

    await store.dispatch(getEventsWithDetectionsAndSegmentsByTime(eventsQueryInput) as any);

    // results should have empty arrays since current interval is not set
    expect(store.getActions()[store.getActions().length - 1].type).toMatchInlineSnapshot(
      `"events/getEventsWithDetectionsAndSegmentsByTime/rejected"`
    );
    expect(store.getActions()[store.getActions().length - 1].payload).toMatchInlineSnapshot(
      `[TypeError: exports.waveformWorker.postMessage is not a function]`
    );
  });

  it('can handle executing the query with the fulfilled state', async () => {
    const mockStoreCreator: MockStoreCreator<AppState, AnyAction> = createMockStore([thunk]);
    const userManagerApi: any = {
      endpoints: {
        getUserProfile: {
          select: jest.fn(() => jest.fn(() => ({ data: { currentTheme: 'currentTheme' } })))
        }
      }
    };
    const mockAppState: AppState = cloneDeep(appState);
    mockAppState.userManagerApi = userManagerApi;
    mockAppState.processingConfigurationApi = {
      endpoints: {
        getProcessingAnalystConfiguration: {
          select: jest.fn(() =>
            jest.fn(() => ({
              data: {
                uiThemes: {
                  name: 'currentTheme',
                  colors: { waveformRaw: 'color', waveformFilterLabel: 'color' }
                }
              }
            }))
          )
        }
      }
    } as any;

    mockAppState.app = { workflow: { timeRange: { startTimeSecs: 4, endTimeSecs: 6 } } } as any;
    const store = mockStoreCreator(mockAppState);

    await store.dispatch(getEventsWithDetectionsAndSegmentsByTime(eventsQueryInput) as any);
    // results should have empty arrays since current interval is not set
    expect(store.getActions()[store.getActions().length - 1].type).toMatchInlineSnapshot(
      `"events/getEventsWithDetectionsAndSegmentsByTime/rejected"`
    );
    expect(store.getActions()[store.getActions().length - 1].payload).toMatchInlineSnapshot(
      `[TypeError: exports.waveformWorker.postMessage is not a function]`
    );
  });
});
