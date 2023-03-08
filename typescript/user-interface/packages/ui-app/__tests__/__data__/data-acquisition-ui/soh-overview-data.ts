import { SohTypes } from '@gms/common-model';
import uniqueId from 'lodash/uniqueId';

import type { SohOverviewContextData } from '../../../src/ts/components/data-acquisition-ui/components/soh-overview/soh-overview-context';

export const channelSoh: SohTypes.ChannelSoh = {
  allSohMonitorValueAndStatuses: [
    {
      status: SohTypes.SohStatusSummary.GOOD,
      value: 1,
      valuePresent: true,
      monitorType: SohTypes.SohMonitorType.LAG,
      hasUnacknowledgedChanges: false,
      contributing: false,
      thresholdMarginal: 1,
      thresholdBad: 10,
      quietUntilMs: 1
    }
  ],
  channelName: 'AAA111',
  channelSohStatus: SohTypes.SohStatusSummary.GOOD
};

export const testStationSoh: SohTypes.UiStationSoh = {
  id: uniqueId(),
  uuid: uniqueId(),
  stationName: 'Test',
  sohStatusSummary: SohTypes.SohStatusSummary.MARGINAL,
  needsAcknowledgement: true,
  needsAttention: true,
  statusContributors: [],
  allStationAggregates: [],
  stationGroups: [
    {
      groupName: 'GroupA',
      stationName: 'Test',
      sohStationCapability: SohTypes.SohStatusSummary.BAD
    }
  ],
  time: 123456789,
  channelSohs: [channelSoh]
};

export const stationAndStationGroupSohStatus: SohTypes.StationAndStationGroupSoh = {
  stationSoh: [
    {
      stationName: 'H05N',
      id: '1',
      uuid: '1',
      sohStatusSummary: SohTypes.SohStatusSummary.GOOD,
      needsAcknowledgement: true,
      needsAttention: true,
      allStationAggregates: [],
      stationGroups: [
        {
          groupName: 'Group 4',
          stationName: 'H05N',
          sohStationCapability: SohTypes.SohStatusSummary.GOOD
        }
      ],
      statusContributors: [
        {
          contributing: true,
          statusSummary: SohTypes.SohStatusSummary.GOOD,
          type: SohTypes.SohMonitorType.LAG,
          value: 5,
          valuePresent: true
        }
      ],
      channelSohs: [],
      time: 2
    },
    {
      stationName: 'H06N',
      id: '2',
      uuid: '2',
      sohStatusSummary: SohTypes.SohStatusSummary.MARGINAL,
      needsAcknowledgement: true,
      needsAttention: true,
      allStationAggregates: [],
      stationGroups: [
        {
          groupName: 'Group 4',
          stationName: 'H06N',
          sohStationCapability: SohTypes.SohStatusSummary.GOOD
        }
      ],
      statusContributors: [
        {
          contributing: true,
          statusSummary: SohTypes.SohStatusSummary.GOOD,
          type: SohTypes.SohMonitorType.LAG,
          value: 5,
          valuePresent: true
        }
      ],
      channelSohs: [],
      time: 2
    },
    {
      stationName: 'H07N',
      id: '3',
      uuid: '3',
      sohStatusSummary: SohTypes.SohStatusSummary.BAD,
      needsAcknowledgement: true,
      needsAttention: true,
      allStationAggregates: [],
      stationGroups: [
        {
          groupName: 'Group 4',
          stationName: 'H07N',
          sohStationCapability: SohTypes.SohStatusSummary.GOOD
        }
      ],
      statusContributors: [
        {
          contributing: true,
          statusSummary: SohTypes.SohStatusSummary.GOOD,
          type: SohTypes.SohMonitorType.LAG,
          value: 5,
          valuePresent: true
        }
      ],
      channelSohs: [],
      time: 2
    },
    {
      stationName: 'H08N',
      id: '4',
      uuid: '4',
      stationGroups: [
        {
          groupName: 'Group 4',
          stationName: 'H08N',
          sohStationCapability: SohTypes.SohStatusSummary.MARGINAL
        }
      ],
      sohStatusSummary: SohTypes.SohStatusSummary.GOOD,
      needsAcknowledgement: true,
      needsAttention: true,
      allStationAggregates: [],
      statusContributors: [
        {
          contributing: true,
          statusSummary: SohTypes.SohStatusSummary.GOOD,
          type: SohTypes.SohMonitorType.ENV_AUTHENTICATION_SEAL_BROKEN,
          value: 5,
          valuePresent: true
        }
      ],
      channelSohs: [],
      time: 2
    }
  ],
  stationGroups: [
    {
      stationGroupName: 'Group 4',
      time: 0,
      groupCapabilityStatus: SohTypes.SohStatusSummary.BAD,
      id: '1',
      priority: 1
    }
  ],
  isUpdateResponse: false
};

export const contextValues: SohOverviewContextData = {
  stationGroupSoh: stationAndStationGroupSohStatus.stationGroups,
  stationSoh: stationAndStationGroupSohStatus.stationSoh,
  acknowledgeSohStatus: jest.fn(),
  glContainer: undefined,
  quietTimerMs: 1,
  updateIntervalSecs: 2,
  selectedStationIds: ['H05N'],
  setSelectedStationIds: jest.fn(),
  sohStationStaleTimeMS: 30000
};
