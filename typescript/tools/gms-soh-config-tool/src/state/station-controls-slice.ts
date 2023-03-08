import { createSlice } from '@reduxjs/toolkit';
import { DataNames } from '../coi-types/data-names';
import { MonitorTypeConfig } from '../coi-types/monitor-types';
import { ThresholdParams } from '../output/build-configuration-option';
import { StationsConfig } from '../routes/Station';

export type StationGroup = { name: string; included: boolean };
export type UserInputError = { hasError: boolean; reason: string };
export type ErrorRecord = {
  id: string;
  hasError: boolean;
  reason: string;
  type: string;
};

export enum RollupType {
  ROLLUP_OF_ROLLUPS = 'ROLLUP_OF_ROLLUPS',
  ROLLUP_OF_CHANNELS = 'ROLLUP_OF_CHANNELS',
  ROLLUP_OF_MONITORS = 'ROLLUP_OF_MONITORS',
}

export enum OperatorType {
  BEST_OF = 'BEST_OF',
  WORST_OF = 'WORST_OF',
  MIN_GOOD_OF = 'MIN_GOOD_OF',
}
export interface RollupEntry {
  id: string;
  rollupType: RollupType;
  operatorType: OperatorType;
  rollups?: RollupEntry[];
  threshold?: ThresholdParams;
  channels?: string[];
  monitors?: string[];
}

const setInitialLoadingStateToFalse = (): Record<string, boolean> => {
  const initialLoadingState: Record<string, boolean> = {};
  initialLoadingState[DataNames.STATION_CAPABILITY_ROLLUP] = false;
  initialLoadingState[DataNames.MONITOR_THRESHOLDS] = false;
  initialLoadingState[DataNames.CHANNEL_CAPABILITY_ROLLUP] = false;
  initialLoadingState[DataNames.STATION_GROUPS] = false;

  return initialLoadingState;
};
export interface StationControlsState {
  /** The station currently being edited */
  stationName: string | null;

  /** Record mapping station names to a list of included channel names */
  selectedChannels: Record<string, string[]>;

  /** Record mapping station config settings */
  stationsConfig: StationsConfig;

  /** Record mapping station names to a list of monitor types for roll up */
  monitorTypesForRollup: Record<string, MonitorTypeConfig[]>;

  /** Record mapping stations to its calculation interval */
  calculationInterval: Record<string, string>;

  /** Record mapping stations to its back off duration  */
  backOffDuration: Record<string, string>;

  /** Record mapping stations to its station groups*/
  stationGroups: Record<string, StationGroup[]>;

  /** Determines if config has user inputs that puts tool in error state */
  error: Record<string, UserInputError>;

  /** Record mapping station capability rollup*/
  stationGroupCapabilityRollup: Record<string, Record<string, RollupEntry>>;

  /** Record of records for each station group and each of the channels for the station*/
  channelCapabilityRollup: Record<
    string,
    Record<string, Record<string, RollupEntry>>
  >;

  /** Keeps track of what query data has been loaded */
  loadedData: Record<string, boolean>;
}

export const stationControlsSlice = createSlice({
  name: 'stationControls',
  initialState: {
    stationName: null,
    selectedChannels: {},
    stationsConfig: {},
    monitorTypesForRollup: {},
    backOffDuration: {},
    calculationInterval: {},
    stationGroups: {},
    error: {},
    stationGroupCapabilityRollup: {},
    channelCapabilityRollup: {},
    loadedData: setInitialLoadingStateToFalse(),
  } as StationControlsState,
  reducers: {
    setStationName(state, action) {
      state.stationName = action.payload;
    },
    setChannelNames(state, action) {
      state.selectedChannels[action.payload.stationName] =
        action.payload.channelNames;
    },
    setStationControls(_state, action) {
      return action.payload;
    },
    setMonitorTypeForRollup(state, action) {
      state.monitorTypesForRollup[action.payload.stationName] =
        action.payload.monitorTypesForRollup;
    },
    setBackOffDuration(state, action) {
      state.backOffDuration[action.payload.stationName] =
        action.payload.backOffDuration;
    },
    setCalculationInterval(state, action) {
      state.calculationInterval[action.payload.stationName] =
        action.payload.calculationInterval;
    },
    setStationGroups(state, action) {
      state.stationGroups[action.payload.stationName] =
        action.payload.stationGroups;
    },
    setHasError(state, action) {
      state.error[action.payload.attributeName] = action.payload.errorInfo;
    },
    clearStationErrors(state) {
      state.error = {};
    },
    setStationGroupCapabilityRollup(state, action) {
      if (
        state.stationGroupCapabilityRollup[action.payload.stationName] ===
        undefined
      ) {
        state.stationGroupCapabilityRollup[action.payload.stationName] = {};
      }
      state.stationGroupCapabilityRollup[action.payload.stationName][
        action.payload.groupName
      ] = action.payload.rollup;
    },
    setChannelCapabilityRollup(state, action) {
      if (
        state.channelCapabilityRollup[action.payload.stationName] === undefined
      ) {
        state.channelCapabilityRollup[action.payload.stationName] = {};
      }
      if (
        state.channelCapabilityRollup[action.payload.stationName][
          action.payload.groupName
        ] === undefined
      ) {
        state.channelCapabilityRollup[action.payload.stationName][
          action.payload.groupName
        ] = {};
      }
      state.channelCapabilityRollup[action.payload.stationName][
        action.payload.groupName
      ][action.payload.channelName] = action.payload.rollup;
    },
    setLoadedData(state, action) {
      state.loadedData[action.payload.dataName] = action.payload.hasLoaded;
    },
    resetLoadedData(state) {
      state.loadedData = setInitialLoadingStateToFalse();
    },
  },
});

export const {
  setStationName,
  setChannelNames,
  setStationControls,
  setMonitorTypeForRollup,
  setBackOffDuration,
  setCalculationInterval,
  setStationGroups,
  setHasError,
  clearStationErrors,
  setStationGroupCapabilityRollup,
  setChannelCapabilityRollup,
  setLoadedData,
  resetLoadedData,
} = stationControlsSlice.actions;
