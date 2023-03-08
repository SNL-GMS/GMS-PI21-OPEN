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
  removeGroupDelay,
  taper
} from '../../../src/ts/filter-processor/gms-filters/constants';
import type { CascadedFiltersParameters } from '../../../src/ts/filter-processor/gms-filters/types/cascade-filter-parameters';
import type { FilterDefinition } from '../../../src/ts/filter-processor/gms-filters/types/filter-definition';
import type { FilterDescription } from '../../../src/ts/filter-processor/gms-filters/types/filter-description';
import type { IIRFilterParameters } from '../../../src/ts/filter-processor/gms-filters/types/iir-filter-parameters';
import type { LinearIIRFilterDescription } from '../../../src/ts/filter-processor/gms-filters/types/linear-iir-filter-definition';
import type { VectorFilterDescription } from '../../../src/ts/filter-processor/gms-filters/types/vector-filter-description';
import {
  getFilterBandType,
  getFilterComputationType,
  getFilterDesignModel
} from '../../../src/ts/filter-processor/gms-filters/util';
import type { GmsFiltersModule } from '../../../src/ts/ui-wasm';
import { convertFromGMSFiltersFilterDefinition } from '../../../src/ts/ui-wasm';
// ! IGNORED TO SUPPORT ESLINT CHECKS WITHOUT REQUIRING TO BUILD THE WASM
// eslint-disable-next-line @typescript-eslint/ban-ts-comment
// @ts-ignore
// eslint-disable-next-line import/no-unresolved
import gmsFilters from '../../../src/ts/wasm/gms-filters/gms-filters.mjs';
import {
  sampleCascadedFilterDefinition,
  sampleCascadedFilterDefinitionDesigned,
  smallSampleData
} from './sample-filter-definition';

describe('FilterProvider::filterCascadeApply', () => {
  test('FilterProvider::filterCascadeApply test', async () => {
    const gmsFiltersModule: GmsFiltersModule = await gmsFilters();
    expect(gmsFiltersModule).toBeDefined();

    let cascadedFiltersParameters: CascadedFiltersParameters;
    let iirFilterParameters1: IIRFilterParameters;
    let iirFilterParameters2: IIRFilterParameters;
    let linearIIRFilterDescription1: LinearIIRFilterDescription;
    let linearIIRFilterDescription2: LinearIIRFilterDescription;
    let filterDescription1: FilterDescription;
    let filterDescription2: FilterDescription;
    const vectorFilterDescription: VectorFilterDescription = new gmsFiltersModule.VectorFilterDescription();
    let filterDefinition: FilterDefinition;

    try {
      cascadedFiltersParameters = gmsFiltersModule.CascadedFiltersParameters.build(
        sampleCascadedFilterDefinition.filterDescription.comments,
        sampleCascadedFilterDefinition.filterDescription.causal,
        sampleCascadedFilterDefinition.filterDescription.parameters.sampleRateHz,
        sampleCascadedFilterDefinition.filterDescription.parameters.sampleRateToleranceHz,
        sampleCascadedFilterDefinition.filterDescription.parameters.groupDelaySec
      );

      const sosNumerator1 = new Float64Array(
        sampleCascadedFilterDefinition.filterDescription.filterDescriptions[0].parameters.bCoefficients
      );
      const sosDenominator1 = new Float64Array(
        sampleCascadedFilterDefinition.filterDescription.filterDescriptions[0].parameters.aCoefficients
      );
      const sosCoefficients1 = new Float64Array([]);

      iirFilterParameters1 = gmsFiltersModule.IIRFilterParameters.buildWithTypedArray(
        sosNumerator1,
        sosDenominator1,
        sosCoefficients1,
        false,
        sosNumerator1.length,
        sampleCascadedFilterDefinition.filterDescription.filterDescriptions[0].parameters
          .groupDelaySec
      );

      const filterDesignModel1 = getFilterDesignModel(
        sampleCascadedFilterDefinition.filterDescription.filterDescriptions[0].filterType
      );

      const filterComputationType1 = getFilterComputationType(
        sampleCascadedFilterDefinition.filterDescription.filterDescriptions[0].filterType
      );

      const filterBandType1 = getFilterBandType(
        sampleCascadedFilterDefinition.filterDescription.filterDescriptions[0].passBandType
      );

      linearIIRFilterDescription1 = gmsFiltersModule.LinearIIRFilterDescription.build(
        iirFilterParameters1,
        filterDesignModel1,
        filterBandType1,
        sampleCascadedFilterDefinition.filterDescription.filterDescriptions[0].lowFrequency,
        sampleCascadedFilterDefinition.filterDescription.filterDescriptions[0].highFrequency,
        sampleCascadedFilterDefinition.filterDescription.filterDescriptions[0].order,
        sampleCascadedFilterDefinition.filterDescription.filterDescriptions[0].parameters
          .sampleRateHz,
        sampleCascadedFilterDefinition.filterDescription.filterDescriptions[0].parameters
          .sampleRateToleranceHz,
        +sampleCascadedFilterDefinition.filterDescription.filterDescriptions[0].zeroPhase,
        taper
      );

      const sosNumerator2 = new Float64Array(
        sampleCascadedFilterDefinition.filterDescription.filterDescriptions[1].parameters.bCoefficients
      );
      const sosDenominator2 = new Float64Array(
        sampleCascadedFilterDefinition.filterDescription.filterDescriptions[1].parameters.aCoefficients
      );
      const sosCoefficients2 = new Float64Array([]);

      const filterDesignModel2 = getFilterDesignModel(
        sampleCascadedFilterDefinition.filterDescription.filterDescriptions[1].filterType
      );

      const filterComputationType2 = getFilterComputationType(
        sampleCascadedFilterDefinition.filterDescription.filterDescriptions[1].filterType
      );

      const filterBandType2 = getFilterBandType(
        sampleCascadedFilterDefinition.filterDescription.filterDescriptions[1].passBandType
      );

      iirFilterParameters2 = gmsFiltersModule.IIRFilterParameters.buildWithTypedArray(
        sosNumerator2,
        sosDenominator2,
        sosCoefficients2,
        false,
        sosNumerator2.length,
        sampleCascadedFilterDefinition.filterDescription.filterDescriptions[1].parameters
          .groupDelaySec
      );

      linearIIRFilterDescription2 = gmsFiltersModule.LinearIIRFilterDescription.build(
        iirFilterParameters2,
        filterDesignModel2,
        filterBandType2,
        sampleCascadedFilterDefinition.filterDescription.filterDescriptions[1].lowFrequency,
        sampleCascadedFilterDefinition.filterDescription.filterDescriptions[1].highFrequency,
        sampleCascadedFilterDefinition.filterDescription.filterDescriptions[1].order,
        sampleCascadedFilterDefinition.filterDescription.filterDescriptions[1].parameters
          .sampleRateHz,
        sampleCascadedFilterDefinition.filterDescription.filterDescriptions[1].parameters
          .sampleRateToleranceHz,
        +sampleCascadedFilterDefinition.filterDescription.filterDescriptions[1].zeroPhase,
        taper
      );

      filterDescription1 = gmsFiltersModule.FilterDescription.build(
        linearIIRFilterDescription1,
        filterComputationType1,
        sampleCascadedFilterDefinition.filterDescription.filterDescriptions[0].comments,
        sampleCascadedFilterDefinition.filterDescription.filterDescriptions[0].causal
      );

      filterDescription2 = gmsFiltersModule.FilterDescription.build(
        linearIIRFilterDescription2,
        filterComputationType2,
        sampleCascadedFilterDefinition.filterDescription.filterDescriptions[1].comments,
        sampleCascadedFilterDefinition.filterDescription.filterDescriptions[1].causal
      );

      vectorFilterDescription.push_back(filterDescription1);
      vectorFilterDescription.push_back(filterDescription2);

      expect(vectorFilterDescription.size()).toEqual(2);

      filterDefinition = gmsFiltersModule.FilterDefinition.build(
        cascadedFiltersParameters,
        vectorFilterDescription,
        sampleCascadedFilterDefinition.name,
        sampleCascadedFilterDefinition.comments,
        false,
        removeGroupDelay,
        vectorFilterDescription.size()
      );

      const designedFilterDefinition = gmsFiltersModule.FilterProvider.filterCascadeDesign(
        filterDefinition
      );

      expect(designedFilterDefinition.isDesigned).toBeTruthy();
      sampleCascadedFilterDefinition.filterDescription.filterDescriptions.forEach((_, i) => {
        expect(
          designedFilterDefinition.filterDescriptions.get(i).linearIIRFilterDescription
            .iirFilterParameters.isDesigned
        ).toBeTruthy();
      });

      const converted = convertFromGMSFiltersFilterDefinition(
        sampleCascadedFilterDefinition,
        designedFilterDefinition
      );
      expect(JSON.stringify(converted)).toEqual(
        JSON.stringify(sampleCascadedFilterDefinitionDesigned)
      );

      const result = gmsFiltersModule.FilterProvider.filterCascadeApply(
        designedFilterDefinition,
        smallSampleData,
        indexOffset,
        indexInc
      );

      expect(result).toBeDefined();
    } catch (e) {
      console.error(e);
      // eslint-disable-next-line jest/no-conditional-expect
      expect(e).not.toBeDefined();
    } finally {
      cascadedFiltersParameters.delete();
      gmsFiltersModule._free(cascadedFiltersParameters as any);

      iirFilterParameters1.delete();
      gmsFiltersModule._free(iirFilterParameters1 as any);

      iirFilterParameters2.delete();
      gmsFiltersModule._free(iirFilterParameters2 as any);

      linearIIRFilterDescription1.delete();
      gmsFiltersModule._free(linearIIRFilterDescription1 as any);

      linearIIRFilterDescription2.delete();
      gmsFiltersModule._free(linearIIRFilterDescription2 as any);

      filterDescription1.delete();
      gmsFiltersModule._free(filterDescription1 as any);

      filterDescription2.delete();
      gmsFiltersModule._free(filterDescription2 as any);

      gmsFiltersModule._free(vectorFilterDescription as any);

      filterDefinition.delete();
      gmsFiltersModule._free(filterDefinition as any);
    }
  });
});
