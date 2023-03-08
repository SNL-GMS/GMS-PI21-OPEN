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
import { FilterComputationType } from '../../../src/ts/filter-processor/gms-filters/types/filter-computation-type';
import type { FilterDescription } from '../../../src/ts/filter-processor/gms-filters/types/filter-description';
import { FilterDesignModel } from '../../../src/ts/filter-processor/gms-filters/types/filter-design-model';
import type { IIRFilterParameters } from '../../../src/ts/filter-processor/gms-filters/types/iir-filter-parameters';
import type { LinearIIRFilterDescription } from '../../../src/ts/filter-processor/gms-filters/types/linear-iir-filter-definition';
import type { GmsFiltersModule } from '../../../src/ts/ui-wasm';
// ! IGNORED TO SUPPORT ESLINT CHECKS WITHOUT REQUIRING TO BUILD THE WASM
// eslint-disable-next-line @typescript-eslint/ban-ts-comment
// @ts-ignore
// eslint-disable-next-line import/no-unresolved
import gmsFilters from '../../../src/ts/wasm/gms-filters/gms-filters.mjs';

describe('GMS Filters Filter Description Test', () => {
  test('FilterDescription is defined and can be created', async () => {
    const gmsFiltersModule: GmsFiltersModule = await gmsFilters();
    expect(gmsFiltersModule).toBeDefined();

    expect(gmsFiltersModule.FilterDescription).toBeDefined();
    expect(gmsFiltersModule.FilterDescription.build).toBeDefined();

    let iirFilterParameters: IIRFilterParameters;
    let linearIIRFilterDescription: LinearIIRFilterDescription;
    let filterDescription: FilterDescription;

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

      filterDescription = gmsFiltersModule.FilterDescription.build(
        linearIIRFilterDescription,
        FilterComputationType.IIR,
        'my comments',
        false
      );

      expect(filterDescription).toBeDefined();

      expect(filterDescription.linearIIRFilterDescription).toBeDefined();

      expect(filterDescription.linearIIRFilterDescription.iirFilterParameters).toBeDefined();

      expect(iirFilterParameters.sosNumerator).toBeDefined();
      expect(iirFilterParameters.getSosNumeratorAsTypedArray()).toEqual(sosNumerator);

      expect(iirFilterParameters.sosDenominator).toBeDefined();
      expect(iirFilterParameters.getSosDenominatorAsTypedArray()).toEqual(sosDenominator);

      expect(iirFilterParameters.sosCoefficients).toBeDefined();
      expect(iirFilterParameters.getSosCoefficientsAsTypedArray()).toEqual(sosCoefficients);

      expect(
        filterDescription.linearIIRFilterDescription.iirFilterParameters.isDesigned
      ).toBeDefined();
      expect(filterDescription.linearIIRFilterDescription.iirFilterParameters.isDesigned).toEqual(
        false
      );

      expect(
        filterDescription.linearIIRFilterDescription.iirFilterParameters.numberOfSos
      ).toBeDefined();
      expect(filterDescription.linearIIRFilterDescription.iirFilterParameters.numberOfSos).toEqual(
        1
      );

      expect(
        filterDescription.linearIIRFilterDescription.iirFilterParameters.groupDelay
      ).toBeDefined();
      expect(filterDescription.linearIIRFilterDescription.iirFilterParameters.groupDelay).toEqual(
        3
      );

      expect(filterDescription.linearIIRFilterDescription.filterDesignModel).toBeDefined();
      expect(filterDescription.linearIIRFilterDescription.filterDesignModel.value).toBeDefined();
      expect(filterDescription.linearIIRFilterDescription.filterDesignModel.value).toEqual(
        FilterDesignModel.CHEBYSHEV_I
      );

      expect(filterDescription.linearIIRFilterDescription.filterBandType).toBeDefined();
      expect(filterDescription.linearIIRFilterDescription.filterBandType.value).toBeDefined();
      expect(filterDescription.linearIIRFilterDescription.filterBandType.value).toEqual(
        FilterBandType.BAND_REJECT
      );

      expect(filterDescription.linearIIRFilterDescription.cutoffLow).toBeDefined();
      expect(filterDescription.linearIIRFilterDescription.cutoffLow).toEqual(2);

      expect(filterDescription.linearIIRFilterDescription.cutoffHigh).toBeDefined();
      expect(filterDescription.linearIIRFilterDescription.cutoffHigh).toEqual(5);
      expect(filterDescription.linearIIRFilterDescription.filterOrder).toBeDefined();
      expect(filterDescription.linearIIRFilterDescription.filterOrder).toEqual(1);

      expect(filterDescription.linearIIRFilterDescription.sampleRate).toBeDefined();
      expect(filterDescription.linearIIRFilterDescription.sampleRate).toEqual(40);

      expect(filterDescription.linearIIRFilterDescription.sampleRateTolerance).toBeDefined();
      expect(filterDescription.linearIIRFilterDescription.sampleRateTolerance).toEqual(13);

      expect(filterDescription.linearIIRFilterDescription.zeroPhase).toBeDefined();
      expect(filterDescription.linearIIRFilterDescription.zeroPhase).toEqual(2);

      expect(filterDescription.linearIIRFilterDescription.taper).toBeDefined();
      expect(filterDescription.linearIIRFilterDescription.taper).toEqual(0);

      expect(filterDescription.filterComputationType).toBeDefined();
      expect(filterDescription.filterComputationType.value).toBeDefined();
      expect(filterDescription.filterComputationType.value).toEqual(FilterComputationType.IIR);

      expect(filterDescription.comments).toBeDefined();
      expect(filterDescription.comments).toEqual('my comments');

      expect(filterDescription.isCausal).toBeDefined();
      expect(filterDescription.isCausal).toEqual(false);
    } catch (e) {
      console.error(e);
      // eslint-disable-next-line jest/no-conditional-expect
      expect(e).not.toBeDefined();
    } finally {
      iirFilterParameters.delete();
      gmsFiltersModule._free(iirFilterParameters as any);

      linearIIRFilterDescription.delete();
      gmsFiltersModule._free(linearIIRFilterDescription as any);

      filterDescription.delete();
      gmsFiltersModule._free(filterDescription as any);
    }
  });
});
