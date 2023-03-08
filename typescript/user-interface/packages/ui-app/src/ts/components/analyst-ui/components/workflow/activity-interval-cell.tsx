import { ContextMenu } from '@blueprintjs/core';
import { WorkflowTypes } from '@gms/common-model';
import { useAppSelector } from '@gms/ui-state';
import classNames from 'classnames';
import includes from 'lodash/includes';
import React from 'react';
import { Provider, useStore } from 'react-redux';

import { IntervalContextMenu } from './context-menus';
import { WorkflowContext } from './workflow-context';

export interface ActivityIntervalCellProps {
  readonly activityInterval: WorkflowTypes.ActivityInterval;
}

/**
 * Determines the text for the cell based on status and active analyst list
 *
 * @param status interval status
 * @param activeAnalysts list of activeAnalyst
 * @returns text for cell to display
 */
export const determineTextForCell = (
  status: WorkflowTypes.IntervalStatus,
  activeAnalysts: string[]
): string => {
  if (!activeAnalysts || activeAnalysts.length === 0) return '';

  switch (status) {
    case WorkflowTypes.IntervalStatus.NOT_STARTED:
    case WorkflowTypes.IntervalStatus.NOT_COMPLETE:
      // !note for complete the name needs to be analyst that marked complete
      return '';
    case WorkflowTypes.IntervalStatus.COMPLETE:
      return activeAnalysts[0];
    default:
      return `${activeAnalysts[0]}${
        activeAnalysts.length > 1 ? ` + ${activeAnalysts.length - 1}` : ''
      }`;
  }
};

export const preventDefaultEvent = (event: React.MouseEvent<HTMLDivElement, MouseEvent>): void => {
  event.preventDefault();
};

// eslint-disable-next-line react/display-name
export const ActivityIntervalCell: React.FunctionComponent<ActivityIntervalCellProps> = React.memo(
  (props: ActivityIntervalCellProps) => {
    const { activityInterval } = props;
    const openIntervalName = useAppSelector(state => state.app.workflow.openIntervalName);
    const openActivityNames = useAppSelector(state => state.app.workflow.openActivityNames);
    const startTimeSecs = useAppSelector(state => state.app.workflow.timeRange.startTimeSecs);
    const context = React.useContext(WorkflowContext);
    const isSelected =
      openIntervalName === activityInterval.stageName &&
      includes(openActivityNames, activityInterval.name) &&
      startTimeSecs === activityInterval.startTime;
    const isStale = context.staleStartTime >= activityInterval.endTime;
    const isClickable = !isStale || isSelected;
    const cellClass = classNames({
      'interval-cell': true,
      'interval-cell--selected': isSelected,
      'interval-cell--not-complete':
        activityInterval.status === WorkflowTypes.IntervalStatus.NOT_COMPLETE,
      'interval-cell--in-progress':
        activityInterval.status === WorkflowTypes.IntervalStatus.IN_PROGRESS,
      'interval-cell--not-started':
        activityInterval.status === WorkflowTypes.IntervalStatus.NOT_STARTED,
      'interval-cell--complete': activityInterval.status === WorkflowTypes.IntervalStatus.COMPLETE,
      'interval-cell--activity-cell': true,
      'interval-cell--stale': isStale,
      'interval-cell--clickable': isClickable
    });
    const store = useStore();
    return (
      <div
        key={activityInterval.startTime}
        data-cy={`${activityInterval.startTime}-${activityInterval.name}`}
        data-start-time={activityInterval.startTime}
        data-activity-interval={`${activityInterval.name}`}
        className={cellClass}
        onDoubleClick={() => {
          if (!isStale && !isSelected) {
            context.openConfirmationPrompt(activityInterval);
          }
        }}
        // if stale do not allow context menu, unless that interval is already selected (active)
        onContextMenu={
          isClickable
            ? e => {
                e.preventDefault();
                // TODO update to ContextMenu2 see https://blueprintjs.com/docs/#popover2-package/context-menu2
                ContextMenu.show(
                  <Provider store={store}>
                    <IntervalContextMenu
                      interval={activityInterval}
                      isSelectedInterval={isSelected}
                      allActivitiesOpenForSelectedInterval={
                        context.allActivitiesOpenForSelectedInterval
                      }
                      openCallback={context.openConfirmationPrompt}
                      closeCallback={context.closeConfirmationPrompt}
                    />
                  </Provider>,
                  {
                    left: e.clientX,
                    top: e.clientY
                  },
                  undefined,
                  true
                );
              }
            : preventDefaultEvent
        }
      >
        <span className="workflow-ellipsis">
          {determineTextForCell(activityInterval.status, activityInterval.activeAnalysts)}
        </span>
      </div>
    );
  }
);
