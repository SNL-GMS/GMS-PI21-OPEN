import { SohTypes } from '@gms/common-model';
import cloneDeep from 'lodash/cloneDeep';
import uniqueId from 'lodash/uniqueId';

import type { StationStatisticsRow } from '../../../src/ts/components/data-acquisition-ui/components/station-statistics/station-statistics-table-context';

const numRows = 100;

export const rowTemplate: StationStatisticsRow = {
  channelEnvironment: {
    isContributing: true,
    status: SohTypes.SohStatusSummary.GOOD,
    value: 12.34
  },
  channelLag: {
    isContributing: true,
    status: SohTypes.SohStatusSummary.MARGINAL,
    value: 2.34
  },
  channelMissing: {
    isContributing: true,
    status: SohTypes.SohStatusSummary.BAD,
    value: 98.76
  },
  channelTimeliness: {
    isContributing: true,
    status: SohTypes.SohStatusSummary.GOOD,
    value: 6.87
  },
  stationEnvironment: 76.29,
  stationLag: 2.34,
  stationMissing: 98.76,
  stationTimeliness: 5.62,
  id: uniqueId('rowTemplate_'),
  needsAcknowledgement: true,
  needsAttention: true,
  stationData: {
    stationName: uniqueId('Station '),
    stationStatus: SohTypes.SohStatusSummary.BAD,
    stationCapabilityStatus: SohTypes.SohStatusSummary.BAD
  },
  stationGroups: [
    {
      groupName: 'GroupA',
      sohStationCapability: SohTypes.SohStatusSummary.BAD,
      stationName: 'Station'
    },
    {
      groupName: 'GroupB',
      sohStationCapability: SohTypes.SohStatusSummary.BAD,
      stationName: 'Station'
    }
  ],
  location: 'cell'
};

export const tableData = (() => {
  const testRows: StationStatisticsRow[] = [];
  for (let i = 0; i < numRows; i += 1) {
    const theRow = cloneDeep(rowTemplate);
    theRow.id = uniqueId('row_');
    theRow.stationData = {
      stationName: uniqueId('Station '),
      stationStatus: SohTypes.SohStatusSummary.BAD,
      stationCapabilityStatus: SohTypes.SohStatusSummary.BAD
    };
    theRow.channelEnvironment.value = i;
    theRow.channelLag.value = i;
    theRow.channelMissing.value = i;
    theRow.channelTimeliness.value = i;
    theRow.stationEnvironment = i;
    theRow.stationLag = i;
    theRow.stationMissing = i;
    theRow.stationTimeliness = i;
    testRows.push(theRow);
  }
  return testRows;
})();
