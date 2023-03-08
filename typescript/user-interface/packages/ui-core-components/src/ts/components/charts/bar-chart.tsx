/* eslint-disable react/prop-types */
/* eslint-disable react/destructuring-assignment */
import * as d3 from 'd3';
import cloneDeep from 'lodash/cloneDeep';
import uniqueId from 'lodash/uniqueId';
import * as React from 'react';
import {
  VictoryAxis,
  VictoryBar,
  VictoryBrushContainer,
  VictoryChart,
  VictoryLine,
  VictoryZoomContainer
} from 'victory';
import type { DomainPropType, VictoryThemeDefinition } from 'victory-core';

import { Axis } from './axis';
import type { BarChartProps } from './types';
import { GMSTheme } from './victory-themes';

/* border color */
const BORDER_COLOR = '#ffffff';

/* offset in pixels to account for the y axis labels */
const yAxisLabelsOffset = 38;
const brushBarsMaxUntilWidthReduction = 50;
const brushBarsWidth = 10;
/* the scale padding for scale band calculation */
const scalePadding = 0.2;
const scrollWindowHeightPx = 110;
/** Determines whether to show the brush scrolling */
let isScrollingEnabled = false;
/** Padding used to calculate an optimal starting domain when brush scrolling enables */
const brushScrollDefaultPadding = 50;

/**
 * Determines optimal size for individual bars as a the brush increases and is set
 *
 * @param scrollDomain x start and x end on brush
 * @param chartWidth width of the bar chart
 * @param barMaxWidth max bar width from props
 * @param barMinWidth min bar width from props
 * @returns a calculated barwidth
 */
const calculateScrollableBarWidth = (
  scrollDomain: { min: number; max: number },
  chartWidth: number,
  barMaxWidth: number,
  barMinWidth: number
): number => {
  const totalBarsShown = Math.round(scrollDomain.max - scrollDomain.min);
  const padding = 10;
  const barWidth = Math.round(chartWidth / totalBarsShown) - padding;
  if (barWidth >= barMaxWidth) {
    return barMaxWidth;
  }
  if (barWidth <= barMinWidth) {
    return barMinWidth;
  }
  return barWidth;
};

/**
 * Builds the Victory Chart for scrolling channels
 *
 * @param props BarChartProps
 * @param scrollDomain
 * @param setScrollDomain
 * @returns VictoryChart
 */
const buildScrollVictoryChart = (
  props: BarChartProps,
  scrollDomain: { min: number; max: number },
  setScrollDomain: React.Dispatch<
    React.SetStateAction<{
      min: number;
      max: number;
    }>
  >
) => {
  if (!isScrollingEnabled) {
    return undefined;
  }
  // Set custom victory theme for the brush container
  // mostly a copy of GMS Theme - but removes labels and ticks
  const GMSThemeBrushContainer: VictoryThemeDefinition = cloneDeep(GMSTheme);
  GMSThemeBrushContainer.independentAxis.style.tickLabels.opacity = 0;
  GMSThemeBrushContainer.independentAxis.style.tickLabels.fill = 'none';
  GMSThemeBrushContainer.independentAxis.style.ticks.stroke = 'none';

  const victoryChartKey = props.id ? `scroll-victory-chart-${props.id}` : 'scroll-victory-chart';
  const victoryAxisKey = props.id ? `scroll-victory-axis-${props.id}` : 'scroll-victory-axis';
  return (
    <VictoryChart
      key={victoryChartKey}
      width={props.widthPx}
      height={scrollWindowHeightPx}
      scale={{ x: 'linear' }}
      padding={{ top: 0, left: 14, right: 20, bottom: 30 }}
      theme={GMSThemeBrushContainer}
      containerComponent={
        <VictoryBrushContainer
          responsive={false}
          brushDimension="x"
          brushDomain={{ x: [scrollDomain.min, scrollDomain.max] }}
          brushStyle={{
            fill: `${props.scrollBrushColor ? props.scrollBrushColor : 'rgb(150, 150, 150)'}`,
            opacity: 0.3
          }}
          defaultBrushArea="move"
          onBrushDomainChange={e => {
            let scrollXDomainStart = e.x[0] as number;
            let scrollXDomainEnd = e.x[1] as number;
            // The two if below are insuring if at the edge of the brush, can go to the end and keep correct values
            if (scrollXDomainStart === 1) {
              scrollXDomainStart = 0;
              scrollXDomainEnd -= 1;
            }
            if (scrollXDomainEnd === props.barDefs.length) {
              scrollXDomainStart += 1;
              scrollXDomainEnd += 1;
            }
            if (scrollXDomainStart < 0 || scrollXDomainEnd < 0) {
              setScrollDomain({ min: 0, max: 5 });
            } else {
              setScrollDomain({ min: scrollXDomainStart, max: scrollXDomainEnd });
            }
          }}
        />
      }
    >
      {/* Empty victory axis removes Y axis */}
      <VictoryAxis key={victoryAxisKey} />
      {props.barDefs.map((barDef, index) => (
        <VictoryBar
          // TODO: fix anti-pattern because it can cause bugs.
          // If the bars were to get sorted somehow, it would not re render them.

          // eslint-disable-next-line react/no-array-index-key
          key={index}
          alignment="start"
          style={{
            data: {
              width:
                props.barDefs.length < brushBarsMaxUntilWidthReduction
                  ? brushBarsWidth
                  : brushBarsWidth / 2,
              fill: barDef.color
            },
            parent: { border: `1px solid ${BORDER_COLOR}` }
          }}
          data={[{ ...barDef.value, brushBarsWidth }]}
          barWidth={
            props.barDefs.length < brushBarsMaxUntilWidthReduction
              ? brushBarsWidth
              : brushBarsWidth / 2
          }
        />
      ))}
    </VictoryChart>
  );
};

/**
 * Builds the VictoryBar part of main Victory Chart
 *
 * @param props BarChartProps
 * @param barWidth
 * @returns VictoryBar
 */
const buildVictoryBar = (props: BarChartProps, barWidth: number) => {
  // forcing an any type to incoming props.dataComponent because victory is
  // very picky about the type coming into the label component
  const DataComponent: any = props.dataComponent ? props.dataComponent : null;
  return props.barDefs?.map((barDef, index) => (
    <VictoryBar
      events={
        props.onContextMenuBar
          ? [
              {
                target: 'data',
                eventHandlers: {
                  onContextMenu: e => [
                    {
                      target: 'data',
                      mutation: passedProps => {
                        props.onContextMenuBar(e, passedProps);
                      }
                    }
                  ]
                }
              }
            ]
          : undefined
      }
      // TODO: fix using indices for keys is an anti-pattern
      // eslint-disable-next-line react/no-array-index-key
      key={index}
      alignment="middle"
      style={{
        data: {
          width: barWidth,
          fill: barDef.color
        },
        parent: { border: `1px solid ${BORDER_COLOR}` }
      }}
      data={[{ ...barDef.value, barWidth }]}
      barWidth={barWidth}
      // Unfortunately have to pass the labels props in order to render label component
      labels={DataComponent ? d => d : undefined}
      // note that the label component can only render a svg
      labelComponent={DataComponent ? <DataComponent /> : undefined}
    />
  ));
};

const buildVictoryZoomContainer = (
  props: BarChartProps,
  scrollDomain: { min: number; max: number }
) => {
  const victoryZoomKey = props.id ? `victory-zoom-container-${props.id}` : 'victory-zoom-container';
  return isScrollingEnabled ? (
    <VictoryZoomContainer
      key={victoryZoomKey}
      allowZoom={false}
      allowPan={false}
      zoomDomain={{ x: [scrollDomain.min, scrollDomain.max] }}
    />
  ) : undefined;
};

/**
 * Build Victory Line for marginal and bad thresholds
 *
 * @param thresholds marginal or bad number lists
 * @param color for bad or marginal line
 * @param key
 * @param victoryLineDomain
 * @returns VictoryLine
 */
const buildVictoryLine = (
  thresholds: number[],
  color: string,
  key: string,
  victoryLineDomain: DomainPropType
) => {
  const offset = 0.5;
  return thresholds?.map((data, index) => (
    <VictoryLine
      domain={victoryLineDomain}
      // TODO: fix using indices for keys is an anti-pattern
      // eslint-disable-next-line react/no-array-index-key
      key={`${key}-${index}`}
      style={{
        data: {
          stroke: color,
          strokeWidth: 2
        }
      }}
      data={[
        { x: index + 1 - offset, y: data },
        { x: index + 1 + offset, y: data }
      ]}
    />
  ));
};

/**
 * Bar chart - renders a Victory bar chart component with axis
 */
// eslint-disable-next-line react/function-component-definition
export const BarChart: React.FunctionComponent<BarChartProps> = props => {
  // eslint-disable-next-line no-unsafe-optional-chaining
  const width = props.widthPx - props.padding?.right - props.padding?.left - yAxisLabelsOffset;
  const categories = props.categories || { x: [], y: [] };
  const xAxisScale = d3.scaleBand().domain(categories.x).range([0, width]).padding(scalePadding);
  const forceUpdateIdRef = React.useRef('');
  let barWidth =
    xAxisScale.bandwidth() > props.maxBarWidth ? props.maxBarWidth : xAxisScale.bandwidth();
  const [scrollDomain, setScrollDomain] = React.useState({
    min: 0,
    max: width / (barWidth + brushScrollDefaultPadding)
  });
  isScrollingEnabled = barWidth < props.minBarWidth;
  barWidth = isScrollingEnabled
    ? calculateScrollableBarWidth(scrollDomain, width, props.maxBarWidth, props.minBarWidth)
    : barWidth;
  const padding = (width - barWidth * categories.x.length) / (categories.x.length + 1);
  const xDomainPadding = barWidth / 2 + padding;

  let height = Math.max(props.heightPx, props.minHeightPx ?? 0);
  height = isScrollingEnabled ? height - scrollWindowHeightPx : height;

  const victoryZoomContainer = buildVictoryZoomContainer(props, scrollDomain);
  const victoryBar = buildVictoryBar(props, barWidth);

  // build the scroll VictoryChart if scroll is enabled
  const scrollVictoryChart = buildScrollVictoryChart(props, scrollDomain, setScrollDomain);
  const victoryChartKey = props.id ?? 'victory-chart';
  const victoryAxisKey = props.id ? `victory-axis-${props.id}` : 'victory-axis';
  const victoryMarginalLines = buildVictoryLine(
    props.thresholdsMarginal,
    '#be8c0b',
    'marginal-',
    undefined
  );
  /* Finds the max of the bad thresholds to ensure domain is above this line,
   if values are not already. Needed to prevent cutting off of threshold line. */
  const victoryLineDomain: DomainPropType = props.thresholdsBad
    ? { y: [0, Math.max(...props.thresholdsBad) + 1] }
    : undefined;

  const victoryBadLines = buildVictoryLine(
    props.thresholdsBad,
    '#d24c4c',
    'bad-',
    victoryLineDomain
  );

  // Used to force a rerender on resizes to keep logic using latest width
  React.useEffect(() => {
    forceUpdateIdRef.current = uniqueId();
  }, [props.widthPx, props.maxBarWidth]);

  return (
    <div className={`core-chart ${props.classNames ?? ''}`} key={`${forceUpdateIdRef.current}`}>
      <VictoryChart
        key={`${victoryChartKey} ${forceUpdateIdRef.current}`}
        // This height is for the vector <svg> inside the victory container
        height={height}
        width={props.widthPx}
        animate={false}
        theme={GMSTheme}
        // This height is telling the parent victory <div> container to be a certain size
        style={{ parent: { height } }}
        padding={props.padding}
        containerComponent={victoryZoomContainer}
        domainPadding={{ x: xDomainPadding }}
      >
        {/*
          Hide the default axis for Victory. This is required because Victory does not
          recognize that our custom component `Axis` is adding the VictoryAxis component.
          The causes Victory to override our Axis and show only the default.
          A VictoryGroup could have been used but it is recommend to no be used with an axis.
          https://formidable.com/open-source/victory/docs/victory-group
        */}
        <VictoryAxis
          key={victoryAxisKey}
          style={{
            axis: { stroke: 'none' },
            ticks: { stroke: 'none' },
            tickLabels: { fill: 'none' }
          }}
        />

        {/* eslint-disable-next-line react/jsx-props-no-spreading */}
        <Axis {...props} rotateAxis />
        {victoryBar}
        {victoryMarginalLines}
        {victoryBadLines}
      </VictoryChart>
      {/* Victory Chart for scroll container */}
      {scrollVictoryChart}
    </div>
  );
};
