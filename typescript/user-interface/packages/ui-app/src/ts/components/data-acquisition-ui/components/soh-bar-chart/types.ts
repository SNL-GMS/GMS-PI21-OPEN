import type { SohTypes } from '@gms/common-model';
import type { ValueType } from '@gms/common-util';
import type GoldenLayout from '@gms/golden-layout';
import type { SohConfigurationQueryProps, SohStatus } from '@gms/ui-state';

import type { Type } from './bar-chart/types';

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

/**
 * SohBarChartProps props
 */
export type SohBarChartProps = {
  glContainer?: GoldenLayout.Container;
  type: Type;
  selectedStationIds: string[];
  sohStatus: SohStatus;
  valueType: ValueType;
  setSelectedStationIds(ids: string[]): void;
} & SohConfigurationQueryProps;

/**
 * SohBarChartProps props
 */
// eslint-disable-next-line @typescript-eslint/no-empty-interface
export interface SohBarChartState {}
