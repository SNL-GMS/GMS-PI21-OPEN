import type { SohTypes } from '@gms/common-model';

/**
 * The data needed to render the cell, passed into the cell render framework
 */
export interface CellData {
  value: number;
  status: SohTypes.SohStatusSummary;
  isContributing: boolean;
}
