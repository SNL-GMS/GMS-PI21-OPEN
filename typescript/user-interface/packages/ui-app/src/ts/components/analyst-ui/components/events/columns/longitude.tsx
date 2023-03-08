import { setDecimalPrecision } from '@gms/common-util';
import type { ColumnDefinition } from '@gms/ui-core-components';
import { EventsColumn } from '@gms/ui-state';
import type Immutable from 'immutable';

import type { EventRow } from '../types';
import { columnDisplayStrings } from '../types';

/**
 * Defines the longitude column definition
 *
 * @param columnsToDisplayMap a map that specifies if the column is visible
 * @returns the column definition
 */
export const longitudeColumnDef = (
  columnsToDisplayMap: Immutable.Map<EventsColumn, boolean>
): ColumnDefinition<EventRow, unknown, number, unknown, unknown> => ({
  headerName: columnDisplayStrings.get(EventsColumn.longitudeDegrees),
  field: EventsColumn.longitudeDegrees,
  headerTooltip: 'Longitude in degrees',
  hide: !columnsToDisplayMap.get(EventsColumn.longitudeDegrees),
  valueFormatter: params => setDecimalPrecision(params.data.longitudeDegrees, 3),
  filterValueGetter: params => setDecimalPrecision(params.data.longitudeDegrees, 3)
});
