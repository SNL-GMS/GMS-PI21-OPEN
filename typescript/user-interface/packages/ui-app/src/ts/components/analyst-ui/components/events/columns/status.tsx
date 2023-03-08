import { EventTypes } from '@gms/common-model';
import { humanReadable, toSentenceCase } from '@gms/common-util';
import type { CellRendererParams, ColumnDefinition } from '@gms/ui-core-components';
import { getColumnPosition, TableCellRenderer } from '@gms/ui-core-components';
import { EventsColumn } from '@gms/ui-state';
import type Immutable from 'immutable';
import * as React from 'react';

import type { EventRow } from '../types';
import { columnDisplayStrings } from '../types';

/**
 * Cell renderer to render the status column
 */
export function StatusCellRenderer(
  props: CellRendererParams<EventRow, unknown, string, unknown, unknown>
) {
  const { data } = props;
  const value = data.status
    ? toSentenceCase(humanReadable(data.status)) ??
      toSentenceCase(humanReadable(EventTypes.EventStatus.NOT_STARTED))
    : toSentenceCase(humanReadable(EventTypes.EventStatus.NOT_STARTED));
  return (
    <TableCellRenderer
      data-col-position={getColumnPosition<EventRow>(props)}
      value={value}
      isNumeric={false}
      tooltipMsg={value}
      cellValueClassName={
        data.status === EventTypes.EventStatus.COMPLETE ? 'table-cell__value--complete' : ''
      }
    />
  );
}

/**
 * Defines the status column definition
 *
 * @param columnsToDisplayMap a map that specifies if the column is visible
 * @returns the column definition
 */
export const statusColumnDef = (
  columnsToDisplayMap: Immutable.Map<EventsColumn, boolean>
): ColumnDefinition<EventRow, unknown, string, unknown, unknown> => ({
  headerName: columnDisplayStrings.get(EventsColumn.status),
  field: EventsColumn.status,
  headerTooltip: columnDisplayStrings.get(EventsColumn.status),
  width: 110,
  hide: !columnsToDisplayMap.get(EventsColumn.status),
  pinned: 'right',
  cellRendererFramework: StatusCellRenderer
});
