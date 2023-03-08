import { Menu, MenuItem } from '@blueprintjs/core';
import type { WorkflowTypes } from '@gms/common-model';
import { useAppSelector } from '@gms/ui-state';
import React from 'react';

export interface IntervalContextMenuProps {
  readonly interval: WorkflowTypes.ActivityInterval | WorkflowTypes.StageInterval;
  readonly isSelectedInterval: boolean;
  readonly allActivitiesOpenForSelectedInterval: boolean;

  readonly openCallback: (
    interval: WorkflowTypes.ActivityInterval | WorkflowTypes.StageInterval
  ) => void;
  readonly closeCallback: (
    interval: WorkflowTypes.ActivityInterval | WorkflowTypes.StageInterval
  ) => void;
}

/**
 * Component that renders the interval context menu.
 */
// eslint-disable-next-line react/function-component-definition
export const IntervalContextMenu: React.FunctionComponent<IntervalContextMenuProps> = (
  props: IntervalContextMenuProps
) => {
  const openIntervalName = useAppSelector(state => state.app.workflow.openIntervalName);
  const {
    interval,
    isSelectedInterval,
    allActivitiesOpenForSelectedInterval,
    openCallback,
    closeCallback
  } = props;

  const isDisabled =
    (allActivitiesOpenForSelectedInterval &&
      isSelectedInterval &&
      openIntervalName === interval.name) ||
    (isSelectedInterval && interval.name !== openIntervalName);
  return (
    <Menu>
      <MenuItem
        className="menu-item-open-interval"
        data-cy="open-interval-btn"
        text="Open interval"
        disabled={isDisabled}
        onClick={() => openCallback(interval)}
      />
      <MenuItem
        className="menu-item-close-interval"
        data-cy="close-interval-btn"
        text="Close interval"
        disabled={!isSelectedInterval}
        onClick={() => closeCallback(interval)}
      />
    </Menu>
  );
};
