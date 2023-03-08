import * as d3 from 'd3';
import orderBy from 'lodash/orderBy';

import type { DataBySampleRate, TimeRange, TimeValuePair } from '../types';

/**
 * Input required to create the position buffer
 */
export interface CreatePositionBufferBySampleRateParams {
  /** Minimum GL value */
  glMin: number;

  /** Maximum GL value */
  glMax: number;

  /** Array containing the vertices */
  values: number[];

  /** Start Time Seconds formatted for display */
  displayStartTimeSecs: number;

  /** End Time Seconds formatted for display */
  displayEndTimeSecs: number;

  /** Start Time in seconds */
  startTimeSecs: number;

  /** End Time in seconds */
  endTimeSecs: number;

  /** End Time in seconds */
  sampleRate: number;
}

/**
 * Input required to create the position buffer
 */
export interface CreatePositionBufferByTimeParams {
  /** Minimum GL value */
  glMin: number;

  /** Maximum GL value */
  glMax: number;

  values: TimeValuePair[];

  /** Start Time Seconds formatted for display */
  displayStartTimeSecs: number;

  /** End Time Seconds formatted for display */
  displayEndTimeSecs: number;
}

/**
 * Convert data in the dataBySampleRate format into a Float32Array position buffer of the format: [x,y,x,y,...]
 *
 * @param dataSampleRate the data to convert
 * @param domain the visible domain in Epoch Seconds, in the form [startTimeSec, endTimeSec]
 *
 * @throws an error if the dataBySampleRate or its values are undefined
 *
 * @returns A promise of a Float32Array of vertices
 */
export const convertToPositionBuffer = (
  dataBySampleRate: DataBySampleRate,
  domain: TimeRange,
  glMin = 0,
  glMax = 100
): Float32Array => {
  if (!dataBySampleRate || dataBySampleRate.values === undefined) {
    throw new Error(
      'Typed array conversion failed; Invalid waveform should contain an array of numbers.'
    );
  }
  if (!domain || domain.endTimeSecs === undefined || domain.endTimeSecs === undefined) {
    throw new Error('Typed array conversion failed; No visible domain was provided.');
  }

  const { values, startTimeSecs, sampleRate } = dataBySampleRate;
  const vertices: Float32Array = new Float32Array(values.length * 2);
  const scaleToGlUnits = d3
    .scaleLinear()
    .domain([domain.startTimeSecs, domain.endTimeSecs])
    .range([glMin, glMax]);

  // Index manipulation for speed
  let i = 0;
  while (i < values.length) {
    vertices[i * 2] = scaleToGlUnits(startTimeSecs + i / sampleRate);
    vertices[i * 2 + 1] = values[i];
    // eslint-disable-next-line no-plusplus
    ++i;
  }
  return vertices;
};

/**
 * Calculate the min, max, and average y-value bounds for the provided position buffer.
 *
 * @param posBuffer formatted buffer of the format x y x y x y x y...
 * @param startIndex inclusive
 * @param endIndex inclusive
 * @returns the min and max y values in the positionBuffer
 */
export const getBoundsForPositionBuffer = (
  posBuffer: Float32Array,
  startIndex = 1,
  endIndex = posBuffer.length - 1
): { max: number; maxSecs: number; min: number; minSecs: number } => {
  let max = -Infinity;
  let maxSecs = -Infinity;
  let min = Infinity;
  let minSecs = Infinity;

  if (posBuffer.length % 2 !== 0) {
    throw new Error('Cannot convert position buffer: must have an even number of elements.');
  }
  if (startIndex % 2 !== 1 || endIndex % 2 !== 1) {
    throw new Error('Cannot convert position buffer: must provide odd indices to access y values.');
  }
  // format is x y x y x y
  // Index manipulation for speed
  let i = startIndex; // the first y value is at index 1. index 0 is an x value
  const end = endIndex + 1 || posBuffer.length;
  while (i < end) {
    // Setting amplitudes min, max and total value
    if (posBuffer[i] > max) {
      max = posBuffer[i];
      maxSecs = posBuffer[i + 1];
    }
    if (posBuffer[i] < min) {
      min = posBuffer[i];
      minSecs = posBuffer[i + 1];
    }
    i += 2;
  }
  return { max, maxSecs, min, minSecs };
};

/**
 * Convert number[] + startTime + sample rate into a 2D position buffer of [x,y,x,y,...].
 *
 * @param params [[ CreatePositionBufferParams ]]
 *
 * @returns A Float32Array of vertices
 */
export const createPositionBufferForDataBySampleRate = ({
  values,
  displayStartTimeSecs,
  displayEndTimeSecs,
  startTimeSecs,
  endTimeSecs,
  sampleRate,
  glMin,
  glMax
}: CreatePositionBufferBySampleRateParams): Float32Array => {
  const vertices = convertToPositionBuffer(
    { values, startTimeSecs, endTimeSecs, sampleRate },
    { startTimeSecs: displayStartTimeSecs, endTimeSecs: displayEndTimeSecs },
    glMin,
    glMax
  );
  return vertices;
};

/**
 * Convert {time,value}[] to position buffer of [x,y,x,y,...].
 *
 * @param data the data by time
 * @param params
 * @returns A Float32Array of vertices
 */
export const createPositionBufferForDataByTime = (
  params: CreatePositionBufferByTimeParams
): Float32Array => {
  // create a typed array to support 2d data
  const vertices: Float32Array = new Float32Array(params.values.length * 2);

  const timeToGlScale = d3
    .scaleLinear()
    .domain([params.displayStartTimeSecs, params.displayEndTimeSecs])
    .range([params.glMin, params.glMax]);

  const values = orderBy(params.values, [value => value.timeSecs]);

  let i = 0;
  // eslint-disable-next-line no-restricted-syntax
  for (const value of values) {
    const x = timeToGlScale(value.timeSecs);
    // eslint-disable-next-line no-plusplus
    vertices[i++] = x;
    // eslint-disable-next-line no-plusplus
    vertices[i++] = value.value;
  }
  return vertices;
};
