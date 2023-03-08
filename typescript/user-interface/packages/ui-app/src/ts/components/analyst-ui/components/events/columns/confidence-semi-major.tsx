import { setDecimalPrecision } from '@gms/common-util';
import type { ColumnDefinition } from '@gms/ui-core-components';
import { EventsColumn } from '@gms/ui-state';
import type Immutable from 'immutable';

import type { EventRow } from '../types';
import { columnDisplayStrings } from '../types';

/**
 * Defines the confidence semi major column definition
 *
 * @param columnsToDisplayMap a map that specifies if the column is visible
 * @returns the column definition
 */
export const confidenceSemiMajorColumnDef = (
  columnsToDisplayMap: Immutable.Map<EventsColumn, boolean>
): ColumnDefinition<EventRow, unknown, number, unknown, unknown> => ({
  headerName: columnDisplayStrings.get(EventsColumn.confidenceSemiMajorAxis),
  field: EventsColumn.confidenceSemiMajorAxis,
  headerTooltip: 'Confidence semi-major axis in kilometers',
  width: 130,
  hide: !columnsToDisplayMap.get(EventsColumn.confidenceSemiMajorAxis),
  valueFormatter: params => setDecimalPrecision(params.data.confidenceSemiMajorAxis, 2),
  filterValueGetter: params => setDecimalPrecision(params.data.confidenceSemiMajorAxis, 2)
});
