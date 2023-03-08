import type GoldenLayout from '@gms/golden-layout';
import type { SohStatus } from '@gms/ui-state';

/**
 * SOH lag redux props
 */
interface SohMapReduxProps {
  glContainer?: GoldenLayout.Container;
  selectedStationIds: string[];
  sohStatus: SohStatus;
  setSelectedStationIds(ids: string[]): void;
}

/**
 * SohMap props
 */
export type SohMapProps = SohMapReduxProps;

/**
 * SOH map panel props
 */
export interface SohMapPanelProps {
  minHeightPx: number;
  selectedStationIds: string[];
  sohStatus: SohStatus;
  setSelectedStationIds(ids: string[]): void;
}
