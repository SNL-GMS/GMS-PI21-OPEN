/* eslint-disable @typescript-eslint/no-magic-numbers */
import { SohTypes } from '@gms/common-model';
import { DistinctColorPalette } from '@gms/ui-util';

import {
  barChartXAxisLabel,
  barChartXAxisTicFormat,
  barChartYAxisLabel,
  barChartYAxisTicFormat,
  chartTickFormatForThousands,
  getChartData,
  getChartHeight,
  getDataType,
  getLabel,
  getName,
  lineChartXAxisLabel,
  lineChartYAxisLabel
} from '../../../../../src/ts/components/data-acquisition-ui/components/historical-trends/utils';
import { testStationSoh } from '../../../../__data__/data-acquisition-ui/soh-overview-data';

// set up window alert and open so we don't see errors
(window as any).alert = jest.fn();
(window as any).open = jest.fn();

describe('Historical Trends Utils', () => {
  it('has functions exported', () => {
    expect(getName).toBeDefined();
    expect(getLabel).toBeDefined();
    expect(getDataType).toBeDefined();
    expect(chartTickFormatForThousands).toBeDefined();
    expect(barChartYAxisLabel).toBeDefined();
    expect(barChartXAxisLabel).toBeDefined();
    expect(barChartYAxisTicFormat).toBeDefined();
    expect(barChartXAxisTicFormat).toBeDefined();
    expect(lineChartYAxisLabel).toBeDefined();
    expect(lineChartXAxisLabel).toBeDefined();
    expect(getChartHeight).toBeDefined();
    expect(getChartData).toBeDefined();
  });

  it('getName returns monitor type name', () => {
    expect(getName(SohTypes.SohMonitorType.MISSING)).toEqual('Missing');
  });

  it('chartTickFormatForThousands to return formatted labels for thousands', () => {
    expect(chartTickFormatForThousands(100)).toEqual(100);
    expect(chartTickFormatForThousands(1000)).toEqual('1k');
  });

  it('barChartYAxisLabel returns label for Average correctly', () => {
    expect(barChartYAxisLabel(SohTypes.SohMonitorType.LAG)).toEqual('Average Lag (s)');
    expect(barChartYAxisLabel(SohTypes.SohMonitorType.MISSING)).toEqual('Average Missing (%)');
  });

  it('lineChartYAxisLabel to get proper labels', () => {
    expect(lineChartYAxisLabel(SohTypes.SohMonitorType.TIMELINESS)).toEqual('Timeliness (s)');
    expect(lineChartYAxisLabel(SohTypes.SohMonitorType.MISSING)).toEqual('Missing (%)');
  });

  it('should get chart data', () => {
    expect(
      getChartData(
        SohTypes.SohMonitorType.LAG,
        testStationSoh,
        {
          stationName: 'testStation',
          calculationTimes: [],
          monitorValues: [],
          minAndMax: undefined,
          percentageSent: undefined
        },
        new DistinctColorPalette(['chan 1', 'chan 2', 'chan 3'])
      )
    ).toEqual({
      barDefs: [],
      categories: { x: [], y: [] },
      lineDefs: [],
      minAndMax: undefined,
      thresholdsBad: [10],
      thresholdsMarginal: [1]
    });
  });
});
