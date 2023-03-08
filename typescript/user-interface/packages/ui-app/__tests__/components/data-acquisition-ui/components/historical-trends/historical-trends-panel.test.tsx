/* eslint-disable react/jsx-no-constructed-context-values */
/* eslint-disable @typescript-eslint/no-explicit-any */
import { SohTypes } from '@gms/common-model';
import { ValueType } from '@gms/common-util';
import type GoldenLayout from '@gms/golden-layout';
import type { UiHistoricalSohAsTypedArray } from '@gms/ui-state';
import { getStore } from '@gms/ui-state';
import { sohConfiguration } from '@gms/ui-state/__tests__/__data__/soh-configuration-query-data';
import { render } from '@testing-library/react';
import Immutable from 'immutable';
import React from 'react';
import { Provider } from 'react-redux';

import { BaseDisplayContext } from '../../../../../src/ts/components/common-ui/components/base-display';
import type { HistoricalTrendsPanelProps } from '../../../../../src/ts/components/data-acquisition-ui/components/historical-trends/historical-trends-panel';
import {
  HistoricalTrendsHistoryPanel,
  useChannelVisibilityMap
} from '../../../../../src/ts/components/data-acquisition-ui/components/historical-trends/historical-trends-panel';
import * as validateNonIdealStateDependency from '../../../../../src/ts/components/data-acquisition-ui/components/historical-trends/non-ideal-states';
import * as Util from '../../../../../src/ts/components/data-acquisition-ui/components/historical-trends/utils';
import type { BarLineChartData } from '../../../../../src/ts/components/data-acquisition-ui/shared/chart/types';
import { renderHook } from '../../../../utils/render-hook-util';

// set up window alert and open so we don't see errors
(window as any).alert = jest.fn();
(window as any).open = jest.fn();

const MOCK_TIME = 1530518207007;
const MOCK_TIME_STR = '2021-01-20 02:34:31';

const mockDate: any = new Date(MOCK_TIME);
mockDate.now = () => MOCK_TIME;
Date.constructor = jest.fn(() => new Date(MOCK_TIME));
jest.spyOn(global, 'Date').mockImplementation(() => mockDate);
Date.now = jest.fn(() => MOCK_TIME);
Date.UTC = jest.fn(() => MOCK_TIME);

jest.mock('moment-precise-range-plugin', () => {
  return {};
});

jest.mock('moment', () => {
  // mock chain builder pattern
  const mMoment = {
    utc: jest.fn(() => mMoment),
    format: jest.fn(() => MOCK_TIME_STR)
  };

  // mock the constructor and to modify instance methods
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const fn: any = jest.fn(() => {
    mMoment.format = jest.fn(() => MOCK_TIME_STR);
    return mMoment;
  });

  // mock moment methods that depend on moment not on a moment instance
  fn.unix = () => ({ utc: () => mMoment });
  return fn;
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

const historicalTrendsPanelProps: HistoricalTrendsPanelProps = {
  monitorType: SohTypes.SohMonitorType.MISSING,
  valueType: ValueType.PERCENTAGE,
  displaySubtitle: 'Historical trends for missing',
  glContainer,
  // eslint-disable-next-line @typescript-eslint/no-magic-numbers
  sohHistoricalDurations: [30000, 90000],
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
  }
};

const historicalSohData: UiHistoricalSohAsTypedArray = {
  stationName: 'test',
  calculationTimes: [1, 2, 3, 4],
  monitorValues: [
    {
      average: 3,
      channelName: 'test',
      type: SohTypes.SohValueType.DURATION,
      values: new Float32Array([1, 2, 3, 4, 1, 2, 3, 4])
    }
  ],
  percentageSent: 0,
  minAndMax: { xMax: 0, yMax: 0, xMin: 0, yMin: 0 }
};

jest.mock('@gms/ui-state', () => {
  const actual = jest.requireActual('@gms/ui-state');
  return {
    ...actual,
    useGetSohConfigurationQuery: jest.fn(() => ({
      data: sohConfiguration,
      isLoading: false
    })),
    useRetrieveDecimatedHistoricalStationSohQuery: jest.fn(() => ({
      data: historicalSohData,
      isLoading: false
    }))
  };
});

describe('Historical Trends Panel for Missing', () => {
  // mock the functions that create data that is passed to the charts
  jest
    .spyOn(validateNonIdealStateDependency, 'validateNonIdealState')
    .mockImplementation(() => undefined);
  const barLineChartData: BarLineChartData = {
    categories: { x: ['x1', 'x2'], y: [] },
    lineDefs: [
      {
        color: 'red',
        id: 1,
        values: new Float32Array([1, 1, 2, 2]),
        average: 1.5
      }
    ],
    barDefs: [
      { color: 'red', id: 3, value: { x: 1, y: 1 } },
      { color: 'red', id: 4, value: { x: 2, y: 2 } }
    ],
    thresholdsMarginal: [1, 2],
    thresholdsBad: [1, 2]
  };
  jest.spyOn(Util, 'getChartData').mockImplementation(() => barLineChartData);
  it('should be defined', () => {
    expect(HistoricalTrendsHistoryPanel).toBeDefined();
  });
  it('should match snapshot', () => {
    // eslint-disable-next-line @typescript-eslint/no-magic-numbers
    jest.setTimeout(15000);
    const store = getStore();
    const { container } = render(
      <Provider store={store}>
        <BaseDisplayContext.Provider
          value={{
            glContainer: { width: 100, height: 100 } as any,
            widthPx: 100,
            heightPx: 100
          }}
        >
          {/* eslint-disable-next-line react/jsx-props-no-spreading */}
          <HistoricalTrendsHistoryPanel {...historicalTrendsPanelProps} />
        </BaseDisplayContext.Provider>
      </Provider>
    );
    expect(container).toMatchSnapshot();
  });
  it('can handle useChannelVisibilityMap', () => {
    let visibilityMap = Immutable.Map<string, boolean>();
    visibilityMap = visibilityMap.set('name1', true);
    visibilityMap = visibilityMap.set('name2', true);
    const [channelVisibilityMap, setChannelVisibilityMap] = renderHook(() =>
      useChannelVisibilityMap(['name1', 'name2'])
    );

    expect(setChannelVisibilityMap).toBeDefined();
    expect(channelVisibilityMap).toEqual(visibilityMap);
  });
});
