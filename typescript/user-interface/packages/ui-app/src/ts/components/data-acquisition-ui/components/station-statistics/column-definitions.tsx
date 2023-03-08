import type {
  CellClickedEvent,
  ColumnDefinition,
  NumberValueGetter,
  RowClickedEvent,
  StringValueGetter
} from '@gms/ui-core-components';

import { messageConfig } from '~components/data-acquisition-ui/config/message-config';
import { compareCellValues } from '~components/data-acquisition-ui/shared/table/utils';

import {
  ChannelEnvironmentCellRenderer,
  ChannelLagCellRenderer,
  ChannelMissingCellRenderer,
  ChannelTimelinessCellRenderer,
  StationEnvironmentCellRenderer,
  StationLagCellRenderer,
  StationMissingCellRenderer,
  StationNameCellRenderer,
  StationTimelinessCellRenderer
} from './cell-renderers';

const defaultColumnWidthPx = 200;
const headerCellBlockClass = 'soh-header-cell';

/**
 * Station Statistics column definition
 */
export type StationStatisticsColumnDefinition = ColumnDefinition<
  { id: string },
  any,
  any,
  any,
  any
>;

/**
 * Table cell clicked event
 */
export type StationStatisticsCellClickedEvent = CellClickedEvent<
  { id: string },
  any,
  number | string
>;

/**
 * Table row clicked event
 */
export type StationStatisticsRowClickedEvent = RowClickedEvent<
  { id: string },
  any,
  number | string
>;

/** define the unique column identifiers, alphabetical for dropdown */
export enum Columns {
  Station = 'Station',
  ChannelIssues = 'Channel Issues',
  ChannelLag = 'Channel Lag',
  ChannelMissing = 'Channel Missing',
  ChannelTimeliness = 'Channel Timeliness',
  StationIssues = 'Station Issues',
  StationLag = 'Station Lag',
  StationMissing = 'Station Missing',
  StationTimeliness = 'Station Timeliness'
}

/**
 * Columns should by default appear Station Missing, Timeliness, Lag, Issues,
 * followed by Channel Missing, Timeliness, Lag, Issues
 */
export const defaultColumnDefinition: StationStatisticsColumnDefinition = {
  headerClass: [`${headerCellBlockClass}`, `${headerCellBlockClass}--neutral`],
  width: defaultColumnWidthPx,
  sortable: true,
  filter: true,
  disableStaticMarkupForHeaderComponentFramework: true,
  disableStaticMarkupForCellRendererFramework: true,
  comparator: compareCellValues
};

const stationNameColumnDef = (
  valueGetter: StringValueGetter
): StationStatisticsColumnDefinition => ({
  colId: Columns.Station,
  width: 150,
  headerName: 'Station',
  pinned: 'left',
  headerTooltip: messageConfig.tooltipMessages.stationStatistics.stationHeader,
  cellRendererFramework: StationNameCellRenderer,
  sort: 'asc',
  valueGetter,
  comparator: (a, b) => a?.localeCompare(b)
});

const stationMissingColumnDef = (
  valueGetter: NumberValueGetter
): StationStatisticsColumnDefinition => ({
  colId: Columns.StationMissing,
  width: 150,
  headerName: 'Station Missing (%)',
  headerTooltip: messageConfig.tooltipMessages.stationStatistics.stationMissingHeader,
  cellRendererFramework: StationMissingCellRenderer,
  valueGetter
});

const stationTimelinessColumnDef = (
  valueGetter: NumberValueGetter
): StationStatisticsColumnDefinition => ({
  colId: Columns.StationTimeliness,
  width: 165,
  headerName: 'Station Timeliness (s)',
  headerTooltip: messageConfig.tooltipMessages.stationStatistics.stationTimelinessHeader,
  cellRendererFramework: StationTimelinessCellRenderer,
  valueGetter
});

const stationLagColumnDef = (
  valueGetter: NumberValueGetter
): StationStatisticsColumnDefinition => ({
  colId: Columns.StationLag,
  width: 120,
  headerName: 'Station Lag (s)',
  headerTooltip: messageConfig.tooltipMessages.stationStatistics.stationLagHeader,
  cellRendererFramework: StationLagCellRenderer,
  valueGetter
});

const stationEnvironmentColumnDef = (
  valueGetter: NumberValueGetter
): StationStatisticsColumnDefinition => ({
  colId: Columns.StationIssues,
  width: 140,
  headerName: 'Station Issues (%)',
  headerTooltip: messageConfig.tooltipMessages.stationStatistics.stationEnvironmentHeader,
  cellRendererFramework: StationEnvironmentCellRenderer,
  valueGetter
});

const channelMissingColumnDef = (
  valueGetter: NumberValueGetter
): StationStatisticsColumnDefinition => ({
  colId: Columns.ChannelMissing,
  width: 155,
  headerName: 'Channel Missing (%)',
  headerTooltip: messageConfig.tooltipMessages.stationStatistics.channelMissingHeader,
  cellRendererFramework: ChannelMissingCellRenderer,
  valueGetter
});

const channelTimelinessColumnDef = (
  valueGetter: NumberValueGetter
): StationStatisticsColumnDefinition => ({
  colId: Columns.ChannelTimeliness,
  width: 165,
  headerName: 'Channel Timeliness (s)',
  headerTooltip: messageConfig.tooltipMessages.stationStatistics.channelTimelinessHeader,
  cellRendererFramework: ChannelTimelinessCellRenderer,
  valueGetter
});

const channelLagColumnDef = (
  valueGetter: NumberValueGetter
): StationStatisticsColumnDefinition => ({
  colId: Columns.ChannelLag,
  width: 130,
  headerName: 'Channel Lag (s)',
  headerTooltip: messageConfig.tooltipMessages.stationStatistics.channelLagHeader,
  cellRendererFramework: ChannelLagCellRenderer,
  valueGetter
});

const channelEnvironmentColumnDef = (
  valueGetter: NumberValueGetter
): StationStatisticsColumnDefinition => ({
  colId: Columns.ChannelIssues,
  width: 150,
  headerName: 'Channel Issues (%)',
  headerTooltip: messageConfig.tooltipMessages.stationStatistics.channelEnvironmentHeader,
  cellRendererFramework: ChannelEnvironmentCellRenderer,
  valueGetter
});

// !Order matters here!
export const buildColumnDefs = (
  stationNameValueGetter: StringValueGetter,
  stationMissingValueGetter: NumberValueGetter,
  stationTimelinessValueGetter: NumberValueGetter,
  stationLagValueGetter: NumberValueGetter,
  stationEnvironmentValueGetter: NumberValueGetter,
  channelMissingValueGetter: NumberValueGetter,
  channelTimelinessValueGetter: NumberValueGetter,
  channelLagValueGetter: NumberValueGetter,
  channelEnvironmentValueGetter: NumberValueGetter
): StationStatisticsColumnDefinition[] => {
  const columnDefinitions: StationStatisticsColumnDefinition[] = [];
  columnDefinitions.push(stationNameColumnDef(stationNameValueGetter));
  columnDefinitions.push(stationMissingColumnDef(stationMissingValueGetter));
  columnDefinitions.push(stationTimelinessColumnDef(stationTimelinessValueGetter));
  columnDefinitions.push(stationLagColumnDef(stationLagValueGetter));
  columnDefinitions.push(stationEnvironmentColumnDef(stationEnvironmentValueGetter));
  columnDefinitions.push(channelMissingColumnDef(channelMissingValueGetter));
  columnDefinitions.push(channelTimelinessColumnDef(channelTimelinessValueGetter));
  columnDefinitions.push(channelLagColumnDef(channelLagValueGetter));
  columnDefinitions.push(channelEnvironmentColumnDef(channelEnvironmentValueGetter));
  return columnDefinitions;
};
