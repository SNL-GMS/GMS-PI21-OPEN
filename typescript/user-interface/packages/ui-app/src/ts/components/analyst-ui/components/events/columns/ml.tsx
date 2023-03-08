import { setDecimalPrecision } from '@gms/common-util';
import type { ColumnDefinition } from '@gms/ui-core-components';
import { EventsColumn } from '@gms/ui-state';
import type Immutable from 'immutable';

import { singleDecimalComparator } from '~common-ui/common/table-utils';

import type { EventRow } from '../types';
import { columnDisplayStrings } from '../types';

/**
 * Defines the ml column definition
 *
 * @param columnsToDisplayMap a map that specifies if the column is visible
 * @returns the column definition
 */
export const mlColumnDef = (
  columnsToDisplayMap: Immutable.Map<EventsColumn, boolean>
): ColumnDefinition<EventRow, unknown, number, unknown, unknown> => ({
  headerName: columnDisplayStrings.get(EventsColumn.magnitudeMl),
  field: EventsColumn.magnitudeMl,
  headerTooltip: 'Local magnitude',
  width: 60,
  hide: !columnsToDisplayMap.get(EventsColumn.magnitudeMl),
  valueFormatter: params => setDecimalPrecision(params.data.magnitudeMl, 1),
  filterValueGetter: params => setDecimalPrecision(params.data.magnitudeMl, 1),
  comparator: singleDecimalComparator
});
