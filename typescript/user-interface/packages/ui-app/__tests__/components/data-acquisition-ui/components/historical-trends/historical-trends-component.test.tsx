import { SohTypes } from '@gms/common-model';
import { ValueType } from '@gms/common-util';
import type GoldenLayout from '@gms/golden-layout';
import { commonActions, dataAcquisitionActions, getStore } from '@gms/ui-state';
import { sohConfiguration } from '@gms/ui-state/__tests__/__data__/soh-configuration-query-data';
import { sohStatus } from '@gms/ui-state/__tests__/__data__/soh-status-data';
import { render } from '@testing-library/react';
import React from 'react';
import { Provider } from 'react-redux';

import { buildHistoricalTrendsComponent } from '../../../../../src/ts/components/data-acquisition-ui/components/historical-trends';

// set up window alert and open so we don't see errors
(window as any).alert = jest.fn();
(window as any).open = jest.fn();

const MOCK_TIME = 1606818240000;
global.Date.now = jest.fn(() => MOCK_TIME);

jest.mock('@gms/ui-state', () => {
  const actual = jest.requireActual('@gms/ui-state');
  return {
    ...actual,
    useGetSohConfigurationQuery: jest.fn(() => sohConfiguration)
  };
});

const glContainer: GoldenLayout.Container = {
  title: 'workflow',
  width: 1900,
  height: 1200,
  isHidden: false,
  layoutManager: undefined,
  parent: undefined,
  tab: undefined,
  close: jest.fn(),
  emit: jest.fn(),
  extendState: jest.fn(),
  getElement: jest.fn(),
  getState: jest.fn(),
  hide: jest.fn(),
  off: jest.fn(),
  on: jest.fn(),
  setSize: jest.fn(),
  setState: jest.fn(),
  setTitle: jest.fn(),
  show: jest.fn(),
  trigger: jest.fn(),
  unbind: jest.fn()
};

describe('HistoricalTrendsComponent', () => {
  it('is buildHistoricalTrendsHistoryComponent exported', () => {
    expect(buildHistoricalTrendsComponent).toBeDefined();
  });

  it('is buildHistoricalTrendsHistoryComponent loading', () => {
    const store = getStore();
    store.dispatch(dataAcquisitionActions.setSohStatus(sohStatus));
    store.dispatch(
      commonActions.setSelectedStationIds([sohStatus.stationAndStationGroupSoh.stationSoh[0].id])
    );

    const Component = buildHistoricalTrendsComponent(
      SohTypes.SohMonitorType.LAG,
      ValueType.FLOAT,
      `title`
    );

    const { container } = render(
      <Provider store={store}>
        <Component glContainer={glContainer} />
      </Provider>
    );
    expect(container).toMatchSnapshot();
  });

  it('is buildHistoricalTrendsHistoryComponent no data', () => {
    const store = getStore();

    const Component = buildHistoricalTrendsComponent(
      SohTypes.SohMonitorType.LAG,
      ValueType.FLOAT,
      `title`
    );
    const { container } = render(
      <Provider store={store}>
        <Component glContainer={glContainer} />
      </Provider>
    );

    expect(container).toMatchSnapshot();
  });

  it('is buildHistoricalTrendsHistoryComponent no selected station', () => {
    const store = getStore();
    store.dispatch(dataAcquisitionActions.setSohStatus(sohStatus));

    const Component = buildHistoricalTrendsComponent(
      SohTypes.SohMonitorType.LAG,
      ValueType.FLOAT,
      `title`
    );
    const { container } = render(
      <Provider store={store}>
        <Component glContainer={glContainer} />
      </Provider>
    );

    expect(container).toMatchSnapshot();
  });

  it('is buildHistoricalTrendsHistoryComponent rendered LAG', () => {
    const store = getStore();
    store.dispatch(dataAcquisitionActions.setSohStatus(sohStatus));
    store.dispatch(
      commonActions.setSelectedStationIds([sohStatus.stationAndStationGroupSoh.stationSoh[0].id])
    );

    const Component = buildHistoricalTrendsComponent(
      SohTypes.SohMonitorType.LAG,
      ValueType.FLOAT,
      `LAG`
    );

    // eslint-disable-next-line react/jsx-props-no-spreading
    const { container } = render(
      <Provider store={store}>
        <Component glContainer={glContainer} />
      </Provider>
    );
    expect(container).toMatchSnapshot();
  });

  it('is buildHistoricalTrendsHistoryComponent rendered MISSING', () => {
    const store = getStore();
    store.dispatch(dataAcquisitionActions.setSohStatus(sohStatus));
    store.dispatch(
      commonActions.setSelectedStationIds([sohStatus.stationAndStationGroupSoh.stationSoh[0].id])
    );

    const Component = buildHistoricalTrendsComponent(
      SohTypes.SohMonitorType.MISSING,
      ValueType.PERCENTAGE,
      `MISSING`
    );

    // eslint-disable-next-line react/jsx-props-no-spreading
    const { container } = render(
      <Provider store={store}>
        <Component glContainer={glContainer} />
      </Provider>
    );
    expect(container).toMatchSnapshot();
  });
});
