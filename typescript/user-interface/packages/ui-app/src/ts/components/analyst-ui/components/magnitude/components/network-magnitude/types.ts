import type { LegacyEventTypes } from '@gms/common-model';
import type { Row } from '@gms/ui-core-components';
import type { AnalystWorkspaceTypes } from '@gms/ui-state';

/**
 * NetworkMagnitude State
 */
// eslint-disable-next-line @typescript-eslint/no-empty-interface
export interface NetworkMagnitudeState {}

/**
 * options that can be passed to ag grid
 */

export interface Options {
  alignedGrids: any[];
  rowClass: string;
}

/**
 * NetworkMagnitude Props
 */
export interface NetworkMagnitudeProps {
  options?: Options;
  locationSolutionSet: LegacyEventTypes.LocationSolutionSet;
  preferredSolutionId: string;
  selectedSolutionId: string;
  computeNetworkMagnitudeSolution: (args: any) => Promise<void>;
  displayedMagnitudeTypes: AnalystWorkspaceTypes.DisplayedMagnitudeTypes;

  setSelectedLocationSolution(locationSolutionSetId: string, locationSolutionId: string): void;
}

/**
 * Data mapped to on a per-magnitude basis
 */
export interface NetworkMagnitudeData {
  magnitude: number;
  stdDeviation: number;
  numberOfDefiningStations: number;
  numberOfNonDefiningStations: number;
}

/**
 * Table row object for Network Magnitude
 */
export interface NetworkMagnitudeRow extends Row {
  id: string;
  isPreferred: boolean;
  location: string;
  dataForMagnitude: Map<LegacyEventTypes.MagnitudeType, NetworkMagnitudeData>;
}
