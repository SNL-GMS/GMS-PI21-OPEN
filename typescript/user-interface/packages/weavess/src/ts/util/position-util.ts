import type { WeavessTypes } from '@gms/weavess-core';
import * as d3 from 'd3';

import type { PositionPercentRange } from '../components/waveform-display/components/measure-window/measure-window-selection/measure-window-selection-area/types';

/**
 *
 * @param timeRange the start and end times of the measure window selection
 * @param viewStartTimeSecs the leftmost visible time
 * @param viewEndTimeSecs the rightmost visible time
 * @returns
 */
export const getMeasureWindowSelectionAreaFraction = (
  timeRange: WeavessTypes.TimeRange | undefined,
  viewStartTimeSecs: number,
  viewEndTimeSecs: number,
  offsetSecs: number
): PositionPercentRange | undefined => {
  if (!timeRange) {
    return undefined;
  }
  const scale = d3.scaleLinear().domain([viewStartTimeSecs, viewEndTimeSecs]).range([0, 1]);
  return {
    startPercent: scale(timeRange.startTimeSecs + offsetSecs) * 100,
    endPercent: scale(timeRange.endTimeSecs + offsetSecs) * 100
  };
};

/**
 * Computes the time in seconds for the mouse x position.
 *
 * @param mouseXPosition the mouse x position from 0 to 1, where 0 is the far left
 * of the canvas and 1 is the far right of the canvas
 * @param totalTimeRange the start and end time in seconds for the entire panel
 * @param viewRange a tuple with each element in [0,1] of form [start, end], where 0 initially represents
 * the start time and 1 initially represents the end time, but these will update as the user interacts.
 * @returns The computed time in seconds
 */
export const computeTimeSecsForMouseXPositionFraction = (
  mouseXPosition: number,
  totalTimeRange: WeavessTypes.TimeRange,
  viewRange: [number, number]
): number => {
  const timeRangeSecs = totalTimeRange.endTimeSecs - totalTimeRange.startTimeSecs;
  const leftCanvasFraction = totalTimeRange.startTimeSecs + timeRangeSecs * viewRange[0];
  const rightCanvasFraction = totalTimeRange.startTimeSecs + timeRangeSecs * viewRange[1];
  const scale = d3.scaleLinear().domain([0, 1]).range([leftCanvasFraction, rightCanvasFraction]);
  return scale(mouseXPosition);
};

/**
 * Converts pixels to a fractional position of the canvas, from 0 to 1.
 *
 * @param canvasBoundingRect the DOM rectangle representing the size and position of the weavess canvas
 * @param xOffsetPx the x position in pixels from the left side
 */
export const convertPixelOffsetToFractionalPosition = (
  canvasBoundingRect: DOMRect,
  xOffsetPx: number
): number => {
  const leftOffset = canvasBoundingRect?.left ?? 0;
  const width = canvasBoundingRect?.width ?? 0;
  return (xOffsetPx - leftOffset) / width;
};

/**
 *
 * @param xOffsetPx the position in pixels from the left side of the window
 * @param canvasBoundingRect the bounding rectangle representing the weavess canvas
 * @param totalTimeRange the time range in seconds from start to end of all data loaded into weavess
 * @param viewRange the fraction of the time that is visible on the canvas. If the full range is visible, it will be [0, 1].
 * @returns the time in seconds that the pixel offset represents.
 */
export const convertPixelOffsetToTime = (
  xOffsetPx: number,
  canvasBoundingRect: DOMRect,
  totalTimeRange: WeavessTypes.TimeRange,
  viewRange: [number, number]
): number => {
  return computeTimeSecsForMouseXPositionFraction(
    convertPixelOffsetToFractionalPosition(canvasBoundingRect, xOffsetPx),
    totalTimeRange,
    viewRange
  );
};
