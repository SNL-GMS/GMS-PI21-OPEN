import type { CommonTypes } from '@gms/common-model';
import { MILLISECONDS_IN_SECOND } from '@gms/common-util';
import { UILogger } from '@gms/ui-util';
import * as d3 from 'd3';
import isEqual from 'lodash/isEqual';
import React from 'react';

import { gmsColors } from '~scss-config/color-preferences';

import { calculateWidth, getScaleForTimeRange } from './workflow-util';

const logger = UILogger.create('GMS_LOG_WORKFLOW', process.env.GMS_LOG_WORKFLOW);

/**
 * Props for the workflow time axis
 */
export interface WorkflowTimeAxisProps {
  readonly timeRange: CommonTypes.TimeRange;
  readonly width: number;
}

const getUpdateTickStyles = (tickSize: number, timeRange: CommonTypes.TimeRange) =>
  function updateTickStyles(this: HTMLElement, tick: Date, index: number) {
    // ! `this` has context of `d3` here; not of the class
    if (tick.getUTCHours() === 0) {
      this.classList.add('day-label');
    }

    // adjust the first and last ticks to ensure that they are always visible
    this.classList.remove('first', 'last', 'hidden');
    if (index === 0 || index === tickSize - 1) {
      const THIRTY_FIVE = 35;
      const tickStartTime = tick.valueOf() / MILLISECONDS_IN_SECOND;
      let width = 0;
      if (index === 0) {
        width = calculateWidth(timeRange.startTimeSecs, tickStartTime);
        if (width < THIRTY_FIVE) {
          this.classList.add('first');
        }
      } else {
        width = calculateWidth(tickStartTime, timeRange.endTimeSecs);
        if (width < THIRTY_FIVE) {
          this.classList.add('last');
        }
      }
    }
  };

/**
 * Time axis for the Workflow display
 */
export class WorkflowTimeAxis extends React.Component<WorkflowTimeAxisProps> {
  /** Handle to the dom element where the time axis will be created */
  private timeAxisContainer: HTMLDivElement;

  /**
   * The d3 time axis
   */
  private timeAxis: d3.Selection<
    Element | d3.EnterElement | Document | Window,
    unknown,
    null,
    undefined
  >;

  /**
   * On mount, create & render the d3 axis
   */
  public componentDidMount(): void {
    this.createAxis();
  }

  public shouldComponentUpdate(nextProps: WorkflowTimeAxisProps): boolean {
    const { timeRange, width } = this.props;
    return !isEqual(nextProps.timeRange, timeRange) || nextProps.width !== width;
  }

  /**
   * re-draw axis on update.
   */
  public componentDidUpdate(prevProps: WorkflowTimeAxisProps): void {
    const { timeRange } = this.props;
    if (!isEqual(prevProps.timeRange, timeRange)) {
      this.updateAxis();
    }
  }

  /**
   * set the scrollLeft style attribute of the time axis
   *
   * @param scrollLeft scroll left
   */
  public setScrollLeft(scrollLeft: number): void {
    this.timeAxisContainer.scrollLeft = scrollLeft;
  }

  /**
   * Create & render the d3 axis
   */
  private readonly createAxis = () => {
    const timeAxisHeight = 25;
    this.timeAxis = d3
      .select(this.timeAxisContainer)
      .append('svg')
      .attr('height', timeAxisHeight)
      .style('fill', gmsColors.gmsMain);
    this.timeAxis.append('g').attr('class', 'workflow-time-axis');

    this.updateAxis();
  };

  private readonly updateAxis = () => {
    const { timeRange } = this.props;

    const { scaleAxis, totalWidth } = getScaleForTimeRange(timeRange);

    if (timeRange) {
      this.timeAxis.attr('width', totalWidth);

      const tickFormatter = (date: Date) =>
        d3.utcDay(date).getTime() < date.getTime()
          ? d3.utcFormat('%H:%M')(date)
          : d3.utcFormat('%Y-%m-%d')(date);

      const axis = d3
        .axisBottom(scaleAxis)
        .ticks(d3.utcHour.every(1))
        .tickFormat(tickFormatter)
        .tickSizeOuter(0);

      const ticks = this.timeAxis.select('.workflow-time-axis');
      ticks.call(axis as any);

      const ticksText = ticks.selectAll('text');
      const tickSize = ticksText.size();
      ticksText.each(getUpdateTickStyles(tickSize, timeRange));
    }
  };

  /**
   * Display the time axis
   */
  public render(): JSX.Element {
    logger.debug(`Rendering WorkflowTimeAxis`, this.props);
    const { width } = this.props;
    return (
      <div className="time-axis-wrapper" style={{ width: `${width}px` }}>
        <div
          className="time-axis"
          ref={ref => {
            this.timeAxisContainer = ref;
          }}
        />
      </div>
    );
  }
}
