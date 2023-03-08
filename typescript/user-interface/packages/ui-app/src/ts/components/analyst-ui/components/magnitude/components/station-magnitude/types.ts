import type { LegacyEventTypes } from '@gms/common-model';
import type { AnalystWorkspaceTypes } from '@gms/ui-state';
import type Immutable from 'immutable';

import type { AmplitudesByStation } from '../../types';

/**
 * StationMagnitude State
 */
export interface StationMagnitudeState {
  computeNetworkMagnitudeSolutionStatus: Immutable.Map<
    string,
    [{ stationName: string; rational: string }]
  >;
}

export interface MagnitudeAndSdData {
  magSolution: LegacyEventTypes.NetworkMagnitudeSolution;
  sdData: StationMagnitudeSdData;
}

/**
 * options that can be passed to ag grid
 */
export interface Options {
  alignedGrids: any[];
}

/**
 * StationMagnitude Props
 */
export interface StationMagnitudeProps {
  options?: Options;
  amplitudesByStation: AmplitudesByStation[];
  historicalMode: boolean;
  selectedSdIds: string[];
  locationSolution: LegacyEventTypes.LocationSolution;
  displayedMagnitudeTypes: AnalystWorkspaceTypes.DisplayedMagnitudeTypes;
  computeNetworkMagnitudeSolution: (args: any) => Promise<void>;
  openEventId: string;

  setSelectedSdIds(ids: string[]): void;
  checkBoxCallback(
    magnitudeType: LegacyEventTypes.MagnitudeType,
    stationNames: string[],
    defining: boolean
  ): Promise<[{ stationName: string; rational: string }]>;
}

export interface StationMagnitudeSdData {
  channel: string;
  phase: string;
  amplitudeValue: number;
  amplitudePeriod: number;
  signalDetectionId: string;
  time: number;
  stationName: string;
  flagForReview: boolean;
}

export interface MagnitudeDataForRow {
  channel: string;
  signalDetectionId: string;
  phase: string;
  amplitudeValue: number;
  amplitudePeriod: number;
  flagForReview: boolean;
  defining: boolean;
  mag: number;
  res: number;
  hasMagnitudeCalculationError: boolean;
  computeNetworkMagnitudeSolutionStatus: string;
}

/**
 * Table row object for station magnitude
 */
export interface StationMagnitudeRow {
  id: string;
  dataForMagnitude: Map<LegacyEventTypes.MagnitudeType, MagnitudeDataForRow>;
  station: string;
  dist: number;
  azimuth: number;
  selectedSdIds: string[];
  historicalMode: boolean;
  azimuthTooltip: string;
  checkBoxCallback(
    magnitudeType: LegacyEventTypes.MagnitudeType,
    stationNames: string[],
    defining: boolean
  ): Promise<void>;
}
export interface StationMagAndSignalDetection {
  stationName: string;
  magnitudeAndSdData: Map<LegacyEventTypes.MagnitudeType, MagnitudeAndSdData>;
}
