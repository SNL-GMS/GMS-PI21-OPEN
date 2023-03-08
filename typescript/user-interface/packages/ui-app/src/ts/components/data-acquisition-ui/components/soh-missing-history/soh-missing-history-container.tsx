import { SohTypes } from '@gms/common-model';
import { ValueType } from '@gms/common-util';

import { messageConfig } from '~components/data-acquisition-ui/config/message-config';

import { buildHistoricalTrendsComponent } from '../historical-trends/historical-trends-component';

/**
 * Create a missing missing component using the shared history component
 */
export const MissingHistoryComponent = buildHistoricalTrendsComponent(
  SohTypes.SohMonitorType.MISSING,
  ValueType.PERCENTAGE,
  messageConfig.labels.missingTrendsSubtitle
);
