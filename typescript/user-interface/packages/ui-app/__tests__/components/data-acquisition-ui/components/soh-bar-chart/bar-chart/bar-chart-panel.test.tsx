/* eslint-disable react/jsx-no-constructed-context-values */
import { SohTypes } from '@gms/common-model';
import { uuid, ValueType } from '@gms/common-util';
import { dataAcquisitionActions, getStore } from '@gms/ui-state';
import { sohConfiguration } from '@gms/ui-state/__tests__/__data__/soh-configuration-query-data';
import { render } from '@testing-library/react';
import React from 'react';
import { Provider } from 'react-redux';

import { BaseDisplayContext } from '../../../../../../src/ts/components/common-ui/components/base-display/base-display-context';
import type { BarChartPanelProps } from '../../../../../../src/ts/components/data-acquisition-ui/components/soh-bar-chart/bar-chart/bar-chart-panel';
import { BarChartPanel } from '../../../../../../src/ts/components/data-acquisition-ui/components/soh-bar-chart/bar-chart/bar-chart-panel';

// set up window alert and open so we don't see errors
(window as any).alert = jest.fn();
(window as any).open = jest.fn();

let idCount = 0;
// eslint-disable-next-line no-plusplus
uuid.asString = jest.fn().mockImplementation(() => ++idCount);

// eslint-disable-next-line import/no-deprecated
const lodash = jest.requireActual('lodash');
lodash.uniqueId = () => '1';

const barChartPanelProps: BarChartPanelProps = {
  minHeightPx: 100,
  chartHeaderHeight: 100,
  type: SohTypes.SohMonitorType.LAG,
  valueType: ValueType.INTEGER,
  station: {
    id: '1',
    uuid: '1',
    needsAcknowledgement: true,
    needsAttention: true,
    sohStatusSummary: undefined,
    stationGroups: [],
    statusContributors: [],
    time: undefined,
    stationName: '1',
    allStationAggregates: [],
    channelSohs: [
      {
        channelName: 'adsf',
        channelSohStatus: undefined,
        allSohMonitorValueAndStatuses: [
          {
            monitorType: SohTypes.SohMonitorType.LAG,
            value: 10,
            valuePresent: true,
            status: SohTypes.SohStatusSummary.GOOD,
            hasUnacknowledgedChanges: true,
            contributing: false,
            quietUntilMs: 1,
            thresholdBad: 3,
            thresholdMarginal: 3
          },
          {
            monitorType: SohTypes.SohMonitorType.LAG,
            value: 11,
            valuePresent: true,
            status: SohTypes.SohStatusSummary.GOOD,
            hasUnacknowledgedChanges: true,
            contributing: false,
            quietUntilMs: 1,
            thresholdBad: 3,
            thresholdMarginal: 3
          }
        ]
      },
      {
        channelName: 'adsf2',
        channelSohStatus: undefined,
        allSohMonitorValueAndStatuses: [
          {
            monitorType: SohTypes.SohMonitorType.LAG,
            value: 10,
            valuePresent: true,
            status: SohTypes.SohStatusSummary.GOOD,
            hasUnacknowledgedChanges: true,
            contributing: false,
            quietUntilMs: 1,
            thresholdBad: 3,
            thresholdMarginal: 3
          },
          {
            monitorType: SohTypes.SohMonitorType.LAG,
            value: 11,
            valuePresent: true,
            status: SohTypes.SohStatusSummary.GOOD,
            hasUnacknowledgedChanges: true,
            contributing: false,
            quietUntilMs: 1,
            thresholdBad: 3,
            thresholdMarginal: 3
          }
        ]
      }
    ]
  },
  sohStatus: {
    lastUpdated: 0,
    loading: false,
    isStale: false,
    stationAndStationGroupSoh: {
      isUpdateResponse: false,
      stationGroups: [],
      stationSoh: [
        {
          id: '1',
          uuid: '1',
          needsAcknowledgement: true,
          needsAttention: true,
          sohStatusSummary: undefined,
          stationGroups: [],
          statusContributors: [],
          time: undefined,
          stationName: '1',
          allStationAggregates: [],
          channelSohs: [
            {
              channelName: 'adsf',
              channelSohStatus: undefined,
              allSohMonitorValueAndStatuses: [
                {
                  monitorType: SohTypes.SohMonitorType.LAG,
                  value: 10,
                  valuePresent: true,
                  status: SohTypes.SohStatusSummary.GOOD,
                  hasUnacknowledgedChanges: true,
                  contributing: false,
                  quietUntilMs: 1,
                  thresholdBad: 3,
                  thresholdMarginal: 3
                },
                {
                  monitorType: SohTypes.SohMonitorType.LAG,
                  value: 11,
                  valuePresent: true,
                  status: SohTypes.SohStatusSummary.GOOD,
                  hasUnacknowledgedChanges: true,
                  contributing: false,
                  quietUntilMs: 1,
                  thresholdBad: 3,
                  thresholdMarginal: 3
                }
              ]
            }
          ]
        }
      ]
    }
  },
  channelSoh: [
    {
      hasUnacknowledgedChanges: false,
      isNullData: false,
      name: 'TestSt.adsf',
      quietDurationMs: undefined,
      quietExpiresAt: 1,
      status: SohTypes.SohStatusSummary.GOOD,
      thresholdBad: 3,
      thresholdMarginal: 3,
      value: 8
    },
    {
      hasUnacknowledgedChanges: true,
      isNullData: false,
      name: 'TestSt.adsf2',
      quietDurationMs: undefined,
      quietExpiresAt: 1,
      status: SohTypes.SohStatusSummary.GOOD,
      thresholdBad: 3,
      thresholdMarginal: 3,
      value: 10
    }
  ],
  sohConfiguration
};
describe('Bar Chart Panel', () => {
  // eslint-disable-next-line @typescript-eslint/no-magic-numbers
  Date.now = jest.fn().mockReturnValue(1573244087715);
  const setState = jest.fn();
  const useStateSpy = jest.spyOn(React, 'useState');
  const mock: any = init => [init, setState];
  useStateSpy.mockImplementation(mock);

  const store = getStore();
  store.dispatch(
    dataAcquisitionActions.setSohStatus({
      isStale: false,
      lastUpdated: 0,
      loading: false,
      stationAndStationGroupSoh: undefined
    })
  );

  const { container } = render(
    <Provider store={store}>
      <BaseDisplayContext.Provider
        value={{
          glContainer: { width: 150, height: 150 } as any,
          widthPx: 150,
          heightPx: 150
        }}
      >
        {/* eslint-disable-next-line react/jsx-props-no-spreading */}
        <BarChartPanel {...barChartPanelProps} />
      </BaseDisplayContext.Provider>
    </Provider>
  );

  it('should be defined', () => {
    expect(BarChartPanel).toBeDefined();
  });

  it('should match snapshot', () => {
    expect(container).toMatchSnapshot();
  });
});
