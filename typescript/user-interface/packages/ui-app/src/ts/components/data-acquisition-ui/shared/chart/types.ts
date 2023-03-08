import type { MinAndMax } from '@gms/common-util';
import type { ChartTypes } from '@gms/ui-core-components';

export interface BarLineChartData {
  categories: {
    x: string[];
    y: string[];
  };
  lineDefs: ChartTypes.WeavessLineDefinition[];
  barDefs?: ChartTypes.BarDefinition[];
  thresholdsMarginal: number[];
  thresholdsBad: number[];
  minAndMax?: MinAndMax;
}
