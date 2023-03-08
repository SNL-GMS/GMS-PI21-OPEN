import type { ColumnDefinition } from '@gms/ui-core-components';
import { EventsColumn } from '@gms/ui-state';
import type Immutable from 'immutable';

import type { EventRow } from '../types';
import { columnDisplayStrings } from '../types';

/**
 * Defines the region column definition
 *
 * @param columnsToDisplayMap a map that specifies if the column is visible
 * @returns the column definition
 */
export const regionColumnDef = (
  columnsToDisplayMap: Immutable.Map<EventsColumn, boolean>
): ColumnDefinition<EventRow, unknown, string, unknown, unknown> => ({
  headerName: columnDisplayStrings.get(EventsColumn.region),
  field: EventsColumn.region,
  headerTooltip: 'Region',
  hide: !columnsToDisplayMap.get(EventsColumn.region)
});
