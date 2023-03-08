import produce from 'immer';
import includes from 'lodash/includes';
import isEqual from 'lodash/isEqual';
import {
  ChannelConstraint,
  ConfigurationOption,
  ConstraintType,
  MonitorTypeConstraint,
  OperatorType,
  StationConstraint,
  StationGroupNameConstraint,
} from '../coi-types';
import {
  MonitorTypeConfig,
  MonitorTypesForRollupStationConfig,
} from '../coi-types/monitor-types';
import {
  convertRollupsToChannelsToStationRollupOperator,
  convertRollupsToSohMonitorsToChannelRollupOperator,
} from '../renderers/station-capability-rollup/util';
import { StationConfig } from '../routes/Station';
import { DefaultThresholdsForStation } from '../state/api-slice';
import { ChannelCapabilityRollupQueryResults } from '../state/retrieve-channel-capability-rollups';
import { ChannelsByMonitorType } from '../state/retrieve-channels-by-monitor-type';
import {
  RollupOperatorOperands,
  StationGroupCapabilityQueryResults,
} from '../state/retrieve-station-group-capability';
import { StationGroupsDefinition } from '../state/retrieve-station-groups';
import { RollupEntry, StationGroup } from '../state/station-controls-slice';
import { isDurationMonitor } from '../util/util';
import {
  doesChannelCapabilityEntryMatchDefault,
  doesStationCapabilityEntryMatchDefault,
} from './util';

export function buildConfigurationOption<ParameterType = unknown>(
  name: string,
  constraintType: ConstraintType.DEFAULT,
  parameters: Record<string, ParameterType>
): ConfigurationOption {
  return {
    name,
    constraints: [
      {
        constraintType,
      },
    ],
    parameters,
  };
}

export function buildStationConstraint(stationName: string): StationConstraint {
  return {
    constraintType: ConstraintType.STRING,
    criterion: 'StationName',
    operator: {
      type: OperatorType.IN,
      negated: false,
    },
    value: [stationName],
  };
}

export function buildStationGroupNameConstraint(
  groupName: string
): StationGroupNameConstraint {
  return {
    constraintType: ConstraintType.STRING,
    criterion: 'StationGroupName',
    operator: {
      type: OperatorType.IN,
      negated: false,
    },
    value: [groupName],
  };
}

export function buildChannelConstraint(channelName: string): ChannelConstraint {
  return {
    constraintType: ConstraintType.STRING,
    criterion: 'ChannelName',
    operator: {
      type: OperatorType.IN,
      negated: false,
    },
    value: [channelName],
  };
}

export function buildMonitorTypeConstraint(
  monitorType: string
): MonitorTypeConstraint {
  return {
    constraintType: ConstraintType.STRING,
    criterion: 'MonitorType',
    operator: {
      type: OperatorType.IN,
      negated: false,
    },
    value: [monitorType],
  };
}

export interface TimeWindowParams {
  backOffDuration: string;
  calculationInterval: string;
}

export interface ThresholdParams {
  goodThreshold: string | number;
  marginalThreshold: string | number;
}

export interface ChannelsPerMonitorForStationParams {
  monitors: Record<string, { channelMode: string; channels: string[] }>;
}

export function buildTimeWindowsConfigForStation(
  stationName: string,
  stationConfig: StationConfig | undefined,
  defaultTimewindows: TimeWindowParams
): ConfigurationOption<Partial<TimeWindowParams>> | undefined {
  if (!stationName) {
    console.error(new Error('No station set'));
    return undefined;
  }
  if (!stationConfig) {
    console.error(new Error(`No config set for station ${stationName}`));
    return undefined;
  }
  return {
    name: `${stationName}_TIMEWINDOWS`,
    constraints: [buildStationConstraint(stationName)],
    parameters: {
      backOffDuration:
        stationConfig.backOffDuration ?? defaultTimewindows.backOffDuration,
      calculationInterval:
        stationConfig.calculationInterval ??
        defaultTimewindows.calculationInterval,
    },
    priority: 1,
  };
}

/**
 * Helper function to create objects to store threshold config data input
 *
 * @param stationName name of the station building threshold config for
 * @param stationConfig data to be used to build config objects
 * @param defaultThresholdsForStation record of all default thresholds
 * @param stationMonitorThresholdsConfigsFromDisk all threshold configs from disk
 * @returns Configuration option array
 */
export function buildMonitorThresholdsConfigForStation(
  stationName: string,
  stationConfig: StationConfig | undefined,
  defaultThresholdsForStation: Record<string, DefaultThresholdsForStation>,
  stationMonitorThresholdsConfigsFromDisk: ConfigurationOption<
    Partial<ThresholdParams>
  >[]
): ConfigurationOption<Partial<ThresholdParams>>[] | undefined | null {
  if (!stationName) {
    console.error(new Error('No station set'));
    return undefined;
  }
  if (!stationConfig) {
    console.error(new Error(`No config set for station ${stationName}`));
    return undefined;
  }

  if (!stationConfig.sohMonitorTypesForRollup) {
    console.error(new Error(`No monitors for station ${stationName}`));
    return undefined;
  }

  let thresholdsConfigsResult: ConfigurationOption<Partial<ThresholdParams>>[] =
    [];
  stationConfig.sohMonitorTypesForRollup.forEach((monitor) => {
    const parameters = {
      goodThreshold: isDurationMonitor(monitor.name)
        ? monitor.goodThreshold
        : parseFloat(monitor.goodThreshold as string),
      marginalThreshold: isDurationMonitor(monitor.name)
        ? monitor.marginalThreshold
        : parseFloat(monitor.marginalThreshold as string),
    };
    if (
      parameters.goodThreshold !==
        defaultThresholdsForStation[stationName].monitors[monitor.name]
          .goodThreshold ||
      parameters.marginalThreshold !==
        defaultThresholdsForStation[stationName].monitors[monitor.name]
          .marginalThreshold
    ) {
      thresholdsConfigsResult.push({
        name: `${monitor.name}_MONITOR_THRESHOLDS_FOR_${stationName}`,
        constraints: [
          buildStationConstraint(stationName),
          buildMonitorTypeConstraint(monitor.name),
        ],
        parameters,
        priority: 1,
      });
    }
    if (monitor.channelOverrides) {
      monitor.channelOverrides.forEach((override) => {
        const defaultChannelThresholdsMap =
          defaultThresholdsForStation[stationName].channels[override.name];
        const defaultChannelPerMonitorThresholds =
          defaultChannelThresholdsMap[monitor.name];
        const channelParameters = {
          goodThreshold: isDurationMonitor(monitor.name)
            ? override.goodThreshold
            : parseFloat(override.goodThreshold as string),
          marginalThreshold: isDurationMonitor(monitor.name)
            ? override.marginalThreshold
            : parseFloat(override.marginalThreshold as string),
        };
        const monitorThresholds = {
          goodThreshold: isDurationMonitor(monitor.name)
            ? monitor.goodThreshold
            : parseFloat(monitor.goodThreshold as string),
          marginalThreshold: isDurationMonitor(monitor.name)
            ? monitor.marginalThreshold
            : parseFloat(monitor.marginalThreshold as string),
        };
        // Is the channels threshold equal to the monitor threshold, or equal to the default/override file threshold (default)
        if (
          (channelParameters.goodThreshold !==
            monitorThresholds.goodThreshold ||
            channelParameters.marginalThreshold !==
              monitorThresholds.marginalThreshold) &&
          (channelParameters.goodThreshold !==
            defaultChannelPerMonitorThresholds.goodThreshold ||
            channelParameters.marginalThreshold !==
              defaultChannelPerMonitorThresholds.marginalThreshold)
        ) {
          if (override.goodThreshold && override.marginalThreshold) {
            thresholdsConfigsResult.push({
              name: `${monitor.name}_MONITOR_FOR_CHANNEL_${override.name}_FOR_STATION_${stationName}_THRESHOLDS`,
              constraints: [
                buildStationConstraint(stationName),
                buildMonitorTypeConstraint(monitor.name),
                buildChannelConstraint(override.name),
              ],
              parameters: channelParameters,
              priority: 1,
            });
          }
        } else {
          // checks if override config already is in disk, since do not have all channel threshold data, only ones
          // user expanded, will push object with default values to override the existing one
          // if not on disk, then won't do anything since its a save where values are the same as default
          if (
            stationMonitorThresholdsConfigsFromDisk.find(
              (config) =>
                config.name ===
                `${monitor.name}_MONITOR_FOR_CHANNEL_${override.name}_FOR_STATION_${stationName}_THRESHOLDS`
            )
          ) {
            thresholdsConfigsResult.push({
              name: `${monitor.name}_MONITOR_FOR_CHANNEL_${override.name}_FOR_STATION_${stationName}_THRESHOLDS`,
              constraints: [
                buildStationConstraint(stationName),
                buildMonitorTypeConstraint(monitor.name),
                buildChannelConstraint(override.name),
              ],
              parameters: {
                goodThreshold: isDurationMonitor(monitor.name)
                  ? override.goodThreshold
                  : parseFloat(override.goodThreshold as string),
                marginalThreshold: isDurationMonitor(monitor.name)
                  ? override.marginalThreshold
                  : parseFloat(override.marginalThreshold as string),
              },
              priority: 1,
            });
          }
        }
      });
    }
  });
  if (thresholdsConfigsResult.length === 0) {
    return null;
  }
  return thresholdsConfigsResult;
}

export function buildTimeWindowsConfig(
  stationName: string | null,
  stationConfig: StationConfig,
  defaultTimewindows: TimeWindowParams
):
  | Record<string, ConfigurationOption<Partial<TimeWindowParams>, unknown>>
  | undefined
  | null {
  if (
    (stationConfig.backOffDuration == null &&
      stationConfig.calculationInterval == null) ||
    stationName == null
  )
    return undefined;

  // If values match default no need to do anything, using null to let
  // caller know it matches default
  if (
    defaultTimewindows.backOffDuration === stationConfig.backOffDuration &&
    defaultTimewindows.calculationInterval === stationConfig.calculationInterval
  ) {
    return null;
  }

  const twConfigs: Record<
    string,
    ConfigurationOption<Partial<TimeWindowParams>>
  > = {};
  const twc = buildTimeWindowsConfigForStation(
    stationName,
    stationConfig,
    defaultTimewindows
  );
  if (twc != null) {
    twConfigs[stationName] = twc;
  }
  return twConfigs;
}

/**
 * Builds the monitor threshold configs to be saved to disk
 *
 * @param stationName name of the station
 * @param stationConfig data used to build config objects
 * @param defaultThresholdsForStation record of all threshold defaults
 * @param stationMonitorThresholdsConfigsFromDisk all threshold configs from disk
 * @returns an array of monitorThresholdsConfigs
 */
export function buildMonitorThresholdsConfig(
  stationName: string | null,
  stationConfig: StationConfig,
  defaultThresholdsForStation: Record<string, DefaultThresholdsForStation>,
  stationMonitorThresholdsConfigsFromDisk: ConfigurationOption<
    Partial<ThresholdParams>
  >[]
):
  | Record<string, ConfigurationOption<Partial<ThresholdParams>, unknown>[]>
  | undefined
  | null {
  if (stationConfig == null || stationName == null) {
    return undefined;
  }
  const monitorThresholdsConfigs: Record<
    string,
    ConfigurationOption<Partial<ThresholdParams>>[]
  > = {};
  const monitorThresholdConfig = buildMonitorThresholdsConfigForStation(
    stationName,
    stationConfig,
    defaultThresholdsForStation,
    stationMonitorThresholdsConfigsFromDisk
  );

  if (monitorThresholdConfig === null) {
    return null;
  }

  if (monitorThresholdConfig != null) {
    monitorThresholdsConfigs[stationName] = monitorThresholdConfig;
  }
  return monitorThresholdsConfigs;
}

export function buildMonitorTypesForRollupStationConfig(
  stationName: string,
  stationConfig: StationConfig | undefined
): MonitorTypesForRollupStationConfig | undefined {
  if (!stationName) {
    console.error(new Error('No station set'));
    return undefined;
  }
  if (!stationConfig) {
    console.error(new Error(`No config set for station ${stationName}`));
    return undefined;
  }
  if (stationConfig.sohMonitorTypesForRollup == null) {
    return undefined;
  }
  const includedMonitorTypes = stationConfig.sohMonitorTypesForRollup.filter(
    (mt) => mt.isIncluded
  );
  return {
    name: `${stationName}_MONITOR_TYPES`,
    constraints: [buildStationConstraint(stationName)],
    parameters: {
      sohMonitorTypesForRollup:
        includedMonitorTypes?.map((mt) => mt.name) ?? [],
    },
    priority: 1,
  };
}

/**
 * Creates configs for monitor types for rollup stations, checks if values
 * from state match default, if so doesn't nothing and returns null
 *
 * @param stationName name of the station
 * @param stationConfig stationConfig from state
 * @param defaultMonitorTypesForRollupStation default monitor types list
 * @returns undefined if no data, null if values match default, otherwise
 * Record<string, MonitorTypesForRollupStationConfig>
 */
export function buildMonitorTypesForRollupStations(
  stationName: string | null,
  stationConfig: StationConfig,
  defaultMonitorTypesForRollupStation: string[]
): Record<string, MonitorTypesForRollupStationConfig> | undefined | null {
  if (stationConfig.sohMonitorTypesForRollup == null || stationName == null)
    return undefined;

  const sortedMonitorTypeNames = stationConfig.sohMonitorTypesForRollup
    .filter((mt) => mt.isIncluded)
    .map((config) => config.name)
    .sort();
  defaultMonitorTypesForRollupStation.sort();

  if (isEqual(sortedMonitorTypeNames, defaultMonitorTypesForRollupStation)) {
    return null;
  }

  const mtsConfigs: Record<string, MonitorTypesForRollupStationConfig> = {};
  const mts = buildMonitorTypesForRollupStationConfig(
    stationName,
    stationConfig
  );
  if (mts != null) {
    mtsConfigs[stationName] = mts;
  }
  return mtsConfigs;
}

export function buildMonitorTypesForRollupChannelConfig(
  stationName: string,
  monitorTypesForRollup: MonitorTypeConfig[],
  defaultMonitorTypesForRollupStation: string[] // defaults for MonitorTypesForRollupStation match MonitorTypesForRollupChannel
): MonitorTypesForRollupStationConfig[] | undefined {
  if (!stationName) {
    console.error(new Error('No station set'));
    return undefined;
  }
  if (monitorTypesForRollup == null) {
    return undefined;
  }
  const sortedIncludedMonitorTypeNames = monitorTypesForRollup
    .filter((mt) => mt.isIncluded)
    .map((config) => config.name)
    .sort();
  const channelConfigs: MonitorTypesForRollupStationConfig[] = [];
  const monitorListForChannels: Record<string, string[]> = {};
  monitorTypesForRollup.forEach((rollup) => {
    if (rollup.channelOverrides) {
      rollup.channelOverrides.forEach((channelOverride) => {
        if (!monitorListForChannels[channelOverride.name]) {
          monitorListForChannels[channelOverride.name] = [];
        }
        if (
          includes(sortedIncludedMonitorTypeNames, rollup.name) &&
          channelOverride.isIncluded
        ) {
          monitorListForChannels[channelOverride.name].push(rollup.name);
        }
      });
    }
  });
  Object.keys(monitorListForChannels).forEach((channelName) => {
    const sortedMonitorListForChannel = produce(
      monitorListForChannels[channelName],
      (draft) => {
        draft.sort();
      }
    );

    if (!isEqual(sortedMonitorListForChannel, sortedIncludedMonitorTypeNames)) {
      channelConfigs.push({
        name: `${stationName}_MONITOR_TYPES_FOR_${channelName}`,
        constraints: [
          buildStationConstraint(stationName),
          buildChannelConstraint(channelName),
        ],
        parameters: {
          // !Backend issue not handing the case empty monitorListForChannels, adding array entry of ['NONE'] as a workaround
          sohMonitorTypesForRollup:
            monitorListForChannels[channelName].length === 0
              ? ['NONE']
              : monitorListForChannels[channelName],
        },
        priority: 1,
      });
    }
  });
  return channelConfigs;
}

export function buildMonitorTypesForRollupChannel(
  stationName: string | null,
  monitorTypesForRollup: MonitorTypeConfig[],
  defaultMonitorTypesForRollupStation: string[],
  monitorTypesForRollupStation:
    | MonitorTypesForRollupStationConfig
    | null
    | undefined
): Record<string, MonitorTypesForRollupStationConfig[]> | undefined | null {
  if (monitorTypesForRollup == null || stationName == null) return undefined;

  const mtsConfigs: Record<string, MonitorTypesForRollupStationConfig[]> = {};
  const mts = buildMonitorTypesForRollupChannelConfig(
    stationName,
    monitorTypesForRollup,
    defaultMonitorTypesForRollupStation // defaults for MonitorTypesForRollupStation match MonitorTypesForRollupChannel
  );
  if (mts != null) {
    if (monitorTypesForRollupStation) {
      mtsConfigs[stationName] = [monitorTypesForRollupStation, ...mts];
    } else {
      mtsConfigs[stationName] = mts;
    }
  }
  if (mtsConfigs[stationName].length !== 0) {
    return mtsConfigs;
  }

  return null;
}

export function buildChannelsPerMonitorForStation(
  stationName: string,
  stationConfig: StationConfig | undefined,
  defaultChannelsPerMonitorForStation: ChannelsPerMonitorForStationParams
):
  | ConfigurationOption<Partial<ChannelsPerMonitorForStationParams>>
  | undefined
  | null {
  if (!stationName) {
    console.error(new Error('No station set'));
    return undefined;
  }
  if (!stationConfig) {
    console.error(new Error(`No config set for station ${stationName}`));
    return undefined;
  }
  if (!stationConfig.sohMonitorTypesForRollup) {
    console.error(new Error(`No monitors for station ${stationName}`));
    return undefined;
  }

  let channelsPerMonitorForStationConfigsResult: ConfigurationOption<
    Partial<ChannelsPerMonitorForStationParams>
  >;
  let channelsPerMonitorType: ChannelsByMonitorType = {};
  stationConfig.sohMonitorTypesForRollup.forEach((monitor) => {
    if (!monitor.channelOverrides) {
      console.error(
        new Error(
          `No channel overrides for ${stationName} monitor ${monitor.name}`
        )
      );
      return undefined;
    }
    const isAllChannelsSelected = monitor.channelOverrides.every(
      (channelOverride) => {
        return channelOverride.isIncluded === true;
      }
    );
    channelsPerMonitorType[monitor.name] = {
      channelsMode: isAllChannelsSelected ? 'USE_ALL' : 'USE_LISTED',
      channels: !isAllChannelsSelected
        ? (monitor.channelOverrides
            .map((channelOverride) => {
              if (channelOverride.isIncluded) {
                return channelOverride.name;
              }
              return undefined;
            })
            .filter((channel) => channel !== undefined) as string[])
        : [],
    };
  });
  channelsPerMonitorForStationConfigsResult = {
    name: `${stationName}_MONITOR_CHANNELS_SELECTIONS`,
    constraints: [buildStationConstraint(stationName)],
    parameters: {
      ...channelsPerMonitorType,
    },
    priority: 1,
  };

  if (isEqual(channelsPerMonitorType, defaultChannelsPerMonitorForStation)) {
    return null;
  }
  return channelsPerMonitorForStationConfigsResult;
}

/**
 * Builds the channels by monitor type configs to be saved to disk
 *
 * @param stationName name of the station
 * @param stationConfig input data to create override config
 * @returns Record of configuration option with ChannelPerMonitorForStationParams
 */
export function buildChannelsPerMonitorForStationConfig(
  stationName: string | null,
  stationConfig: StationConfig,
  defaultChannelsPerMonitorForStation: ChannelsPerMonitorForStationParams
):
  | Record<
      string,
      ConfigurationOption<Partial<ChannelsPerMonitorForStationParams>, unknown>
    >
  | undefined
  | null {
  if (stationConfig.sohMonitorTypesForRollup == null || stationName == null)
    return undefined;

  const channelPerMonitorForStationConfigs: Record<
    string,
    ConfigurationOption<Partial<ChannelsPerMonitorForStationParams>>
  > | null = {};
  const channelPerMonitorForStationConfig = buildChannelsPerMonitorForStation(
    stationName,
    stationConfig,
    defaultChannelsPerMonitorForStation
  );

  if (channelPerMonitorForStationConfig === null) {
    return null;
  }

  if (
    channelPerMonitorForStationConfig !== undefined &&
    channelPerMonitorForStationConfig != null
  ) {
    channelPerMonitorForStationConfigs[stationName] =
      channelPerMonitorForStationConfig;
  }
  return channelPerMonitorForStationConfigs;
}

/**
 * Creates a station definitions adding station to groups or removing them based on 'stationGroups'
 *
 * @param stationName name of the station to add or remove to station definitions
 * @param stationDefinitions station definitions read from file on disk
 * @param stationGroups station groups with user input for included or not included
 * @returns station definitions
 */
export function buildProcessingStationGroupDefinition(
  stationName: string | null,
  stationDefinitions: StationGroupsDefinition[],
  stationGroups: StationGroup[]
): StationGroupsDefinition[] {
  if (!stationName) {
    throw new Error('no station selected');
  }
  let stationGroupDefinitions: StationGroupsDefinition[] = [];
  stationDefinitions.forEach((definition) => {
    let stations = definition.stationNames;
    let group = stationGroups.find((g) => g.name === definition.name);
    const isStationInStationName = includes(stations, stationName);
    if (group && group.included && !isStationInStationName) {
      stations.push(stationName);
    }
    if (group && !group.included && isStationInStationName) {
      stations = stations.filter((station) => station !== stationName);
    }
    stations = stations.sort();
    stationGroupDefinitions.push({
      ...definition,
      stationNames: stations,
    });
  });
  return stationGroupDefinitions;
}

/**
 * Creates a config entry for station capability rollup
 *
 * @param stationName name of the station
 * @param allChannelNames list of all channels used to know if all is selected
 * @param groupName name of the group
 * @param defaultStationCapabilities default station by group capabilities
 * @param stationCapabilityToSave entry to save for station capability
 * @returns config entry or null if matches default
 */
export function buildStationCapabilityRollupForStation(
  stationName: string,
  allChannelNames: string[],
  groupName: string,
  defaultStationCapabilities: Record<
    string,
    Record<string, RollupOperatorOperands>
  >,
  stationCapabilityToSave: RollupEntry
):
  | ConfigurationOption<Partial<StationGroupCapabilityQueryResults>>
  | undefined
  | null {
  if (!stationName) {
    console.error(new Error('No station set'));
    return undefined;
  }
  if (!stationCapabilityToSave) {
    console.error(
      new Error(
        `No config set for station capability for station ${stationName}, group ${groupName}`
      )
    );
    return undefined;
  }

  let stationCapabilityRollupConfigsResult: ConfigurationOption<
    Partial<StationGroupCapabilityQueryResults>
  >;

  const convertedRollupsToChannelsToStationRollupOperator =
    convertRollupsToChannelsToStationRollupOperator(
      stationCapabilityToSave,
      allChannelNames
    );

  if (
    doesStationCapabilityEntryMatchDefault(
      convertedRollupsToChannelsToStationRollupOperator,
      defaultStationCapabilities[stationName][groupName],
      allChannelNames
    )
  ) {
    return null;
  }

  stationCapabilityRollupConfigsResult = {
    name: `${stationName}_${groupName}_CAPABILITY_ROLLUP`,
    constraints: [
      buildStationConstraint(stationName),
      buildStationGroupNameConstraint(groupName),
    ],
    parameters: {
      channelsToStationRollupOperator: {
        ...convertedRollupsToChannelsToStationRollupOperator,
      },
    },
    priority: 1,
  };

  return stationCapabilityRollupConfigsResult;
}

/**
 * Builds station capability rollup configs
 *
 * @param stationName name of the station
 * @param allChannelNames list of all channels used to know if all is selected
 * @param defaultStationCapabilities default station by group capabilities
 * @param stationCapabilitiesToSave current state of station capabilities
 * @returns Record key on station and array of configs
 */
export function buildStationCapabilityRollupConfig(
  stationName: string | null,
  allChannelNames: string[],
  defaultStationCapabilities: Record<
    string,
    Record<string, RollupOperatorOperands>
  >,
  stationCapabilitiesToSave: Record<string, RollupEntry>
):
  | Record<
      string,
      ConfigurationOption<
        Partial<StationGroupCapabilityQueryResults>,
        unknown
      >[]
    >
  | undefined
  | null {
  if (stationCapabilitiesToSave == null || stationName == null)
    return undefined;

  const capabilityRollupConfigs:
    | ConfigurationOption<Partial<StationGroupCapabilityQueryResults>>[]
    | null = [];
  Object.keys(stationCapabilitiesToSave).forEach((groupName) => {
    const stationCapabilityRollupConfig =
      buildStationCapabilityRollupForStation(
        stationName,
        allChannelNames,
        groupName,
        defaultStationCapabilities,
        stationCapabilitiesToSave[groupName]
      );

    if (stationCapabilityRollupConfig === null) {
      return null;
    }

    if (
      stationCapabilityRollupConfig !== undefined &&
      stationCapabilityRollupConfig != null
    ) {
      capabilityRollupConfigs.push(stationCapabilityRollupConfig); // if differ than default add config to array of configs
    }
  });
  const stationCapabilityRollupConfigs:
    | Record<
        string,
        ConfigurationOption<
          Partial<StationGroupCapabilityQueryResults>,
          unknown
        >[]
      >
    | undefined
    | null = {};
  // Updating the record for station name to the potentially array of capability configs if any were found to
  // To be differ than default/overrides
  if (capabilityRollupConfigs && capabilityRollupConfigs.length > 0) {
    stationCapabilityRollupConfigs[stationName] = capabilityRollupConfigs;
    return stationCapabilityRollupConfigs;
  }
  return null;
}

/**
 * Creates a config entry for channel capability rollup
 *
 * @param stationName name of the station
 * @param supportedMonitorNames list of supported monitor names
 * @param groupName name of the group
 * @param defaultChannelCapabilities default station by group by channel capabilities
 * @param channelCapabilityToSave entry to save for channel capability
 * @returns config entry or null if matches default
 */
export function buildChannelCapabilityRollupForStation(
  stationName: string,
  supportedMonitorNames: string[],
  groupName: string,
  channelName: string,
  defaultChannelCapabilities: Record<
    string,
    Record<string, Record<string, RollupOperatorOperands>>
  >,
  channelCapabilityToSave: RollupEntry
):
  | ConfigurationOption<Partial<ChannelCapabilityRollupQueryResults>>
  | undefined
  | null {
  if (!stationName) {
    console.error(new Error('No station set'));
    return undefined;
  }
  if (!channelCapabilityToSave) {
    console.error(
      new Error(
        `No config set for channel capability for station ${stationName}, group ${groupName}, channel ${channelName}`
      )
    );
    return undefined;
  }

  let channelCapabilityRollupConfigsResult: ConfigurationOption<
    Partial<ChannelCapabilityRollupQueryResults>
  >;

  const convertedRollupsToSohMonitorsToChannelRollupOperator =
    convertRollupsToSohMonitorsToChannelRollupOperator(
      channelCapabilityToSave,
      supportedMonitorNames
    );

  if (
    doesChannelCapabilityEntryMatchDefault(
      convertedRollupsToSohMonitorsToChannelRollupOperator,
      defaultChannelCapabilities[stationName][groupName][channelName],
      supportedMonitorNames
    )
  ) {
    return null;
  }

  channelCapabilityRollupConfigsResult = {
    name: `${stationName}_${groupName}_${channelName}_CAPABILITY_ROLLUP`,
    constraints: [
      buildStationConstraint(stationName),
      buildStationGroupNameConstraint(groupName),
      buildChannelConstraint(channelName),
    ],
    parameters: {
      sohMonitorsToChannelRollupOperator: {
        ...convertedRollupsToSohMonitorsToChannelRollupOperator,
      },
    },
    priority: 1,
  };

  return channelCapabilityRollupConfigsResult;
}

/**
 * Builds channel capability rollup configs
 *
 * @param stationName name of the station
 * @param supportedMonitorNames list of supported monitor names
 * @param defaultChannelCapabilities default station by group capabilities
 * @param channelCapabilitiesToSave current state of channel capabilities
 * @returns Record key on station and array of configs
 */
export function buildChannelCapabilityRollupConfig(
  stationName: string | null,
  supportedMonitorNames: string[],
  defaultChannelCapabilities: Record<
    string,
    Record<string, Record<string, RollupOperatorOperands>>
  >,
  channelCapabilitiesToSave: Record<string, Record<string, RollupEntry>>
):
  | Record<
      string,
      ConfigurationOption<
        Partial<ChannelCapabilityRollupQueryResults>,
        unknown
      >[]
    >
  | undefined
  | null {
  if (channelCapabilitiesToSave == null || stationName == null)
    return undefined;

  const capabilityRollupConfigs:
    | ConfigurationOption<Partial<ChannelCapabilityRollupQueryResults>>[]
    | null = [];
  Object.keys(channelCapabilitiesToSave).forEach((groupName) => {
    Object.keys(channelCapabilitiesToSave[groupName]).forEach((channelName) => {
      const channelCapabilityRollupConfig =
        buildChannelCapabilityRollupForStation(
          stationName,
          supportedMonitorNames,
          groupName,
          channelName,
          defaultChannelCapabilities,
          channelCapabilitiesToSave[groupName][channelName]
        );

      if (channelCapabilityRollupConfig === null) {
        return null;
      }

      if (
        channelCapabilityRollupConfig !== undefined &&
        channelCapabilityRollupConfig != null
      ) {
        capabilityRollupConfigs.push(channelCapabilityRollupConfig); // if differ than default add config to array of configs
      }
    });
  });
  const channelCapabilityRollupConfigs:
    | Record<
        string,
        ConfigurationOption<
          Partial<ChannelCapabilityRollupQueryResults>,
          unknown
        >[]
      >
    | undefined
    | null = {};
  // Updating the record for station name to the potentially array of capability configs if any were found to
  // To be differ than default/overrides
  if (capabilityRollupConfigs && capabilityRollupConfigs.length > 0) {
    channelCapabilityRollupConfigs[stationName] = capabilityRollupConfigs;
    return channelCapabilityRollupConfigs;
  }
  return null;
}
