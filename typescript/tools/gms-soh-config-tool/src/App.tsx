import { Settings } from '@mui/icons-material';
import { Alert } from '@mui/material';
import { CreateCSSProperties, makeStyles, PropsFunc } from '@mui/styles';
import produce, { Draft } from 'immer';
import isEqual from 'lodash/isEqual';
import * as React from 'react';
import { NavLink, Outlet } from 'react-router-dom';
import './App.css';
import {
  channelCapabilityRollupConfigName,
  ConfigurationOption,
  stationCapabilityRollupConfigName,
} from './coi-types';
import { Nav } from './components/Nav';
import { windowAPI } from './electron-util';
import { useDataframeReceiver } from './init/use-dataframe-receiver';
import { usePersistentSettings } from './init/use-persistent-settings';
import { useSohControlConfig } from './init/use-soh-control-config';
import {
  useProcessingStationGroups,
  useStationGroups,
} from './init/use-station-groups';
import { useSupportedMonitorTypes } from './init/use-supported-monitor-types';
import {
  buildChannelCapabilityRollupConfig,
  buildChannelsPerMonitorForStationConfig,
  buildMonitorThresholdsConfig,
  buildMonitorTypesForRollupChannel,
  buildMonitorTypesForRollupStations,
  buildProcessingStationGroupDefinition,
  buildStationCapabilityRollupConfig,
  buildTimeWindowsConfig,
  ThresholdParams,
  TimeWindowParams,
} from './output/build-configuration-option';
import { useSelectedChannels } from './renderers/channel-checklist/ChannelChecklist';
import { StationConfig } from './routes/Station';
import { useSyncWithFrameworksConfig } from './services/use-sync-with-frameworks-config';
import {
  ChannelsPerMonitorForStationConfig,
  MonitorsForRollupStationConfig,
  TimeWindowsConfig,
  useRefreshUiConfig,
  useUpdateConfig,
} from './state/api-slice';
import { useAppSelector } from './state/react-redux-hooks';
import { ChannelCapabilityRollupQueryResults } from './state/retrieve-channel-capability-rollups';
import { StationGroupCapabilityQueryResults } from './state/retrieve-station-group-capability';
import { StationGroupsDefinition } from './state/retrieve-station-groups';
import { LoadingState, useAppContext, useAppState } from './state/state';
import { layoutStyles } from './styles/layout';
import {
  determineDefaultChannelCapabilityForStation,
  determineDefaultChannelsPerMonitorForStation,
  determineDefaultMonitorTypesForRollupStation,
  determineDefaultStationCapabilityForStation,
  determineDefaultThresholdsForStation,
  determineDefaultTimewindowsForStation,
} from './util/util';

const linkStyles:
  | React.CSSProperties
  | CreateCSSProperties<{}>
  | PropsFunc<{}, CreateCSSProperties<{}>> = {
  color: 'white',
  position: 'relative',
  marginRight: '1em',
  textDecoration: 'none',
  '&.active:hover': {
    cursor: 'default',
  },
  '&::after': {
    content: '""',
    position: 'absolute',
    height: '2px',
    bottom: '-4px',
    margin: '0 auto',
    left: '0',
    right: '0',
    width: '50%',
    background: 'transparent',
    transition: '.5s',
  },
  '&.active::after': {
    content: '""',
    position: 'absolute',
    height: '2px',
    bottom: '-4px',
    margin: '0 auto',
    left: '0',
    right: '0',
    width: '80%',
    background: 'white',
    transition: '.5s',
  },
  '&:hover::after': {
    width: '80%',
    background: 'white',
  },
};

const useStyles = makeStyles({
  ...layoutStyles,
  link: {
    ...linkStyles,
  },
  iconLink: {
    ...linkStyles,
    display: 'flex',
    alignItems: 'center',
  },
});

const buildUiSohSettingsConfiguration = (config: any) => {
  return {
    name: config.name,
    constraints: config.constraints,
    parameters: config.parameters,
  };
};

export const useExportConfiguration = () => {
  const { data } = useAppContext();
  const state = useAppSelector((s) => s.stationControls);
  const [updateConfig] = useUpdateConfig();
  const [refreshUiConfig] = useRefreshUiConfig();
  const { allChannels } = useSelectedChannels();
  const stationGroupNames = state.stationGroups[state.stationName ?? '']?.map(
    (group) => group.name
  );
  const allChannelNames = allChannels.map((channel) => channel.name);
  const supportedMonitorNames = data?.supportedMonitorTypes;

  const exportConfiguration = React.useCallback(async () => {
    const stationConfigToSave: StationConfig = {
      backOffDuration: state.backOffDuration[state.stationName ?? ''],
      calculationInterval: state.calculationInterval[state.stationName ?? ''],
      sohMonitorTypesForRollup:
        state.monitorTypesForRollup[state.stationName ?? ''],
    };

    const uiSohSettingsConfig = buildUiSohSettingsConfiguration(data);

    const monitorTypesForRollupStationFromDisk =
      (await windowAPI.electronAPI.loadConfigFromDir(
        'soh-control.soh-monitor-types-for-rollup-station'
      )) as MonitorsForRollupStationConfig[];

    const monitorTypesForRollupChannelFromDisk =
      (await windowAPI.electronAPI.loadConfigFromDir(
        'soh-control.soh-monitor-types-for-rollup-channel'
      )) as MonitorsForRollupStationConfig[];

    const defaultMonitorTypesForRollupStationConfig =
      monitorTypesForRollupStationFromDisk.find(
        (config) =>
          config.name === 'default-soh-monitor-types-for-rollup-station'
      );

    const defaultMonitorTypesForRollupStation =
      determineDefaultMonitorTypesForRollupStation(
        monitorTypesForRollupStationFromDisk,
        defaultMonitorTypesForRollupStationConfig,
        state.stationName
      );

    const monitorTypesForRollupStation = buildMonitorTypesForRollupStations(
      state.stationName,
      stationConfigToSave,
      defaultMonitorTypesForRollupStation
    );

    // monitor types for rollup station and channel default/overrides are identical thus using the defaults for
    // monitor types for rollup station, if they are not identical there is an issue else where we wont handle in the tool
    const monitorTypesForRollupChannel = buildMonitorTypesForRollupChannel(
      state.stationName,
      stationConfigToSave.sohMonitorTypesForRollup,
      defaultMonitorTypesForRollupStation,
      monitorTypesForRollupStation
        ? monitorTypesForRollupStation[state.stationName ?? '']
        : undefined
    );

    const timeWindowsFromDisk = (await windowAPI.electronAPI.loadConfigFromDir(
      'soh-control.soh-monitor-timewindows'
    )) as TimeWindowsConfig[];

    const defaultTimeWindowConfig = timeWindowsFromDisk.find(
      (config) => config.name === 'default-soh-monitor-timewindows'
    );

    const defaultTimeWindowsForStation: TimeWindowParams =
      determineDefaultTimewindowsForStation(
        timeWindowsFromDisk,
        defaultTimeWindowConfig,
        state.stationName
      );

    const stationTimewindowsConfig = buildTimeWindowsConfig(
      state.stationName,
      stationConfigToSave,
      {
        backOffDuration: defaultTimeWindowsForStation.backOffDuration,
        calculationInterval: defaultTimeWindowsForStation.calculationInterval,
      }
    );

    const stationMonitorThresholdsConfigsFromDisk =
      (await windowAPI.electronAPI.loadConfigFromDir(
        'soh-control.soh-monitor-thresholds'
      )) as ConfigurationOption<Partial<ThresholdParams>>[];

    const stationMonitorThresholdsConfig = buildMonitorThresholdsConfig(
      state.stationName,
      stationConfigToSave,
      determineDefaultThresholdsForStation(
        stationMonitorThresholdsConfigsFromDisk,
        state.stationName,
        data.supportedMonitorTypes ?? [],
        allChannelNames
      ),
      stationMonitorThresholdsConfigsFromDisk
    );

    const channelsPerMonitorForStationConfigsFromDisk =
      (await windowAPI.electronAPI.loadConfigFromDir(
        'soh-control.channels-by-monitor-type'
      )) as ChannelsPerMonitorForStationConfig[];

    const defaultChannelsPerMonitorForStationConfig =
      channelsPerMonitorForStationConfigsFromDisk.find(
        (config) => config.name === 'default-channels-by-monitor-type'
      );

    const defaultChannelsPerMonitorForStation =
      determineDefaultChannelsPerMonitorForStation(
        channelsPerMonitorForStationConfigsFromDisk,
        defaultChannelsPerMonitorForStationConfig,
        state.stationName
      );

    const channelsPerMonitorForStationConfig =
      buildChannelsPerMonitorForStationConfig(
        state.stationName,
        stationConfigToSave,
        defaultChannelsPerMonitorForStation
      );

    const stationCapabilitiesFromDisk =
      (await windowAPI.electronAPI.loadConfigFromDir(
        stationCapabilityRollupConfigName
      )) as ConfigurationOption<Partial<StationGroupCapabilityQueryResults>>[];

    const defaultStationCapabilityConfig = stationCapabilitiesFromDisk.find(
      (config) => config.name === 'default-station-capability-rollup'
    );

    const defaultStationCapabilitiesForStation =
      determineDefaultStationCapabilityForStation(
        stationCapabilitiesFromDisk,
        defaultStationCapabilityConfig,
        state.stationName,
        stationGroupNames
      );

    const stationCapabilityRollupConfig = buildStationCapabilityRollupConfig(
      state.stationName,
      allChannelNames,
      defaultStationCapabilitiesForStation,
      state.stationGroupCapabilityRollup[state.stationName ?? '']
    );

    const channelCapabilitiesFromDisk =
      (await windowAPI.electronAPI.loadConfigFromDir(
        channelCapabilityRollupConfigName
      )) as ConfigurationOption<Partial<ChannelCapabilityRollupQueryResults>>[];

    const defaultChannelCapabilityConfig = channelCapabilitiesFromDisk.find(
      (config) => config.name === 'default-channel-capability-rollup'
    );

    const defaultChannelCapabilitiesForStation =
      determineDefaultChannelCapabilityForStation(
        channelCapabilitiesFromDisk,
        defaultChannelCapabilityConfig,
        state.stationName,
        stationGroupNames,
        allChannelNames
      );

    const channelCapabilityRollupConfig = buildChannelCapabilityRollupConfig(
      state.stationName,
      supportedMonitorNames,
      defaultChannelCapabilitiesForStation,
      state.channelCapabilityRollup[state.stationName ?? '']
    );

    const processingStationGroupDefinitionsFromDisk =
      await windowAPI.electronAPI.loadConfigFromDir(
        '../station-reference/definitions'
      );

    const stationGroupDefinition = buildProcessingStationGroupDefinition(
      state.stationName,
      processingStationGroupDefinitionsFromDisk as StationGroupsDefinition[],
      state.stationGroups[state.stationName ?? '']
    );

    // This file is not put into the config service, changes only written to disk
    await windowAPI.electronAPI.saveDataAsFile(
      stationGroupDefinition,
      `../station-reference/definitions/processing-station-group-definition.json`
    );

    await windowAPI.electronAPI.saveDataAsFile(
      uiSohSettingsConfig,
      `ui.soh-settings/${uiSohSettingsConfig.constraints}`
    );

    if (stationCapabilityRollupConfig !== undefined) {
      if (stationCapabilityRollupConfig === null) {
        if (
          stationCapabilitiesFromDisk.find((stationCapabilityConfig) =>
            stationCapabilityConfig.name.includes(`${state.stationName}_`)
          )
        ) {
          await windowAPI.electronAPI.deleteFile(
            stationCapabilityRollupConfigName,
            `${state.stationName}.json`
          );
        }
      } else {
        Object.keys(stationCapabilityRollupConfig).forEach(async (sn) => {
          await windowAPI.electronAPI.saveDataAsFile(
            stationCapabilityRollupConfig[sn],
            `${stationCapabilityRollupConfigName}/${sn}.json`
          );
        });

        const stationCapabilityConfigsFromDisk =
          await windowAPI.electronAPI.loadConfigFromDir(
            stationCapabilityRollupConfigName
          );

        const stationCapabilityConfigData = {
          name: stationCapabilityRollupConfigName,
          configurationOptions: stationCapabilityConfigsFromDisk,
          changeTime: new Date().toISOString(),
        };

        await updateConfig(stationCapabilityConfigData);
      }
    }

    if (channelCapabilityRollupConfig !== undefined) {
      if (channelCapabilityRollupConfig === null) {
        if (
          channelCapabilitiesFromDisk.find((channelCapabilityConfig) =>
            channelCapabilityConfig.name.includes(`${state.stationName}_`)
          )
        ) {
          await windowAPI.electronAPI.deleteFile(
            channelCapabilityRollupConfigName,
            `${state.stationName}.json`
          );
        }
      } else {
        Object.keys(channelCapabilityRollupConfig).forEach(async (sn) => {
          await windowAPI.electronAPI.saveDataAsFile(
            channelCapabilityRollupConfig[sn],
            `${channelCapabilityRollupConfigName}/${sn}.json`
          );
        });

        const channelCapabilityConfigsFromDisk =
          await windowAPI.electronAPI.loadConfigFromDir(
            channelCapabilityRollupConfigName
          );

        const channelCapabilityConfigData = {
          name: channelCapabilityRollupConfigName,
          configurationOptions: channelCapabilityConfigsFromDisk,
          changeTime: new Date().toISOString(),
        };

        await updateConfig(channelCapabilityConfigData);
      }
    }

    if (stationTimewindowsConfig !== undefined) {
      if (stationTimewindowsConfig === null) {
        if (
          timeWindowsFromDisk.find(
            (timeWindowConfig) =>
              timeWindowConfig.name === `${state.stationName}_TIMEWINDOWS`
          )
        ) {
          await windowAPI.electronAPI.deleteFile(
            'soh-control.soh-monitor-timewindows',
            `${state.stationName}.json`
          );
        }
      } else {
        Object.keys(stationTimewindowsConfig).forEach(async (sn) => {
          await windowAPI.electronAPI.saveDataAsFile(
            stationTimewindowsConfig[sn],
            `soh-control.soh-monitor-timewindows/${sn}.json`
          );
        });

        const timeWindowConfigsFromDisk =
          await windowAPI.electronAPI.loadConfigFromDir(
            'soh-control.soh-monitor-timewindows'
          );

        const timewindowsConfigData = {
          name: 'soh-control.soh-monitor-timewindows',
          configurationOptions: timeWindowConfigsFromDisk,
          changeTime: new Date().toISOString(),
        };

        await updateConfig(timewindowsConfigData);
      }
    }
    if (monitorTypesForRollupStation !== undefined) {
      if (monitorTypesForRollupStation === null) {
        if (
          monitorTypesForRollupStationFromDisk.find(
            (config) => config.name === `${state.stationName}_MONITOR_TYPES`
          )
        ) {
          await windowAPI.electronAPI.deleteFile(
            'soh-control.soh-monitor-types-for-rollup-station',
            `${state.stationName}.json`
          );
        }
      } else {
        Object.keys(monitorTypesForRollupStation).forEach(async (sn) => {
          await windowAPI.electronAPI.saveDataAsFile(
            monitorTypesForRollupStation[sn],
            `soh-control.soh-monitor-types-for-rollup-station/${sn}.json`
          );
        });

        const monitorTypesForRollupFromDisk =
          await windowAPI.electronAPI.loadConfigFromDir(
            'soh-control.soh-monitor-types-for-rollup-station'
          );

        const monitorTypesForRollupConfigData = {
          name: 'soh-control.soh-monitor-types-for-rollup-station',
          configurationOptions: monitorTypesForRollupFromDisk,
          changeTime: new Date().toISOString(),
        };

        await updateConfig(monitorTypesForRollupConfigData);
      }
    }
    if (monitorTypesForRollupChannel !== undefined) {
      if (monitorTypesForRollupChannel === null) {
        if (
          monitorTypesForRollupChannelFromDisk.find(
            (config) =>
              config.name === `${state.stationName}_MONITOR_TYPES` ||
              config.name.includes(`${state.stationName}_MONITOR_TYPES_FOR`)
          )
        ) {
          await windowAPI.electronAPI.deleteFile(
            'soh-control.soh-monitor-types-for-rollup-channel',
            `${state.stationName}.json`
          );
        }
      } else {
        Object.keys(monitorTypesForRollupChannel).forEach(async (sn) => {
          await windowAPI.electronAPI.saveDataAsFile(
            monitorTypesForRollupChannel[sn],
            `soh-control.soh-monitor-types-for-rollup-channel/${sn}.json`
          );
        });

        const monitorTypesForRollupFromDisk =
          await windowAPI.electronAPI.loadConfigFromDir(
            'soh-control.soh-monitor-types-for-rollup-channel'
          );

        const monitorTypesForRollupChannelConfigData = {
          name: 'soh-control.soh-monitor-types-for-rollup-channel',
          configurationOptions: monitorTypesForRollupFromDisk,
          changeTime: new Date().toISOString(),
        };

        await updateConfig(monitorTypesForRollupChannelConfigData);
      }
    }
    if (stationMonitorThresholdsConfig !== undefined) {
      if (stationMonitorThresholdsConfig === null) {
        // !Not always querying channel data so wont know if channels have changed
        // !Thus if a data exists for channels will not delete file
        // !Know channel data exists in custom overrides by looking for '_MONITOR_FOR_CHANNEL_'
        if (
          stationMonitorThresholdsConfigsFromDisk.find((config) =>
            config.name.includes(`_MONITOR_THRESHOLDS_FOR_${state.stationName}`)
          ) &&
          !stationMonitorThresholdsConfigsFromDisk.find((config) =>
            config.name.includes('_MONITOR_FOR_CHANNEL_')
          )
        ) {
          await windowAPI.electronAPI.deleteFile(
            'soh-control.soh-monitor-thresholds',
            `${state.stationName}.json`
          );
          const sohMonitorThresholdsFromDisk =
            await windowAPI.electronAPI.loadConfigFromDir(
              'soh-control.soh-monitor-thresholds'
            );

          const sohMonitorThresholdsConfigData = {
            name: 'soh-control.soh-monitor-thresholds',
            configurationOptions: sohMonitorThresholdsFromDisk,
            changeTime: new Date().toISOString(),
          };
          // !Have to update config on delete since monitor thresholds effect
          // !The values of channel thresholds when changed, so if monitor thresholds
          // !are modified but channels are not expanded, on channel expand query fires
          // !so this update ensures channels have latest changes
          await updateConfig(sohMonitorThresholdsConfigData);
        }
      } else {
        Object.keys(stationMonitorThresholdsConfig).forEach(async (sn) => {
          await windowAPI.electronAPI.saveDataAsFile(
            stationMonitorThresholdsConfig[sn],
            `soh-control.soh-monitor-thresholds/${sn}.json`
          );
        });

        const sohMonitorThresholdsFromDisk =
          await windowAPI.electronAPI.loadConfigFromDir(
            'soh-control.soh-monitor-thresholds'
          );

        const sohMonitorThresholdsConfigData = {
          name: 'soh-control.soh-monitor-thresholds',
          configurationOptions: sohMonitorThresholdsFromDisk,
          changeTime: new Date().toISOString(),
        };

        await updateConfig(sohMonitorThresholdsConfigData);
      }
    }
    if (channelsPerMonitorForStationConfig !== undefined) {
      if (channelsPerMonitorForStationConfig === null) {
        if (
          channelsPerMonitorForStationConfigsFromDisk.find(
            (config) =>
              config.name === `${state.stationName}_MONITOR_CHANNELS_SELECTIONS`
          )
        ) {
          await windowAPI.electronAPI.deleteFile(
            'soh-control.channels-by-monitor-type',
            `${state.stationName}.json`
          );
        }
      } else {
        Object.keys(channelsPerMonitorForStationConfig).forEach(async (sn) => {
          await windowAPI.electronAPI.saveDataAsFile(
            channelsPerMonitorForStationConfig[sn],
            `soh-control.channels-by-monitor-type/${sn}.json`
          );
        });

        const channelsPerMonitorFromDisk =
          await windowAPI.electronAPI.loadConfigFromDir(
            'soh-control.channels-by-monitor-type'
          );

        const channelsPerMonitorConfigData = {
          name: 'soh-control.channels-by-monitor-type',
          configurationOptions: channelsPerMonitorFromDisk,
          changeTime: new Date().toISOString(),
        };

        await updateConfig(channelsPerMonitorConfigData);
      }
    }
    // ! The ui processing configuration current doesn't update after making updates
    // ! So to get the changes to reflect after saving, calling refresh to keep it in sync
    await refreshUiConfig();
  }, [
    allChannelNames,
    data,
    refreshUiConfig,
    state.backOffDuration,
    state.calculationInterval,
    state.channelCapabilityRollup,
    state.monitorTypesForRollup,
    state.stationGroupCapabilityRollup,
    state.stationGroups,
    state.stationName,
    stationGroupNames,
    supportedMonitorNames,
    updateConfig,
  ]);
  return [exportConfiguration];
};

export interface AppProps {
  transitionStage: 'fadeIn' | 'fadeOut';
  onTransitionEnd: () => void;
}

const App: React.VFC<AppProps> = ({
  transitionStage,
  onTransitionEnd,
}: AppProps) => {
  const appState = useAppState();
  const { data, setData } = appState;
  const classes = useStyles();
  const [loadingState, setLoadingState] = React.useState<LoadingState>({
    numRequested: 0,
    numComplete: 0,
  });
  usePersistentSettings();
  useSohControlConfig(setData, setLoadingState);
  useDataframeReceiver(setData, setLoadingState);
  const processingStationGroups = useProcessingStationGroups(setLoadingState);
  useSupportedMonitorTypes(data, setData, setLoadingState);

  const stationGroups = useStationGroups(processingStationGroups ?? []);
  if (!isEqual(stationGroups, data.stationGroups)) {
    setData(
      produce(data, (draft: Draft<any>) => {
        draft.processingStationGroups = processingStationGroups;
        draft.stationGroups = stationGroups;
      })
    );
  }

  useSyncWithFrameworksConfig(data);

  const error = useAppSelector((state) => state['app-settings'].error);
  return (
    <React.Fragment>
      <Nav>
        <NavLink className={classes.link} to='/'>
          Station
        </NavLink>
        <NavLink className={classes.iconLink} to='app-settings'>
          <Settings />
        </NavLink>
      </Nav>
      <main
        className={`${classes.container} ${transitionStage}`}
        onAnimationEnd={onTransitionEnd}
      >
        {error && <Alert severity='error'>{error}</Alert>}
        <Outlet context={{ ...appState, ...loadingState }} />
      </main>
    </React.Fragment>
  );
};

export default App;
