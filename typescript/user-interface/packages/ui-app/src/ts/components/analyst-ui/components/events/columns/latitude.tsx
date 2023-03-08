import { setDecimalPrecision } from '@gms/common-util';
import type { ColumnDefinition } from '@gms/ui-core-components';
import { EventsColumn } from '@gms/ui-state';
import type Immutable from 'immutable';

import type { EventRow } from '../types';
import { columnDisplayStrings } from '../types';

/**
 * Defines the latitude column definition
 *
 * @param columnsToDisplayMap a map that specifies if the column is visible
 * @returns the column definition
 */
export const latitudeColumnDef = (
  columnsToDisplayMap: Immutable.Map<EventsColumn, boolean>
): ColumnDefinition<EventRow, unknown, number, unknown, unknown> => ({
  headerName: columnDisplayStrings.get(EventsColumn.latitudeDegrees),
  field: EventsColumn.latitudeDegrees,
  headerTooltip: 'Latitude in degrees',
  hide: !columnsToDisplayMap.get(EventsColumn.latitudeDegrees),
  valueFormatter: params => setDecimalPrecision(params.data.latitudeDegrees, 3),
  filterValueGetter: params => setDecimalPrecision(params.data.latitudeDegrees, 3)
});
