/* eslint-disable react/function-component-definition */
// TODO: move this file to ui state
/* eslint-disable import/no-extraneous-dependencies */
// eslint-disable-next-line workspaces/require-dependency
import { uiChannelSegment } from '@gms/ui-app/__tests__/components/__data__/weavess-channel-segment-data';
import { renderHook } from '@testing-library/react-hooks';
import clone from 'lodash/clone';
import React from 'react';
import { Provider } from 'react-redux';
import { create } from 'react-test-renderer';
import type { AnyAction } from 'redux';
import type { MockStoreCreator } from 'redux-mock-store';
import createMockStore from 'redux-mock-store';
import thunk from 'redux-thunk';

import { dataInitialState } from '../../../src/ts/app';
import type { GetChannelSegmentsByChannelsQueryArgs } from '../../../src/ts/app/api/data/channel-segment/types';
import {
  useGetChannelSegments,
  useGetChannelSegmentsByChannels
} from '../../../src/ts/app/hooks/channel-segment-hooks';
import type { AppState } from '../../../src/ts/app/store';
import { getStore } from '../../../src/ts/app/store';
import { appState } from '../../test-util';

describe('channel segment hooks', () => {
  it('exists', () => {
    expect(useGetChannelSegments).toBeDefined();
    expect(useGetChannelSegmentsByChannels).toBeDefined();
  });

  it('useGetChannelSegmentsByChannels resturns an object with loading values', () => {
    const mockStoreCreator: MockStoreCreator<AppState, AnyAction> = createMockStore([thunk]);
    const dataInitialStateCopy = clone(dataInitialState);
    dataInitialStateCopy.uiChannelSegments = {
      'PDAR.BHZ': { unfiltered: [uiChannelSegment] }
    };
    const mockAppState = appState;
    mockAppState.data = dataInitialStateCopy;
    const store = mockStoreCreator(mockAppState);
    const queryArgs: GetChannelSegmentsByChannelsQueryArgs = {
      startTime: 100,
      endTime: 200,
      channels: [
        { name: 'PDAR.BHZ', effectiveAt: 101 },
        { name: 'PDAR.BHA', effectiveAt: 101 }
      ]
    };
    const Wrapper = ({ children }) => <Provider store={store}>{children}</Provider>;
    const { result } = renderHook(() => useGetChannelSegmentsByChannels(queryArgs), {
      wrapper: Wrapper
    });
    expect(result.current).toMatchSnapshot();
  });

  it('hook query for channel segments', () => {
    const store = getStore();

    const Component1: React.FC = () => {
      const result = useGetChannelSegments({ startTimeSecs: 0, endTimeSecs: 1000 });
      return <>{JSON.stringify(result.data)}</>;
    };

    const Component2: React.FC = () => {
      // call twice to hit other blocks of code
      const result = useGetChannelSegments({ startTimeSecs: 0, endTimeSecs: 1000 });
      return <>{JSON.stringify(result.data)}</>;
    };

    expect(
      create(
        <Provider store={store}>
          <Component1 />
          <Component2 />
        </Provider>
      ).toJSON()
    ).toMatchSnapshot();

    expect(
      create(
        <Provider store={store}>
          <Component1 />
          <Component2 />
        </Provider>
      ).toJSON()
    ).toMatchSnapshot();
  });
});
