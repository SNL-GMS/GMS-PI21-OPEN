import {
  channelCapabilityRollupConfigName,
  monitorTypesForRollupChannelConfigName,
  stationCapabilityRollupConfigName,
  stationGroupNamesConfigName,
} from '../coi-types/configuration-types';
import { usePutConfig } from '../state/api-slice';
import { AppData } from '../state/state';

export const useSyncWithFrameworksConfig = (data: AppData) => {
  const timewindowsConfigData = {
    name: 'soh-control.soh-monitor-timewindows',
    configurationOptions: data['soh-control.soh-monitor-timewindows'],
    changeTime: new Date().toISOString(),
  };
  const monitorTypesForRollupStationConfigData = {
    name: 'soh-control.soh-monitor-types-for-rollup-station',
    configurationOptions:
      data['soh-control.soh-monitor-types-for-rollup-station'],
    changeTime: new Date().toISOString(),
  };
  const sohMonitorThresholds = {
    name: 'soh-control.soh-monitor-thresholds',
    configurationOptions: data['soh-control.soh-monitor-thresholds'],
    changeTime: new Date().toISOString(),
  };
  const channelsByMonitorType = {
    name: 'soh-control.channels-by-monitor-type',
    configurationOptions: data['soh-control.channels-by-monitor-type'],
    changeTime: new Date().toISOString(),
  };

  const stationGroupNames = {
    name: stationGroupNamesConfigName,
    configurationOptions: data[stationGroupNamesConfigName],
    changeTime: new Date().toISOString(),
  };

  const stationCapabilityRollup = {
    name: stationCapabilityRollupConfigName,
    configurationOptions: data[stationCapabilityRollupConfigName],
    changeTime: new Date().toISOString(),
  };

  const channelCapabilityRollup = {
    name: channelCapabilityRollupConfigName,
    configurationOptions: data[channelCapabilityRollupConfigName],
    changeTime: new Date().toISOString(),
  };

  const monitorTypesForRollupChannel = {
    name: monitorTypesForRollupChannelConfigName,
    configurationOptions: data[monitorTypesForRollupChannelConfigName],
    changeTime: new Date().toISOString(),
  };

  usePutConfig(timewindowsConfigData, {
    skip:
      !Array.isArray(data['soh-control.soh-monitor-timewindows']) ||
      data['soh-control.soh-monitor-timewindows'].length === 0,
  });
  usePutConfig(monitorTypesForRollupStationConfigData, {
    skip:
      !Array.isArray(
        data['soh-control.soh-monitor-types-for-rollup-station']
      ) ||
      data['soh-control.soh-monitor-types-for-rollup-station'].length === 0,
  });
  usePutConfig(sohMonitorThresholds, {
    skip:
      !Array.isArray(data['soh-control.soh-monitor-thresholds']) ||
      data['soh-control.soh-monitor-thresholds'].length === 0,
  });
  usePutConfig(channelsByMonitorType, {
    skip:
      !Array.isArray(data['soh-control.channels-by-monitor-type']) ||
      data['soh-control.channels-by-monitor-type'].length === 0,
  });
  usePutConfig(stationGroupNames, {
    skip:
      !Array.isArray(data[stationGroupNamesConfigName]) ||
      data[stationGroupNamesConfigName].length === 0,
  });
  usePutConfig(stationCapabilityRollup, {
    skip:
      !Array.isArray(data[stationCapabilityRollupConfigName]) ||
      data[stationCapabilityRollupConfigName].length === 0,
  });
  usePutConfig(channelCapabilityRollup, {
    skip:
      !Array.isArray(data[channelCapabilityRollupConfigName]) ||
      data[channelCapabilityRollupConfigName].length === 0,
  });
  usePutConfig(monitorTypesForRollupChannel, {
    skip:
      !Array.isArray(data[monitorTypesForRollupChannelConfigName]) ||
      data[monitorTypesForRollupChannelConfigName].length === 0,
  });
};
