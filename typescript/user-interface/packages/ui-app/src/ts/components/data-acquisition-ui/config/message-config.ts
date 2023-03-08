import { millisToStringWithMaxPrecision } from '@gms/common-util';

export interface MessageConfig {
  readonly labels: {
    readonly sohToolbar: {
      readonly filterByStationGroup: string;
      readonly filterStatuses: string;
      readonly filterMonitorsByStatus: string;
      readonly updateTimeDisplay: string;
      readonly updateTimeDisplayIssue: (staleMs: number) => string;
      readonly interval: string;
    };
    readonly environmentalSubtitle: string;
    readonly lagSubtitle: string;
    readonly timelinessSubtitle: string;
    readonly missingSubtitle: string;
    readonly lagTrendsSubtitle: string;
    readonly missingTrendsSubtitle: string;
    readonly timelinessTrendsSubtitle: string;
    decimationDescription(value: number): string;
  };
  readonly table: {
    readonly noDataMessage: string;
  };
  readonly tooltipMessages: {
    readonly sohToolbar: {
      readonly filerByStationGroup: string;
      readonly selectStatuses: string;
      readonly lastUpdateTime: string;
      readonly interval: string;
    };
    readonly stationStatistics: {
      readonly nonContributingCell: string;
      readonly nullCell: string;
      readonly notReceivedCell: string;
      readonly channelTimelinessHeader: string;
      readonly channelEnvironmentHeader: string;
      readonly channelLagHeader: string;
      readonly channelMissingHeader: string;
      readonly stationTimelinessHeader: string;
      readonly stationEnvironmentHeader: string;
      readonly stationLagHeader: string;
      readonly stationMissingHeader: string;
      readonly stationHeader: string;
      readonly topCount: string;
      readonly bottomCount: string;
      readonly stationCell: string;
      readonly badge: string;
    };
  };
}
export const messageConfig: MessageConfig = {
  labels: {
    sohToolbar: {
      filterByStationGroup: 'Filter by Station Group',
      filterStatuses: 'Filter by Status',
      filterMonitorsByStatus: 'Filter Monitors By Status',
      updateTimeDisplay: 'Last Updated',
      updateTimeDisplayIssue: (staleMs: number) =>
        `The data currently being displayed is more than ${millisToStringWithMaxPrecision(
          staleMs,
          1
        )} old`,
      interval: 'Update Interval'
    },
    environmentalSubtitle: 'Current percent environmental issues per channel',
    lagSubtitle: 'Current lag per channel',
    timelinessSubtitle: 'Current timeliness per channel',
    missingSubtitle: 'Current percent missing data per channel',
    lagTrendsSubtitle: 'Historical trends for lag',
    missingTrendsSubtitle: 'Historical trends for missing',
    timelinessTrendsSubtitle: 'Historical trends for timeliness',
    decimationDescription: (value: number) => `Displaying ${value}% of available points`
  },
  table: {
    noDataMessage: 'No SOH to display'
  },
  tooltipMessages: {
    sohToolbar: {
      filerByStationGroup: 'Set which station groups appear',
      selectStatuses: 'Set which statuses appear in the lower bin',
      lastUpdateTime: 'Most recent SOH data received',
      interval: 'Interval at which SOH data is processed'
    },
    stationStatistics: {
      nonContributingCell: 'This value did not contribute to the SOH status for this station',
      nullCell: 'This value was unknown in the latest SOH status update',
      notReceivedCell: 'This value was not received in the latest SOH status update',
      channelTimelinessHeader:
        'Longest time in seconds since the time of the latest data sample that has been acquired on a single channel (now - latest data sample time that has been acquired, worst channel)',
      channelEnvironmentHeader:
        "Largest percentage of 'bad' indicators on a single channel/environmental monitor pair over a configurable time window",
      channelLagHeader:
        'Longest transmission time for received data samples on a single channel over a configurable time window (reception time - latest data sample time, worst channel)',
      channelMissingHeader:
        'Largest percentage of missing data on a single channel over a configurable time window (worst channel)',
      stationTimelinessHeader:
        'Time in seconds since the time of the latest data sample that has been acquired on any channel (now - latest data sample time that has been acquired on any channel)',
      stationEnvironmentHeader:
        "Percentage of 'bad' indicators across all channels for configurable time window",
      stationLagHeader:
        'Average transmission time for received data samples across all channels over a configurable time window, e.g., last 10 minutes (reception time - latest data sample time)',
      stationMissingHeader:
        'Total percentage of missing data across all channels over a configurable time window',
      stationHeader: 'Name of the station',
      topCount: 'BAD group status count',
      bottomCount: 'MARGINAL group status count',
      stationCell: 'Capability Rollup - ',
      badge: 'Worst of Rollup - '
    }
  }
};
