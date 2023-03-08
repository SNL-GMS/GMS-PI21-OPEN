import { includes } from 'lodash';
import parse from 'parse-duration';
import { parse as tinyDurationParse } from 'tinyduration';
import { ConfigurationOption } from '../coi-types';
import {
  ChannelsPerMonitorForStationParams,
  ThresholdParams,
  TimeWindowParams,
} from '../output/build-configuration-option';
import {
  ChannelsPerMonitorForStationConfig,
  DefaultThresholdsForStation,
  MonitorsForRollupStationConfig,
  TimeWindowsConfig,
} from '../state/api-slice';
import { ChannelCapabilityRollupQueryResults } from '../state/retrieve-channel-capability-rollups';
import {
  RollupOperatorOperands,
  StationGroupCapabilityQueryResults,
} from '../state/retrieve-station-group-capability';
import { UserInputError } from '../state/station-controls-slice';

/**
 * Checks to see if input is a valid duration
 *
 * @param candidateDuration string of duration
 * @returns boolean if valid duration
 */
export const isValidDuration = (candidateDuration: string): boolean => {
  try {
    tinyDurationParse(candidateDuration);
    return true;
  } catch (e) {
    return false;
  }
};

/**
 * Temp function to know duration monitor types
 *
 * @param monitorType name of the monitor
 * @returns boolean if LAG or TIMELINESS (durations)
 */
export const isDurationMonitor = (monitorType: string) => {
  // PO instructed to hardcode this check for now
  if (monitorType === 'LAG' || monitorType === 'TIMELINESS') {
    return true;
  }
};

/**
 * Determines if input error
 *
 * @param isDuration boolean
 * @param value ether duration or percent
 * @returns boolean determining if error
 */
export const isPercentOrDurationError = (
  isDuration: boolean,
  value: string
): boolean => {
  // Only query on click, so by default undefined, however returning
  // false since don't want to provide an error when undefined
  if (value === undefined) {
    return false;
  }

  // PO instructed to hardcode this check for now
  if (isDuration) {
    if (parse(value) === 0) {
      return true;
    }
    return !isValidDuration(value);
  }

  if (!(parseFloat(value) <= 100 && parseFloat(value) >= 0)) {
    return true;
  }
  return false;
};

/**
 * Determines if marginal is greater than good or equal to good
 *
 * @param isDuration is duration monitor
 * @param goodValue value of good
 * @param marginalValue value of marginal
 * @returns boolean
 */
export const isMarginalGreaterThanOrEqualToGood = (
  isDuration: boolean,
  goodValue: string,
  marginalValue: string
): boolean => {
  if (isDuration) {
    return parse(goodValue) <= parse(marginalValue);
  }
  return parseFloat(goodValue) <= parseFloat(marginalValue);
};

/**
 * Determines if good is greater than marginal or equal to marginal
 *
 * @param isDuration is duration monitor
 * @param goodValue value of good
 * @param marginalValue value of marginal
 * @returns boolean
 */
export const isGoodGreaterThanOrEqualToMarginal = (
  isDuration: boolean,
  goodValue: string,
  marginalValue: string
): boolean => {
  if (isDuration) {
    return parse(goodValue) <= parse(marginalValue);
  }
  return parseFloat(marginalValue) <= parseFloat(goodValue);
};

/**
 * Uses default json and overrides to determine default values
 *
 * @param stationName name of the station
 * @param monitorNames list of monitor names
 * @param defaultConfigs default configs from disk
 * @param stationMonitorThresholdsConfigsFromDisk  all configs from disk
 * @returns record of thresholds defaults for monitors
 */
const determineMonitorThresholdDefaults = (
  stationName: string,
  monitorNames: string[],
  defaultConfigs: ConfigurationOption<Partial<ThresholdParams>>[],
  stationMonitorThresholdsConfigsFromDisk: ConfigurationOption<
    Partial<ThresholdParams>
  >[]
): Record<string, ThresholdParams> => {
  const monitorDefaults: Record<string, ThresholdParams> = {};
  monitorNames.forEach((monitorName) => {
    defaultConfigs.forEach((config) => {
      const monitorConstraint = config.constraints.find(
        (c) => c.criterion === 'MonitorType'
      );
      if (monitorConstraint) {
        if (
          includes(monitorConstraint.value, monitorName) &&
          config.parameters
        ) {
          monitorDefaults[monitorName] = {
            goodThreshold: config.parameters.goodThreshold ?? 'no default',
            marginalThreshold:
              config.parameters.marginalThreshold ?? 'no default',
          };
        }
      }
    });
    stationMonitorThresholdsConfigsFromDisk.forEach((config) => {
      config.constraints.forEach((constraint) => {
        if (constraint.constraintType === 'DEFAULT') {
          const stationConstraint = config.constraints.find(
            (c) => c.criterion === 'StationName'
          );
          const monitorConstraint = config.constraints.find(
            (c) => c.criterion === 'MonitorType'
          );
          if (stationConstraint && monitorConstraint) {
            if (
              includes(stationConstraint.value, stationName) &&
              includes(monitorConstraint.value, monitorName) &&
              config.parameters &&
              config.parameters.goodThreshold &&
              config.parameters.marginalThreshold
            ) {
              monitorDefaults[monitorName] = {
                goodThreshold: config.parameters.goodThreshold,
                marginalThreshold: config.parameters.marginalThreshold,
              };
            }
          }
        }
      });
    });
  });
  return monitorDefaults;
};

/**
 * Uses default json and overrides to determine default values
 *
 * @param stationName name of the station
 * @param monitorNames list of monitor names
 * @param channelNames list of channel names
 * @param defaultConfigs default configs from disk
 * @param stationMonitorThresholdsConfigsFromDisk  all configs from disk
 * @returns record of thresholds defaults for channels
 */
const determineChannelThresholdDefaults = (
  stationName: string,
  monitorNames: string[],
  channelNames: string[],
  defaultConfigs: ConfigurationOption<Partial<ThresholdParams>>[],
  stationMonitorThresholdsConfigsFromDisk: ConfigurationOption<
    Partial<ThresholdParams>
  >[]
): Record<string, Record<string, ThresholdParams>> => {
  const channelDefaults: Record<string, Record<string, ThresholdParams>> = {};
  const threshold: Record<string, ThresholdParams> = {};

  channelNames.forEach((channelName) => {
    monitorNames.forEach((monitorName) => {
      defaultConfigs.forEach((config) => {
        const monitorConstraint = config.constraints.find(
          (c) => c.criterion === 'MonitorType'
        );
        if (monitorConstraint) {
          if (
            includes(monitorConstraint.value, monitorName) &&
            config.parameters
          ) {
            threshold[monitorName] = {
              goodThreshold: config.parameters.goodThreshold ?? 'no default',
              marginalThreshold:
                config.parameters.marginalThreshold ?? 'no default',
            };
            channelDefaults[channelName] = threshold;
          }
        }
      });
      stationMonitorThresholdsConfigsFromDisk.forEach((config) => {
        config.constraints.forEach((constraint) => {
          if (constraint.constraintType === 'DEFAULT') {
            const stationConstraint = config.constraints.find(
              (c) => c.criterion === 'StationName'
            );
            const monitorConstraint = config.constraints.find(
              (c) => c.criterion === 'MonitorType'
            );
            const channelConstraint = config.constraints.find(
              (c) => c.criterion === 'ChannelName'
            );
            if (stationConstraint && monitorConstraint && channelConstraint) {
              if (
                includes(stationConstraint.value, stationName) &&
                includes(monitorConstraint.value, monitorName) &&
                includes(channelConstraint.value, channelName) &&
                config.parameters &&
                config.parameters.goodThreshold &&
                config.parameters.marginalThreshold
              ) {
                threshold[monitorName] = {
                  goodThreshold: config.parameters.goodThreshold,
                  marginalThreshold: config.parameters.marginalThreshold,
                };
                channelDefaults[channelName] = threshold;
              }
            } else if (stationConstraint && monitorConstraint) {
              if (
                includes(stationConstraint.value, stationName) &&
                includes(monitorConstraint.value, monitorName) &&
                config.parameters &&
                config.parameters.goodThreshold &&
                config.parameters.marginalThreshold
              ) {
                threshold[monitorName] = {
                  goodThreshold: config.parameters.goodThreshold,
                  marginalThreshold: config.parameters.marginalThreshold,
                };
                channelDefaults[channelName] = threshold;
              }
            }
          }
        });
      });
    });
  });
  return channelDefaults;
};

/**
 * Uses default json and overrides to determine default values
 * for thresholds for station monitors and channels
 *
 * @param stationMonitorThresholdsConfigsFromDisk  all configs from disk
 * @param stationName name of the station
 * @param monitorNames list of monitor names
 * @param channelNames list of channel names
 * @returns Record<string, DefaultThresholdsForStation>
 */
export const determineDefaultThresholdsForStation = (
  stationMonitorThresholdsConfigsFromDisk: ConfigurationOption<
    Partial<ThresholdParams>
  >[],
  stationName: string | null,
  monitorNames: string[],
  channelNames: string[]
): Record<string, DefaultThresholdsForStation> => {
  if (!stationMonitorThresholdsConfigsFromDisk) {
    throw new Error('default.json must exist for thresholds');
  }
  if (!stationName) {
    throw new Error('No station selected to save time windows');
  }

  const defaultThresholdsForStation: Record<
    string,
    DefaultThresholdsForStation
  > = {};
  let defaultConfigs: ConfigurationOption<Partial<ThresholdParams>>[] = [];
  stationMonitorThresholdsConfigsFromDisk.forEach((config) => {
    if (config.name.includes('default')) {
      defaultConfigs.push(config);
    }
  });

  defaultThresholdsForStation[stationName] = {
    monitors: determineMonitorThresholdDefaults(
      stationName,
      monitorNames,
      defaultConfigs,
      stationMonitorThresholdsConfigsFromDisk
    ),
    channels: determineChannelThresholdDefaults(
      stationName,
      monitorNames,
      channelNames,
      defaultConfigs,
      stationMonitorThresholdsConfigsFromDisk
    ),
  };
  return defaultThresholdsForStation;
};

/**
 * Uses default.json and other default constrain types
 * to determine default channels per monitor for station
 *
 * @param channelsPerMonitorForStationConfigsFromDisk config data from disk
 * @param defaultConfig config data from default.json
 * @param stationName name of the station
 * @returns undefined if error, null if matches default, or
 * Record<string, { channelMode: string; channels: string[] }>;
 */
export const determineDefaultChannelsPerMonitorForStation = (
  channelsPerMonitorForStationConfigsFromDisk: ChannelsPerMonitorForStationConfig[],
  defaultConfig: ChannelsPerMonitorForStationConfig | undefined,
  stationName: string | null
): ChannelsPerMonitorForStationParams => {
  if (!defaultConfig) {
    throw new Error(
      'default.json must exist with name default-channels-by-monitor-type'
    );
  }
  if (!stationName) {
    throw new Error('No station selected to save time windows');
  }

  let defaultChannelsPerMonitorForStation: ChannelsPerMonitorForStationParams =
    defaultConfig.parameters;

  channelsPerMonitorForStationConfigsFromDisk.forEach((config) => {
    config.constraints.forEach((constraint) => {
      if (constraint.constraintType === 'DEFAULT') {
        const d = config.constraints.find((c) => c.criterion === 'StationName');
        if (d) {
          if (includes(d.value, stationName)) {
            defaultChannelsPerMonitorForStation = config.parameters;
          }
        }
      }
    });
  });

  return defaultChannelsPerMonitorForStation;
};

/**
 * Uses default.json and other default constrain types
 * to determine default station capability configs per group for station
 *
 * @param stationCapabilityConfigsFromDisk configs from disk
 * @param defaultConfig default config value
 * @param stationName name of the station
 * @param stationGroupNames names of all the groups
 * @returns default RollupOperatorOperands
 */
export const determineDefaultStationCapabilityForStation = (
  stationCapabilityConfigsFromDisk: ConfigurationOption<
    Partial<StationGroupCapabilityQueryResults>
  >[],
  defaultConfig:
    | ConfigurationOption<Partial<StationGroupCapabilityQueryResults>>
    | undefined,
  stationName: string | null,
  stationGroupNames: string[] | null
): Record<string, Record<string, RollupOperatorOperands>> => {
  if (!defaultConfig) {
    throw new Error(
      'default.json must exist with name default-station-capability-rollup'
    );
  }
  if (!defaultConfig.parameters.channelsToStationRollupOperator) {
    throw new Error(
      'default.json has no parameters default-station-capability-rollup'
    );
  }
  if (!stationName) {
    throw new Error('No station selected to save station capability rollup');
  }

  if (!stationGroupNames) {
    throw new Error('No station groups for station');
  }

  let defaultStationCapabilityForStation: RollupOperatorOperands =
    defaultConfig.parameters.channelsToStationRollupOperator;

  stationCapabilityConfigsFromDisk.forEach((config) => {
    config.constraints.forEach((constraint) => {
      if (constraint.constraintType === 'DEFAULT') {
        const d = config.constraints.find((c) => c.criterion === 'StationName');
        if (d) {
          if (includes(d.value, stationName)) {
            defaultStationCapabilityForStation =
              config.parameters as RollupOperatorOperands;
          }
        }
      }
    });
  });

  const stationCapabilityRollupDefaults: Record<
    string,
    RollupOperatorOperands
  > = {};
  stationGroupNames.forEach((stationGroupName) => {
    stationCapabilityRollupDefaults[stationGroupName] =
      defaultStationCapabilityForStation;
    stationCapabilityConfigsFromDisk.forEach((config) => {
      config.constraints.forEach((constraint) => {
        if (constraint.constraintType === 'DEFAULT') {
          const stationConstraint = config.constraints.find(
            (c) => c.criterion === 'StationName'
          );
          const groupNameConstraint = config.constraints.find(
            (c) => c.criterion === 'StationGroupName'
          );
          if (stationConstraint && groupNameConstraint) {
            if (
              includes(stationConstraint.value, stationName) &&
              includes(groupNameConstraint.value, stationGroupName) &&
              config.parameters &&
              config.parameters.channelsToStationRollupOperator
            ) {
              stationCapabilityRollupDefaults[stationGroupName] =
                config.parameters.channelsToStationRollupOperator;
            }
          } else if (!stationConstraint && groupNameConstraint) {
            if (
              includes(groupNameConstraint.value, stationGroupName) &&
              config.parameters &&
              config.parameters.channelsToStationRollupOperator
            ) {
              stationCapabilityRollupDefaults[stationGroupName] =
                config.parameters.channelsToStationRollupOperator;
            }
          }
        }
      });
    });
  });
  const defaultStationCapabilitiesForStation: Record<
    string,
    Record<string, RollupOperatorOperands>
  > = {};
  defaultStationCapabilitiesForStation[stationName] =
    stationCapabilityRollupDefaults;
  return defaultStationCapabilitiesForStation;
};

/**
 * Uses default.json and other default constrain types
 * to determine default channel capability configs per group for channel
 *
 * @param channelCapabilityConfigsFromDisk configs from disk
 * @param defaultConfig default config value
 * @param stationName name of the station
 * @param stationGroupNames names of all the groups
 * @param allChannelNames name of all the channels
 * @returns default RollupOperatorOperands
 */
export const determineDefaultChannelCapabilityForStation = (
  channelCapabilityConfigsFromDisk: ConfigurationOption<
    Partial<ChannelCapabilityRollupQueryResults>
  >[],
  defaultConfig:
    | ConfigurationOption<Partial<ChannelCapabilityRollupQueryResults>>
    | undefined,
  stationName: string | null,
  stationGroupNames: string[] | null,
  allChannelNames: string[] | null
): Record<string, Record<string, Record<string, RollupOperatorOperands>>> => {
  if (!defaultConfig) {
    throw new Error(
      'default.json must exist with name default-channel-capability-rollup'
    );
  }
  if (!defaultConfig.parameters.sohMonitorsToChannelRollupOperator) {
    throw new Error(
      'default.json has no parameters default-channel-capability-rollup'
    );
  }
  if (!stationName) {
    throw new Error('No station selected to save station capability rollup');
  }

  if (!stationGroupNames) {
    throw new Error('No group names for station');
  }

  if (!allChannelNames) {
    throw new Error('No channel names for station');
  }

  let defaultChannelCapabilityForStation: RollupOperatorOperands =
    defaultConfig.parameters.sohMonitorsToChannelRollupOperator;

  channelCapabilityConfigsFromDisk.forEach((config) => {
    config.constraints.forEach((constraint) => {
      if (constraint.constraintType === 'DEFAULT') {
        const d = config.constraints.find((c) => c.criterion === 'StationName');
        if (d) {
          if (includes(d.value, stationName)) {
            defaultChannelCapabilityForStation = config.parameters
              .sohMonitorsToChannelRollupOperator as RollupOperatorOperands;
          }
        }
      }
    });
  });

  const channelCapabilityGroups: Record<
    string,
    Record<string, RollupOperatorOperands>
  > = {};

  const channelCapabilityRollupDefaults: Record<
    string,
    RollupOperatorOperands
  > = {};
  stationGroupNames.forEach((groupName) => {
    allChannelNames.forEach((channelName) => {
      channelCapabilityRollupDefaults[channelName] =
        defaultChannelCapabilityForStation;
      channelCapabilityGroups[groupName] = channelCapabilityRollupDefaults;
      channelCapabilityConfigsFromDisk.forEach((config) => {
        config.constraints.forEach((constraint) => {
          if (constraint.constraintType === 'DEFAULT') {
            const stationConstraint = config.constraints.find(
              (c) => c.criterion === 'StationName'
            );
            const groupNameConstraint = config.constraints.find(
              (c) => c.criterion === 'StationGroupName'
            );
            if (stationConstraint && groupNameConstraint) {
              if (
                includes(stationConstraint.value, stationName) &&
                includes(groupNameConstraint.value, channelName) &&
                config.parameters &&
                config.parameters.sohMonitorsToChannelRollupOperator
              ) {
                channelCapabilityRollupDefaults[channelName] =
                  config.parameters.sohMonitorsToChannelRollupOperator;
                channelCapabilityGroups[groupName] =
                  channelCapabilityRollupDefaults;
              }
            } else if (!stationConstraint && groupNameConstraint) {
              if (
                includes(groupNameConstraint.value, channelName) &&
                config.parameters &&
                config.parameters.sohMonitorsToChannelRollupOperator
              ) {
                channelCapabilityRollupDefaults[channelName] =
                  config.parameters.sohMonitorsToChannelRollupOperator;
                channelCapabilityGroups[groupName] =
                  channelCapabilityRollupDefaults;
              }
            }
          }
        });
      });
    });
  });

  const defaultChannelCapabilitiesForStation: Record<
    string,
    Record<string, Record<string, RollupOperatorOperands>>
  > = {};
  defaultChannelCapabilitiesForStation[stationName] = channelCapabilityGroups;
  return defaultChannelCapabilitiesForStation;
};

/**
 * Uses default.json and other default constrain types from time window directory to
 * determine default values. First looks to see if station is in any overrides, if not
 * uses the default.json values.
 *
 * @param timeWindowsFromDisk array of data from files in time window directory
 * @param defaultConfig default config from default.json
 * @param stationName name of the station to get defaults for
 * @returns default TimeWindows
 */
export const determineDefaultTimewindowsForStation = (
  timeWindowsFromDisk: TimeWindowsConfig[],
  defaultConfig: TimeWindowsConfig | undefined,
  stationName: string | null
): TimeWindowParams => {
  if (!defaultConfig) {
    throw new Error(
      'default.json must exist with name default-soh-monitor-timewindows'
    );
  }
  if (!stationName) {
    throw new Error('No station selected to save time windows');
  }
  let defaultTimeWindowsForStation: TimeWindowParams = {
    backOffDuration: defaultConfig.parameters.backOffDuration,
    calculationInterval: defaultConfig.parameters.calculationInterval,
  };
  timeWindowsFromDisk.forEach((config) => {
    config.constraints.forEach((constraint) => {
      if (constraint.constraintType === 'DEFAULT') {
        const d = config.constraints.find((c) => c.criterion === 'StationName');
        if (d) {
          if (includes(d.value, stationName)) {
            defaultTimeWindowsForStation.backOffDuration =
              config.parameters.backOffDuration;
            defaultTimeWindowsForStation.calculationInterval =
              config.parameters.calculationInterval;
          }
        }
      }
    });
  });
  return defaultTimeWindowsForStation;
};

/**
 * Determines default monitor types for rollup station
 *
 * @param monitorTypesForRollupStationFromDisk configs from disk
 * @param defaultConfig config for default.json
 * @param stationName name of the selected station
 * @returns default list of monitor types for rollup station
 */
export const determineDefaultMonitorTypesForRollupStation = (
  monitorTypesForRollupStationFromDisk: MonitorsForRollupStationConfig[],
  defaultConfig: MonitorsForRollupStationConfig | undefined,
  stationName: string | null
): string[] => {
  if (!defaultConfig) {
    throw new Error(
      'default.json must exist with name default-soh-monitor-types-for-rollup-station'
    );
  }
  if (!stationName) {
    throw new Error('No station selected to save monitors for rollup station');
  }
  let defaultMonitorTypesForRollupStation: string[] =
    defaultConfig.parameters.sohMonitorTypesForRollup;
  monitorTypesForRollupStationFromDisk.forEach((config) => {
    config.constraints.forEach((constraint) => {
      if (constraint.constraintType === 'DEFAULT') {
        const d = config.constraints.find((c) => c.criterion === 'StationName');
        if (d) {
          if (includes(d.value, stationName)) {
            defaultMonitorTypesForRollupStation =
              config.parameters.sohMonitorTypesForRollup;
          }
        }
      }
    });
  });
  return defaultMonitorTypesForRollupStation;
};

/**
 * Uses attributes map to determine if app has any errors and returns
 * list of errors and reasons
 *
 * @param attributesMap Map of attributes with error input
 * @returns array of UserInputErrors
 */
export const determineAppErrorState = (
  attributesMap: Record<string, UserInputError>
): UserInputError[] => {
  let errors: UserInputError[] = [];
  const attributesNames = Object.keys(attributesMap);
  if (!attributesNames || attributesNames.length === 0) {
    return errors;
  }

  attributesNames.forEach((name) => {
    if (attributesMap[name].hasError) {
      errors.push({ hasError: true, reason: attributesMap[name].reason });
    }
  });

  return errors;
};

/**
 * Uses attributes map to determine if app has any errors and returns
 * list of errors and reasons
 *
 * @param attributesMap Map of attributes with error input
 * @param appSection string of app section
 * @returns array of UserInputErrors
 */
export const determineSectionContainsErrorState = (
  attributesMap: Record<string, UserInputError>,
  appSection: string
): UserInputError[] => {
  let errors: UserInputError[] = [];
  const attributesNames = Object.keys(attributesMap);
  if (!attributesNames || attributesNames.length === 0) {
    return errors;
  }

  attributesNames.forEach((name) => {
    if (
      attributesMap[name].hasError &&
      attributesMap[name].reason.includes(appSection)
    ) {
      errors.push({ hasError: true, reason: attributesMap[name].reason });
    }
  });

  return errors;
};

/**
 * Loops through data load and checks if any are false;
 *
 * @param dataLoadedRecord redux state record of data being loaded
 * @returns boolean
 */
export const determineAppHasLoadedAllData = (
  dataLoadedRecord: Record<string, boolean>
): boolean => {
  let hasLoaded = true;
  const dataNames = Object.keys(dataLoadedRecord);

  dataNames.forEach((name) => {
    if (dataLoadedRecord[name] === false) {
      hasLoaded = false;
    }
  });

  return hasLoaded;
};
