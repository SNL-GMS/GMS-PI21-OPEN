import type { SohTypes } from '@gms/common-model';
import React from 'react';

import type { CellData } from '~components/data-acquisition-ui/shared/table/types';

/**
 * Station Statistics table row data
 */
export interface StationStatisticsRow {
  id: string;
  location: string;
  stationData: {
    stationName: string;
    stationStatus: SohTypes.SohStatusSummary;
    stationCapabilityStatus: SohTypes.SohStatusSummary;
  };
  stationGroups: SohTypes.StationSohCapabilityStatus[];
  channelEnvironment: CellData;
  channelLag: CellData;
  channelMissing: CellData;
  channelTimeliness: CellData;
  stationEnvironment: number;
  stationLag: number;
  stationMissing: number;
  stationTimeliness: number;
  needsAcknowledgement: boolean;
  needsAttention: boolean;
}

/**
 * Station Statistics table data context
 */
export const StationStatisticsTableDataContext: React.Context<{
  data: StationStatisticsRow[];
}> = React.createContext<{
  data: StationStatisticsRow[];
}>(undefined);
