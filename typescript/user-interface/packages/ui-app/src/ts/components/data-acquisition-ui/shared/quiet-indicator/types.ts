import type { PieChartProps } from '@gms/ui-core-components';

export interface QuietIndicatorProps extends PieChartProps {
  status: string;
}

export interface QuietIndicatorWithTooltipProps extends Omit<QuietIndicatorProps, 'percent'> {
  quietTimingInfo: QuietTimingInfo;
}

export interface QuietTimingInfo {
  quietDurationMs: number;
  quietUntilMs: number;
}
