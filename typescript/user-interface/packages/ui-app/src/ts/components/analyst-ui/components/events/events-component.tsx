import { IanDisplays } from '@gms/common-model/lib/displays/types';
import type GoldenLayout from '@gms/golden-layout';
import { nonIdealStateWithNoSpinner, WithNonIdealStates } from '@gms/ui-core-components';
import { useAppSelector, useGetProcessingAnalystConfigurationQuery } from '@gms/ui-state';
import { UILogger } from '@gms/ui-util';
import * as React from 'react';

import {
  processingAnalystConfigNonIdealStateDefinitions,
  timeRangeNonIdealStateDefinitions
} from '~analyst-ui/common/non-ideal-states/non-ideal-state-defs';
import { BaseDisplay } from '~common-ui/components/base-display';
import { CommonNonIdealStateDefs } from '~common-ui/components/non-ideal-states';

import type { EventsPanelProps } from './events-panel';
import { EventsPanel } from './events-panel';

const logger = UILogger.create('GMS_LOG_EVENTS', process.env.GMS_LOG_EVENTS);

export interface EventsComponentProps {
  // passed in from golden-layout
  readonly glContainer?: GoldenLayout.Container;
}

export const EventsPanelOrNonIdealState = WithNonIdealStates<EventsPanelProps>(
  [
    ...CommonNonIdealStateDefs.baseNonIdealStateDefinitions,
    ...processingAnalystConfigNonIdealStateDefinitions,
    ...timeRangeNonIdealStateDefinitions('events'),
    {
      condition: (props: EventsPanelProps): boolean => {
        return props.stageName === undefined;
      },
      element: nonIdealStateWithNoSpinner('Error', 'Invalid stage name')
    }
  ],
  EventsPanel
);

export function EventsComponent(props: EventsComponentProps) {
  logger.debug(`Rendering EventsComponent`, props);
  const { glContainer } = props;

  const processingAnalystConfigurationQuery = useGetProcessingAnalystConfigurationQuery();

  const timeRange = useAppSelector(state => state.app.workflow.timeRange);

  const stageName = useAppSelector(state => state.app.workflow.openIntervalName);

  return (
    <BaseDisplay
      glContainer={glContainer}
      className="events-display-window gms-body-text"
      data-cy="events-display-window"
      tabName={IanDisplays.EVENTS}
    >
      <EventsPanelOrNonIdealState
        timeRange={timeRange}
        stageName={stageName}
        processingAnalystConfigurationQuery={processingAnalystConfigurationQuery}
      />
    </BaseDisplay>
  );
}
