import { Icon, IconSize, Intent } from '@blueprintjs/core';
import { IconNames } from '@blueprintjs/icons';
import type { CellRendererParams, ColumnDefinition } from '@gms/ui-core-components';
import { EventsColumn, useAppSelector } from '@gms/ui-state';
import classNames from 'classnames';
import type Immutable from 'immutable';
import * as React from 'react';

import type { EventRow } from '../types';
import { columnDisplayStrings } from '../types';

/**
 * Cell renderer to render the conflict marker column
 */
export const ConflictMarkerCellRenderer: React.FunctionComponent<CellRendererParams<
  EventRow,
  unknown,
  boolean,
  unknown,
  unknown
  // eslint-disable-next-line react/function-component-definition
>> = (props: CellRendererParams<EventRow, unknown, boolean, unknown, unknown>) => {
  const openEventId = useAppSelector(state => state.app.analyst.openEventId);
  const { data } = props;
  const color = data.id === openEventId ? 'black' : 'red';
  const isConflict = data.conflict;
  return !isConflict ? (
    <div className={classNames('table-cell', 'events__conflict-marker')}>
      <Icon icon={IconNames.ISSUE} intent={Intent.DANGER} size={IconSize.LARGE} color={color} />
    </div>
  ) : (
    <div className="table-cell" style={{ height: '36px' }}>
      <span>TBD</span>
    </div>
  );
};

/**
 * Defines the conflict marker column definition
 *
 * @param columnsToDisplayMap a map that specifies if the column is visible
 * @returns the column definition
 */
export const conflictColumnDef = (
  columnsToDisplayMap: Immutable.Map<EventsColumn, boolean>
): ColumnDefinition<EventRow, unknown, boolean, unknown, unknown> => ({
  headerName: columnDisplayStrings.get(EventsColumn.conflict),
  field: EventsColumn.conflict,
  headerTooltip: columnDisplayStrings.get(EventsColumn.conflict),
  width: 100,
  hide: !columnsToDisplayMap.get(EventsColumn.conflict),
  cellRendererFramework: ConflictMarkerCellRenderer,
  pinned: 'left'
});
