import { uuid } from '@gms/common-util';
import type { Container } from '@gms/golden-layout';
import { dataAcquisitionActions, getStore } from '@gms/ui-state';
import { sohConfiguration } from '@gms/ui-state/__tests__/__data__/soh-configuration-query-data';
import { render } from '@testing-library/react';
import React from 'react';
import { Provider } from 'react-redux';

import type { SohBarChartProps } from '../../../../../src/ts/components/data-acquisition-ui/components/soh-bar-chart/soh-bar-chart-component';
import { SohBarChart } from '../../../../../src/ts/components/data-acquisition-ui/components/soh-bar-chart/soh-bar-chart-component';
import { channelSoh } from '../../../../__data__/data-acquisition-ui/soh-overview-data';
import { useQueryStateResult } from '../../../../__data__/test-util-data';

// mock the uuid
uuid.asString = jest.fn().mockImplementation(() => '12345789');

// eslint-disable-next-line @typescript-eslint/no-var-requires, @typescript-eslint/no-require-imports
const Enzyme = require('enzyme');

jest.mock('@gms/ui-state', () => {
  const actual = jest.requireActual('@gms/ui-state');
  return {
    ...actual,
    useGetSohConfigurationQuery: jest.fn(() => ({
      data: sohConfiguration,
      isLoading: false
    }))
  };
});

uuid.asString = jest.fn().mockReturnValue('1e872474-b19f-4325-9350-e217a6feddc0');

// set up window alert and open so we don't see errors
(window as any).alert = jest.fn();
(window as any).open = jest.fn();
window.ResizeObserver = jest.fn(() => {
  return { observe: jest.fn(), disconnect: jest.fn(), unobserve: jest.fn() };
});
const globalAny: any = global;
globalAny.ResizeObserver = window.ResizeObserver;
globalAny.DOMRect = jest.fn(() => ({}));

describe('SohBarChart class', () => {
  // eslint-disable-next-line @typescript-eslint/no-magic-numbers
  global.Date.now = jest.fn(() => 1530518207007);

  const glWidth = 1000;
  const glHeight = 500;

  const selectedStationIds = ['A'];

  const setSelectedStationIds = jest.fn();

  const myGLContainer: Container = {
    // Container
    width: glWidth,
    height: glHeight,
    parent: undefined,
    tab: undefined,
    title: 'container-title',
    layoutManager: undefined,
    isHidden: false,
    setState: jest.fn(),
    extendState: jest.fn(),
    getState: jest.fn(),
    getElement: jest.fn(),
    hide: jest.fn(),
    show: jest.fn(),
    setSize: jest.fn(),
    setTitle: jest.fn(),
    close: jest.fn(),
    // EventEmitter
    on: jest.fn(),
    emit: jest.fn(),
    trigger: jest.fn(),
    unbind: jest.fn(),
    off: jest.fn()
  };
  const myEnvReduxProps: any = {
    glContainer: myGLContainer,
    selectedStationIds,
    setSelectedStationIds
  };

  const sohStationAndGroupStatusQuery: any = {
    loading: false,
    stationAndStationGroupSoh: {
      stationSoh: [
        {
          stationName: 'A'
        }
      ]
    }
  };

  const channelSohForStationQuery: any = {
    channelSohForStation: {
      channelSohs: [channelSoh],
      stationName: 'A'
    }
  };

  const sohStatus: any = {
    loading: false,
    stationAndStationGroupSoh: {
      stationSoh: [
        {
          channelSohs: [channelSoh],
          stationName: 'A'
        }
      ]
    }
  };

  const sohConfigurationQuery = useQueryStateResult;
  sohConfigurationQuery.data = sohConfiguration;

  const myProps: SohBarChartProps = {
    sohStatus,
    ...myEnvReduxProps,
    channelSohForStationQuery,
    sohStationAndGroupStatusQuery,
    sohConfigurationQuery,
    saveStationGroupSohStatus: jest.fn(),
    quietChannelMonitorStatuses: jest.fn(async () => new Promise(jest.fn())),
    mutate: undefined,
    result: undefined,
    getSohConfiguration: jest.fn(() => ({ unsubscribe: jest.fn() })) as any
  };

  const store = getStore();
  store.dispatch(
    dataAcquisitionActions.setSohStatus({
      isStale: false,
      lastUpdated: 0,
      loading: false,
      stationAndStationGroupSoh: undefined
    })
  );

  const sohBarChart = Enzyme.mount(
    <Provider store={store}>
      {/* eslint-disable-next-line react/jsx-props-no-spreading */}
      <SohBarChart {...{ ...myProps, type: 'LAG' as any }} />
    </Provider>
  );

  it('should be defined', () => {
    expect(SohBarChart).toBeDefined();
  });

  it('should get station info', () => {
    sohBarChart.update();
    const stationLagInfo = sohBarChart.find(SohBarChart).instance().getStation();
    expect(stationLagInfo).toBeDefined();
  });

  // it.skip('should acknowledge channel monitor status', () => {
  //   const stationName = 'AAA';
  //   const sohMonType: SohTypes.SohMonitorType = SohTypes.SohMonitorType.LAG;
  //   const chanMonPair: SohTypes.ChannelMonitorPair = {
  //     channelName: 'AAA111',
  //     monitorType: sohMonType
  //   };
  //   const channelPairs: SohTypes.ChannelMonitorPair[] = [chanMonPair];
  //   // sohBarChart.find(SohBarChart).instance().quietChannelMonitorStatuses(stationName, channelPairs);
  // });

  it('should match snapshot', () => {
    const { container } = render(
      <Provider store={store}>
        {/* eslint-disable-next-line react/jsx-props-no-spreading */}
        <SohBarChart {...{ ...myProps, type: 'LAG' as any }} />
      </Provider>
    );
    expect(container).toMatchSnapshot();
  });
});
