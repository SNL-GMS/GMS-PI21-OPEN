import type { CommonTypes } from '@gms/common-model';
import type GoldenLayout from '@gms/golden-layout';
import { WithNonIdealStates } from '@gms/ui-core-components';
import type { ProcessingAnalystConfigurationQuery } from '@gms/ui-state';
import { useGetEvents } from '@gms/ui-state';
import React from 'react';

import { eventNonIdealStateDefinitions } from '~analyst-ui/common/non-ideal-states/non-ideal-state-defs';

import type { EventsTablePanelProps } from './events-table-panel';
import { EventsTablePanel } from './events-table-panel';

export interface EventsPanelProps {
  readonly glContainer?: GoldenLayout.Container;
  readonly processingAnalystConfigurationQuery: ProcessingAnalystConfigurationQuery;
  readonly timeRange: CommonTypes.TimeRange;
  readonly stageName: string;
}

export const EventsTablePanelOrNonIdealState = WithNonIdealStates<EventsTablePanelProps>(
  [...eventNonIdealStateDefinitions],
  EventsTablePanel
);

// eslint-disable-next-line react/function-component-definition
export const EventsPanel: React.FunctionComponent<EventsPanelProps> = (props: EventsPanelProps) => {
  const { glContainer, timeRange } = props;

  const eventResults = useGetEvents();

  return (
    <div className="event-panel" data-cy="event-panel">
      <EventsTablePanelOrNonIdealState
        glContainer={glContainer}
        timeRange={timeRange}
        eventResults={eventResults}
      />
    </div>
  );
};
