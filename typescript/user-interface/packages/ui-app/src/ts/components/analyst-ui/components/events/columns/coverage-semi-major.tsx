import { setDecimalPrecision } from '@gms/common-util';
import type { ColumnDefinition } from '@gms/ui-core-components';
import { EventsColumn } from '@gms/ui-state';
import type Immutable from 'immutable';

import type { EventRow } from '../types';
import { columnDisplayStrings } from '../types';

/**
 * Defines the coverage semi major column definition
 *
 * @param columnsToDisplayMap a map that specifies if the column is visible
 * @returns the column definition
 */
export const coverageSemiMajorColumnDef = (
  columnsToDisplayMap: Immutable.Map<EventsColumn, boolean>
): ColumnDefinition<EventRow, unknown, number, unknown, unknown> => ({
  headerName: columnDisplayStrings.get(EventsColumn.coverageSemiMajorAxis),
  field: EventsColumn.coverageSemiMajorAxis,
  headerTooltip: 'Coverage semi-major axis in kilometers',
  width: 130,
  hide: !columnsToDisplayMap.get(EventsColumn.coverageSemiMajorAxis),
  valueFormatter: params => setDecimalPrecision(params.data.coverageSemiMajorAxis, 2),
  filterValueGetter: params => setDecimalPrecision(params.data.coverageSemiMajorAxis, 2)
});
