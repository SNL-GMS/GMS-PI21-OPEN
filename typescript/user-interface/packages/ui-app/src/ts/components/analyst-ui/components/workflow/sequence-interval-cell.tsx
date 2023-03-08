import { ContextMenu } from '@blueprintjs/core';
import { WorkflowTypes } from '@gms/common-model';
import {
  isProcessingSequenceInterval,
  isStageInterval
} from '@gms/common-model/lib/workflow/types';
import { PercentBar } from '@gms/ui-core-components';
import { useAppSelector } from '@gms/ui-state';
import classNames from 'classnames';
import React from 'react';
import { Provider, useStore } from 'react-redux';

import { preventDefaultEvent } from './activity-interval-cell';
import { IntervalContextMenu } from './context-menus';
import { WorkflowContext } from './workflow-context';
import { getPercentComplete, isStageIntervalPercentBar } from './workflow-util';

export interface SequenceIntervalCellProps {
  readonly stageInterval: WorkflowTypes.StageInterval | WorkflowTypes.ProcessingSequenceInterval;
  readonly workflow: WorkflowTypes.Workflow;
}

const getDataCy = stageInterval => {
  if (isStageInterval(stageInterval)) {
    return stageInterval.stageMode;
  }
  if (isProcessingSequenceInterval(stageInterval)) {
    return stageInterval.stageName;
  }
  return undefined;
};

/**
 * A sequence interval cell used to render the smaller blocks
 */
export const SequenceIntervalCell: React.FunctionComponent<SequenceIntervalCellProps> = React.memo(
  function SeqIntervalCell(props: SequenceIntervalCellProps) {
    const { stageInterval, workflow } = props;
    const openIntervalName = useAppSelector(state => state.app.workflow.openIntervalName);
    const startTimeSecs = useAppSelector(state => state.app.workflow.timeRange.startTimeSecs);
    const context = React.useContext(WorkflowContext);
    const isSelected =
      openIntervalName === stageInterval.name && startTimeSecs === stageInterval.startTime;
    const isStale = context.staleStartTime >= stageInterval.endTime;
    const isOpenable =
      isStageInterval(stageInterval) &&
      stageInterval.stageMode === WorkflowTypes.StageMode.INTERACTIVE &&
      (!isStale || isSelected);
    const cellClass = classNames({
      'interval-cell': true,
      'interval-cell--selected': isSelected,
      'interval-cell--not-complete':
        stageInterval.status === WorkflowTypes.IntervalStatus.NOT_COMPLETE,
      'interval-cell--in-progress':
        stageInterval.status === WorkflowTypes.IntervalStatus.IN_PROGRESS,
      'interval-cell--not-started':
        stageInterval.status === WorkflowTypes.IntervalStatus.NOT_STARTED,
      'interval-cell--complete': stageInterval.status === WorkflowTypes.IntervalStatus.COMPLETE,
      'interval-cell--skipped': stageInterval.status === WorkflowTypes.IntervalStatus.SKIPPED,
      'interval-cell--failed': stageInterval.status === WorkflowTypes.IntervalStatus.FAILED,
      'interval-cell--stale': isStale,
      'interval-cell--activity-cell': isProcessingSequenceInterval(stageInterval),
      'interval-cell--clickable': isOpenable
    });
    const store = useStore();

    // only want interactive stage intervals to show a context menu, other wise a right click should be a no-op
    // if stale do not allow context menu, unless that interval is already selected (active)
    const onContextMenu =
      isStageInterval(stageInterval) && isOpenable
        ? (e: { preventDefault: () => void; clientX: any; clientY: any }) => {
            e.preventDefault();
            // TODO update to ContextMenu2 see https://blueprintjs.com/docs/#popover2-package/context-menu2
            ContextMenu.show(
              <Provider store={store}>
                <IntervalContextMenu
                  interval={stageInterval}
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
        : preventDefaultEvent;

    return isStageInterval(stageInterval) && isStageIntervalPercentBar(stageInterval) ? (
      <div className={cellClass} onContextMenu={preventDefaultEvent}>
        <PercentBar percentage={getPercentComplete(stageInterval, workflow)} />
      </div>
    ) : (
      <div
        key={stageInterval.startTime}
        className={cellClass}
        data-sequence-interval={`${stageInterval.name}`}
        data-cy={`${getDataCy(stageInterval)}`}
        data-interval="generic-interval"
        data-start-time={stageInterval.startTime}
        onDoubleClick={() => {
          if (
            isStageInterval(stageInterval) &&
            stageInterval.stageMode === WorkflowTypes.StageMode.INTERACTIVE &&
            !isStale &&
            !isSelected
          ) {
            context.openConfirmationPrompt(stageInterval);
          }
        }}
        onContextMenu={onContextMenu}
      />
    );
  }
);
