import { MonitorTypeConfig } from '../../coi-types/monitor-types';
import {
  ChannelMode,
  ChannelsByMonitorType,
} from '../../state/retrieve-channels-by-monitor-type';

/**
 * First checks if any of the channel modes for the monitors is 'USE_ALL'
 * if so then at least one monitor has all channels so should be selected
 *
 * Next if none have 'USE_ALL', loops through to see if channel is found in any
 * of the monitors, if so then should be selected, if channel is not found in
 * any of the monitors, will not be selected
 *
 * @param channelsForMonitors record of monitors with channels and channels mode
 * @param channels list of all channels
 * @returns selected channels
 */
export const determineSelectedChannels = (
  channelsForMonitors: ChannelsByMonitorType | undefined,
  channels: string[]
): string[] => {
  if (!channelsForMonitors || !channels) {
    return [];
  }

  let isAllSelected = false;
  Object.keys(channelsForMonitors).forEach((monitor) => {
    if (channelsForMonitors[monitor].channelsMode === ChannelMode.USE_ALL) {
      isAllSelected = true;
    }
  });

  if (isAllSelected) {
    return channels;
  }

  let selectedChannels: string[] = [];
  channels.forEach((channel) => {
    Object.keys(channelsForMonitors).forEach((monitor) => {
      if (
        channelsForMonitors[monitor].channels.includes(channel) &&
        !selectedChannels.includes(channel)
      ) {
        selectedChannels.push(channel);
      }
    });
  });
  return selectedChannels;
};

/**
 * Determines if any channel is included in any of the monitor channel overrides
 * Used to know if need to update selectedChannels in state since all individual
 * Channels at the monitor level have been set to not included
 *
 * @param monitorTypesRollup rollup data in redux
 * @param clickedChannel clicked channel at the monitor level
 * @returns boolean if channel is found included in any channel override
 */
export const isSelectedChannelIncludedInAnyMonitor = (
  monitorTypesRollup: MonitorTypeConfig[],
  clickedChannel: string
): boolean => {
  let isSelectedChannelIncluded = false;
  monitorTypesRollup.forEach((monitor) => {
    if (monitor.channelOverrides) {
      monitor.channelOverrides.forEach((override) => {
        if (override.name === clickedChannel && override.isIncluded === true) {
          isSelectedChannelIncluded = true;
        }
      });
    }
  });
  return isSelectedChannelIncluded;
};
