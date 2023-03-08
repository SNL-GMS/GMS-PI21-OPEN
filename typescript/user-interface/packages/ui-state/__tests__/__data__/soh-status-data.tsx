import { SohTypes } from '@gms/common-model';
import { SohStatusSummary } from '@gms/common-model/lib/soh/types';

import type { SohStatus } from '../../src/ts/app/state/data-acquisition/types';

export const sohStatus: SohStatus = {
  lastUpdated: 0,
  loading: false,
  isStale: false,
  stationAndStationGroupSoh: {
    isUpdateResponse: false,
    stationGroups: ['ALL1', 'CD1.1', 'GSE'].map(groupName => ({
      groupCapabilityStatus: SohStatusSummary.BAD,
      id: groupName,
      priority: 1,
      stationGroupName: groupName,
      time: 1657216381512
    })),
    stationSoh: ['ABC', 'TEST'].map((stationName, index) => ({
      id: stationName,
      uuid: stationName,
      needsAcknowledgement: !!index,
      needsAttention: !!index,
      sohStatusSummary: undefined,
      stationGroups: [],
      statusContributors: [],
      time: undefined,
      stationName,
      allStationAggregates: [],
      channelSohs: [
        {
          channelName: 'adsf',
          channelSohStatus: undefined,
          allSohMonitorValueAndStatuses: [
            {
              monitorType: SohTypes.SohMonitorType.LAG,
              value: 10,
              valuePresent: true,
              status: SohTypes.SohStatusSummary.GOOD,
              hasUnacknowledgedChanges: !!index,
              contributing: false,
              quietUntilMs: 1,
              thresholdBad: 3,
              thresholdMarginal: 3
            },
            {
              monitorType: SohTypes.SohMonitorType.LAG,
              value: 11,
              valuePresent: true,
              status: SohTypes.SohStatusSummary.GOOD,
              hasUnacknowledgedChanges: !!index,
              contributing: false,
              quietUntilMs: 1,
              thresholdBad: 3,
              thresholdMarginal: 3
            }
          ]
        }
      ]
    }))
  }
};
