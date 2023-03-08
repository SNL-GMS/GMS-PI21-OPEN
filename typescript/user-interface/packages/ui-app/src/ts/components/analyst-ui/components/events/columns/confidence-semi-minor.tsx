import { setDecimalPrecision } from '@gms/common-util';
import type { ColumnDefinition } from '@gms/ui-core-components';
import { EventsColumn } from '@gms/ui-state';
import type Immutable from 'immutable';

import type { EventRow } from '../types';
import { columnDisplayStrings } from '../types';

/**
 * Defines the confidence semi minor column definition
 *
 * @param columnsToDisplayMap a map that specifies if the column is visible
 * @returns the column definition
 */
export const confidenceSemiMinorColumnDef = (
  columnsToDisplayMap: Immutable.Map<EventsColumn, boolean>
): ColumnDefinition<EventRow, unknown, number, unknown, unknown> => ({
  headerName: columnDisplayStrings.get(EventsColumn.confidenceSemiMinorAxis),
  field: EventsColumn.confidenceSemiMinorAxis,
  headerTooltip: 'Confidence semi-minor axis in kilometers',
  width: 130,
  hide: !columnsToDisplayMap.get(EventsColumn.confidenceSemiMinorAxis),
  valueFormatter: params => setDecimalPrecision(params.data.confidenceSemiMinorAxis, 2),
  filterValueGetter: params => setDecimalPrecision(params.data.confidenceSemiMinorAxis, 2)
});
