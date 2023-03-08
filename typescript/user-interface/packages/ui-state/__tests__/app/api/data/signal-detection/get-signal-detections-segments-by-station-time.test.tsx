/* eslint-disable @typescript-eslint/no-magic-numbers */
import type { CommonTypes } from '@gms/common-model';
import { uuid } from '@gms/common-util';
import { enableMapSet } from 'immer';
import cloneDeep from 'lodash/cloneDeep';
import type { AnyAction } from 'redux';
import type { MockStoreCreator } from 'redux-mock-store';
import createMockStore from 'redux-mock-store';
import thunk from 'redux-thunk';

import { getStore } from '../../../../../src/ts/app';
import {
  addGetSignalDetectionsWithSegmentsByStationAndTimeReducers,
  getSignalDetectionsAndSegmentsByStationAndTime,
  shouldSkipGetSignalDetectionsWithSegmentsByStationAndTime
} from '../../../../../src/ts/app/api/data/signal-detection/get-signal-detections-segments-by-station-time';
import type { GetSignalDetectionsWithSegmentsByStationAndTimeQueryArgs } from '../../../../../src/ts/app/api/data/signal-detection/types';
import { reducer } from '../../../../../src/ts/app/state/reducer';
import type { AppState } from '../../../../../src/ts/app/store';
import { appState } from '../../../../test-util';

enableMapSet();

const MOCK_TIME = 1606818240000;
global.Date.now = jest.fn(() => MOCK_TIME);

// mock the uuid
uuid.asString = jest.fn().mockImplementation(() => '12345789');

jest.mock('../../../../../src/ts/workers', () => {
  const actual = jest.requireActual('../../../../../src/ts/workers');
  return {
    ...actual,
    getSignalDetections: jest.fn(async () =>
      Promise.reject(new Error('Rejected getSignalDetections'))
    )
  };
});

const now = 1234567890 / 1000;
const timeRange: CommonTypes.TimeRange = {
  startTimeSecs: now - 3600,
  endTimeSecs: now
};

const asarStation = {
  name: 'ASAR',
  effectiveAt: timeRange.startTimeSecs
};

const signalDetectionQueryArgs: GetSignalDetectionsWithSegmentsByStationAndTimeQueryArgs = {
  station: asarStation,
  startTime: timeRange.startTimeSecs,
  endTime: timeRange.endTimeSecs,
  stageId: {
    name: 'AL1',
    effectiveTime: timeRange.startTimeSecs
  }
};

describe('Get Signal Detection by Station', () => {
  it('have defined', () => {
    expect(shouldSkipGetSignalDetectionsWithSegmentsByStationAndTime).toBeDefined();
    expect(getSignalDetectionsAndSegmentsByStationAndTime).toBeDefined();
    expect(addGetSignalDetectionsWithSegmentsByStationAndTimeReducers).toBeDefined();
  });

  it('build a builder using getSignalDetectionsAndSegmentsByStationReducers', () => {
    const mapKeys = [
      'signalDetection/getSignalDetectionsAndSegmentsByStationAndTime/pending',
      'signalDetection/getSignalDetectionsAndSegmentsByStationAndTime/fulfilled',
      'signalDetection/getSignalDetectionsAndSegmentsByStationAndTime/rejected'
    ];
    const builderMap = new Map();
    const builder: any = {
      addCase: (k, v) => {
        builderMap.set(k.type, v);
        return builder;
      }
    };
    addGetSignalDetectionsWithSegmentsByStationAndTimeReducers(builder);
    expect(builderMap).toMatchSnapshot();

    // eslint-disable-next-line prefer-const
    let state = { queries: { getSignalDetectionWithSegmentsByStationAndTime: {} } };
    // eslint-disable-next-line prefer-const
    let action = {
      meta: { requestId: 12345, arg: { station: { name: 'stationName' } } },
      payload: { signalDetections: [], uiChannelSegments: [] }
    };
    builderMap.get(mapKeys[0])(state, action);
    expect(state).toMatchSnapshot();
    builderMap.get(mapKeys[1])(state, action);
    expect(state).toMatchSnapshot();
    builderMap.get(mapKeys[2])(state, action);
    expect(state).toMatchSnapshot();
  });

  it('can determine when to skip query execution', () => {
    expect(
      shouldSkipGetSignalDetectionsWithSegmentsByStationAndTime({
        ...signalDetectionQueryArgs,
        startTime: undefined
      })
    ).toBeTruthy();
    expect(
      shouldSkipGetSignalDetectionsWithSegmentsByStationAndTime({
        ...signalDetectionQueryArgs,
        endTime: undefined
      })
    ).toBeTruthy();
    expect(
      shouldSkipGetSignalDetectionsWithSegmentsByStationAndTime({
        ...signalDetectionQueryArgs,
        station: undefined
      })
    ).toBeTruthy();
    expect(
      shouldSkipGetSignalDetectionsWithSegmentsByStationAndTime({
        ...signalDetectionQueryArgs,
        stageId: undefined
      })
    ).toBeTruthy();
    expect(
      shouldSkipGetSignalDetectionsWithSegmentsByStationAndTime(signalDetectionQueryArgs)
    ).toBeFalsy();
  });

  it('will not execute query if the args are invalid', async () => {
    const mockStoreCreator: MockStoreCreator<AppState, AnyAction> = createMockStore([thunk]);

    const store = mockStoreCreator(appState);

    await store.dispatch(
      getSignalDetectionsAndSegmentsByStationAndTime({
        ...signalDetectionQueryArgs,
        station: null
      }) as any
    );

    // results should have empty arrays since current interval is not set
    expect(store.getActions()).toHaveLength(0);
  });

  it('will not execute query if the current interval is not defined', async () => {
    const mockStoreCreator: MockStoreCreator<AppState, AnyAction> = createMockStore([thunk]);

    const store = mockStoreCreator(appState);

    await store.dispatch(
      getSignalDetectionsAndSegmentsByStationAndTime(signalDetectionQueryArgs) as any
    );

    // results should have empty arrays since current interval is not set
    expect(store.getActions()[store.getActions().length - 1].type).toEqual(
      'signalDetection/getSignalDetectionsAndSegmentsByStationAndTime/rejected'
    );

    expect(store.getActions()[store.getActions().length - 1].payload).toMatchInlineSnapshot(
      `[TypeError: exports.waveformWorker.postMessage is not a function]`
    );

    expect(
      reducer(store.getState() as any, store.getActions()[store.getActions().length - 1])
    ).toMatchSnapshot();
  });

  it('can handle rejected state', async () => {
    const mockStoreCreator: MockStoreCreator<AppState, AnyAction> = createMockStore([thunk]);

    const realStore = getStore();

    const state = cloneDeep(realStore.getState());
    state.app.workflow.timeRange.startTimeSecs = 4;
    state.app.workflow.timeRange.endTimeSecs = 6;

    const store = mockStoreCreator(state);

    await store.dispatch(
      getSignalDetectionsAndSegmentsByStationAndTime(signalDetectionQueryArgs) as any
    );

    expect(store.getActions()[store.getActions().length - 1].type).toEqual(
      'signalDetection/getSignalDetectionsAndSegmentsByStationAndTime/rejected'
    );
  });
});
