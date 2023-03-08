import { SohTypes } from '@gms/common-model';
import { ValueType } from '@gms/common-util';

import { messageConfig } from '~components/data-acquisition-ui/config/message-config';

import { buildHistoricalTrendsComponent } from '../historical-trends/historical-trends-component';

/**
 * Create a lag missing component using the shared history component
 */
export const LagHistoryComponent = buildHistoricalTrendsComponent(
  SohTypes.SohMonitorType.LAG,
  ValueType.INTEGER,
  messageConfig.labels.lagTrendsSubtitle
);
