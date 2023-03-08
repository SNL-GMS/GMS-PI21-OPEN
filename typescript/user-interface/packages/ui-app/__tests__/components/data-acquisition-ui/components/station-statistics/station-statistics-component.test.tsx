import { SohTypes } from '@gms/common-model';
import type { UiStationSoh } from '@gms/common-model/lib/soh/types';
import type { Container } from '@gms/golden-layout';
import { getStore } from '@gms/ui-state';
import { sohConfiguration } from '@gms/ui-state/__tests__/__data__/soh-configuration-query-data';
import { render } from '@testing-library/react';
import React from 'react';
import { Provider } from 'react-redux';

import type { StationStatisticsProps } from '../../../../../src/ts/components/data-acquisition-ui/components/station-statistics/station-statistics-component';
import { StationStatisticsComponent } from '../../../../../src/ts/components/data-acquisition-ui/components/station-statistics/station-statistics-component';
import { testStationSoh } from '../../../../__data__/data-acquisition-ui/soh-overview-data';
import { useQueryStateResult } from '../../../../__data__/test-util-data';

const glWidth = 1000;
const glHeight = 500;

// messing with normal stuff
const gLContainer: Container = {
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

const setSelectedStationIds = jest.fn();

const selectedStationIds = ['A'];

const reduxProps: any = {
  glContainer: gLContainer,
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

const sohConfigurationQuery = useQueryStateResult;
sohConfigurationQuery.data = sohConfiguration;

const channel: SohTypes.ChannelSoh = {
  allSohMonitorValueAndStatuses: [
    {
      status: SohTypes.SohStatusSummary.GOOD,
      value: 1,
      valuePresent: true,
      monitorType: SohTypes.SohMonitorType.ENV_ZEROED_DATA,
      hasUnacknowledgedChanges: false,
      contributing: false,
      thresholdMarginal: 1,
      thresholdBad: 10,
      quietUntilMs: 1
    }
  ],
  channelName: 'AAA111',
  channelSohStatus: SohTypes.SohStatusSummary.GOOD
};

const sohStatus: any = {
  loading: false,
  stationAndStationGroupSoh: {
    stationSoh: [
      {
        channelSohs: [channel],
        stationName: 'A'
      }
    ]
  }
};

// set up window alert and open so we don't see errors
(window as any).alert = jest.fn();
(window as any).open = jest.fn();
describe('Station Statistics Component', () => {
  const props: StationStatisticsProps = {
    ...reduxProps,
    sohStatus,
    sohConfigurationQuery,
    sohStationAndGroupStatusQuery,
    saveStationGroupSohStatus: jest.fn(),
    acknowledgeStationsByName: jest.fn(async () => new Promise(jest.fn())),
    mutate: undefined,
    result: undefined
  };

  const propsWithNoConfigQuery: StationStatisticsProps = {
    ...reduxProps,
    sohStatus,
    sohStationAndGroupStatusQuery,
    saveStationGroupSohStatus: jest.fn(),
    acknowledgeStationsByName: jest.fn(async () => new Promise(jest.fn())),
    mutate: undefined,
    result: undefined
  };

  const stationStatistics: any = new StationStatisticsComponent(props);
  const stationStatisticsNoConfigQuery: any = new StationStatisticsComponent(
    propsWithNoConfigQuery
  );
  const stationSoh: UiStationSoh = testStationSoh;
  it('is defined', () => {
    expect(StationStatisticsComponent).toBeDefined();
  });

  it.skip('can shallow render', () => {
    const { container } = render(
      <Provider store={getStore()}>
        {/* eslint-disable-next-line react/jsx-props-no-spreading */}
        <StationStatisticsComponent {...props} />
      </Provider>
    );
    expect(container).toMatchSnapshot();
  });

  it('Check SOH Display Criteria can handle when config query is not ready', () => {
    stationStatisticsNoConfigQuery.checkSOHDisplayCriteria([stationSoh]);
    expect(stationStatisticsNoConfigQuery.sohCheckDisplayCriteriaMap.size).toEqual(0);
  });

  it('can Check SOH Display Criteria with no entries', () => {
    stationStatistics.checkSOHDisplayCriteria([stationSoh]);
    expect(stationStatistics.sohCheckDisplayCriteriaMap.size).toEqual(1);
  });

  it('can Check SOH Display Criteria with an entry', () => {
    stationSoh.time = 1234567;
    stationStatistics.lastSohReport = 0;
    stationStatistics.sohLagReportPeriod = 0;
    stationStatistics.checkSOHDisplayCriteria([stationSoh]);
    expect(stationStatistics.sohCheckDisplayCriteriaMap.size).toEqual(1);
  });

  it('can run componentDidUpdate which adds to the map', () => {
    stationStatistics.componentDidUpdate(props);
    expect(stationStatistics.sohCheckDisplayCriteriaMap.size).toEqual(2);
  });
});
