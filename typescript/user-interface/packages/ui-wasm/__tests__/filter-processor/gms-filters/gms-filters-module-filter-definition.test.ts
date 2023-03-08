/* eslint-disable no-underscore-dangle */
/* eslint-disable @typescript-eslint/no-magic-numbers */
/**
 * @jest-environment node
 */

/**
 * !!! Super important info about returning array values
 * https://stackoverflow.com/questions/17883799/how-to-handle-passing-returning-array-pointers-to-emscripten-compiled-code
 */

import type { CascadedFiltersParameters } from '../../../src/ts/filter-processor/gms-filters/types/cascade-filter-parameters';
import { FilterBandType } from '../../../src/ts/filter-processor/gms-filters/types/filter-band-type';
import { FilterComputationType } from '../../../src/ts/filter-processor/gms-filters/types/filter-computation-type';
import type { FilterDefinition } from '../../../src/ts/filter-processor/gms-filters/types/filter-definition';
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

describe('GMS Filters Filter Definition Test', () => {
  test('FilterDefinition is defined and can be created', async () => {
    const gmsFiltersModule: GmsFiltersModule = await gmsFilters();

    expect(gmsFiltersModule.FilterDefinition).toBeDefined();
    expect(gmsFiltersModule.FilterDefinition.build).toBeDefined();

    let vectorFilterDescription: VectorFilterDescription;
    let cascadedFiltersParameters: CascadedFiltersParameters;
    let iirFilterParameters: IIRFilterParameters;
    let linearIIRFilterDescription: LinearIIRFilterDescription;
    let filterDescription1: FilterDescription;
    let filterDescription2: FilterDescription;
    let filterDefinition: FilterDefinition;

    try {
      const sosNumerator = new Float64Array([1.1, 2.2, 3.3]);
      const sosDenominator = new Float64Array([4.4, 5.5, 6.6]);
      const sosCoefficients = new Float64Array([7.7, 8.8, 9.9]);

      cascadedFiltersParameters = gmsFiltersModule.CascadedFiltersParameters.build(
        'my comments',
        false,
        1,
        2,
        3
      );

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

      filterDescription1 = gmsFiltersModule.FilterDescription.build(
        linearIIRFilterDescription,
        FilterComputationType.IIR,
        'my comments',
        false
      );

      filterDescription2 = gmsFiltersModule.FilterDescription.build(
        linearIIRFilterDescription,
        FilterComputationType.IIR,
        'my comments',
        false
      );

      vectorFilterDescription = new gmsFiltersModule.VectorFilterDescription();
      vectorFilterDescription.push_back(filterDescription1);
      vectorFilterDescription.push_back(filterDescription2);

      filterDefinition = gmsFiltersModule.FilterDefinition.build(
        cascadedFiltersParameters,
        vectorFilterDescription,
        'my name',
        'my comments',
        false,
        true,
        8
      );

      expect(filterDefinition).toBeDefined();

      expect(filterDefinition.cascadedFiltersParameters).toBeDefined();
      expect(filterDefinition.cascadedFiltersParameters.comments).toBeDefined();
      expect(filterDefinition.cascadedFiltersParameters.comments).toEqual('my comments');
      expect(filterDefinition.cascadedFiltersParameters.isCausal).toBeDefined();
      expect(filterDefinition.cascadedFiltersParameters.isCausal).toEqual(false);
      expect(filterDefinition.cascadedFiltersParameters.sampleRate).toBeDefined();
      expect(filterDefinition.cascadedFiltersParameters.sampleRate).toEqual(1);
      expect(filterDefinition.cascadedFiltersParameters.sampleRateTolerance).toBeDefined();
      expect(filterDefinition.cascadedFiltersParameters.sampleRateTolerance).toEqual(2);
      expect(filterDefinition.cascadedFiltersParameters.groupDelay).toBeDefined();
      expect(filterDefinition.cascadedFiltersParameters.groupDelay).toEqual(3);

      expect(filterDefinition.filterDescriptions).toBeDefined();
      expect(filterDefinition.filterDescriptions).toBeDefined();
      expect(filterDefinition.filterDescriptions.size()).toEqual(vectorFilterDescription.size());
      expect(filterDefinition.filterDescriptions).toEqual(vectorFilterDescription);

      expect(filterDefinition.name).toBeDefined();
      expect(filterDefinition.name).toEqual('my name');

      expect(filterDefinition.comments).toBeDefined();
      expect(filterDefinition.comments).toEqual('my comments');

      expect(filterDefinition.isDesigned).toBeDefined();
      expect(filterDefinition.isDesigned).toEqual(false);

      expect(filterDefinition.removeGroupDelay).toBeDefined();
      expect(filterDefinition.removeGroupDelay).toEqual(true);

      expect(filterDefinition.numberOfFilterDescriptions).toBeDefined();
      expect(filterDefinition.numberOfFilterDescriptions).toEqual(8);
    } catch (e) {
      console.error(e);
      // eslint-disable-next-line jest/no-conditional-expect
      expect(e).not.toBeDefined();
    } finally {
      gmsFiltersModule._free(vectorFilterDescription as any);

      cascadedFiltersParameters.delete();
      gmsFiltersModule._free(cascadedFiltersParameters as any);

      iirFilterParameters.delete();
      gmsFiltersModule._free(iirFilterParameters as any);

      linearIIRFilterDescription.delete();
      gmsFiltersModule._free(linearIIRFilterDescription as any);

      filterDescription1.delete();
      gmsFiltersModule._free(filterDescription1 as any);

      filterDescription2.delete();
      gmsFiltersModule._free(filterDescription2 as any);

      filterDefinition.delete();
      gmsFiltersModule._free(filterDefinition as any);
    }
  });
});
