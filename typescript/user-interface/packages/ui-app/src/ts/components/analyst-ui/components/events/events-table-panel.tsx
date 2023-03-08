import type { CommonTypes } from '@gms/common-model';
import { EventTypes } from '@gms/common-model';
import type GoldenLayout from '@gms/golden-layout';
import type { EventsFetchResult } from '@gms/ui-state';
import { useAppSelector, useEventStatusQuery, useWorkflowQuery } from '@gms/ui-state';
import { EventFilters } from '@gms/ui-state/lib/app/state/events';
import React from 'react';

import {
  convertObjectToEventFiltersMap,
  convertObjectToEventsColumnMap
} from '~common-ui/common/table-utils';

import type { MapEventSource } from '../map/types';
import { IANConfirmOpenEventPopup } from './confirm-open-event-popup';
import { EventsTable } from './events-table';
import { EventsToolbar } from './events-toolbar';
import { buildEventRow } from './events-util';
import type { EventRow } from './types';
import { EdgeTypes } from './types';

export interface EventsTablePanelProps {
  readonly glContainer?: GoldenLayout.Container;
  readonly timeRange: CommonTypes.TimeRange;
  readonly eventResults: EventsFetchResult;
}

/**
 * Use event rows in events toolbar props to populate the events list display
 *
 * @param eventQuery The query containing events to be filtered for display in the events toolbar
 * @param timeRange The open interval time range
 * @param eventsToDisplay Events set to display in the events table panel
 * @returns
 */
export const useEventRows = (
  eventQuery: EventsFetchResult,
  timeRange: CommonTypes.TimeRange
): EventRow[] | MapEventSource[] => {
  const openIntervalName = useAppSelector(state => state.app.workflow.openIntervalName);

  const workflowQuery = useWorkflowQuery();

  const stageNames = React.useMemo(
    () => (workflowQuery.isSuccess ? workflowQuery.data?.stages.map(stage => stage.name) : []),
    [workflowQuery.isSuccess, workflowQuery.data?.stages]
  );

  const findEventStatusQuery = useEventStatusQuery();
  const openEventId = useAppSelector(state => state.app.analyst.openEventId);
  const eventRows: EventRow[] = [];

  if (eventQuery.data) {
    const events = eventQuery.data;

    events.forEach(event => {
      let eventHypothesis = EventTypes.findPreferredEventHypothesis(
        event,
        openIntervalName,
        stageNames
      );
      if (
        eventHypothesis === undefined ||
        eventHypothesis.locationSolutions === undefined ||
        eventHypothesis.rejected
      ) {
        eventHypothesis = EventTypes.findEventHypothesisParent(event, eventHypothesis);
      }
      const eventIsOpen = openEventId === event.id;
      const locationSolution = EventTypes.findPreferredLocationSolution(
        eventHypothesis.id.hypothesisId,
        event.eventHypotheses
      );

      eventRows.push(
        buildEventRow(
          event.id,
          eventHypothesis,
          locationSolution.id,
          timeRange,
          findEventStatusQuery.data,
          eventIsOpen
        )
      );
    });
  }
  return eventRows;
};

// eslint-disable-next-line react/function-component-definition
export const EventsTablePanel: React.FunctionComponent<EventsTablePanelProps> = (
  props: EventsTablePanelProps
) => {
  const { eventResults, timeRange } = props;

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

  const [eventId, setEventId] = React.useState(undefined);

  const [isCurrentlyOpen, setIsCurrentlyOpen] = React.useState(false);

  let eventRows: EventRow[] = useEventRows(eventResults, timeRange);
  // if before is toggled off and the event is in the before buffer
  // or after is toggled off and the event is in the after buffer
  // don't display the event
  if (!eventsToDisplay.get(EventFilters.BEFORE)) {
    eventRows = eventRows.filter(eventRow => eventRow.edgeEventType !== EdgeTypes.BEFORE);
  }
  if (!eventsToDisplay.get(EventFilters.AFTER)) {
    eventRows = eventRows.filter(eventRow => eventRow.edgeEventType !== EdgeTypes.AFTER);
  }

  return (
    <div className="event-panel" data-cy="event-panel">
      <IANConfirmOpenEventPopup
        isCurrentlyOpen={isCurrentlyOpen}
        setIsCurrentlyOpen={setIsCurrentlyOpen}
        eventId={eventId}
        setEventId={setEventId}
        parentComponentId="event-list"
      />
      <EventsToolbar
        completeEventsCount={
          eventRows.filter(s => s.status === EventTypes.EventStatus.COMPLETE).length
        }
        remainingEventsCount={
          eventRows.filter(s => s.status !== EventTypes.EventStatus.COMPLETE).length
        }
        rejectedEventsCount={0}
        conflictsEventsCount={eventRows.filter(s => s.conflict).length}
        disableMarkSelectedComplete
        handleMarkSelectedComplete={() => window.alert("Mark complete hasn't been implemented")}
      />
      <EventsTable setEventId={setEventId} columnsToDisplay={columnsToDisplay} data={eventRows} />
    </div>
  );
};
