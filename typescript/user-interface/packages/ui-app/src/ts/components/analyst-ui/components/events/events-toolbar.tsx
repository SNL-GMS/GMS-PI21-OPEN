import {
  ButtonToolbarItem,
  CheckboxDropdownToolbarItem,
  LabelValueToolbarItem,
  Toolbar
} from '@gms/ui-core-components';
import {
  EventFilters,
  eventsActions,
  EventsColumn,
  useAppDispatch,
  useAppSelector
} from '@gms/ui-state';
import type Immutable from 'immutable';
import React from 'react';

import {
  convertMapToObject,
  convertObjectToEventFiltersMap,
  convertObjectToEventsColumnMap
} from '~common-ui/common/table-utils';
import { useBaseDisplaySize } from '~common-ui/components/base-display/base-display-hooks';
import { semanticColors } from '~scss-config/color-preferences';

import { columnDisplayStrings } from './types';

const marginForToolbarPx = 40;

export interface EventsToolbarProps {
  readonly completeEventsCount: number;
  readonly remainingEventsCount: number;
  readonly rejectedEventsCount: number;
  readonly conflictsEventsCount: number;
  readonly disableMarkSelectedComplete: boolean;
  handleMarkSelectedComplete(): void;
}

// eslint-disable-next-line react/function-component-definition
export const EventsToolbar: React.FunctionComponent<EventsToolbarProps> = ({
  completeEventsCount,
  remainingEventsCount,
  rejectedEventsCount,
  conflictsEventsCount,
  disableMarkSelectedComplete,
  handleMarkSelectedComplete
}: EventsToolbarProps) => {
  const [widthPx] = useBaseDisplaySize();

  const dispatch = useAppDispatch();
  const eventsColumnsToDisplayObject = useAppSelector(state => state.app.events.eventsColumns);
  const columnsToDisplay = React.useMemo(
    () => convertObjectToEventsColumnMap(eventsColumnsToDisplayObject),
    [eventsColumnsToDisplayObject]
  );

  const edgeEventsToDisplayObject = useAppSelector(state => state.app.events.edgeEvents);
  const eventsToDisplay = React.useMemo(
    () => convertObjectToEventFiltersMap(edgeEventsToDisplayObject),
    [edgeEventsToDisplayObject]
  );

  const toolbarItemsLeft: JSX.Element[] = React.useMemo(
    () => [
      <LabelValueToolbarItem
        key="completedevents"
        labelValueColor={semanticColors.analystComplete}
        tooltip="Number of completed events"
        label="Complete"
        labelValue={<span className="monospace">{completeEventsCount.toString()}</span>}
        style={{ marginRight: '1em' }}
      />,
      <LabelValueToolbarItem
        key="remainingevents"
        labelValueColor={semanticColors.analystToWork}
        tooltip="Number of remaining events"
        label="Remaining"
        labelValue={<span className="monospace">{remainingEventsCount.toString()}</span>}
        style={{ marginRight: '1em' }}
      />,
      <LabelValueToolbarItem
        key="rejectedevents"
        labelValueColor={semanticColors.analystUnassociated}
        tooltip="Number of rejected events"
        label="Rejected"
        labelValue={<span className="monospace">{rejectedEventsCount.toString()}</span>}
        style={{ marginRight: '1em' }}
      />,
      <LabelValueToolbarItem
        key="conflictevents"
        tooltip="Number of events with conflicts"
        label="Conflicts"
        labelValue={<span className="monospace">{conflictsEventsCount.toString()}</span>}
        style={{ marginRight: '1em' }}
      />
    ],
    [completeEventsCount, conflictsEventsCount, rejectedEventsCount, remainingEventsCount]
  );

  const toolbarItemsRight: JSX.Element[] = React.useMemo(() => {
    const setColumnsToDisplay = (cols: Immutable.Map<EventsColumn, boolean>) =>
      dispatch(eventsActions.updateEventsColumns(convertMapToObject(cols)));

    const setEventsToDisplay = (events: Immutable.Map<EventFilters, boolean>) => {
      dispatch(eventsActions.updateEdgeEvents(convertMapToObject(events)));
    };
    return [
      <CheckboxDropdownToolbarItem
        key="shownevents"
        enumOfKeys={EventFilters}
        label="Show events"
        menuLabel="Show events"
        widthPx={150}
        tooltip="Set which columns are visible"
        values={eventsToDisplay}
        onChange={setEventsToDisplay}
        cyData="filter-column"
      />,
      <CheckboxDropdownToolbarItem
        key="showncolumns"
        enumOfKeys={EventsColumn}
        enumKeysToDisplayStrings={columnDisplayStrings}
        label="Show columns"
        menuLabel="Show columns"
        widthPx={150}
        tooltip="Set which columns are visible"
        values={columnsToDisplay}
        onChange={setColumnsToDisplay}
        cyData="filter-column"
      />,
      <ButtonToolbarItem
        key="markcomplete"
        cyData="mark-open-complete"
        tooltip="Mark selected events complete"
        label="Mark selected complete"
        widthPx={170}
        onButtonClick={handleMarkSelectedComplete}
        disabled={disableMarkSelectedComplete}
      />
    ];
  }, [
    columnsToDisplay,
    disableMarkSelectedComplete,
    eventsToDisplay,
    handleMarkSelectedComplete,
    dispatch
  ]);

  return (
    <Toolbar
      toolbarWidthPx={widthPx}
      parentContainerPaddingPx={marginForToolbarPx}
      itemsLeft={toolbarItemsLeft}
      itemsRight={toolbarItemsRight}
    />
  );
};
