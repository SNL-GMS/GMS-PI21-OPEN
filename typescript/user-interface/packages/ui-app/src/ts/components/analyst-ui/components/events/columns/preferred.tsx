import type { ColumnDefinition } from '@gms/ui-core-components';
import { EventsColumn } from '@gms/ui-state';
import type Immutable from 'immutable';

import type { EventRow } from '../types';
import { columnDisplayStrings } from '../types';

/**
 * Defines the preferred column definition
 *
 * @param columnsToDisplayMap a map that specifies if the column is visible
 * @returns the column definition
 */
export const preferredColumnDef = (
  columnsToDisplayMap: Immutable.Map<EventsColumn, boolean>
): ColumnDefinition<EventRow, unknown, string, unknown, unknown> => ({
  headerName: columnDisplayStrings.get(EventsColumn.preferred),
  field: EventsColumn.preferred,
  headerTooltip: columnDisplayStrings.get(EventsColumn.preferred),
  hide: !columnsToDisplayMap.get(EventsColumn.preferred),
  width: 115
});
