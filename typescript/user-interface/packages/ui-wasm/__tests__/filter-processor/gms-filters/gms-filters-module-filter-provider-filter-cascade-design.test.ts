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
import {
  sampleCascadedFilterDefinition,
  sampleCascadedFilterDefinitionDesigned
} from './sample-filter-definition';

describe('FilterProvider::filterCascadeDesign', () => {
  test('FilterProvider::filterCascadeDesign test', async () => {
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
        sampleCascadedFilterDefinition.filterDescription.parameters.sampleRateToleranceHz
      );

      const sosNumerator1 = new Float64Array(
        sampleCascadedFilterDefinition.filterDescription.filterDescriptions[0].parameters.bCoefficients
      );
      const sosDenominator1 = new Float64Array(
        sampleCascadedFilterDefinition.filterDescription.filterDescriptions[0].parameters.aCoefficients
      );
      const sosCoefficients1 = new Float64Array();

      iirFilterParameters1 = gmsFiltersModule.IIRFilterParameters.buildWithTypedArray(
        sosNumerator1,
        sosDenominator1,
        sosCoefficients1,
        false,
        sosNumerator1.length,
        sampleCascadedFilterDefinition.filterDescription.filterDescriptions[0].parameters
          .groupDelaySec
      );

      const sosNumerator2 = new Float64Array(
        sampleCascadedFilterDefinition.filterDescription.filterDescriptions[1].parameters.bCoefficients
      );
      const sosDenominator2 = new Float64Array(
        sampleCascadedFilterDefinition.filterDescription.filterDescriptions[1].parameters.aCoefficients
      );
      const sosCoefficients2 = new Float64Array();

      iirFilterParameters2 = gmsFiltersModule.IIRFilterParameters.buildWithTypedArray(
        sosNumerator2,
        sosDenominator2,
        sosCoefficients2,
        false,
        sosNumerator2.length,
        sampleCascadedFilterDefinition.filterDescription.filterDescriptions[1].parameters
          .groupDelaySec
      );

      const filterDesignModel1 =
        FilterDesignModel[
          sampleCascadedFilterDefinition.filterDescription.filterDescriptions[0].filterType.split(
            '_'
          )[1] as keyof typeof FilterDesignModel
        ];

      const filterComputationType1 =
        FilterComputationType[
          sampleCascadedFilterDefinition.filterDescription.filterDescriptions[0].filterType.split(
            '_'
          )[0] as keyof typeof FilterComputationType
        ];

      const filterBandType1 =
        FilterBandType[
          sampleCascadedFilterDefinition.filterDescription.filterDescriptions[0]
            .passBandType as keyof typeof FilterBandType
        ];

      const filterDesignModel2 =
        FilterDesignModel[
          sampleCascadedFilterDefinition.filterDescription.filterDescriptions[1].filterType.split(
            '_'
          )[1] as keyof typeof FilterDesignModel
        ];

      const filterComputationType2 =
        FilterComputationType[
          sampleCascadedFilterDefinition.filterDescription.filterDescriptions[1].filterType.split(
            '_'
          )[0] as keyof typeof FilterComputationType
        ];

      const filterBandType2 =
        FilterBandType[
          sampleCascadedFilterDefinition.filterDescription.filterDescriptions[1]
            .passBandType as keyof typeof FilterBandType
        ];

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
        0
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
        0
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

      filterDefinition = gmsFiltersModule.FilterDefinition.build(
        cascadedFiltersParameters,
        vectorFilterDescription,
        sampleCascadedFilterDefinition.name,
        sampleCascadedFilterDefinition.comments,
        false,
        true,
        vectorFilterDescription.size()
      );

      expect(iirFilterParameters1.isDesigned).toBeFalsy();
      expect(iirFilterParameters2.isDesigned).toBeFalsy();
      expect(filterDefinition.numberOfFilterDescriptions).toEqual(2);

      const result = gmsFiltersModule.FilterProvider.filterCascadeDesign(filterDefinition);
      expect(result).toBeDefined();
      expect(result.isDesigned).toBeTruthy();

      expect(result.cascadedFiltersParameters.comments).toEqual(
        sampleCascadedFilterDefinitionDesigned.filterDescription.comments
      );
      expect(result.cascadedFiltersParameters.isCausal).toEqual(
        sampleCascadedFilterDefinitionDesigned.filterDescription.causal
      );
      expect(result.cascadedFiltersParameters.sampleRate).toEqual(
        sampleCascadedFilterDefinitionDesigned.filterDescription.parameters.sampleRateHz
      );
      expect(result.cascadedFiltersParameters.sampleRateTolerance).toEqual(
        sampleCascadedFilterDefinitionDesigned.filterDescription.parameters.sampleRateToleranceHz
      );
      expect(result.cascadedFiltersParameters.groupDelay).toEqual(
        sampleCascadedFilterDefinitionDesigned.filterDescription.parameters.groupDelaySec
      );

      expect(result.filterDescriptions).toBeDefined();
      expect(result.numberOfFilterDescriptions).toEqual(
        sampleCascadedFilterDefinitionDesigned.filterDescription.filterDescriptions.length
      );

      for (let i = 0; i < result.filterDescriptions.size(); i += 1) {
        expect(
          result.filterDescriptions
            .get(i)
            .linearIIRFilterDescription.iirFilterParameters.sosNumerator.size()
        ).toEqual(
          sampleCascadedFilterDefinitionDesigned.filterDescription.filterDescriptions[i].parameters
            .bCoefficients.length
        );
        for (
          let j = 0;
          j <
          result.filterDescriptions
            .get(i)
            .linearIIRFilterDescription.iirFilterParameters.sosNumerator.size();
          j += 1
        ) {
          expect(
            result.filterDescriptions
              .get(i)
              .linearIIRFilterDescription.iirFilterParameters.sosNumerator.get(j)
          ).toEqual(
            sampleCascadedFilterDefinitionDesigned.filterDescription.filterDescriptions[i]
              .parameters.bCoefficients[j]
          );
        }

        expect(
          result.filterDescriptions
            .get(i)
            .linearIIRFilterDescription.iirFilterParameters.sosDenominator.size()
        ).toEqual(
          sampleCascadedFilterDefinitionDesigned.filterDescription.filterDescriptions[i].parameters
            .aCoefficients.length
        );
        for (
          let j = 0;
          j <
          result.filterDescriptions
            .get(i)
            .linearIIRFilterDescription.iirFilterParameters.sosDenominator.size();
          j += 1
        ) {
          expect(
            result.filterDescriptions
              .get(i)
              .linearIIRFilterDescription.iirFilterParameters.sosDenominator.get(j)
          ).toEqual(
            sampleCascadedFilterDefinitionDesigned.filterDescription.filterDescriptions[i]
              .parameters.aCoefficients[j]
          );

          expect(
            result.filterDescriptions.get(i).linearIIRFilterDescription.iirFilterParameters
              .isDesigned
          ).toBeTruthy();
          expect(
            result.filterDescriptions.get(i).linearIIRFilterDescription.iirFilterParameters
              .numberOfSos
          ).toEqual(
            sampleCascadedFilterDefinitionDesigned.filterDescription.filterDescriptions[i]
              .parameters.aCoefficients.length
          );
          expect(
            result.filterDescriptions.get(i).linearIIRFilterDescription.iirFilterParameters
              .groupDelay
          ).toEqual(
            sampleCascadedFilterDefinitionDesigned.filterDescription.filterDescriptions[i]
              .parameters.groupDelaySec
          );

          expect(
            result.filterDescriptions.get(i).linearIIRFilterDescription.filterDesignModel.value
          ).toEqual(FilterDesignModel.BUTTERWORTH);

          expect(
            result.filterDescriptions.get(i).linearIIRFilterDescription.filterBandType.value
          ).toEqual(FilterBandType.BAND_PASS);

          expect(result.filterDescriptions.get(i).linearIIRFilterDescription.cutoffLow).toEqual(
            sampleCascadedFilterDefinitionDesigned.filterDescription.filterDescriptions[i]
              .lowFrequency
          );

          expect(result.filterDescriptions.get(i).linearIIRFilterDescription.cutoffHigh).toEqual(
            sampleCascadedFilterDefinitionDesigned.filterDescription.filterDescriptions[i]
              .highFrequency
          );

          expect(result.filterDescriptions.get(i).linearIIRFilterDescription.filterOrder).toEqual(
            sampleCascadedFilterDefinitionDesigned.filterDescription.filterDescriptions[i].order
          );

          expect(result.filterDescriptions.get(i).linearIIRFilterDescription.sampleRate).toEqual(
            sampleCascadedFilterDefinitionDesigned.filterDescription.filterDescriptions[i]
              .parameters.sampleRateHz
          );

          expect(
            result.filterDescriptions.get(i).linearIIRFilterDescription.sampleRateTolerance
          ).toEqual(
            sampleCascadedFilterDefinitionDesigned.filterDescription.filterDescriptions[i]
              .parameters.sampleRateToleranceHz
          );

          expect(!!result.filterDescriptions.get(i).linearIIRFilterDescription.zeroPhase).toEqual(
            sampleCascadedFilterDefinitionDesigned.filterDescription.filterDescriptions[i].zeroPhase
          );

          expect(result.filterDescriptions.get(i).filterComputationType.value).toEqual(
            FilterComputationType.IIR
          );

          expect(result.filterDescriptions.get(i).comments).toEqual(
            sampleCascadedFilterDefinitionDesigned.filterDescription.filterDescriptions[i].comments
          );

          expect(result.filterDescriptions.get(i).isCausal).toEqual(
            sampleCascadedFilterDefinitionDesigned.filterDescription.filterDescriptions[i].causal
          );

          expect(result.isDesigned).toBeTruthy();

          expect(result.name).toEqual(sampleCascadedFilterDefinitionDesigned.name);
          expect(result.comments).toEqual(sampleCascadedFilterDefinitionDesigned.comments);
        }
      }

      // * test 2nd description
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
