/* eslint-disable react/jsx-no-constructed-context-values */
import { SohTypes } from '@gms/common-model';
import { uuid, ValueType } from '@gms/common-util';
import { dataAcquisitionActions, getStore } from '@gms/ui-state';
import { sohConfiguration } from '@gms/ui-state/__tests__/__data__/soh-configuration-query-data';
import { render } from '@testing-library/react';
import uniqueId from 'lodash/uniqueId';
import React from 'react';
import { Provider } from 'react-redux';

import { BaseDisplayContext } from '../../../../../src/ts/components/common-ui/components/base-display';
import type { SohBarChartPanelProps } from '../../../../../src/ts/components/data-acquisition-ui/components/soh-bar-chart/soh-bar-chart-panel';
import { SohBarChartPanel } from '../../../../../src/ts/components/data-acquisition-ui/components/soh-bar-chart/soh-bar-chart-panel';
import type { SohContextData } from '../../../../../src/ts/components/data-acquisition-ui/shared/soh-context';
import { SohContext } from '../../../../../src/ts/components/data-acquisition-ui/shared/soh-context';
import { FilterableSOHTypes } from '../../../../../src/ts/components/data-acquisition-ui/shared/toolbars/soh-toolbar';

uuid.asString = jest.fn().mockImplementation(uniqueId);

// set up window alert and open so we don't see errors
(window as any).alert = jest.fn();
(window as any).open = jest.fn();
window.ResizeObserver = jest.fn(() => {
  return { observe: jest.fn(), disconnect: jest.fn(), unobserve: jest.fn() };
});
const globalAny: any = global;
globalAny.ResizeObserver = window.ResizeObserver;
globalAny.DOMRect = jest.fn(() => ({}));

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

describe('SohBarChartPanel class', () => {
  const channelStatusesToDisplay: Map<FilterableSOHTypes, boolean> = new Map<
    FilterableSOHTypes,
    boolean
  >();
  const columnHeaderData = FilterableSOHTypes.GOOD;
  channelStatusesToDisplay.set(columnHeaderData, true);
  const monitorStatusesToDisplay: Map<any, boolean> = new Map();
  monitorStatusesToDisplay.set(SohTypes.SohStatusSummary.GOOD, true);
  const myProps: SohBarChartPanelProps = {
    minHeightPx: 100,
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
    sohConfiguration
  };

  const contextDefaults: SohContextData = {
    glContainer: {} as any,
    selectedAceiType: SohTypes.AceiType.BEGINNING_TIME_OUTAGE,
    setSelectedAceiType: jest.fn()
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

  const { container } = render(
    <Provider store={store}>
      <BaseDisplayContext.Provider
        value={{
          glContainer: { width: 150, height: 150 } as any,
          widthPx: 150,
          heightPx: 150
        }}
      >
        <SohContext.Provider value={contextDefaults}>
          {/* eslint-disable-next-line react/jsx-props-no-spreading */}
          <SohBarChartPanel {...myProps} />
        </SohContext.Provider>
      </BaseDisplayContext.Provider>
    </Provider>
  );
  it('should be defined', () => {
    expect(SohBarChartPanel).toBeDefined();
  });
  it('should match snapshot', () => {
    expect(container).toMatchSnapshot();
  });
});
