import { useState } from 'react';
import { useOutletContext } from 'react-router-dom';
import { ConfigurationOption } from '../coi-types';
import { MonitorType } from '../coi-types/monitor-types';
import { ProcessingStationGroups } from '../coi-types/processing-types';
import type { StationsConfig } from '../routes/Station';

export interface LoadingState {
  numRequested: number;
  numComplete: number;
}

export interface AppData {
  targetDir: string;
  processingStationGroupFilePath: string;
  serviceURL: string;
  'soh-control.soh-monitor-timewindows': ConfigurationOption[];
  'soh-control.soh-monitor-thresholds': ConfigurationOption[];
  'soh-control.soh-monitor-types-for-rollup-station': ConfigurationOption<
    string[]
  >[];
  'soh-control.soh-monitor-types-for-rollup-channel': ConfigurationOption[];
  'soh-control.channels-by-monitor-type': ConfigurationOption[];
  'dataframe-receiver.channel-lookup': ConfigurationOption[];
  'soh-control.station-group-names': ConfigurationOption[];
  'soh-control.station-capability-rollup': ConfigurationOption[];
  'soh-control.channel-capability-rollup': ConfigurationOption[];
  processingStationGroups: ProcessingStationGroups;
  stationGroups: string[];
  supportedMonitorTypes: MonitorType[];
  stationConfigSettings: StationsConfig;
}
export interface AppState {
  data: AppData;
  setData: React.Dispatch<React.SetStateAction<AppData>>;
  loadingState: LoadingState;
}

/**
 * Must be wrapped in the App component's Outlet in order to provide access to the context.
 * @returns the {@link AppState}
 */
export const useAppContext = () => {
  return useOutletContext<AppState>();
};

/**
 * TODO getting called more than once, figure out why
 * Call this once to create the app state. Subsequently, call {@link useAppContext} from
 * within the router in order to get the state for the app.
 */
export const useAppState = () => {
  const [data, setData] = useState<any>({}); // todo use redux to store this
  return {
    data,
    setData,
  };
};
