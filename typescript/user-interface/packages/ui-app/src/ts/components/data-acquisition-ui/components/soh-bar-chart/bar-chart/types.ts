import type { SohTypes } from '@gms/common-model';

export type Type =
  | SohTypes.SohMonitorType.TIMELINESS
  | SohTypes.SohMonitorType.MISSING
  | SohTypes.SohMonitorType.LAG;

export interface ChannelSohForMonitorType {
  value: number;
  status: SohTypes.SohStatusSummary;
  quietExpiresAt: number;
  quietDurationMs?: number;
  name: string;
  thresholdBad: number;
  thresholdMarginal: number;
  hasUnacknowledgedChanges: boolean;
  isNullData?: boolean;
}
