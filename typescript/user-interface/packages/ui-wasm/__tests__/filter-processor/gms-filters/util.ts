/**
 * @jest-environment node
 */

/* eslint-disable @typescript-eslint/no-magic-numbers */
import waveformPayload from './90-minute-waveform-payload.json';

export const ORDER = 1;
export const Hz20 = 20.0;
export const Hz40 = 40.0;
export const AMPLITUDE = 2.123456789;
export const FREQUENCY = 0.3123456789;
export const FLOW = 1.0;
export const FHIGH = 2.0;
export const NUM_SAMPLES = 72000;
export const NUM_FTYPES = 4;
export const NUM_ZP = 2;
export const NUM_ADJUST = 2;
export const NUM_CALLS = 3;
export const NUM_DATA_N = 9;
export const DOUBLE_SIZE = 8;
export const FLOAT_SIZE = 4;

export const loadPayload = (): Float64Array => {
  let cnt = 0;
  const temp: number[] = [];
  Object.values(waveformPayload).forEach((value, i) => {
    if (i % 2 !== 0) {
      temp[cnt] = waveformPayload[i];
      cnt += 1;
    }
  });
  return Float64Array.from([...temp, ...temp, ...temp, ...temp]);
};
