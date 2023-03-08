import * as React from 'react';
import {
  channelCapabilityRollupConfigName,
  monitorTypesForRollupChannelConfigName,
  stationCapabilityRollupConfigName,
  stationGroupNamesConfigName,
} from '../coi-types/configuration-types';
import type { LoadingState } from '../state/state';
import { useConfigFromDirectory } from '../util/custom-hooks';

export const useSohControlConfig = (
  setData: React.Dispatch<any>,
  setLoadingState: React.Dispatch<React.SetStateAction<LoadingState>>
) => {
  useConfigFromDirectory(
    'soh-control.soh-monitor-timewindows',
    setData,
    setLoadingState
  );
  useConfigFromDirectory(
    'soh-control.soh-monitor-thresholds',
    setData,
    setLoadingState
  );
  useConfigFromDirectory(
    'soh-control.soh-monitor-types-for-rollup-station',
    setData,
    setLoadingState
  );
  useConfigFromDirectory(
    monitorTypesForRollupChannelConfigName,
    setData,
    setLoadingState
  );
  useConfigFromDirectory(
    'soh-control.channels-by-monitor-type',
    setData,
    setLoadingState
  );
  useConfigFromDirectory(stationGroupNamesConfigName, setData, setLoadingState);
  useConfigFromDirectory(
    stationCapabilityRollupConfigName,
    setData,
    setLoadingState
  );
  useConfigFromDirectory(
    channelCapabilityRollupConfigName,
    setData,
    setLoadingState
  );
};
