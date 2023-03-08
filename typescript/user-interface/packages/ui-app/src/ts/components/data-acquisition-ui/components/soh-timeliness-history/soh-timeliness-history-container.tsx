import { SohTypes } from '@gms/common-model';
import { ValueType } from '@gms/common-util';

import { messageConfig } from '~components/data-acquisition-ui/config/message-config';

import { buildHistoricalTrendsComponent } from '../historical-trends/historical-trends-component';

/**
 * Create a timeliness history component using the shared history component
 */
export const TimelinessHistoryComponent = buildHistoricalTrendsComponent(
  SohTypes.SohMonitorType.TIMELINESS,
  ValueType.INTEGER,
  messageConfig.labels.timelinessTrendsSubtitle
);
