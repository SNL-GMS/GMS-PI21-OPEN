/**
 * @jest-environment node
 */

/* eslint-disable @typescript-eslint/unbound-method */
/* eslint-disable no-underscore-dangle */
/* eslint-disable @typescript-eslint/no-magic-numbers */
/**
 * !!! Super important info about returning array values
 * https://stackoverflow.com/questions/17883799/how-to-handle-passing-returning-array-pointers-to-emscripten-compiled-code
 */
import { FilterType } from '@gms/common-model/lib/filter-list/types';
import produce from 'immer';

import {
  cascadeFilterApply,
  cascadeFilterDesign,
  gmsFiltersModulePromise
} from '../../../src/ts/ui-wasm';
// ! IGNORED TO SUPPORT ESLINT CHECKS WITHOUT REQUIRING TO BUILD THE WASM
// eslint-disable-next-line @typescript-eslint/ban-ts-comment
// @ts-ignore
// eslint-disable-next-line import/no-unresolved
import gmsFilters from '../../../src/ts/wasm/gms-filters/gms-filters.mjs';
import {
  sampleCascadedFilterDefinition,
  sampleCascadedFilterDefinitionDesigned,
  smallSampleData,
  smallSampleDataResult
} from './sample-filter-definition';

describe('GMS Cascade Filters Test', () => {
  beforeAll(async () => {
    await gmsFiltersModulePromise;
  });

  test('exists', async () => {
    const gmsFiltersModule = await gmsFilters();
    expect(gmsFiltersModule).toBeDefined();

    expect(cascadeFilterDesign).toBeDefined();
    expect(cascadeFilterApply).toBeDefined();
  });

  test('can call cascadeFilterDesign', async () => {
    const designed1 = await cascadeFilterDesign(sampleCascadedFilterDefinition);
    const designed2 = await cascadeFilterDesign(sampleCascadedFilterDefinition);

    expect(designed1).toBeDefined();
    expect(JSON.stringify(designed1)).toEqual(
      JSON.stringify(sampleCascadedFilterDefinitionDesigned)
    );

    expect(designed2).toBeDefined();
    expect(JSON.stringify(designed2)).toEqual(
      JSON.stringify(sampleCascadedFilterDefinitionDesigned)
    );

    expect(designed1).toEqual(designed2);
  });

  test('can call cascadeFilterApply', async () => {
    const designed = await cascadeFilterDesign(sampleCascadedFilterDefinition);
    expect(designed).toBeDefined();
    expect(JSON.stringify(designed)).toEqual(
      JSON.stringify(sampleCascadedFilterDefinitionDesigned)
    );

    const result = await cascadeFilterApply(designed, smallSampleData);
    expect(result).toBeDefined();
    expect(result).toEqual(smallSampleDataResult);
  });

  test('check cascadeFilterDesign error conditions', async () => {
    await expect(
      cascadeFilterDesign({
        ...sampleCascadedFilterDefinition,
        filterDescription: {
          ...sampleCascadedFilterDefinition.filterDescription,
          filterType: FilterType.FIR_HAMMING as any
        }
      })
    ).rejects.toThrow();

    await expect(
      cascadeFilterDesign(({
        ...sampleCascadedFilterDefinition,
        filterDescription: {
          ...sampleCascadedFilterDefinition.filterDescription,
          filterDescriptions: []
        }
      } as unknown) as any)
    ).rejects.toThrow();

    await expect(
      cascadeFilterDesign({
        ...sampleCascadedFilterDefinition,
        filterDescription: {
          ...sampleCascadedFilterDefinition.filterDescription,
          filterDescriptions: [
            sampleCascadedFilterDefinition.filterDescription.filterDescriptions[0],
            produce(
              sampleCascadedFilterDefinition.filterDescription.filterDescriptions[1],
              draft => {
                draft.parameters = {
                  ...sampleCascadedFilterDefinition.filterDescription.filterDescriptions[1]
                    .parameters,
                  aCoefficients: undefined
                };
              }
            )
          ]
        }
      })
    ).rejects.toThrow();

    await expect(
      cascadeFilterDesign({
        ...sampleCascadedFilterDefinition,
        filterDescription: {
          ...sampleCascadedFilterDefinition.filterDescription,
          filterDescriptions: [
            sampleCascadedFilterDefinition.filterDescription.filterDescriptions[0],
            produce(
              sampleCascadedFilterDefinition.filterDescription.filterDescriptions[1],
              draft => {
                draft.filterType = FilterType.CASCADE as any;
              }
            )
          ]
        }
      })
    ).rejects.toThrow();

    await expect(
      cascadeFilterDesign({
        ...sampleCascadedFilterDefinition,
        filterDescription: {
          ...sampleCascadedFilterDefinition.filterDescription,
          filterDescriptions: [
            sampleCascadedFilterDefinition.filterDescription.filterDescriptions[0],
            produce(
              sampleCascadedFilterDefinition.filterDescription.filterDescriptions[1],
              draft => {
                draft.parameters = {
                  ...sampleCascadedFilterDefinition.filterDescription.filterDescriptions[1]
                    .parameters,
                  bCoefficients: undefined
                };
              }
            )
          ]
        }
      })
    ).rejects.toThrow();

    await expect(
      cascadeFilterDesign({
        ...sampleCascadedFilterDefinition,
        filterDescription: {
          ...sampleCascadedFilterDefinition.filterDescription,
          filterDescriptions: [
            sampleCascadedFilterDefinition.filterDescription.filterDescriptions[0],
            produce(
              sampleCascadedFilterDefinition.filterDescription.filterDescriptions[1],
              draft => {
                draft.parameters = {
                  ...sampleCascadedFilterDefinition.filterDescription.filterDescriptions[1]
                    .parameters,
                  aCoefficients: []
                };
              }
            )
          ]
        }
      })
    ).rejects.toThrow();

    await expect(
      cascadeFilterDesign({
        ...sampleCascadedFilterDefinition,
        filterDescription: {
          ...sampleCascadedFilterDefinition.filterDescription,
          filterDescriptions: [
            sampleCascadedFilterDefinition.filterDescription.filterDescriptions[0],
            produce(
              sampleCascadedFilterDefinition.filterDescription.filterDescriptions[1],
              draft => {
                draft.parameters = {
                  ...sampleCascadedFilterDefinition.filterDescription.filterDescriptions[1]
                    .parameters,
                  bCoefficients: []
                };
              }
            )
          ]
        }
      })
    ).rejects.toThrow();

    await expect(
      cascadeFilterDesign({
        ...sampleCascadedFilterDefinition,
        filterDescription: {
          ...sampleCascadedFilterDefinition.filterDescription,
          filterDescriptions: [
            sampleCascadedFilterDefinition.filterDescription.filterDescriptions[0],
            produce(
              sampleCascadedFilterDefinition.filterDescription.filterDescriptions[1],
              draft => {
                draft.parameters = {
                  ...sampleCascadedFilterDefinition.filterDescription.filterDescriptions[1]
                    .parameters,
                  aCoefficients: [1],
                  bCoefficients: [1, 2]
                };
              }
            )
          ]
        }
      })
    ).rejects.toThrow();

    await expect(cascadeFilterDesign(undefined)).rejects.toThrow();
  });

  test('check cascadeFilterApply error conditions', async () => {
    await expect(cascadeFilterApply(undefined, new Float64Array())).rejects.toThrow();
  });
});
