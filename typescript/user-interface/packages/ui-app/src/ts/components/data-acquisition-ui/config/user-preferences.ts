import type { HorizontalDividerTypes } from '@gms/ui-core-components';

import { semanticColors } from '~scss-config/color-preferences';
import { gmsLayout } from '~scss-config/layout-preferences';

export interface DataAcquisitionUserPreferences {
  readonly colors: {
    readonly ok: string;
    readonly warning: string;
    readonly strongWarning: string;
    readonly none: string;
  };
  readonly overviewMinContainerRange: HorizontalDividerTypes.HorizontalDividerSizeRange;
  readonly stationStatisticsMinContainerRange: HorizontalDividerTypes.HorizontalDividerSizeRange;
  readonly sohStatusUpdateTime: number;
  readonly transferredFilesGapsUpdateTime: number;
  readonly tableRowHeightPx: number;
  readonly defaultOverviewGroupHeight: number;
  readonly minChartHeightPx: number;
  readonly minChartWidthPx: number;
}

export const dataAcquisitionUserPreferences: DataAcquisitionUserPreferences = {
  colors: {
    ok: semanticColors.dataAcqOk,
    warning: semanticColors.dataAcqWarning,
    strongWarning: semanticColors.dataAcqStrongWarning,
    none: semanticColors.dataAcqNone
  },
  overviewMinContainerRange: {
    minimumTopHeightPx: 145,
    minimumBottomHeightPx: 110
  },
  stationStatisticsMinContainerRange: {
    minimumTopHeightPx: 145,
    minimumBottomHeightPx: 110
  },
  sohStatusUpdateTime: 60000,
  transferredFilesGapsUpdateTime: 600000,
  tableRowHeightPx: 36,
  defaultOverviewGroupHeight: 360,
  minChartWidthPx: gmsLayout.minChartWidthPx,
  minChartHeightPx: gmsLayout.minChartHeightPx
};
