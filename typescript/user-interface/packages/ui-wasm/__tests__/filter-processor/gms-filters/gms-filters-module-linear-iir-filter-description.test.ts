/* eslint-disable no-underscore-dangle */
/* eslint-disable @typescript-eslint/no-magic-numbers */
/**
 * @jest-environment node
 */

/**
 * !!! Super important info about returning array values
 * https://stackoverflow.com/questions/17883799/how-to-handle-passing-returning-array-pointers-to-emscripten-compiled-code
 */

import { FilterBandType } from '../../../src/ts/filter-processor/gms-filters/types/filter-band-type';
import { FilterDesignModel } from '../../../src/ts/filter-processor/gms-filters/types/filter-design-model';
import type { IIRFilterParameters } from '../../../src/ts/filter-processor/gms-filters/types/iir-filter-parameters';
import type { LinearIIRFilterDescription } from '../../../src/ts/filter-processor/gms-filters/types/linear-iir-filter-definition';
import type { GmsFiltersModule } from '../../../src/ts/ui-wasm';
// ! IGNORED TO SUPPORT ESLINT CHECKS WITHOUT REQUIRING TO BUILD THE WASM
// eslint-disable-next-line @typescript-eslint/ban-ts-comment
// @ts-ignore
// eslint-disable-next-line import/no-unresolved
import gmsFilters from '../../../src/ts/wasm/gms-filters/gms-filters.mjs';

describe('GMS Filters Linear IIR Filter Description Test', () => {
  test('LinearIIRFilterDescription is defined and can be created', async () => {
    const gmsFiltersModule: GmsFiltersModule = await gmsFilters();
    expect(gmsFiltersModule).toBeDefined();

    expect(gmsFiltersModule.LinearIIRFilterDescription).toBeDefined();
    expect(gmsFiltersModule.LinearIIRFilterDescription.build).toBeDefined();

    let iirFilterParameters: IIRFilterParameters;
    let linearIIRFilterDescription: LinearIIRFilterDescription;

    try {
      const sosNumerator = new Float64Array([1.1, 2.2, 3.3]);
      const sosDenominator = new Float64Array([4.4, 5.5, 6.6]);
      const sosCoefficients = new Float64Array([7.7, 8.8, 9.9]);

      iirFilterParameters = gmsFiltersModule.IIRFilterParameters.buildWithTypedArray(
        sosNumerator,
        sosDenominator,
        sosCoefficients,
        false,
        1,
        3
      );

      linearIIRFilterDescription = gmsFiltersModule.LinearIIRFilterDescription.build(
        iirFilterParameters,
        FilterDesignModel.CHEBYSHEV_I,
        FilterBandType.BAND_REJECT,
        2,
        5,
        1,
        40,
        13,
        2,
        0
      );

      expect(linearIIRFilterDescription).toBeDefined();

      expect(linearIIRFilterDescription.iirFilterParameters).toBeDefined();

      expect(iirFilterParameters.sosNumerator).toBeDefined();
      expect(iirFilterParameters.getSosNumeratorAsTypedArray()).toEqual(sosNumerator);

      expect(iirFilterParameters.sosDenominator).toBeDefined();
      expect(iirFilterParameters.getSosDenominatorAsTypedArray()).toEqual(sosDenominator);

      expect(iirFilterParameters.sosCoefficients).toBeDefined();
      expect(iirFilterParameters.getSosCoefficientsAsTypedArray()).toEqual(sosCoefficients);

      expect(linearIIRFilterDescription.iirFilterParameters.isDesigned).toBeDefined();
      expect(linearIIRFilterDescription.iirFilterParameters.isDesigned).toEqual(false);

      expect(linearIIRFilterDescription.iirFilterParameters.numberOfSos).toBeDefined();
      expect(linearIIRFilterDescription.iirFilterParameters.numberOfSos).toEqual(1);

      expect(linearIIRFilterDescription.iirFilterParameters.groupDelay).toBeDefined();
      expect(linearIIRFilterDescription.iirFilterParameters.groupDelay).toEqual(3);

      expect(linearIIRFilterDescription.filterDesignModel).toBeDefined();
      expect(linearIIRFilterDescription.filterDesignModel.value).toBeDefined();
      expect(linearIIRFilterDescription.filterDesignModel.value).toEqual(
        FilterDesignModel.CHEBYSHEV_I
      );

      expect(linearIIRFilterDescription.filterBandType).toBeDefined();
      expect(linearIIRFilterDescription.filterBandType.value).toBeDefined();
      expect(linearIIRFilterDescription.filterBandType.value).toEqual(FilterBandType.BAND_REJECT);

      expect(linearIIRFilterDescription.cutoffLow).toBeDefined();
      expect(linearIIRFilterDescription.cutoffLow).toEqual(2);

      expect(linearIIRFilterDescription.cutoffHigh).toBeDefined();
      expect(linearIIRFilterDescription.cutoffHigh).toEqual(5);

      expect(linearIIRFilterDescription.filterOrder).toBeDefined();
      expect(linearIIRFilterDescription.filterOrder).toEqual(1);

      expect(linearIIRFilterDescription.sampleRate).toBeDefined();
      expect(linearIIRFilterDescription.sampleRate).toEqual(40);

      expect(linearIIRFilterDescription.sampleRateTolerance).toBeDefined();
      expect(linearIIRFilterDescription.sampleRateTolerance).toEqual(13);

      expect(linearIIRFilterDescription.zeroPhase).toBeDefined();
      expect(linearIIRFilterDescription.zeroPhase).toEqual(2);

      expect(linearIIRFilterDescription.taper).toBeDefined();
      expect(linearIIRFilterDescription.taper).toEqual(0);
    } catch (e) {
      console.error(e);
      // eslint-disable-next-line jest/no-conditional-expect
      expect(e).not.toBeDefined();
    } finally {
      iirFilterParameters.delete();
      gmsFiltersModule._free(iirFilterParameters as any);

      linearIIRFilterDescription.delete();
      gmsFiltersModule._free(linearIIRFilterDescription as any);
    }
  });
});
