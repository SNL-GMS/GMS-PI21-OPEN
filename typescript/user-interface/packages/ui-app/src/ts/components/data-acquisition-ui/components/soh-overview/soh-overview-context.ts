import type { SohTypes } from '@gms/common-model';
import type GoldenLayout from '@gms/golden-layout';
import React from 'react';

/**
 * The type for the data used by the context
 */
export interface SohOverviewContextData {
  glContainer: GoldenLayout.Container;
  stationGroupSoh: SohTypes.StationGroupSohStatus[];
  stationSoh: SohTypes.UiStationSoh[];
  selectedStationIds: string[];
  sohStationStaleTimeMS: number;
  updateIntervalSecs: number;
  quietTimerMs: number;
  setSelectedStationIds(ids: string[]): void;
  acknowledgeSohStatus(stationIds: string[], comment?: string): void;
}

/**
 * Instantiate the Context and set up the defaults
 */
export const SohOverviewContext: React.Context<SohOverviewContextData> = React.createContext<
  SohOverviewContextData
>(undefined);
