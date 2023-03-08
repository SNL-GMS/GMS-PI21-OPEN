/* eslint-disable react/destructuring-assignment */
import { UILogger } from '@gms/ui-util';
import { WeavessConstants } from '@gms/weavess-core';
import * as d3 from 'd3';
import debounce from 'lodash/debounce';
import defer from 'lodash/defer';
import isNumber from 'lodash/isNumber';
import mean from 'lodash/mean';
import React from 'react';
import ResizeObserver from 'resize-observer-polyfill';

import type { YAxisProps, YAxisState } from './types';

const logger = UILogger.create('GMS_LOG_WEAVESS', process.env.GMS_LOG_WEAVESS);

/**
 * Y axis for an individual waveform
 */
export class YAxis extends React.Component<YAxisProps, YAxisState> {
  /** Handle to the axis wrapper HTMLElement */
  private axisRef: HTMLElement | null;

  /** Handle to the d3 svg selection, where the axis will be created. */
  private svgAxis: d3.Selection<SVGGElement, unknown, null, undefined>;

  private readonly resizeObserver: ResizeObserver;

  /**
   * Constructor
   *
   * @param props Y Axis props as YAxisProps
   */
  public constructor(props: YAxisProps) {
    super(props);
    this.state = {};
    this.resizeObserver = new ResizeObserver(this.display);
  }

  // ******************************************
  // BEGIN REACT COMPONENT LIFECYCLE METHODS
  // ******************************************

  /**
   * Called immediately after a component is mounted.
   * Setting state here will trigger re-rendering.
   */
  public componentDidMount(): void {
    if (!this.axisRef) return;
    this.resizeObserver.observe(this.axisRef);

    const svg = d3.select(this.axisRef).append('svg');
    svg
      .attr('height', `100%`)
      // eslint-disable-next-line @typescript-eslint/no-magic-numbers
      .attr('width', 50);
    this.svgAxis = svg
      .append('g')
      .attr('class', 'y-axis__ordinate')
      .attr('height', `${this.props.heightInPercentage}%`)
      // eslint-disable-next-line @typescript-eslint/no-magic-numbers
      .attr('transform', `translate(${49},0)`);
    this.display();
  }

  /**
   * React lifecycle
   *
   * @param nextProps props for the axis of type YAxisProps
   *
   * @returns boolean
   */
  public shouldComponentUpdate(nextProps: YAxisProps): boolean {
    const hasChanged = !(
      this.props.maxAmplitude === nextProps.maxAmplitude &&
      this.props.minAmplitude === nextProps.minAmplitude &&
      this.props.heightInPercentage === nextProps.heightInPercentage
    );
    return hasChanged;
  }

  /**
   * Called immediately after updating occurs. Not called for the initial render.
   *
   * @param prevProps the previous props
   * @param prevState the previous state
   */
  public componentDidUpdate(): void {
    this.display();
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
    logger.error(`Weavess YAxis Error: ${error} : ${info}`);
  }

  /**
   * Cleanup the resize observer
   */
  public componentWillUnmount(): void {
    if (this.axisRef) this.resizeObserver.unobserve(this.axisRef);
  }

  // ******************************************
  // END REACT COMPONENT LIFECYCLE METHODS
  // ******************************************

  // eslint-disable-next-line react/sort-comp
  public render(): JSX.Element {
    const axisRange = this.calcAxisRange();
    return (
      <div
        className="y-axis"
        data-cy="y-axis"
        data-min={axisRange?.min}
        data-max={axisRange?.max}
        ref={this.handleRef}
        style={{
          height: `calc(${this.props.heightInPercentage ?? 100}% - ${
            WeavessConstants.WAVEFORM_PADDING_PX * 2
          }px)`,
          margin: `${WeavessConstants.WAVEFORM_PADDING_PX}px 0 ${WeavessConstants.WAVEFORM_PADDING_PX}px 0`
        }}
      />
    );
  }

  /**
   * Draw the axis
   */
  // eslint-disable-next-line react/sort-comp
  public readonly display = () => defer(debounce(this.drawAxis));

  public readonly calcAxisRange = (): { min: number; max: number } | undefined => {
    // adjust the min/max with a pixel padding to ensure that all of the number are visible
    const min = this.props.minAmplitude;
    const max = this.props.maxAmplitude;

    // If both min and max are numbers (helps where min or max are equal to 0)
    if (isNumber(min) && isNumber(max)) {
      return {
        min,
        max
      };
    }
    return undefined;
  };

  /**
   * Updates the SVG with the min/max amplitudes and ticks
   */
  private readonly drawAxis = (): void => {
    if (!this.axisRef) return;

    // Get min max amplitude values
    const axisRange = this.calcAxisRange();

    // If the axisRange is undefined clear the yAxis amplitudes and tick marks
    if (!axisRange) {
      const scale = d3.scaleLinear().domain([]).range([0, 0]);
      this.svgAxis.call(
        d3.axisLeft(scale).ticks(0).tickFormat(null).tickSizeOuter(0).tickValues([])
      );
      return;
    }
    const totalTicks = this.props.yAxisTicks ? this.props.yAxisTicks.length : 3;

    // do this right at the start, before making any changes that affect style.
    // getting clientHeight causes the browser to calculate layout, which is expensive.
    const axisHeight = this.axisRef.clientHeight;
    d3.select(this.axisRef)
      .select('svg')
      .attr('height', `100%`)
      // eslint-disable-next-line @typescript-eslint/no-magic-numbers
      .attr('width', 50)
      .attr('overflow', 'visible');

    const yAxisScale = d3
      .scaleLinear()
      .domain([axisRange.min, axisRange.max])
      .range([axisHeight, 0]);

    let tickValues: number[] = [];

    // set the floating point precision of amplitude
    let precision = 1;
    if (this.props.yAxisTicks) {
      tickValues = this.props.yAxisTicks;
    } else {
      let meanValue = mean([axisRange.min, axisRange.max]);
      const meanValueZeroMin = -0.099;
      const meanValueZeroMax = 0.099;
      // make sure we don't display -0
      if (meanValue && meanValue >= meanValueZeroMin && meanValue <= meanValueZeroMax) {
        meanValue = Math.abs(meanValue);
      }

      // Display 0 digits if min or max absolute value is >= 1.0
      if (Math.max(Math.abs(axisRange.min), Math.abs(axisRange.max)) >= 1.0) {
        precision = 0;
      }
      tickValues = [axisRange.min, meanValue, axisRange.max];
    }

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    const tickFormatter: any = (value: number): any => value.toFixed(precision);

    const yAxis = d3
      .axisLeft(yAxisScale)
      .ticks(totalTicks)
      .tickFormat(tickFormatter)
      .tickSizeOuter(0)
      .tickValues(tickValues);

    this.svgAxis.call(yAxis);

    // TODO allow the tick marks to be more customizable within WEAVESS
    // TODO allow props to specify whether or not tick marks should be rendered (none, all, odd, or even)
    if (!this.props.yAxisTicks) {
      const paddingPx = 6;
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      const groups: any = this.svgAxis.selectAll('text');
      // eslint-disable-next-line no-underscore-dangle
      d3.select(groups._groups[0][0]).attr('transform', `translate(0,${-1 * paddingPx})`);
      // eslint-disable-next-line no-underscore-dangle
      d3.select(groups._groups[0][2]).attr('transform', `translate(0,${paddingPx})`);
    }
  };

  /**
   * handles the ref callback function on the axis div
   *
   * @param axisRef the ref provided by the div
   */
  private readonly handleRef = axisRef => {
    if (axisRef) {
      this.axisRef = axisRef;
    }
  };
}
