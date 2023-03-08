import type { SohTypes } from '@gms/common-model';
import { stripOutFirstOccurrence } from '@gms/common-util';
import type { ColumnDefinition, ValueGetterParams } from '@gms/ui-core-components';
import orderBy from 'lodash/orderBy';

import { compareCellValues } from '~components/data-acquisition-ui/shared/table/utils';

import { ChannelCellRenderer } from './cell-renderers/channel-cell-renderer';
import { MonitorTypeCellRenderer } from './cell-renderers/monitor-type-cell-renderer';
import { getChannelColumnHeaderClass } from './soh-environment-utils';
import type { EnvironmentTableContext } from './types';

const monitorTypeWidthPx = 200;

/** The header name for the monitor type column */
export const headerNameMonitorType = 'Monitor Type';

export type EnvironmentColumnDefinition = ColumnDefinition<
  { id: string },
  EnvironmentTableContext,
  any,
  any,
  any
>;

export type MonitorTypeColumnDefinition = ColumnDefinition<
  { id: string },
  EnvironmentTableContext,
  string,
  any,
  any
>;

export type ChannelColumnDefinition = ColumnDefinition<
  { id: string },
  EnvironmentTableContext,
  number,
  any,
  {
    name: string;
    status: SohTypes.SohStatusSummary;
  }
>;

export type MonitorTypeValueGetterParams = ValueGetterParams<
  {
    id: string;
  },
  EnvironmentTableContext,
  string,
  any,
  any
>;

export type ChannelValueGetterParams = ValueGetterParams<
  {
    id: string;
  },
  EnvironmentTableContext,
  string,
  any,
  any
>;

/**
 * The default column definition settings.
 */
export const defaultColumnDefinition: EnvironmentColumnDefinition = {
  disableStaticMarkupForHeaderComponentFramework: true,
  disableStaticMarkupForCellRendererFramework: true,
  sortable: true,
  filter: true
};

/**
 * Returns the environment table column definitions based on the provided channel data.
 * Also, filters out the channel columns that have a status that is marked as not visible.
 *
 * @param channelNames the channel names
 * @param monitorTypeValueGetter the monitor type value getter
 * @param channelValueGetter the channel value getter
 */
export const getEnvironmentColumnDefinitions = (
  channelNames: string[],
  monitorTypeValueGetter: (params) => string,
  channelValueGetter: (params) => number,
  namesOfChannelsToHide: string[]
): EnvironmentColumnDefinition[] => {
  const columnDefinitions: EnvironmentColumnDefinition[] = [];
  const headerCellBlockClass = 'soh-header-cell';

  // defines the monitor type column definition
  const monitorTypeColumnDefinition: MonitorTypeColumnDefinition = {
    colId: headerNameMonitorType,
    headerName: headerNameMonitorType,
    cellRendererFramework: MonitorTypeCellRenderer,
    headerClass: [`${headerCellBlockClass}`, `${headerCellBlockClass}--neutral`],
    width: monitorTypeWidthPx,
    pinned: 'left',
    suppressMovable: true,
    sort: 'asc',
    comparator: (a, b) => a?.localeCompare(b),
    valueGetter: monitorTypeValueGetter
  };
  columnDefinitions.push(monitorTypeColumnDefinition);

  const channelColumnDefinitions: ChannelColumnDefinition[] = [];
  orderBy(channelNames, 'channelName').forEach(name => {
    channelColumnDefinitions.push({
      colId: name,
      headerName: stripOutFirstOccurrence(name),
      field: name,
      width: 160,
      hide: namesOfChannelsToHide.includes(name),
      headerClass: params => {
        // use the unique column id (which is the channel name)
        if (params.context.rollupStatusByChannelName) {
          const rollup = params.context.rollupStatusByChannelName.get(name);
          const colCellData = params.context.dataReceivedByChannelName.get(name);
          return getChannelColumnHeaderClass(headerCellBlockClass, rollup, colCellData);
        }
        return headerCellBlockClass;
      },
      cellRendererFramework: ChannelCellRenderer,
      comparator: compareCellValues,
      valueGetter: channelValueGetter
    });
  });
  columnDefinitions.push(...channelColumnDefinitions);
  return columnDefinitions;
};
