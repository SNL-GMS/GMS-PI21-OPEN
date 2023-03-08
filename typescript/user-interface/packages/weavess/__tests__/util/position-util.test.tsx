/* eslint-disable @typescript-eslint/no-magic-numbers */
import type { WeavessTypes } from '@gms/weavess-core/lib/weavess-core';

import {
  computeTimeSecsForMouseXPositionFraction,
  convertPixelOffsetToTime,
  getMeasureWindowSelectionAreaFraction
} from '../../src/ts/util/position-util';

const HOUR = 60 * 60;
const MIDNIGHT = 24 * HOUR; // in seconds
const FIFTEEN_MINS = 15 * 60; // in seconds
const timeRange: WeavessTypes.TimeRange = {
  startTimeSecs: MIDNIGHT,
  endTimeSecs: MIDNIGHT + HOUR
};
const viewableTimeRange: WeavessTypes.TimeRange = {
  startTimeSecs: timeRange.startTimeSecs - FIFTEEN_MINS,
  endTimeSecs: timeRange.endTimeSecs + FIFTEEN_MINS
};

const domRect: DOMRect = {
  height: 100,
  width: 100,
  x: 0,
  y: 0,
  bottom: 0,
  top: 100,
  left: 0,
  right: 100,
  toJSON: jest.fn()
};

const offsetSeconds = 0;

describe('position util', () => {
  test('getMeasureWindowSelectionAreaFraction matches snapshot', () => {
    // First call with undefined time range
    expect(
      getMeasureWindowSelectionAreaFraction(
        undefined,
        viewableTimeRange.startTimeSecs,
        viewableTimeRange.endTimeSecs,
        offsetSeconds
      )
    ).toBeUndefined();

    // Now call with legit time rang
    expect(
      getMeasureWindowSelectionAreaFraction(
        timeRange,
        viewableTimeRange.startTimeSecs,
        viewableTimeRange.endTimeSecs,
        offsetSeconds
      )
    ).toMatchSnapshot();
  });

  test('computeTimeSecsForMouseXPositionFraction matches snapshot', () => {
    expect(
      computeTimeSecsForMouseXPositionFraction(0.5, viewableTimeRange, [0, 1])
    ).toMatchInlineSnapshot(`88200`);
  });

  test('convertPixelOffsetToTime matches snapshot', () => {
    // Note this calls computeTimeSecsForMouseXPositionFraction and convertPixelOffsetToFractionalPosition
    // functions in position util
    expect(convertPixelOffsetToTime(50, domRect, viewableTimeRange, [0, 1])).toMatchInlineSnapshot(
      `88200`
    );

    // call it with undefined DOM
    expect(
      convertPixelOffsetToTime(50, {} as DOMRect, viewableTimeRange, [0, 1])
    ).toMatchInlineSnapshot(`NaN`);
  });
});
