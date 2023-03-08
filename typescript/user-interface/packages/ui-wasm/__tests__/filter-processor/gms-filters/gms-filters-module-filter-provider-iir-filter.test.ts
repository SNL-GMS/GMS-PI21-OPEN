/* eslint-disable no-underscore-dangle */
/* eslint-disable @typescript-eslint/no-magic-numbers */
/**
 * @jest-environment node
 */

/**
 * !!! Super important info about returning array values
 * https://stackoverflow.com/questions/17883799/how-to-handle-passing-returning-array-pointers-to-emscripten-compiled-code
 */

import type { GmsFiltersModule } from '../../../src/ts/ui-wasm';
// ! IGNORED TO SUPPORT ESLINT CHECKS WITHOUT REQUIRING TO BUILD THE WASM
// eslint-disable-next-line @typescript-eslint/ban-ts-comment
// @ts-ignore
// eslint-disable-next-line import/no-unresolved
import gmsFilters from '../../../src/ts/wasm/gms-filters/gms-filters.mjs';
import { AMPLITUDE, DOUBLE_SIZE, FREQUENCY, Hz20, loadPayload, NUM_SAMPLES } from './util';

describe('FilterProvider::cFilterIIRApply', () => {
  let realWaveform: Float64Array;

  beforeAll(() => {
    realWaveform = loadPayload();
  });

  test('FilterProvider::cFilterIIRApply test', async () => {
    const gmsFiltersModule: GmsFiltersModule = await gmsFilters();
    expect(gmsFiltersModule).toBeDefined();

    const dataInputs: number[] = [];
    for (let n = 0; n < NUM_SAMPLES; n += 1) {
      dataInputs[n] = (AMPLITUDE * Math.sin(2 * Math.PI * n * FREQUENCY)) / Hz20;
    }
    // Filter is destructive. Preserve inputs for comparison!
    const inputs = dataInputs.map(x => x);

    const sosNumerator = new Float64Array([1.1, 2.2, 3.3]);
    const sosDenominator = new Float64Array([4.4, 5.5, 6.6]);
    const sosCoefficients = new Float64Array([7.7, 8.8, 9.9]);

    let inputPtr = 0;
    let sosNumeratorPtr = 0;
    let sosDenominatorPtr = 0;
    let sosCoefficientsPtr = 0;
    let results: Float64Array = new Float64Array();

    try {
      inputPtr = gmsFiltersModule._malloc(NUM_SAMPLES * DOUBLE_SIZE);
      gmsFiltersModule.HEAPF64.set(inputs, inputPtr / DOUBLE_SIZE);

      sosNumeratorPtr = gmsFiltersModule._malloc(
        sosNumerator.BYTES_PER_ELEMENT * sosNumerator.length
      );
      gmsFiltersModule.HEAPF64.set(sosNumerator, sosNumeratorPtr / sosNumerator.BYTES_PER_ELEMENT);

      sosDenominatorPtr = gmsFiltersModule._malloc(
        sosDenominator.BYTES_PER_ELEMENT * sosDenominator.length
      );
      gmsFiltersModule.HEAPF64.set(
        sosDenominator,
        sosDenominatorPtr / sosDenominator.BYTES_PER_ELEMENT
      );

      sosCoefficientsPtr = gmsFiltersModule._malloc(
        sosCoefficients.BYTES_PER_ELEMENT * sosCoefficients.length
      );
      gmsFiltersModule.HEAPF64.set(
        sosCoefficients,
        sosCoefficientsPtr / sosDenominator.BYTES_PER_ELEMENT
      );

      const cFilterIIRApply = gmsFiltersModule.cwrap('cFilterIIRApply', null, [
        'number',
        'number',
        'number',
        'number',
        'number',
        'number',
        'number',
        'number',
        'number'
      ]);

      cFilterIIRApply(inputPtr, NUM_SAMPLES, 0, 1, 0, 0, sosNumeratorPtr, sosDenominatorPtr, 3);

      // this is the values stored in the input pointer. They should have changed.
      results = gmsFiltersModule.HEAPF64.subarray(
        inputPtr / DOUBLE_SIZE,
        inputPtr / DOUBLE_SIZE + NUM_SAMPLES
      );

      expect(results).toHaveLength(dataInputs.length);
    } catch (e) {
      console.error(e);
      // eslint-disable-next-line jest/no-conditional-expect
      expect(e).not.toBeDefined();
    } finally {
      gmsFiltersModule._free(sosNumeratorPtr);
      gmsFiltersModule._free(sosDenominatorPtr);
      gmsFiltersModule._free(sosCoefficientsPtr);
      gmsFiltersModule._free(inputPtr);
      gmsFiltersModule._free(results as any);
    }
  });

  test('FilterProvider::cFilterIIRApply simple performance test', async () => {
    const gmsFiltersModule: GmsFiltersModule = await gmsFilters();
    expect(gmsFiltersModule).toBeDefined();
    const sampleSize = realWaveform.length;

    const sosNumerator = new Float64Array([1.1, 2.2, 3.3]);
    const sosDenominator = new Float64Array([4.4, 5.5, 6.6]);
    const sosCoefficients = new Float64Array([7.7, 8.8, 9.9]);

    let inputPtr = 0;
    let sosNumeratorPtr = 0;
    let sosDenominatorPtr = 0;
    let sosCoefficientsPtr = 0;
    let results: Float64Array = new Float64Array();

    try {
      inputPtr = gmsFiltersModule._malloc(sampleSize * DOUBLE_SIZE);
      gmsFiltersModule.HEAPF64.set(realWaveform, inputPtr / DOUBLE_SIZE);

      sosNumeratorPtr = gmsFiltersModule._malloc(
        sosNumerator.BYTES_PER_ELEMENT * sosNumerator.length
      );
      gmsFiltersModule.HEAPF64.set(sosNumerator, sosNumeratorPtr / sosNumerator.BYTES_PER_ELEMENT);

      sosDenominatorPtr = gmsFiltersModule._malloc(
        sosDenominator.BYTES_PER_ELEMENT * sosDenominator.length
      );
      gmsFiltersModule.HEAPF64.set(
        sosDenominator,
        sosDenominatorPtr / sosDenominator.BYTES_PER_ELEMENT
      );

      sosCoefficientsPtr = gmsFiltersModule._malloc(
        sosCoefficients.BYTES_PER_ELEMENT * sosCoefficients.length
      );
      gmsFiltersModule.HEAPF64.set(
        sosCoefficients,
        sosCoefficientsPtr / sosDenominator.BYTES_PER_ELEMENT
      );

      const cFilterIIRApply = gmsFiltersModule.cwrap('cFilterIIRApply', null, [
        'number',
        'number',
        'number',
        'number',
        'number',
        'number',
        'number',
        'number',
        'number'
      ]);

      const startingMs = Date.now();

      cFilterIIRApply(inputPtr, sampleSize, 0, 1, 0, 0, sosNumeratorPtr, sosDenominatorPtr, 3);

      // this is the values stored in the input pointer. They should have changed.
      results = gmsFiltersModule.HEAPF64.subarray(
        inputPtr / DOUBLE_SIZE,
        inputPtr / DOUBLE_SIZE + sampleSize
      );

      const totalMs = Date.now() - startingMs;
      console.debug(`Total running time: ${totalMs}`);
      expect(results).toHaveLength(sampleSize);
      expect(realWaveform).not.toBeNull();
      expect(realWaveform).toHaveLength(sampleSize);
      expect(realWaveform[0]).toEqual(226.9774932861328);

      if (totalMs > 50) {
        console.warn('Running gmsFiltersModule IIR FILTER took more than 50ms');
      }
      expect(totalMs).toBeLessThanOrEqual(200); // 200 ms
    } catch (e) {
      console.error(e);
      // eslint-disable-next-line jest/no-conditional-expect
      expect(e).not.toBeDefined();
    } finally {
      gmsFiltersModule._free(sosNumeratorPtr);
      gmsFiltersModule._free(sosDenominatorPtr);
      gmsFiltersModule._free(sosCoefficientsPtr);
      gmsFiltersModule._free(inputPtr);
      gmsFiltersModule._free(results as any);
    }
  });
});
