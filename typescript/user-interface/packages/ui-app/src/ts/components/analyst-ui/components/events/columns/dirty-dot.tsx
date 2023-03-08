import type { CellRendererParams, ColumnDefinition } from '@gms/ui-core-components';
import { getColumnPosition, TableCellRenderer } from '@gms/ui-core-components';
import * as React from 'react';

import type { EventRow } from '../types';

/**
 * Cell renderer to render the dirty dot column
 */
export function DirtyDotCellRenderer(
  props: CellRendererParams<EventRow, unknown, boolean, unknown, unknown>
) {
  const value = '';
  return (
    <TableCellRenderer
      data-col-position={getColumnPosition<EventRow>(props)}
      value={value}
      isNumeric={false}
      tooltipMsg={value}
    />
  );
}

/**
 * Defines the dirty dot column definition
 *
 * @param columnsToDisplayMap a map that specifies if the column is visible
 * @returns the column definition
 */
export const dirtyDotColumnDef: ColumnDefinition<EventRow, unknown, boolean, unknown, unknown> = {
  headerName: '',
  field: 'dirtyDot',
  headerTooltip: 'Modified',
  width: 10,
  pinned: 'left',
  cellRendererFramework: DirtyDotCellRenderer
};
