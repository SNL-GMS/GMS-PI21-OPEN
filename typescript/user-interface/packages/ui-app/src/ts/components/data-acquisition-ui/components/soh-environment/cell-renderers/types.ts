import type { EnvironmentalSoh } from '../types';

export interface QuietIndicatorWrapperProps {
  data: EnvironmentalSoh;
  diameterPx: number;
  className?: string;
}

export interface EnvironmentCellValueProps {
  hasUnacknowledgedChanges: boolean;
  value: number | string;
}
