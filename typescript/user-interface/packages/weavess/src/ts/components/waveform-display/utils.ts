import 'moment-precise-range-plugin';

import { MILLISECONDS_IN_SECOND } from '@gms/common-util';
import type { WeavessTypes } from '@gms/weavess-core';
import { WeavessConstants } from '@gms/weavess-core';
import * as d3 from 'd3';
import moment from 'moment';

/**
 * Calculates the left percentage for a given time based on the provided start and end times.
 *
 * @param timeSeconds The time to calculate the left percentage on
 * @param startTimeSeconds The start time in seconds
 * @param endTimeSeconds The end time in seconds
 *
 * @returns left percentage as a number
 */
export const calculateLeftPercent = (
  timeSeconds: number,
  startTimeSeconds: number,
  endTimeSeconds: number
): number => {
  const scale = d3.scaleLinear().domain([startTimeSeconds, endTimeSeconds]).range([0, 1]);
  return scale(timeSeconds) * WeavessConstants.PERCENT_100;
};
/**
 * Cleans up after THREE js objects such as Camera and Scene
 *
 * @param obj THREE object
 */
// eslint-disable-next-line @typescript-eslint/explicit-module-boundary-types, @typescript-eslint/no-explicit-any
export const clearThree = (obj: any): void => {
  while (obj.children?.length > 0) {
    clearThree(obj.children[0]);
    obj.remove(obj.children[0]);
  }
  if (obj.geometry) obj.geometry.dispose();

  if (obj.material) {
    // in case of map, bumpMap, normalMap, envMap ...
    Object.keys(obj.material).forEach(prop => {
      if (!obj.material[prop]) return;
      if (obj.material[prop] !== null && typeof obj.material[prop].dispose === 'function')
        obj.material[prop].dispose();
    });
    obj.material.dispose();
  }
};

/**
 * Calculates the right percentage for a given time based on the provided start and end times.
 *
 * @param timeSeconds The time to calculate the left percentage on
 * @param startTimeSeconds The start time in seconds
 * @param endTimeSeconds The end time in seconds
 *
 * @returns right percentage as a number
 */
export const calculateRightPercent = (
  timeSeconds: number,
  startTimeSeconds: number,
  endTimeSeconds: number
): number =>
  WeavessConstants.PERCENT_100 -
  calculateLeftPercent(timeSeconds, startTimeSeconds, endTimeSeconds);

/**
 * Helper function to format the number of seconds difference between start and end time
 *
 * @param interval time interval in epoch seconds
 * @returns string formatted number of seconds
 */
const deltaTimeString = (interval: WeavessTypes.TimeRange): string => {
  const deltaSecs = interval.endTimeSecs - interval.startTimeSecs;
  if (deltaSecs > 1) {
    return `${(moment as any).preciseDiff(
      moment.unix(interval.endTimeSecs),
      moment.unix(interval.startTimeSecs)
    )}`;
  }

  let precision = 5;
  if (deltaSecs > 1 / MILLISECONDS_IN_SECOND) {
    precision = 4;
  }
  return `${Number.parseFloat(deltaSecs.toFixed(precision))} seconds`;
};

/**
 * Time range of display interval as human-readable string
 *
 * @param interval
 * @returns interval formatted string to display
 */
export const timeRangeDisplayString = (interval: WeavessTypes.TimeRange): string => {
  if (!interval) {
    return ``;
  }

  return `${moment
    .unix(interval.startTimeSecs)
    .utc()
    .format('YYYY-MM-DD HH:mm:ss.SSS')} + ${deltaTimeString(interval)}`;
};
