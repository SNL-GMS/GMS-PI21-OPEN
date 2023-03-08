/* eslint-disable no-underscore-dangle */
/* eslint-disable @typescript-eslint/no-magic-numbers */
/**
 * @jest-environment node
 */

/**
 * !!! Super important info about returning array values
 * https://stackoverflow.com/questions/17883799/how-to-handle-passing-returning-array-pointers-to-emscripten-compiled-code
 */

import {
  indexInc,
  indexOffset,
  taper
} from '../../../src/ts/filter-processor/gms-filters/constants';
import { FilterBandType } from '../../../src/ts/filter-processor/gms-filters/types/filter-band-type';
import { FilterDesignModel } from '../../../src/ts/filter-processor/gms-filters/types/filter-design-model';
import type { IIRFilterParameters } from '../../../src/ts/filter-processor/gms-filters/types/iir-filter-parameters';
import type { LinearIIRFilterDescription } from '../../../src/ts/filter-processor/gms-filters/types/linear-iir-filter-definition';
import type { GmsFiltersModule } from '../../../src/ts/ui-wasm';
import { convertFromGMSFiltersLinearIIRFilterDescription } from '../../../src/ts/ui-wasm';
// ! IGNORED TO SUPPORT ESLINT CHECKS WITHOUT REQUIRING TO BUILD THE WASM
// eslint-disable-next-line @typescript-eslint/ban-ts-comment
// @ts-ignore
// eslint-disable-next-line import/no-unresolved
import gmsFilters from '../../../src/ts/wasm/gms-filters/gms-filters.mjs';
import {
  sampleFilterDefinition,
  sampleFilterDefinitionDesigned,
  smallSampleData,
  smallSampleDataResult
} from './sample-filter-definition';

describe('FilterProvider::filterIIRApply', () => {
  test('FilterProvider::filterIIRApply test', async () => {
    const gmsFiltersModule: GmsFiltersModule = await gmsFilters();
    expect(gmsFiltersModule).toBeDefined();

    let iirFilterParameters: IIRFilterParameters;
    let linearIIRFilterDescription: LinearIIRFilterDescription;
    let designedLinearIIRFilterDescription: LinearIIRFilterDescription;

    try {
      const sosNumerator = new Float64Array(
        sampleFilterDefinition.filterDescription.parameters.bCoefficients
      );
      const sosDenominator = new Float64Array(
        sampleFilterDefinition.filterDescription.parameters.aCoefficients
      );
      const sosCoefficients = new Float64Array([]);

      const isDesigned = false;

      iirFilterParameters = gmsFiltersModule.IIRFilterParameters.buildWithTypedArray(
        sosNumerator,
        sosDenominator,
        sosCoefficients,
        isDesigned,
        sosNumerator.length,
        sampleFilterDefinition.filterDescription.parameters.groupDelaySec
      );

      const filterDesignModel =
        FilterDesignModel[
          sampleFilterDefinition.filterDescription.filterType.split(
            '_'
          )[1] as keyof typeof FilterDesignModel
        ];

      const filterBandType =
        FilterBandType[
          sampleFilterDefinition.filterDescription.passBandType as keyof typeof FilterBandType
        ];

      linearIIRFilterDescription = gmsFiltersModule.LinearIIRFilterDescription.build(
        iirFilterParameters,
        filterDesignModel,
        filterBandType,
        sampleFilterDefinition.filterDescription.lowFrequency,
        sampleFilterDefinition.filterDescription.highFrequency,
        sampleFilterDefinition.filterDescription.order,
        sampleFilterDefinition.filterDescription.parameters.sampleRateHz,
        sampleFilterDefinition.filterDescription.parameters.sampleRateToleranceHz,
        +sampleFilterDefinition.filterDescription.zeroPhase,
        taper
      );

      designedLinearIIRFilterDescription = gmsFiltersModule.FilterProvider.filterIIRDesign(
        linearIIRFilterDescription
      );
      expect(designedLinearIIRFilterDescription).toBeDefined();
      expect(designedLinearIIRFilterDescription.iirFilterParameters.isDesigned).toBeTruthy();

      const converted = convertFromGMSFiltersLinearIIRFilterDescription(
        sampleFilterDefinitionDesigned,
        designedLinearIIRFilterDescription
      );
      expect(JSON.stringify(converted)).toEqual(JSON.stringify(sampleFilterDefinitionDesigned));

      const result = gmsFiltersModule.FilterProvider.filterIIRApply(
        smallSampleData,
        indexOffset,
        indexInc,
        linearIIRFilterDescription
      );
      expect(result).toBeDefined();
      expect(result).toEqual(smallSampleDataResult);
    } catch (e) {
      console.error(e);
      // eslint-disable-next-line jest/no-conditional-expect
      expect(e).not.toBeDefined();
    } finally {
      iirFilterParameters.delete();
      gmsFiltersModule._free(iirFilterParameters as any);

      linearIIRFilterDescription.delete();
      gmsFiltersModule._free(linearIIRFilterDescription as any);

      designedLinearIIRFilterDescription.delete();
      gmsFiltersModule._free(designedLinearIIRFilterDescription as any);
    }
  });
});
