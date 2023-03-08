import React from 'react';

/**
 * The type for the data used by the context
 */
export interface StationStatisticsContextData {
  updateIntervalSecs: number;
  quietTimerMs: number;
  sohStationStaleTimeMS: number;
  selectedStationIds: string[];
  setSelectedStationIds(ids: string[]): void;
  acknowledgeSohStatus(stationIds: string[], comment?: string): void;
}

/**
 * Instantiate the Context and set up the defaults
 */
export const StationStatisticsContext: React.Context<StationStatisticsContextData> = React.createContext<
  StationStatisticsContextData
>(undefined);
