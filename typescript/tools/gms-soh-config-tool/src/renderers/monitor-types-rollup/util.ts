import produce from 'immer';
import {
  ChannelOverrides,
  MonitorTypeConfig,
  ThresholdsMap,
} from '../../coi-types/monitor-types';
import {
  ChannelMode,
  ChannelsByMonitorType,
} from '../../state/retrieve-channels-by-monitor-type';
import { ChannelWithThresholds } from '../../state/retrieve-thresholds-for-channels-for-monitor-in-station';

/**
 * Determines the value to use, if no value in loading state and sets to ''
 *
 * @param thresholdMap map of threshold data where key is name of attribute for threshold
 * @param thresholdKey name of the monitor
 * @returns value to display in text field
 */
export const determineThresholdValue = (
  thresholdMap: Record<
    string,
    {
      goodThreshold: string | number;
      marginalThreshold: string | number;
    }
  >,
  thresholdKey: string
): {
  goodThreshold: string | number;
  marginalThreshold: string | number;
} => {
  if (thresholdMap[thresholdKey]) {
    return thresholdMap[thresholdKey];
  }
  return {
    goodThreshold: 'null',
    marginalThreshold: 'null',
  };
};

/**
 * Uses the channelsWithThresholds returned from the query to update
 * the existing config and add thm to the channel overrides
 *
 * @param monitorTypeForRollup current config to update
 * @param channelsWithThresholds channels with thresholds from query
 * @param monitorType name of the monitor
 * @returns updated config with channelsWithThresholds
 */
export const addChannelsWithThresholdsForMonitorToConfig = (
  monitorTypeForRollup: MonitorTypeConfig[],
  channelsWithThresholds: ChannelWithThresholds[],
  monitorType: string
): MonitorTypeConfig[] => {
  if (!monitorTypeForRollup) {
    throw new Error('No monitor type rollup for expanded monitor');
  }

  if (!channelsWithThresholds) {
    throw new Error('Query for channels with thresholds for monitor failed');
  }
  return produce(monitorTypeForRollup, (draft) => {
    const monitorTypeRollup = draft.find(
      (rollup) => rollup.name === monitorType
    );
    if (
      channelsWithThresholds &&
      monitorTypeRollup &&
      monitorTypeRollup.channelOverrides
    ) {
      monitorTypeRollup.channelOverrides.forEach((override) => {
        override.goodThreshold =
          channelsWithThresholds.find(
            (channelWithThreshold) =>
              channelWithThreshold.channelName === override.name
          )?.goodThreshold ?? 0;
        override.marginalThreshold =
          channelsWithThresholds.find(
            (channelWithThreshold) =>
              channelWithThreshold.channelName === override.name
          )?.marginalThreshold ?? 0;
      });
    }
  });
};

/**
 * Converts query result to a threshold map
 *
 * @param channelsWithThresholds result from query
 * @returns a threshold map
 */
export const convertChannelsWithThresholdsToThresholdsMap = (
  channelsWithThresholds: ChannelWithThresholds[] | undefined
): ThresholdsMap => {
  const thresholdsMap: ThresholdsMap = {};
  if (!channelsWithThresholds) return thresholdsMap;

  channelsWithThresholds.forEach((channelWithThreshold) => {
    thresholdsMap[channelWithThreshold.channelName] = {
      goodThreshold: channelWithThreshold.goodThreshold,
      marginalThreshold: channelWithThreshold.marginalThreshold,
    };
  });

  return thresholdsMap;
};

/**
 * Converts channel overrides with thresholds from monitor rollup config to threshold map
 *
 * @param channelOverridesWithThresholds channel overrides from config with thresholds
 * @returns a threshold map
 */
export const convertChannelOverridesWithThresholdsToThresholdsMap = (
  channelOverridesWithThresholds: ChannelOverrides[] | undefined
): ThresholdsMap => {
  const thresholdsMap: ThresholdsMap = {};
  if (!channelOverridesWithThresholds) return thresholdsMap;

  channelOverridesWithThresholds.forEach((channelOverrideWithThreshold) => {
    thresholdsMap[channelOverrideWithThreshold.name] = {
      goodThreshold: channelOverrideWithThreshold.goodThreshold ?? '',
      marginalThreshold: channelOverrideWithThreshold.marginalThreshold ?? '',
    };
  });

  return thresholdsMap;
};

/**
 * Updates good threshold value for a channel in a monitor type
 *
 * @param monitorType name of the monitor
 * @param channelName name of the channel
 * @param value user input value
 * @param monitorTypeConfigs current monitor type configs
 * @returns new monitor type configs with updated threshold value
 */
export const updateGoodThresholdForChannel = (
  monitorType: string,
  channelName: string,
  value: string | number,
  monitorTypeConfigs: MonitorTypeConfig[]
): MonitorTypeConfig[] => {
  return produce(monitorTypeConfigs, (draft) => {
    const monitorTypeChannelOverrides = draft.find(
      (config) => config.name === monitorType
    )?.channelOverrides;

    if (!monitorTypeChannelOverrides) {
      throw new Error('no channel overrides');
    }

    const channelOverride = monitorTypeChannelOverrides.find(
      (override) => override.name === channelName
    );

    if (!channelOverride) {
      throw new Error('no channel override');
    }
    channelOverride.goodThreshold = value;
  });
};

/**
 * Updates marginal threshold value for a channel in a monitor type
 *
 * @param monitorType name of the monitor
 * @param channelName name of the channel
 * @param value user input value
 * @param monitorTypeConfigs current monitor type configs
 * @returns new monitor type configs with updated threshold value
 */
export const updateMarginalThresholdForChannel = (
  monitorType: string,
  channelName: string,
  value: string | number,
  monitorTypeConfigs: MonitorTypeConfig[]
): MonitorTypeConfig[] => {
  return produce(monitorTypeConfigs, (draft) => {
    const monitorTypeChannelOverrides = draft.find(
      (config) => config.name === monitorType
    )?.channelOverrides;

    if (!monitorTypeChannelOverrides) {
      throw new Error('no channel overrides');
    }

    const channelOverride = monitorTypeChannelOverrides.find(
      (override) => override.name === channelName
    );

    if (!channelOverride) {
      throw new Error('no channel override');
    }
    channelOverride.marginalThreshold = value;
  });
};

/**
 * Updates good threshold value for a monitor type
 *
 * @param monitorType name of the monitor
 * @param value user input value
 * @param monitorTypeConfigs current monitor type configs
 * @returns new monitor type configs with updated threshold value
 */
export const updateGoodThresholdForMonitor = (
  monitorType: string,
  value: string | number,
  monitorTypeConfigs: MonitorTypeConfig[]
): MonitorTypeConfig[] => {
  return produce(monitorTypeConfigs, (draft) => {
    const monitorTypeConfig = draft.find(
      (config) => config.name === monitorType
    );

    if (!monitorTypeConfig) {
      throw new Error('no monitor type found');
    }

    monitorTypeConfig.goodThreshold = value;

    if (monitorTypeConfig.channelOverrides) {
      monitorTypeConfig.channelOverrides.forEach((channel) => {
        channel.goodThreshold = value;
      });
    }
  });
};

/**
 * Updates marginal threshold value for a monitor type
 *
 * @param monitorType name of the monitor
 * @param value user input value
 * @param monitorTypeConfigs current monitor type configs
 * @returns new monitor type configs with updated threshold value
 */
export const updateMarginalThresholdForMonitor = (
  monitorType: string,
  value: string | number,
  monitorTypeConfigs: MonitorTypeConfig[]
): MonitorTypeConfig[] => {
  return produce(monitorTypeConfigs, (draft) => {
    const monitorTypeConfig = draft.find(
      (config) => config.name === monitorType
    );

    if (!monitorTypeConfig) {
      throw new Error('no monitor type found');
    }

    monitorTypeConfig.marginalThreshold = value;

    if (monitorTypeConfig.channelOverrides) {
      monitorTypeConfig.channelOverrides.forEach((channel) => {
        channel.marginalThreshold = value;
      });
    }
  });
};

/**
 * Updates the isIncluded attribute for a monitors channel override entry
 *
 * @param monitorTypeConfigMapToUpdate current config array
 * @param monitorType name of the monitor for the channel being updated
 * @param includedChannels list of included channels
 * @param clickedChannel channel being updated
 * @returns new and updated MonitorTypeConfig[]
 */
export const updateMonitorChannelOverrides = (
  monitorTypeConfigMapToUpdate: MonitorTypeConfig[],
  monitorType: string,
  includedChannels: string[],
  clickedChannel: string
) => {
  return produce(monitorTypeConfigMapToUpdate, (draft) => {
    const foundMonitorType = draft.find((mt) => mt.name === monitorType);
    if (foundMonitorType) {
      const channelToUpdate = foundMonitorType.channelOverrides?.find(
        (channelOverride) => channelOverride.name === clickedChannel
      );
      if (!channelToUpdate) {
        throw new Error('no channel to update');
      }
      channelToUpdate.isIncluded = includedChannels.includes(clickedChannel);
    }
  });
};

/**
 * ! Only update the specific pieces that have changed.
 *
 * @param selectedMonitorTypes
 * @param newListOfSelected
 * @param supportedMonitorTypes
 * @param monitorThresholdsMap current map of form data
 * @returns Array of monitorTypeConfig to be reflected in form data
 */
export const getUpdatedMonitorTypes = (
  selectedMonitorTypes: MonitorTypeConfig[],
  newListOfSelected: string[],
  supportedMonitorTypes: string[],
  monitorThresholdsMap: Record<
    string,
    {
      goodThreshold: string | number;
      marginalThreshold: string | number;
      isIncluded: boolean;
      channelOverrides: ChannelOverrides[];
    }
  >
): MonitorTypeConfig[] =>
  produce(selectedMonitorTypes, (draft) => {
    supportedMonitorTypes.forEach((mtName) => {
      const goodThreshold = monitorThresholdsMap[mtName].goodThreshold;
      const marginalThreshold = monitorThresholdsMap[mtName].marginalThreshold;
      const channelOverrides = monitorThresholdsMap[mtName].channelOverrides;
      const foundMT = draft?.find((mt) => mt.name === mtName);
      if (foundMT) {
        foundMT.isIncluded = !!newListOfSelected.find((smt) => smt === mtName);
        foundMT.name = mtName;
        foundMT.goodThreshold = goodThreshold;
        foundMT.marginalThreshold = marginalThreshold;
        foundMT.channelOverrides = channelOverrides;
      } else {
        draft.push({
          name: mtName,
          isIncluded: !!newListOfSelected.find((smt) => smt === mtName),
          goodThreshold,
          marginalThreshold,
          channelOverrides,
        });
      }
    });
  });

/**
 * Converts map of thresholds to array of MonitorTypeConfig
 *
 * @param monitorTypeConfigMapToUpdate the map of monitorThresholds
 * @param selectedMonitorTypes
 * @returns array of monitorTypeConfig
 */
export const convertThresholdMapToThresholdConfig = (
  monitorTypeConfigMapToUpdate: Record<
    string,
    {
      goodThreshold: string | number;
      marginalThreshold: string | number;
      isIncluded: boolean;
      channelOverrides: ChannelOverrides[];
    }
  >,
  selectedMonitorTypes: string[]
): MonitorTypeConfig[] => {
  let newMonitorTypesForRollupStation: MonitorTypeConfig[] = [];
  Object.keys(monitorTypeConfigMapToUpdate).forEach((monitorType) => {
    const monitorTypeForRollupStation: MonitorTypeConfig = {
      name: monitorType,
      goodThreshold: monitorTypeConfigMapToUpdate[monitorType].goodThreshold,
      marginalThreshold:
        monitorTypeConfigMapToUpdate[monitorType].marginalThreshold,
      isIncluded:
        selectedMonitorTypes.find((type) => type === monitorType) !== undefined,
      channelOverrides:
        monitorTypeConfigMapToUpdate[monitorType].channelOverrides,
    };
    newMonitorTypesForRollupStation.push(monitorTypeForRollupStation);
  });
  return newMonitorTypesForRollupStation;
};

/**
 * Converts array of monitorTypeConfig to monitorTypeConfigMap
 *
 * @param monitorTypeConfigs array to convert to monitorTypeConfigMap
 * @returns Monitor Type Threshold Map key on station
 */
export const convertThresholdConfigToThresHoldMap = (
  monitorTypeConfigs: MonitorTypeConfig[]
): Record<
  string,
  {
    goodThreshold: string | number;
    marginalThreshold: string | number;
    isIncluded: boolean;
    channelOverrides: ChannelOverrides[];
  }
> => {
  if (!monitorTypeConfigs) {
    return {};
  }
  const newThresholdMap: Record<
    string,
    {
      goodThreshold: string | number;
      marginalThreshold: string | number;
      isIncluded: boolean;
      channelOverrides: ChannelOverrides[];
    }
  > = {};
  monitorTypeConfigs.forEach((monitorThreshold) => {
    newThresholdMap[monitorThreshold.name] = {
      goodThreshold: monitorThreshold.goodThreshold ?? '',
      marginalThreshold: monitorThreshold.marginalThreshold ?? '',
      isIncluded: monitorThreshold.isIncluded ?? false,
      channelOverrides: monitorThreshold.channelOverrides ?? [],
    };
  });
  return newThresholdMap;
};

/**
 * Checks if mode is 'USE_ALL' if so then is included
 * If not 'USE_ALL' then checks to see if the channel name is in the list of channels
 * if found channel is included else not included
 *
 * @param resolvedChannelsByMonitorType record of monitor types with  channel mode and channel data
 * @param monitorType name of the monitor
 * @param channelName name of the channel
 * @returns
 */
export const isChannelIncludedForMonitorType = (
  resolvedChannelsByMonitorType: ChannelsByMonitorType | undefined,
  monitorType: string,
  channelName: string
): boolean => {
  if (!resolvedChannelsByMonitorType || !monitorType || !channelName) {
    throw new Error(
      `Missing channel by monitor type data for monitor: ${monitorType} and channel: ${channelName}`
    );
  }

  if (
    resolvedChannelsByMonitorType[monitorType]?.channelsMode ===
    ChannelMode.USE_ALL
  ) {
    return true;
  }

  if (
    resolvedChannelsByMonitorType[monitorType]?.channels.includes(channelName)
  ) {
    return true;
  }
  return false;
};

/**
 * Converts array of monitorTypeConfig to thresholdMap
 *
 * @param monitorTypeConfigs array to convert to monitorTypeConfigMap
 * @returns ThresholdMap
 */
export const convertMonitorTypeConfigToThresholdMap = (
  monitorTypeConfigs: MonitorTypeConfig[]
): ThresholdsMap => {
  if (!monitorTypeConfigs) {
    return {};
  }
  const newThresholdMap: ThresholdsMap = {};
  monitorTypeConfigs.forEach((monitor) => {
    newThresholdMap[monitor.name] = {
      goodThreshold: monitor.goodThreshold ?? '',
      marginalThreshold: monitor.marginalThreshold ?? '',
    };
  });
  return newThresholdMap;
};
