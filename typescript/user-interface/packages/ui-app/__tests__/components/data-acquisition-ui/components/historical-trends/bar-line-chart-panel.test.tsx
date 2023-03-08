/* eslint-disable @typescript-eslint/no-magic-numbers */
import { SohTypes } from '@gms/common-model';
import { ValueType } from '@gms/common-util';
import { transformHistoricalSohData } from '@gms/ui-state';
import { DistinctColorPalette } from '@gms/ui-util';
import { render } from '@testing-library/react';
import type { AxiosResponse } from 'axios';
import Immutable from 'immutable';
import React from 'react';

import type { BarLineChartPanelProps } from '../../../../../src/ts/components/data-acquisition-ui/components/historical-trends/bar-line-chart-panel';
import {
  BarLineChartPanel,
  useBarLineChartData
} from '../../../../../src/ts/components/data-acquisition-ui/components/historical-trends/bar-line-chart-panel';
import { testStationSoh } from '../../../../__data__/data-acquisition-ui/soh-overview-data';

// set up window alert and open so we don't see errors
(window as any).alert = jest.fn();
(window as any).open = jest.fn();

const MOCK_TIME = 1606818240000;
const TIME_INCREMENT = 60000;
global.Date.now = jest.fn(() => MOCK_TIME);

describe('BarLineChartPanel', () => {
  const distinctColorPalette = new DistinctColorPalette(['channel name', 'channel name 2']);

  it('is BarLineChartPanel exported', () => {
    expect(BarLineChartPanel).toBeDefined();
  });

  it('is useBarLineChartData exported', () => {
    expect(useBarLineChartData).toBeDefined();
  });

  it('renders BarLineChartPanel undefined data', () => {
    const props: BarLineChartPanelProps = {
      legendTitle: 'My title',
      startTimeMs: Date.now(),
      endTimeMs: Date.now() + TIME_INCREMENT,
      heightPx: 100,
      widthPx: 100,
      entryVisibilityMap: Immutable.Map(),
      colorPalette: distinctColorPalette,
      monitorType: SohTypes.SohMonitorType.LAG,
      station: testStationSoh,
      valueType: ValueType.FLOAT,
      uiHistoricalSoh: {
        calculationTimes: [0, 1, 2],
        stationName: `station name`,
        minAndMax: {
          xMax: 1,
          xMin: 0,
          yMax: 1,
          yMin: 0
        },
        monitorValues: [
          {
            channelName: `channel name`,
            average: 66.66,
            type: SohTypes.SohValueType.PERCENT,
            values: undefined
          }
        ],
        percentageSent: 100
      }
    };

    // eslint-disable-next-line react/jsx-props-no-spreading
    const { container } = render(<BarLineChartPanel {...props} />);
    expect(container).toMatchSnapshot();
  });

  it('renders BarLineChartPanel with percent data', async () => {
    const response: AxiosResponse<SohTypes.UiHistoricalSoh> = {
      config: {},
      headers: {},
      status: 200,
      statusText: '',
      request: {},
      data: {
        calculationTimes: [0, 1, 2],
        stationName: `station name`,
        monitorValues: [
          {
            channelName: `channel name`,
            average: 66.66,
            values: {
              values: [50, 60, 90],
              type: SohTypes.SohValueType.PERCENT
            }
          }
        ],
        percentageSent: 100
      }
    };
    const result = await transformHistoricalSohData(0, 2)(response.data);
    const props: BarLineChartPanelProps = {
      legendTitle: 'My title',
      startTimeMs: Date.now(),
      endTimeMs: Date.now() + TIME_INCREMENT,
      heightPx: 100,
      widthPx: 100,
      entryVisibilityMap: Immutable.Map(),
      colorPalette: distinctColorPalette,
      monitorType: SohTypes.SohMonitorType.LAG,
      station: testStationSoh,
      valueType: ValueType.FLOAT,
      uiHistoricalSoh: result
    };

    // eslint-disable-next-line react/jsx-props-no-spreading
    const { container } = render(<BarLineChartPanel {...props} />);
    expect(container).toMatchSnapshot();
  });

  it('renders tooltip as Unknown', async () => {
    const response: AxiosResponse<SohTypes.UiHistoricalSoh> = {
      config: {},
      headers: {},
      status: 200,
      statusText: '',
      request: {},
      data: {
        calculationTimes: [0, 1, 2],
        stationName: `station name`,
        monitorValues: [
          {
            channelName: `channel name`,
            average: 66.66,
            values: {
              values: [50, 60, 90],
              type: SohTypes.SohValueType.PERCENT
            }
          },
          {
            channelName: `channel name 2`,
            average: -1,
            values: {
              values: [-1, -1, -1],
              type: SohTypes.SohValueType.PERCENT
            }
          }
        ],
        percentageSent: 100
      }
    };
    const result = await transformHistoricalSohData(0, 2)(response.data);
    const props: BarLineChartPanelProps = {
      legendTitle: 'My title',
      startTimeMs: Date.now(),
      endTimeMs: Date.now() + TIME_INCREMENT,
      heightPx: 100,
      widthPx: 100,
      entryVisibilityMap: Immutable.Map(),
      colorPalette: distinctColorPalette,
      monitorType: SohTypes.SohMonitorType.LAG,
      station: testStationSoh,
      valueType: ValueType.FLOAT,
      uiHistoricalSoh: result
    };

    // eslint-disable-next-line react/jsx-props-no-spreading
    const { container } = render(<BarLineChartPanel {...props} />);
    expect(container).toMatchSnapshot();
  });

  it('renders non-ideal state when all data is Unknown', async () => {
    const response: AxiosResponse<SohTypes.UiHistoricalSoh> = {
      config: {},
      headers: {},
      status: 200,
      statusText: '',
      request: {},
      data: {
        calculationTimes: [0, 1, 2],
        stationName: `station name`,
        monitorValues: [
          {
            channelName: `channel name`,
            average: -1,
            values: {
              values: [-1, -1, -1],
              type: SohTypes.SohValueType.PERCENT
            }
          }
        ],
        percentageSent: 100
      }
    };
    const result = await transformHistoricalSohData(0, 2)(response.data);
    const props: BarLineChartPanelProps = {
      legendTitle: 'My title',
      startTimeMs: Date.now(),
      endTimeMs: Date.now() + TIME_INCREMENT,
      heightPx: 100,
      widthPx: 100,
      entryVisibilityMap: Immutable.Map(),
      colorPalette: distinctColorPalette,
      monitorType: SohTypes.SohMonitorType.LAG,
      station: testStationSoh,
      valueType: ValueType.FLOAT,
      uiHistoricalSoh: result
    };

    // eslint-disable-next-line react/jsx-props-no-spreading
    const { container } = render(<BarLineChartPanel {...props} />);
    expect(container).toMatchSnapshot();
  });

  it('renders BarLineChartPanel with duration data', async () => {
    const response: AxiosResponse<SohTypes.UiHistoricalSoh> = {
      config: {},
      headers: {},
      status: 200,
      statusText: '',
      request: {},
      data: {
        calculationTimes: [0, 1, 2],
        stationName: `station name`,
        monitorValues: [
          {
            channelName: `channel name`,
            average: 6100.7,
            values: {
              values: [5000, 6000, 9000],
              type: SohTypes.SohValueType.DURATION
            }
          }
        ],
        percentageSent: 100
      }
    };
    const result = await transformHistoricalSohData(0, 2)(response.data);
    const props: BarLineChartPanelProps = {
      legendTitle: 'My title',
      startTimeMs: Date.now(),
      endTimeMs: Date.now() + TIME_INCREMENT,
      heightPx: 100,
      widthPx: 100,
      entryVisibilityMap: Immutable.Map(),
      colorPalette: distinctColorPalette,
      monitorType: SohTypes.SohMonitorType.LAG,
      station: testStationSoh,
      valueType: ValueType.FLOAT,
      uiHistoricalSoh: result
    };

    // eslint-disable-next-line react/jsx-props-no-spreading
    const { container } = render(<BarLineChartPanel {...props} />);
    expect(container).toMatchSnapshot();
  });
});
