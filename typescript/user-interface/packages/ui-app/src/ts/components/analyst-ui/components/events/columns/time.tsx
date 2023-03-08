import { formatTimeForDisplay } from '@gms/common-util';
import type { ColumnDefinition } from '@gms/ui-core-components';
import { EventsColumn } from '@gms/ui-state';
import type Immutable from 'immutable';

import { medCellWidthPx } from '~common-ui/common/table-types';

import type { EventRow } from '../types';
import { columnDisplayStrings } from '../types';
/**
 * Defines the time column definition
 *
 * @param columnsToDisplayMap a map that specifies if the column is visible
 * @returns the column definition
 */
export const timeColumnDef = (
  columnsToDisplayMap: Immutable.Map<EventsColumn, boolean>
): ColumnDefinition<EventRow, unknown, number, unknown, unknown> => ({
  headerName: columnDisplayStrings.get(EventsColumn.time),
  field: EventsColumn.time,
  headerTooltip: 'Time',
  width: medCellWidthPx,
  hide: !columnsToDisplayMap.get(EventsColumn.time),
  valueFormatter: params => formatTimeForDisplay(params.data.time),
  filterValueGetter: params => formatTimeForDisplay(params.data.time),
  sort: 'asc',
  pinned: 'left'
});
