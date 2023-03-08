/* eslint-disable react/prop-types */
/* eslint-disable react/destructuring-assignment */
import { Weavess } from '@gms/weavess';
import { WeavessConstants, WeavessTypes } from '@gms/weavess-core';
import memoizeOne from 'memoize-one';
import * as React from 'react';

import { nonIdealStateWithNoSpinner } from '../../util';
import type { LineChartWeavessProps, WeavessLineDefinition } from './types';

const WEAVESS_XAXIS_HEIGHT_PX = 50;

// the default y range that is used when the min and max are equal or undefined
export const defaultRange = 10;

// padding percent used to calculate a consistent padding on the default y-axis range
export const paddingPercent = 0.02;

/**
 * Custom vertical label for the Weavess line chart y-axis.
 */
// eslint-disable-next-line react/function-component-definition
export const YAxisLabel: React.FunctionComponent<WeavessTypes.LabelProps> = p => (
  <div className="chart__axis">
    <div className="inner rotate">{p.channel.name}</div>
  </div>
);

/**
 * Returns the default range and tick values.
 *
 * @param yMin the minimum y value
 * @param yMax the maximum y value
 * @param numAxisTicks the number of ticks to show in the y-axis
 */
export const getTicksAndRange = (
  yMin: number,
  yMax: number,
  numAxisTicks = 6
): {
  defaultRange?: WeavessTypes.Range;
  yAxisTicks?: number[];
} => {
  // TODO This should probably be handled more gracefully down in the WEAVESS package
  // This would ensure that all displays render and display ticks consistently
  // For now the `line-chart-weavess` component is handling special cases like
  // for specifying `n` number of ticks or when the min/max are equal or undefined

  // min or max should not equal -1 ('Unknown')
  if (yMin === -1 || yMax === -1) {
    throw new Error(`Trend min/max is Unknown`);
  }

  // determine the actual min/max to use and account for the case if they are equal or undefined
  let min = -defaultRange;
  let max = defaultRange;
  if (yMin !== undefined && !Number.isNaN(yMin) && yMax !== undefined && !Number.isNaN(yMax)) {
    min = yMin;
    max = yMax;
  }

  let numberOfYAxisTicks = numAxisTicks;

  // if equal set a range around value
  if (min === max) {
    const adjust = 0.1;
    min -= adjust;
    max += adjust;
    numberOfYAxisTicks = 3;
  }

  const ticksAndRange = { yAxisTicks: undefined, defaultRange: undefined };
  const yRange = Math.abs(max - min);
  const yAxisTicks: number[] = [];

  // Create incremental ticks unless they are equal then just the single value
  const stepBy = yRange / (numberOfYAxisTicks - 1);
  for (let i = 0; i < numberOfYAxisTicks - 1; i += 1) {
    yAxisTicks[i] = min + stepBy * i;
  }
  yAxisTicks.push(max);

  ticksAndRange.yAxisTicks = yAxisTicks;
  ticksAndRange.defaultRange = {
    // provide a padding on the default range to ensure no
    // y-axis values are cut off the screen
    min: min - yRange * paddingPercent,
    max: max + yRange * paddingPercent
  };
  return ticksAndRange;
};

/**
 * Convert to Weavess data for rendering
 *
 * @param lineDefs the line definitions and data
 * @param yAxisLabel the y-axis label
 * @param height the height of the chart
 * @param yMin the minimum y value
 * @param yMax the maximum y value
 * @param numberOfYAxisTicks the number of ticks to show in the y-axis
 */
export const convertToWeavessData = memoizeOne(
  (
    lineDefs: WeavessLineDefinition[],
    yAxisLabel: string,
    height: number,
    yMin: number,
    yMax: number,
    numberOfYAxisTicks = 6
  ) => {
    const stationsBuilder: WeavessTypes.Station[] = [];
    const dataSegments: WeavessTypes.DataSegment[] = [];

    // Values in building ChannelSegmentBoundaries used in
    // setting max, min and average values
    const offset = Math.max(Math.abs(yMax), Math.abs(yMin));
    let samplesCount = 0;
    let totalValue = 0;

    if (lineDefs && lineDefs.length > 0) {
      lineDefs.forEach(lineDef => {
        const dataByTime: WeavessTypes.DataByTime = { values: lineDef.values };

        // Update values used to populate the ChannelSegmentBoundaries in the ChannelSegment
        samplesCount += lineDef.values.length / 2;
        totalValue += samplesCount * lineDef.average;
        dataSegments.push({
          color: lineDef.color,
          displayType: [WeavessTypes.DisplayType.LINE],
          pointSize: 4,
          data: dataByTime
        });
      });

      const channelSegmentsRecord: Record<string, WeavessTypes.ChannelSegment[]> = {};
      channelSegmentsRecord.data = [
        {
          channelName: 'LineChartChannel',
          wfFilterId: WeavessTypes.UNFILTERED,
          isSelected: false,
          dataSegments,
          channelSegmentBoundaries: {
            topMax: yMax,
            bottomMax: yMin,
            channelSegmentId: WeavessTypes.UNFILTERED,
            offset,
            samplesCount,
            channelAvg: totalValue / samplesCount
          }
        }
      ];

      stationsBuilder.push({
        id: 'id',
        name: `name`,
        defaultChannel: {
          height: height - WEAVESS_XAXIS_HEIGHT_PX,
          ...getTicksAndRange(yMin, yMax, numberOfYAxisTicks),
          id: 'id',
          name: yAxisLabel,
          waveform: {
            channelSegmentId: 'data',
            channelSegmentsRecord
          }
        },
        nonDefaultChannels: undefined, // Has no child channels
        areChannelsShowing: false
      });
    }

    return stationsBuilder;
  }
);

/**
 * Line chart - renders a Weavess line chart component
 */
// eslint-disable-next-line react/function-component-definition
export const LineChartWeavess: React.FunctionComponent<LineChartWeavessProps> = props => {
  try {
    const stations = convertToWeavessData(
      props.lineDefs,
      props.yAxisLabel,
      props.heightPx,
      props.minAndMax?.yMin,
      props.minAndMax?.yMax
    );
    return (
      <div className={`core-chart ${props.classNames ?? ''}`}>
        <div
          className="weavess-line-chart container"
          style={{
            height: props.heightPx,
            width: props.widthPx
          }}
        >
          <div className="weavess-line-chart inner">
            <div className="weavess-line-chart chart">
              <Weavess
                viewableInterval={{
                  startTimeSecs: props.startTimeMs / WeavessConstants.MILLISECONDS_IN_SECOND,
                  endTimeSecs: props.endTimeMs / WeavessConstants.MILLISECONDS_IN_SECOND
                }}
                isControlledComponent={false}
                minimumOffset={0}
                maximumOffset={0}
                stations={stations}
                selections={{
                  channels: undefined
                }}
                initialConfiguration={{
                  defaultChannel: {
                    disableMeasureWindow: true
                  },
                  suppressLabelYAxis: false,
                  labelWidthPx: 65,
                  xAxisLabel: props.xAxisLabel
                }}
                customLabel={YAxisLabel}
                events={WeavessConstants.DEFAULT_UNDEFINED_EVENTS}
              />
            </div>
          </div>
        </div>
      </div>
    );
  } catch (error) {
    return nonIdealStateWithNoSpinner('Data Error', error.message);
  }
};
