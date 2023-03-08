/* eslint-disable @typescript-eslint/no-unused-vars */
import { SohTypes } from '@gms/common-model';
import type { NumberCellRendererParams } from '@gms/ui-core-components';
import { UILogger } from '@gms/ui-util';

import type { StationStatisticsRow } from '../../../../../../src/ts/components/data-acquisition-ui/components/station-statistics/station-statistics-table-context';
import type { CellData } from '../../../../../../src/ts/components/data-acquisition-ui/shared/table/types';

const logger = UILogger.create('GMS_COMMON_DATA_TEST', process.env.GMS_COMMON_DATA_TEST);

export const stationGroups: SohTypes.StationSohCapabilityStatus = {
  groupName: 'Group',
  stationName: 'test',
  sohStationCapability: SohTypes.SohStatusSummary.GOOD
};
export const cellData: CellData = {
  value: 1,
  status: SohTypes.SohStatusSummary.GOOD,
  isContributing: true
};
export const row: StationStatisticsRow = {
  id: 'test',
  stationData: {
    stationName: 'test',
    stationStatus: SohTypes.SohStatusSummary.GOOD,
    stationCapabilityStatus: SohTypes.SohStatusSummary.GOOD
  },
  stationGroups: [stationGroups],
  channelEnvironment: cellData,
  channelLag: cellData,
  channelMissing: cellData,
  channelTimeliness: cellData,
  stationEnvironment: 1,
  stationLag: 2,
  stationMissing: 3,
  stationTimeliness: 4,
  needsAcknowledgement: false,
  needsAttention: true,
  location: 'cell'
};
export const cellRendererProps: NumberCellRendererParams = {
  value: 2.5,
  valueFormatted: 3,
  node: undefined,
  data: row,
  colDef: undefined,
  api: undefined,
  columnApi: undefined,
  context: {
    data: row
  },
  getValue: jest.fn(),
  setValue: jest.fn(),
  formatValue: jest.fn(),
  column: undefined,
  rowIndex: 1,
  refreshCell: undefined,
  eGridCell: undefined,
  eParentOfValue: undefined,
  registerRowDragger(
    rowDraggerElement: HTMLElement,
    dragStartPixels?: number,
    value?: string
  ): void {
    logger.info('registerRowDragger function not implemented');
  }
};
