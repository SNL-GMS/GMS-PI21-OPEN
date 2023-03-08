import { setDecimalPrecision } from '@gms/common-util';
import type { ColumnDefinition } from '@gms/ui-core-components';
import { EventsColumn } from '@gms/ui-state';
import type Immutable from 'immutable';

import type { EventRow } from '../types';
import { columnDisplayStrings } from '../types';

/**
 * Defines the depth column definition
 *
 * @param columnsToDisplayMap a map that specifies if the column is visible
 * @returns the column definition
 */
export const depthColumnDef = (
  columnsToDisplayMap: Immutable.Map<EventsColumn, boolean>
): ColumnDefinition<EventRow, unknown, number, unknown, unknown> => ({
  headerName: columnDisplayStrings.get(EventsColumn.depthKm),
  field: EventsColumn.depthKm,
  headerTooltip: 'Depth in kilometers',
  hide: !columnsToDisplayMap.get(EventsColumn.depthKm),
  valueFormatter: params => setDecimalPrecision(params.data.depthKm, 3),
  filterValueGetter: params => setDecimalPrecision(params.data.depthKm, 3)
});
