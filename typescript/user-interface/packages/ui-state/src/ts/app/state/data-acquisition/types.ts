import type { Displays, ReferenceStationTypes } from '@gms/common-model';
import { SohTypes } from '@gms/common-model';
import { createEnumTypeGuard } from '@gms/common-util';
import Immutable from 'immutable';

export enum DataAcquisitionKeyAction {}

// Placeholder for future SOH key actions
export const DataAcquisitionKeyActions: Immutable.Map<
  string,
  DataAcquisitionKeyAction
> = Immutable.Map([]);

export enum FilterableSOHTypes {
  GOOD = SohTypes.SohStatusSummary.GOOD,
  MARGINAL = SohTypes.SohStatusSummary.MARGINAL,
  BAD = SohTypes.SohStatusSummary.BAD,
  NONE = SohTypes.SohStatusSummary.NONE
}

export enum FilterableSOHTypesDrillDown {
  GOOD = SohTypes.SohStatusSummary.GOOD,
  MARGINAL = SohTypes.SohStatusSummary.MARGINAL,
  BAD = SohTypes.SohStatusSummary.BAD
}

/** The initial statuses to show */
export const initialFiltersToDisplay: Record<FilterableSOHTypes, boolean> = {
  [FilterableSOHTypes.GOOD]: true,
  [FilterableSOHTypes.BAD]: true,
  [FilterableSOHTypes.MARGINAL]: true,
  [FilterableSOHTypes.NONE]: true
};

/** The initial statuses to show for drill down toolbar */
export const initialFiltersToDisplayDrillDown: Record<FilterableSOHTypes, boolean> = {
  [FilterableSOHTypes.GOOD]: true,
  [FilterableSOHTypes.BAD]: true,
  [FilterableSOHTypes.MARGINAL]: true
};

/**
 * The SOH Status
 */
export interface SohStatus {
  /** timestamp of when the data was last updated */
  lastUpdated: number;
  /* true if the station soh is stale; false otherwise */
  isStale: boolean;
  /** true if the initial query is still loading (has not completed) */
  loading: boolean;
  /** the station and station group SOH data */
  stationAndStationGroupSoh: SohTypes.StationAndStationGroupSoh;
}

export type FilterLists =
  | Displays.SohDisplays.STATION_STATISTICS
  | Displays.SohDisplays.SOH_OVERVIEW
  | `soh-environment-channel-statuses`
  | `soh-environment-monitor-statuses`
  | Displays.SohDisplays.SOH_LAG
  | Displays.SohDisplays.SOH_MISSING
  | Displays.SohDisplays.SOH_TIMELINESS
  | 'soh-overview-groups';

export interface DataAcquisitionState {
  selectedAceiType: SohTypes.AceiType;
  selectedProcessingStation: ReferenceStationTypes.ReferenceStation;
  unmodifiedProcessingStation: ReferenceStationTypes.ReferenceStation;
  stationStatisticsGroup: string;
  data: {
    sohStatus: SohStatus;
  };
  filtersToDisplay: Partial<
    Record<FilterLists, Record<FilterableSOHTypes | FilterableSOHTypesDrillDown | string, boolean>>
  >;
}

export interface SetSohFilterAction {
  list: FilterLists;
  filters: Record<FilterableSOHTypes | FilterableSOHTypesDrillDown, boolean>;
}

export const isDataAcquisitionKeyAction = createEnumTypeGuard(DataAcquisitionKeyAction);
