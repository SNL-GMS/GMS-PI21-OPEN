import type { CommonTypes } from '@gms/common-model';
import { MILLISECONDS_IN_SECOND, toDate } from '@gms/common-util';
import { UILogger } from '@gms/ui-util';
import * as d3 from 'd3';
import React from 'react';

import { getScaleForTimeRange } from './workflow-util';

const logger = UILogger.create('GMS_LOG_WORKFLOW', process.env.GMS_LOG_WORKFLOW);

/**
 * DayBoundaryIndicator Props
 */
export interface DayBoundaryIndicatorProps {
  readonly timeRange: CommonTypes.TimeRange;
  readonly width: number;
  readonly height: number;
}

/**
 * @DayBoundaryIndicator
 * The marker between days in the workflow table
 * It is kept in synch with the workflow table through its onScroll callback
 * Line indicating the transition from one day from the next
 */
export class DayBoundaryIndicator extends React.Component<DayBoundaryIndicatorProps> {
  private dayBoundaryContainerRef: HTMLElement;

  public shouldComponentUpdate(nextProps: DayBoundaryIndicatorProps): boolean {
    return this.internalUpdate(nextProps);
  }

  private readonly internalUpdate = (
    nextProps: DayBoundaryIndicatorProps,
    force = false
  ): boolean => {
    const {
      height,
      timeRange: { startTimeSecs, endTimeSecs }
    } = this.props;
    const { height: nextHeight, timeRange: nextTimeRange } = nextProps;
    const { startTimeSecs: nextStartTimeSecs, endTimeSecs: nextEndTimeSecs } = nextTimeRange;

    const shouldUpdate =
      force || startTimeSecs !== nextStartTimeSecs || endTimeSecs !== nextEndTimeSecs;

    // no reason to update the height; if about to rerender
    if (!shouldUpdate && height !== nextHeight) {
      logger.debug(`Updating the DayBoundaryIndicator height`, height, nextHeight);
      this.dayBoundaryContainerRef.style.height = `${nextHeight}px`;
    }
    return shouldUpdate;
  };

  /**
   * Synchronizes the day indicator with the main display
   *
   * @param scrollTo pixel value reflecting how scrolled the workflow is
   */
  public readonly scrollDayIndicator = (scrollTo: number): void => {
    if (!this.dayBoundaryContainerRef) return;
    this.dayBoundaryContainerRef.style.marginLeft = `${(-scrollTo).toString()}px`;
  };

  public render(): JSX.Element {
    const { height, timeRange } = this.props;
    const { totalWidth, scaleToPosition } = getScaleForTimeRange(timeRange);

    const days = d3.utcDay
      .every(1)
      .range(toDate(timeRange.startTimeSecs), toDate(timeRange.endTimeSecs));

    logger.debug(`Rendering DayBoundaryIndicator`, this.props, days.length);

    return (
      <div
        ref={ref => {
          this.dayBoundaryContainerRef = ref;
        }}
        className="workflow-day-boundary-container"
        style={{ width: totalWidth, height }}
      >
        {days.map(value => {
          return (
            <div
              key={value.valueOf()}
              className="workflow-day-divider"
              style={{
                left: `${scaleToPosition(value.valueOf() / MILLISECONDS_IN_SECOND)}px`,
                height: `100%`
              }}
            />
          );
        })}
      </div>
    );
  }
}
