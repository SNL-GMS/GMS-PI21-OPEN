/* eslint-disable no-underscore-dangle */
/* eslint-disable @typescript-eslint/no-magic-numbers */
/**
 * @jest-environment node
 */

/**
 * !!! Super important info about returning array values
 * https://stackoverflow.com/questions/17883799/how-to-handle-passing-returning-array-pointers-to-emscripten-compiled-code
 */

import type { IIRFilterParameters } from '../../../src/ts/filter-processor/gms-filters/types/iir-filter-parameters';
import type { VectorDouble } from '../../../src/ts/filter-processor/gms-filters/types/vector-double';
import type { GmsFiltersModule } from '../../../src/ts/ui-wasm';
// ! IGNORED TO SUPPORT ESLINT CHECKS WITHOUT REQUIRING TO BUILD THE WASM
// eslint-disable-next-line @typescript-eslint/ban-ts-comment
// @ts-ignore
// eslint-disable-next-line import/no-unresolved
import gmsFilters from '../../../src/ts/wasm/gms-filters/gms-filters.mjs';

describe('GMS Filters IIR Filters Parameters Test', () => {
  test('IIRFilterParameters is defined and can be created', async () => {
    const gmsFiltersModule: GmsFiltersModule = await gmsFilters();
    expect(gmsFiltersModule).toBeDefined();

    expect(gmsFiltersModule.IIRFilterParameters).toBeDefined();
    expect(gmsFiltersModule.IIRFilterParameters.build).toBeDefined();

    let sosNumerator: VectorDouble;
    let sosDenominator: VectorDouble;
    let sosCoefficients: VectorDouble;
    let iirFilterParameters: IIRFilterParameters;

    try {
      sosNumerator = new gmsFiltersModule.VectorDouble();
      sosNumerator.push_back(1.1);
      sosNumerator.push_back(2.2);
      sosNumerator.push_back(3.3);

      sosDenominator = new gmsFiltersModule.VectorDouble();
      sosDenominator.push_back(4.4);
      sosDenominator.push_back(5.5);
      sosDenominator.push_back(6.6);

      sosCoefficients = new gmsFiltersModule.VectorDouble();
      sosCoefficients.push_back(7.7);
      sosCoefficients.push_back(8.8);
      sosCoefficients.push_back(9.9);

      expect(sosNumerator.size()).toEqual(3);
      expect(sosNumerator.get(0)).toEqual(1.1);
      expect(sosNumerator.get(1)).toEqual(2.2);
      expect(sosNumerator.get(2)).toEqual(3.3);

      expect(sosDenominator.size()).toEqual(3);
      expect(sosDenominator.get(0)).toEqual(4.4);
      expect(sosDenominator.get(1)).toEqual(5.5);
      expect(sosDenominator.get(2)).toEqual(6.6);

      expect(sosDenominator.size()).toEqual(3);
      expect(sosCoefficients.get(0)).toEqual(7.7);
      expect(sosCoefficients.get(1)).toEqual(8.8);
      expect(sosCoefficients.get(2)).toEqual(9.9);

      iirFilterParameters = gmsFiltersModule.IIRFilterParameters.build(
        sosNumerator,
        sosDenominator,
        sosCoefficients,
        false,
        3,
        1
      );
      expect(iirFilterParameters).toBeDefined();

      expect(iirFilterParameters.sosNumerator).toBeDefined();
      expect(iirFilterParameters.sosNumerator.size()).toEqual(3);
      expect(iirFilterParameters.sosNumerator.get(0)).toEqual(1.1);
      expect(iirFilterParameters.sosNumerator.get(1)).toEqual(2.2);
      expect(iirFilterParameters.sosNumerator.get(2)).toEqual(3.3);
      expect(iirFilterParameters.sosNumerator).toEqual(sosNumerator);
      expect(iirFilterParameters.getSosNumeratorAsTypedArray()).toEqual(
        new Float64Array([1.1, 2.2, 3.3])
      );

      expect(iirFilterParameters.sosDenominator).toBeDefined();
      expect(iirFilterParameters.sosDenominator.size()).toEqual(3);
      expect(iirFilterParameters.sosDenominator.get(0)).toEqual(4.4);
      expect(iirFilterParameters.sosDenominator.get(1)).toEqual(5.5);
      expect(iirFilterParameters.sosDenominator.get(2)).toEqual(6.6);
      expect(iirFilterParameters.sosDenominator).toEqual(sosDenominator);
      expect(iirFilterParameters.getSosDenominatorAsTypedArray()).toEqual(
        new Float64Array([4.4, 5.5, 6.6])
      );

      expect(iirFilterParameters.sosCoefficients).toBeDefined();
      expect(iirFilterParameters.sosCoefficients.size()).toEqual(3);
      expect(iirFilterParameters.sosCoefficients.get(0)).toEqual(7.7);
      expect(iirFilterParameters.sosCoefficients.get(1)).toEqual(8.8);
      expect(iirFilterParameters.sosCoefficients.get(2)).toEqual(9.9);
      expect(iirFilterParameters.sosCoefficients).toEqual(sosCoefficients);
      expect(iirFilterParameters.getSosCoefficientsAsTypedArray()).toEqual(
        new Float64Array([7.7, 8.8, 9.9])
      );

      expect(iirFilterParameters.isDesigned).toBeDefined();
      expect(iirFilterParameters.isDesigned).toEqual(false);

      expect(iirFilterParameters.numberOfSos).toBeDefined();
      expect(iirFilterParameters.numberOfSos).toEqual(3);

      expect(iirFilterParameters.groupDelay).toBeDefined();
      expect(iirFilterParameters.groupDelay).toEqual(1);
    } catch (e) {
      console.error(e);
      // eslint-disable-next-line jest/no-conditional-expect
      expect(e).not.toBeDefined();
    } finally {
      gmsFiltersModule._free(sosNumerator as any);
      gmsFiltersModule._free(sosDenominator as any);
      gmsFiltersModule._free(sosCoefficients as any);

      iirFilterParameters.delete();
      gmsFiltersModule._free(iirFilterParameters as any);
    }
  });

  test('IIRFilterParameters is defined and can be created using TypedArrays', async () => {
    const gmsFiltersModule: GmsFiltersModule = await gmsFilters();
    expect(gmsFiltersModule).toBeDefined();

    expect(gmsFiltersModule.IIRFilterParameters).toBeDefined();
    expect(gmsFiltersModule.IIRFilterParameters.build).toBeDefined();

    const sosNumerator = new Float64Array([1.1, 2.2, 3.3]);
    const sosDenominator = new Float64Array([4.4, 5.5, 6.6]);
    const sosCoefficients = new Float64Array([7.7, 8.8, 9.9]);
    let iirFilterParameters: IIRFilterParameters;

    try {
      iirFilterParameters = gmsFiltersModule.IIRFilterParameters.buildWithTypedArray(
        sosNumerator,
        sosDenominator,
        sosCoefficients,
        false,
        3,
        1
      );
      expect(iirFilterParameters).toBeDefined();

      expect(iirFilterParameters.sosNumerator).toBeDefined();
      expect(iirFilterParameters.sosNumerator).toBeDefined();
      expect(iirFilterParameters.sosNumerator.size()).toEqual(3);
      expect(iirFilterParameters.sosNumerator.get(0)).toEqual(1.1);
      expect(iirFilterParameters.sosNumerator.get(1)).toEqual(2.2);
      expect(iirFilterParameters.sosNumerator.get(2)).toEqual(3.3);
      expect(iirFilterParameters.getSosNumeratorAsTypedArray()).toEqual(sosNumerator);

      expect(iirFilterParameters.sosDenominator).toBeDefined();
      expect(iirFilterParameters.sosDenominator).toBeDefined();
      expect(iirFilterParameters.sosDenominator.size()).toEqual(3);
      expect(iirFilterParameters.sosDenominator.get(0)).toEqual(4.4);
      expect(iirFilterParameters.sosDenominator.get(1)).toEqual(5.5);
      expect(iirFilterParameters.sosDenominator.get(2)).toEqual(6.6);
      expect(iirFilterParameters.getSosDenominatorAsTypedArray()).toEqual(sosDenominator);

      expect(iirFilterParameters.sosCoefficients).toBeDefined();
      expect(iirFilterParameters.sosCoefficients).toBeDefined();
      expect(iirFilterParameters.sosCoefficients.size()).toEqual(3);
      expect(iirFilterParameters.sosCoefficients.get(0)).toEqual(7.7);
      expect(iirFilterParameters.sosCoefficients.get(1)).toEqual(8.8);
      expect(iirFilterParameters.sosCoefficients.get(2)).toEqual(9.9);
      expect(iirFilterParameters.getSosCoefficientsAsTypedArray()).toEqual(sosCoefficients);

      expect(iirFilterParameters.isDesigned).toBeDefined();
      expect(iirFilterParameters.isDesigned).toEqual(false);

      expect(iirFilterParameters.numberOfSos).toBeDefined();
      expect(iirFilterParameters.numberOfSos).toEqual(3);

      expect(iirFilterParameters.groupDelay).toBeDefined();
      expect(iirFilterParameters.groupDelay).toEqual(1);
    } catch (e) {
      console.error(e);
      // eslint-disable-next-line jest/no-conditional-expect
      expect(e).not.toBeDefined();
    } finally {
      iirFilterParameters.delete();
      gmsFiltersModule._free(iirFilterParameters as any);
    }
  });
});
