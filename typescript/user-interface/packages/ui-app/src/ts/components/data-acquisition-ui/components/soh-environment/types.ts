import type { SohTypes } from '@gms/common-model';
import React from 'react';

import type { QuietTimingInfo } from '~components/data-acquisition-ui/shared/quiet-indicator';
import type { DataReceivedStatus } from '~components/data-acquisition-ui/shared/table/utils';

export interface EnvironmentalSoh {
  value: number;
  status: SohTypes.SohStatusSummary;
  monitorTypes: SohTypes.SohMonitorType;
  channelName: string;
  quietTimingInfo: QuietTimingInfo;
  hasUnacknowledgedChanges: boolean;
  isSelected: boolean;
  isContributing: boolean;
}

export interface EnvironmentTableRow {
  id: string;
  monitorType: SohTypes.SohMonitorType;
  monitorIsSelected: boolean;
  monitorStatus: SohTypes.SohStatusSummary;
  valueAndStatusByChannelName: Map<string, EnvironmentalSoh>;
}

export interface EnvironmentTableContext {
  selectedChannelMonitorPairs: SohTypes.ChannelMonitorPair[];
  rollupStatusByChannelName: Map<string, SohTypes.SohStatusSummary>;
  dataReceivedByChannelName: Map<string, DataReceivedStatus>;
}

export const EnvironmentTableDataContext: React.Context<{
  data: EnvironmentTableRow[];
}> = React.createContext<{
  data: EnvironmentTableRow[];
}>(undefined);
