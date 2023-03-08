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
import type { VectorFilterDescription } from '../../../src/ts/filter-processor/gms-filters/types/vector-filter-description';
import type { GmsFiltersModule } from '../../../src/ts/ui-wasm';
// ! IGNORED TO SUPPORT ESLINT CHECKS WITHOUT REQUIRING TO BUILD THE WASM
// eslint-disable-next-line @typescript-eslint/ban-ts-comment
// @ts-ignore
// eslint-disable-next-line import/no-unresolved
import gmsFilters from '../../../src/ts/wasm/gms-filters/gms-filters.mjs';

describe('GMS Filters Vector Filter Description Test', () => {
  test('vector filter description exists and can be created', async () => {
    const gmsFiltersModule: GmsFiltersModule = await gmsFilters();
    expect(gmsFiltersModule).toBeDefined();

    expect(gmsFiltersModule.VectorFilterDescription).toBeDefined();

    let vectorFilterDescription: VectorFilterDescription;
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

      vectorFilterDescription = new gmsFiltersModule.VectorFilterDescription();
      vectorFilterDescription.push_back(filterDescription);

      expect(vectorFilterDescription).not.toBeNull();
      expect(vectorFilterDescription.size()).toEqual(1);
      expect(vectorFilterDescription.get(0)).toBeDefined();
      expect(vectorFilterDescription.get(0)).toEqual(filterDescription);
    } catch (e) {
      console.error(e);
      // eslint-disable-next-line jest/no-conditional-expect
      expect(e).not.toBeDefined();
    } finally {
      gmsFiltersModule._free(vectorFilterDescription as any);

      iirFilterParameters.delete();
      gmsFiltersModule._free(iirFilterParameters as any);

      linearIIRFilterDescription.delete();
      gmsFiltersModule._free(linearIIRFilterDescription as any);

      filterDescription.delete();
      gmsFiltersModule._free(filterDescription as any);
    }
  });
});
