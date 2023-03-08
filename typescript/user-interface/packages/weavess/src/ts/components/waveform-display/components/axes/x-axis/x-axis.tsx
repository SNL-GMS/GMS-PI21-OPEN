/* eslint-disable react/destructuring-assignment */
import { Colors } from '@blueprintjs/core';
import { UILogger } from '@gms/ui-util';
import { WeavessConstants } from '@gms/weavess-core';
import * as d3 from 'd3';
import throttle from 'lodash/throttle';
import moment from 'moment';
import React from 'react';

import type { XAxisProps, XAxisState } from './types';

const logger = UILogger.create('GMS_LOG_WEAVESS', process.env.GMS_LOG_WEAVESS);

/**
 * The tick formatter used for the axis.
 *
 * @param date the date object
 * @returns the formatted time string
 */
export const tickFormatter = (date: Date): string => moment.utc(date).format('HH:mm:ss.SSS');

/** Time interval threshold to use sub millisecond time formatting */
const minTimeIntervalSecs = 60.0;

/** Constants used by sub milliseconds time formatter */
const MICROS_IN_A_SECOND = 10000;
const TEN = 10;
const THOUSAND = 1000;
const getNumberString = (num: number): string => (num < TEN ? `0${num}` : `${num}`);
let numberOfDigits = 3;
/**
 * The tick formatter used for the axis when time interval is below minimum threshold
 *
 * @param epochSeconds time in epoch seconds
 * @returns the formatted time string
 */
export const subMillisecondFormatter = (epochSeconds: number): string => {
  // Get the sub second string to 4 decimal places. Note toFixed rounds
  const microSeconds = (epochSeconds * MICROS_IN_A_SECOND) % MICROS_IN_A_SECOND;

  const subSecondString = (microSeconds / MICROS_IN_A_SECOND)
    .toFixed(numberOfDigits)
    .replace('0.', '');

  // Round seconds to the nearest millisecond and create Date
  const roundedSeconds = Math.round(epochSeconds * MICROS_IN_A_SECOND) / MICROS_IN_A_SECOND;
  const date = new Date(roundedSeconds * THOUSAND);
  return `${getNumberString(date.getUTCHours())}:${getNumberString(
    date.getUTCMinutes()
  )}:${getNumberString(date.getUTCSeconds())}.${subSecondString}`;
};

/**
 * A D3-based Time Axis component
 */
export class XAxis extends React.PureComponent<XAxisProps, XAxisState> {
  /** A handle to the axis wrapper HTML element */
  public axisRef: HTMLElement | null;

  /** A handle to the svg selection d3 returns, where the axis will be created */
  private svgAxis: d3.Selection<SVGGElement, unknown, null, undefined>;

  /**
   * Constructor
   *
   * @param props X Axis props as XAxisProps
   */
  public constructor(props: XAxisProps) {
    super(props);
    this.state = {};
  }

  // ******************************************
  // BEGIN REACT COMPONENT LIFECYCLE METHODS
  // ******************************************

  /**
   * Called immediately after a component is mounted.
   * Setting state here will trigger re-rendering.
   */
  public componentDidMount(): void {
    const svg = d3
      .select(this.axisRef)
      .append('svg')
      .attr('width', '100%')
      // eslint-disable-next-line @typescript-eslint/no-magic-numbers
      .attr('height', WeavessConstants.DEFAULT_XAXIS_HEIGHT_PIXELS)
      .style('fill', Colors.LIGHT_GRAY5);

    this.svgAxis = svg.append('g').attr('class', 'x-axis-axis');
    this.update();
  }

  /**
   * Called immediately after updating occurs. Not called for the initial render.
   *
   * @param prevProps the previous props
   * @param prevState the previous state
   */
  public componentDidUpdate(prevProps: XAxisProps): void {
    if (prevProps.displayInterval !== this.props.displayInterval) this.update();
  }

  /**
   * Catches exceptions generated in descendant components.
   * Unhandled exceptions will cause the entire component tree to unmount.
   *
   * @param error the error that was caught
   * @param info the information about the error
   */
  // eslint-disable-next-line @typescript-eslint/explicit-module-boundary-types
  public componentDidCatch(error, info): void {
    logger.error(`Weavess XAxis Error: ${error} : ${info}`);
  }

  // ******************************************
  // END REACT COMPONENT LIFECYCLE METHODS
  // ******************************************

  // eslint-disable-next-line react/sort-comp
  public render(): JSX.Element {
    if (
      !this.props.displayInterval ||
      Number.isNaN(this.props.displayInterval.startTimeSecs) ||
      Number.isNaN(this.props.displayInterval.endTimeSecs)
    ) {
      return <div />;
    }
    return (
      <div
        className={this.props.borderTop ? 'x-axis' : 'x-axis no-border'}
        style={{
          height: `${WeavessConstants.DEFAULT_XAXIS_HEIGHT_PIXELS}px`
        }}
        data-start-time={this.props.displayInterval.startTimeSecs}
        data-end-time={this.props.displayInterval.endTimeSecs}
        data-cy="x-axis"
      >
        <div
          ref={axis => {
            this.axisRef = axis;
          }}
          style={{
            width: '100%'
          }}
        />
        <div
          style={{
            textAlign: 'center'
          }}
        >
          {this.props.label}
        </div>
      </div>
    );
  }

  /**
   * Re-draw the axis based on new parameters
   * Not a react life cycle method. Used to manually update the time axis
   * This is done to keep it performant, and not have to rerender the DOM
   */
  public readonly update = (): void => {
    throttle(this.internalUpdate, WeavessConstants.ONE_FRAME_MS * 1)();
  };

  private readonly internalUpdate = (): void => {
    if (!this.axisRef) return;
    const timeInterval =
      this.props.displayInterval.endTimeSecs - this.props.displayInterval.startTimeSecs;

    // Figure out when to use Date formatting vs sub millisecond formatting (sub seconds to 4 places)
    const useDateScale = timeInterval > minTimeIntervalSecs;
    const range = [this.props.labelWidthPx, this.axisRef.clientWidth - this.props.scrollbarWidthPx];
    const x = useDateScale
      ? d3
          .scaleUtc()
          .domain([
            new Date(
              this.props.displayInterval.startTimeSecs * WeavessConstants.MILLISECONDS_IN_SECOND
            ),
            new Date(
              this.props.displayInterval.endTimeSecs * WeavessConstants.MILLISECONDS_IN_SECOND
            )
          ])
          .range(range)
      : d3
          .scaleLinear()
          .domain([
            this.props.displayInterval.startTimeSecs,
            this.props.displayInterval.endTimeSecs
          ])
          .range(range);

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    const tf: any = useDateScale ? tickFormatter : subMillisecondFormatter;

    const spaceBetweenTicksPx = 150;

    let numTicks = Math.floor(
      (this.axisRef.clientWidth - this.props.labelWidthPx - this.props.scrollbarWidthPx) /
        spaceBetweenTicksPx
    );

    // If using sub milliseconds figure out num ticks based on time interval so
    // we don't have multiple time ticks with the same value
    if (!useDateScale) {
      numberOfDigits = 3;
      if (timeInterval < 1) {
        numberOfDigits = 4;
      }
      // Calculate the number of microseconds will use if less than calculated based on width
      // min is 1
      const numMicroSecs = Math.max(Math.round(timeInterval * MICROS_IN_A_SECOND), 1);
      numTicks = numMicroSecs < numTicks ? numMicroSecs : numTicks;
    }
    const tickSize = 7;
    const xAxis = d3.axisBottom(x).ticks(numTicks).tickSize(tickSize).tickFormat(tf);
    this.svgAxis.call(xAxis);
  };
}
