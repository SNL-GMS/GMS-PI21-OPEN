import { setDecimalPrecision } from '@gms/common-util';
import type { ColumnDefinition } from '@gms/ui-core-components';
import { EventsColumn } from '@gms/ui-state';
import type Immutable from 'immutable';

import { singleDecimalComparator } from '~common-ui/common/table-utils';

import type { EventRow } from '../types';
import { columnDisplayStrings } from '../types';

/**
 * Defines the ms column definition
 *
 * @param columnsToDisplayMap a map that specifies if the column is visible
 * @returns the column definition
 */
export const msColumnDef = (
  columnsToDisplayMap: Immutable.Map<EventsColumn, boolean>
): ColumnDefinition<EventRow, unknown, number, unknown, unknown> => ({
  headerName: columnDisplayStrings.get(EventsColumn.magnitudeMs),
  field: EventsColumn.magnitudeMs,
  headerTooltip: 'Surface wave magnitude',
  width: 60,
  hide: !columnsToDisplayMap.get(EventsColumn.magnitudeMs),
  valueFormatter: params => setDecimalPrecision(params.data.magnitudeMs, 1),
  filterValueGetter: params => setDecimalPrecision(params.data.magnitudeMs, 1),
  comparator: singleDecimalComparator
});
