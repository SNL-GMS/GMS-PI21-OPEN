/* eslint-disable react/function-component-definition */
/* eslint-disable @typescript-eslint/no-magic-numbers */
import { SohTypes } from '@gms/common-model';
import { act, renderHook } from '@testing-library/react-hooks';
import React from 'react';
import { Provider } from 'react-redux';

import type { AppState } from '../../../src/ts/app';
import {
  useAcknowledgeSohStatus,
  useClientLog,
  useGetAcknowledgeSohStatusMutation,
  useGetQuietSohStatusMutation,
  useQuietSohStatus
} from '../../../src/ts/app/hooks/system-event-gateway-hooks';
import { getStore } from '../../../src/ts/app/store';
import { sohStatus } from '../../__data__/soh-status-data';
import { appState } from '../../test-util';

jest.mock('../../../src/ts/app/api/system-event-gateway/system-event-gateway-api-slice', () => {
  const actual = jest.requireActual(
    '../../../src/ts/app/api/system-event-gateway/system-event-gateway-api-slice'
  );
  return {
    ...actual,
    useQuietSohStatusMutation: () => [jest.fn()],
    useAcknowledgeSohStatusMutation: () => [jest.fn()]
  };
});

jest.mock('../../../src/ts/app/hooks/react-redux-hooks', () => {
  const actual = jest.requireActual('../../../src/ts/app/hooks/react-redux-hooks');
  return {
    ...actual,
    useAppSelector: jest.fn((stateFunc: (state: AppState) => any) => {
      const state: AppState = appState;
      state.app.dataAcquisition.data.sohStatus = sohStatus;
      return stateFunc(state);
    })
  };
});

describe('processing station hooks', () => {
  it('exists', () => {
    expect(useAcknowledgeSohStatus).toBeDefined();
    expect(useGetQuietSohStatusMutation).toBeDefined();
    expect(useClientLog).toBeDefined();
    expect(useGetAcknowledgeSohStatusMutation).toBeDefined();
    expect(useQuietSohStatus).toBeDefined();
  });

  it('can call quiet mutation', () => {
    const channelToQuiet: SohTypes.ChannelMonitorInput = {
      stationName: 'AAK',
      channelMonitorPairs: [
        {
          channelName: 'AAK.AAK.AK01',
          monitorType: SohTypes.SohMonitorType.ENV_AUTHENTICATION_SEAL_BROKEN
        }
      ],
      userName: 'foo',
      quietDurationMs: 5 * 60 * 1000,
      comment: 'foo quieting for 5 mins'
    };
    const useQuietFunc = useQuietSohStatus();
    expect(useQuietFunc).toBeDefined();
    expect(async () => useQuietFunc(channelToQuiet)).not.toThrowError();
  });

  it('can call acknowledge mutation', () => {
    const stationToAcknowledge: SohTypes.AcknowledgeSohStatus = {
      stationNames: ['AAK'],
      userName: 'foo',
      comment: 'foo acknowledging AAK'
    };
    const store = getStore();
    const Wrapper = ({ children }) => <Provider store={store}>{children}</Provider>;
    const { result } = renderHook(() => useAcknowledgeSohStatus(), {
      wrapper: Wrapper
    });
    expect(result.current).toBeDefined();
    act(() => {
      const myCurrent: any = result.current;
      myCurrent(stationToAcknowledge);
    });
  });
});
