/* eslint-disable react/jsx-no-useless-fragment */
import { WorkflowTypes } from '@gms/common-model';
import {
  isAutomaticProcessingStageInterval,
  isProcessingSequenceInterval,
  isStageInterval
} from '@gms/common-model/lib/workflow/types';
import { humanReadable, secondsToString, toSentenceCase } from '@gms/common-util';
import { LabelValue, TooltipWrapper } from '@gms/ui-core-components';
import uniqueId from 'lodash/uniqueId';
import React from 'react';

export interface TooltipPanelProps {
  readonly status: string;
  readonly activeAnalysts: string;
  readonly lastModified: string;
  readonly startTime: string;
  readonly endTime: string;
  readonly isStale: boolean;
  readonly tooltipRef: React.MutableRefObject<HTMLElement>;
  readonly setTooltipKey: React.Dispatch<React.SetStateAction<string>>;
}

export interface TooltipProps {
  readonly interval: WorkflowTypes.ActivityInterval | WorkflowTypes.StageInterval;
  readonly activeAnalysts?: string[]; // Roll up of active analysts to show on cell
  readonly staleStartTime: number;
}

/**
 * Gets the status of the provided interval
 *
 * @param interval the interval
 * @returns a string representation of the status
 */
export const getStatus = (
  interval: WorkflowTypes.ActivityInterval | WorkflowTypes.StageInterval
): string => {
  if (!interval) {
    return undefined;
  }

  const status = `${humanReadable(interval.status)}`;

  if (interval.status === WorkflowTypes.IntervalStatus.IN_PROGRESS) {
    if (isStageInterval(interval) && isAutomaticProcessingStageInterval(interval)) {
      if (interval.sequenceIntervals?.length > 0) {
        return `${status} (${interval.sequenceIntervals[0].lastExecutedStepName})`;
      }
    }
    if (isProcessingSequenceInterval(interval)) {
      return `${status} (${interval.lastExecutedStepName})`;
    }
  }
  return status;
};

/**
 * Gets the active analysts of the provided interval
 *
 * @param interval the interval
 * @returns a string representation of the active analysts
 */
export const getActiveAnalysts = (
  interval: WorkflowTypes.ActivityInterval | WorkflowTypes.StageInterval
): string => {
  if (!interval) {
    return undefined;
  }

  let activeAnalysts: string;
  if (WorkflowTypes.isActivityInterval(interval)) {
    activeAnalysts =
      interval.activeAnalysts && interval.activeAnalysts.length > 0
        ? interval.activeAnalysts.join(', ')
        : undefined;
  }
  return activeAnalysts;
};

function InternalTooltipPanel(props: TooltipPanelProps) {
  const {
    status,
    activeAnalysts,
    lastModified,
    startTime,
    endTime,
    isStale,
    tooltipRef,
    setTooltipKey
  } = props;
  return (
    // eslint-disable-next-line jsx-a11y/no-static-element-interactions
    <div
      ref={ref => {
        tooltipRef.current = ref;
      }}
      className="workflow-tooltip"
      data-cy="workflow-tooltip"
      // setting the tab index so the keydown listener can be active
      // eslint-disable-next-line jsx-a11y/no-noninteractive-tabindex
      tabIndex={0}
      onKeyDown={e => {
        if (e.key === 'Escape') {
          // Setting the key here to force the tooltip to close when esc is pressed
          // blueprint default close wasn't working with various solutions attempts
          setTooltipKey(uniqueId());
        }
      }}
    >
      {isStale && status === undefined ? (
        <LabelValue
          label="Stale"
          value="Interval is stale"
          tooltip="Status"
          containerClass="workflow-tooltip-container"
        />
      ) : undefined}
      {status ? (
        <LabelValue
          label="Status"
          value={`${status} ${isStale ? '(Stale)' : ''}`}
          tooltip="Status"
          containerClass="workflow-tooltip-container"
        />
      ) : undefined}
      {activeAnalysts ? (
        <LabelValue
          label="Active Analysts"
          value={activeAnalysts}
          tooltip="Active Analysts"
          containerClass="workflow-tooltip-container"
        />
      ) : undefined}
      {startTime ? (
        <LabelValue
          label="Start Time"
          value={startTime}
          numeric
          tooltip="Start Time"
          containerClass="workflow-tooltip-container"
        />
      ) : undefined}
      {endTime ? (
        <LabelValue
          label="End Time"
          value={endTime}
          numeric
          tooltip="End Time"
          containerClass="workflow-tooltip-container"
        />
      ) : undefined}
      {lastModified ? (
        <LabelValue
          label="Last Modified"
          value={lastModified}
          numeric
          tooltip="Last Modified"
          containerClass="workflow-tooltip-container"
        />
      ) : undefined}
    </div>
  );
}

export const TooltipPanel = React.memo(InternalTooltipPanel);

function InternalTooltip(props: React.PropsWithChildren<TooltipProps>) {
  const { children, interval, activeAnalysts, staleStartTime } = props;
  const [tooltipKey, setTooltipKey] = React.useState<string>(uniqueId());
  const tooltipRef = React.useRef<HTMLElement>(null);
  if (!interval) {
    return <>{children}</>;
  }
  const status: string = toSentenceCase(getStatus(interval));
  // Active analysts can be a roll up of all analysts when it's a stage, when it's a rollup
  // it's passed in, if not, goes through and finds active analysts for that activity
  const activeAnalyst: string = activeAnalysts
    ? activeAnalysts.join(', ')
    : getActiveAnalysts(interval);
  const lastModified: string = secondsToString(interval.modificationTime);
  const startTime: string = secondsToString(interval.startTime);
  const endTime: string = secondsToString(interval.endTime);
  const isStale = staleStartTime >= interval.endTime;
  return (
    <TooltipWrapper
      // setting key to uniqueId so can update it with a key press to force it to close
      // when esc key is pressed
      key={tooltipKey}
      onOpened={() => {
        tooltipRef?.current.focus();
      }}
      content={
        <TooltipPanel
          status={status}
          activeAnalysts={activeAnalyst}
          lastModified={lastModified}
          startTime={startTime}
          endTime={endTime}
          isStale={isStale}
          tooltipRef={tooltipRef}
          setTooltipKey={setTooltipKey}
        />
      }
    >
      {children ?? <></>}
    </TooltipWrapper>
  );
}

export const Tooltip = React.memo(InternalTooltip);
