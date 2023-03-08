import type { ChannelTypes, FacetedTypes } from '@gms/common-model';
import cloneDeep from 'lodash/cloneDeep';
import type { AnyAction } from 'redux';
import type { MockStoreCreator } from 'redux-mock-store';
import createMockStore from 'redux-mock-store';
import thunk from 'redux-thunk';

import type { GetChannelSegmentsByChannelQueryArgs } from '../../../../../src/ts/app';
import {
  addGetChannelSegmentsByChannelReducers,
  getChannelSegmentsByChannel,
  shouldSkipGetChannelSegmentsByChannel
} from '../../../../../src/ts/app/api/data/channel-segment/get-channel-segments-by-channel';
import type { AppState } from '../../../../../src/ts/app/store';
import { getStore } from '../../../../../src/ts/app/store';
import { appState } from '../../../../test-util';

jest.mock('../../../../../src/ts/workers', () => {
  const actual = jest.requireActual('../../../../../src/ts/workers');
  return {
    ...actual,
    fetchChannelSegmentsByChannel: jest.fn(async () =>
      Promise.reject(new Error('Rejected fetchChannelSegmentsByChannel'))
    )
  };
});

const fiveMinutes = 300000;
const endTimeSecs = 123456789;
const startTimeSecs = (endTimeSecs - fiveMinutes) / 1000;

const aakChannel: FacetedTypes.VersionReference<ChannelTypes.Channel> = {
  name: 'AAK.AAK00.BHE',
  effectiveAt: startTimeSecs
};

const waveformQueryChannelInput: GetChannelSegmentsByChannelQueryArgs = {
  channel: aakChannel,
  startTime: startTimeSecs,
  endTime: endTimeSecs / 1000
};

describe('Get Channel Segments for Channels', () => {
  it('have defined', () => {
    expect(shouldSkipGetChannelSegmentsByChannel).toBeDefined();
    expect(getChannelSegmentsByChannel).toBeDefined();
    expect(addGetChannelSegmentsByChannelReducers).toBeDefined();
  });

  it('build a builder using addGetChannelSegmentsByChannelReducers', () => {
    const mapKeys = [
      'channelSegment/getChannelSegmentsByChannel/pending',
      'channelSegment/getChannelSegmentsByChannel/fulfilled',
      'channelSegment/getChannelSegmentsByChannel/rejected'
    ];
    const builderMap = new Map();
    const builder: any = {
      addCase: (k, v) => {
        builderMap.set(k.type, v);
        return builder;
      }
    };
    addGetChannelSegmentsByChannelReducers(builder);
    expect(builderMap).toMatchSnapshot();

    // eslint-disable-next-line prefer-const
    let state = { queries: { getChannelSegmentsByChannel: {} } };
    // eslint-disable-next-line prefer-const
    let action = {
      meta: { requestId: 12345, arg: { channel: { name: 'channelName' } } },
      payload: []
    };
    builderMap.get(mapKeys[0])(state, action);
    expect(state).toMatchSnapshot();
    builderMap.get(mapKeys[1])(state, action);
    expect(state).toMatchSnapshot();
    builderMap.get(mapKeys[2])(state, action);
    expect(state).toMatchSnapshot();
  });

  it('can determine when to skip query execution', () => {
    expect(shouldSkipGetChannelSegmentsByChannel(undefined)).toBeTruthy();
    expect(
      shouldSkipGetChannelSegmentsByChannel({ ...waveformQueryChannelInput, startTime: undefined })
    ).toBeTruthy();
    expect(
      shouldSkipGetChannelSegmentsByChannel({ ...waveformQueryChannelInput, endTime: undefined })
    ).toBeTruthy();
    expect(
      shouldSkipGetChannelSegmentsByChannel({ ...waveformQueryChannelInput, channel: undefined })
    ).toBeTruthy();
    expect(shouldSkipGetChannelSegmentsByChannel(waveformQueryChannelInput)).toBeFalsy();
  });

  it('will not execute query if the args are invalid', async () => {
    const mockStoreCreator: MockStoreCreator<AppState, AnyAction> = createMockStore([thunk]);

    const store = mockStoreCreator(appState);

    await store.dispatch(
      getChannelSegmentsByChannel({ ...waveformQueryChannelInput, channel: null }) as any
    );

    // results should have empty arrays since current interval is not set
    expect(store.getActions()).toHaveLength(0);
  });
  it('will not execute query if the current interval is not defined', async () => {
    const mockStoreCreator: MockStoreCreator<AppState, AnyAction> = createMockStore([thunk]);

    const store = mockStoreCreator(appState);

    await store.dispatch(getChannelSegmentsByChannel(waveformQueryChannelInput) as any);

    // results should have empty arrays since current interval is not set
    expect(store.getActions()[store.getActions().length - 1].type).toEqual(
      'channelSegment/getChannelSegmentsByChannel/rejected'
    );
    expect(store.getActions()[store.getActions().length - 1].payload).toMatchInlineSnapshot(
      `[Error: Rejected fetchChannelSegmentsByChannel]`
    );
  });

  it('can handle rejected state', async () => {
    const mockStoreCreator: MockStoreCreator<AppState, AnyAction> = createMockStore([thunk]);

    const realStore = getStore();

    const state = cloneDeep(realStore.getState());
    state.app.workflow.timeRange.startTimeSecs = 4;
    state.app.workflow.timeRange.endTimeSecs = 6;

    const store = mockStoreCreator(state);

    await store.dispatch(getChannelSegmentsByChannel(waveformQueryChannelInput) as any);

    expect(store.getActions()[store.getActions().length - 1].type).toEqual(
      'channelSegment/getChannelSegmentsByChannel/rejected'
    );
  });
});
