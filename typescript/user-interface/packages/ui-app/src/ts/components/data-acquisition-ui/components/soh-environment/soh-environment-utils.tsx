import { SohTypes } from '@gms/common-model';

import type { DataReceivedStatus } from '~components/data-acquisition-ui/shared/table/utils';
import { getWorseStatus } from '~components/data-acquisition-ui/shared/table/utils';
import { FilterableSOHTypes } from '~components/data-acquisition-ui/shared/toolbars/soh-toolbar';
import { convertSohMonitorTypeToAceiMonitorType } from '~components/data-acquisition-ui/shared/utils';

import type { EnvironmentalSoh, EnvironmentTableRow } from './types';

export const getPerChannelEnvRollup = (channel: SohTypes.ChannelSoh): SohTypes.SohStatusSummary =>
  channel.allSohMonitorValueAndStatuses
    .filter(s => (SohTypes.isEnvironmentalIssue(s.monitorType) && s ? s.contributing : false))
    .map(mvs => mvs.status)
    .reduce(getWorseStatus, SohTypes.SohStatusSummary.NONE);

/**
 * Returns the environment table rows based on the channels SOH data
 *
 * @param channel the channel SOH data
 */
export const getEnvironmentTableRows = (
  channels: SohTypes.ChannelSoh[],
  selectedChannelMonitorPairs: SohTypes.ChannelMonitorPair[],
  aceiType: SohTypes.AceiType
): EnvironmentTableRow[] =>
  Object.keys(SohTypes.SohMonitorType)
    .filter(key => SohTypes.isEnvironmentalIssue(SohTypes.SohMonitorType[key]))
    .map(key => {
      const mvsForMonitorType = channels.map(chan => {
        const mvs: SohTypes.SohMonitorValueAndStatus = chan.allSohMonitorValueAndStatuses.find(
          entryMVS => entryMVS.monitorType === SohTypes.SohMonitorType[key]
        );
        return {
          channelName: chan.channelName,
          isContributing: mvs ? mvs.contributing : false,
          mvs
        };
      });
      return {
        id: key,
        monitorType: SohTypes.SohMonitorType[key],
        monitorIsSelected:
          convertSohMonitorTypeToAceiMonitorType(SohTypes.SohMonitorType[key]) === aceiType,
        monitorStatus: mvsForMonitorType
          .filter(mvs => mvs.isContributing)
          .map(mvs => mvs.mvs?.status)
          .reduce(getWorseStatus, SohTypes.SohStatusSummary.NONE),
        valueAndStatusByChannelName: new Map<string, EnvironmentalSoh>(
          mvsForMonitorType.map(a => [
            a.channelName,
            {
              value: a.mvs && a.mvs.valuePresent ? a.mvs.value : undefined,
              status: a.mvs?.status,
              monitorTypes: a.mvs?.monitorType,
              channelName: a.channelName,
              quietTimingInfo: {
                quietUntilMs: a.mvs?.quietUntilMs,
                quietDurationMs: a.mvs?.quietDurationMs
              },
              hasUnacknowledgedChanges: a.mvs?.hasUnacknowledgedChanges,
              isContributing: a.isContributing,
              isSelected:
                selectedChannelMonitorPairs.find(
                  (cm: SohTypes.ChannelMonitorPair) =>
                    cm.channelName === a.channelName &&
                    cm.monitorType === SohTypes.SohMonitorType[key]
                ) !== undefined
            }
          ])
        )
      };
    });

/**
 * Returns the Channel SOH data that should be displayed based on the channel statuses
 * that should be displayed.
 *
 * @param channelSohs the list of channel SOH data
 * @param channelStatusesToDisplay the statuses that are set to visible
 */
export const getChannelSohToDisplay = (
  channelSohs: SohTypes.ChannelSoh[],
  channelStatusesToDisplay: Record<FilterableSOHTypes, boolean>
): SohTypes.ChannelSoh[] =>
  channelSohs.filter(
    channel => channelStatusesToDisplay[FilterableSOHTypes[getPerChannelEnvRollup(channel)]]
  );

export const getChannelColumnHeaderClass = (
  baseClass: string,
  status: SohTypes.SohStatusSummary,
  dataReceivedRollup: DataReceivedStatus
): string[] => {
  const classes: string[] = [baseClass];
  classes.push(`${baseClass}--${status.toLocaleLowerCase()}`);
  classes.push(`${baseClass}--${dataReceivedRollup.toLowerCase()}`);
  return classes;
};
