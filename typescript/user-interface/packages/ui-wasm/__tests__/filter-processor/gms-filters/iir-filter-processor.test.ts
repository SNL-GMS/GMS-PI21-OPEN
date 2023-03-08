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

import { gmsFiltersModulePromise, iirFilterApply, iirFilterDesign } from '../../../src/ts/ui-wasm';
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

describe('GMS IIR Filters Test', () => {
  beforeAll(async () => {
    await gmsFiltersModulePromise;
  });

  test('exists', async () => {
    const gmsFiltersModule = await gmsFilters();
    expect(gmsFiltersModule).toBeDefined();

    expect(iirFilterDesign).toBeDefined();
    expect(iirFilterApply).toBeDefined();
  });

  test('can call iirFilterDesign', async () => {
    const designed1 = await iirFilterDesign(sampleFilterDefinition);
    const designed2 = await iirFilterDesign(sampleFilterDefinition);

    expect(designed1).toBeDefined();
    expect(JSON.stringify(designed1)).toEqual(JSON.stringify(sampleFilterDefinitionDesigned));

    expect(designed2).toBeDefined();
    expect(JSON.stringify(designed2)).toEqual(JSON.stringify(sampleFilterDefinitionDesigned));

    expect(designed1).toEqual(designed2);
  });

  test('can call iirFilterApply', async () => {
    const designed = await iirFilterDesign(sampleFilterDefinition);

    expect(designed).toBeDefined();
    expect(JSON.stringify(designed)).toEqual(JSON.stringify(sampleFilterDefinitionDesigned));

    const result = await iirFilterApply(designed, new Float64Array(smallSampleData));

    expect(result).toBeDefined();
    expect(result).toEqual(smallSampleDataResult);
  });

  test('check iirFilterDesign error conditions', async () => {
    await expect(
      iirFilterDesign({
        ...sampleFilterDefinition,
        filterDescription: {
          ...sampleFilterDefinition.filterDescription,
          filterType: FilterType.FIR_HAMMING
        }
      })
    ).rejects.toThrow();

    await expect(
      iirFilterDesign(({
        ...sampleFilterDefinition,
        filterDescription: {
          ...sampleFilterDefinition.filterDescription,
          filterDescriptions: []
        }
      } as unknown) as any)
    ).rejects.toThrow();

    await expect(
      iirFilterDesign({
        ...sampleFilterDefinition,
        filterDescription: {
          ...sampleFilterDefinition.filterDescription,
          parameters: {
            ...sampleFilterDefinition.filterDescription.parameters,
            aCoefficients: undefined
          }
        }
      })
    ).rejects.toThrow();

    await expect(
      iirFilterDesign({
        ...sampleFilterDefinition,
        filterDescription: {
          ...sampleFilterDefinition.filterDescription,
          parameters: {
            ...sampleFilterDefinition.filterDescription.parameters,
            bCoefficients: undefined
          }
        }
      })
    ).rejects.toThrow();

    await expect(
      iirFilterDesign({
        ...sampleFilterDefinition,
        filterDescription: {
          ...sampleFilterDefinition.filterDescription,
          parameters: {
            ...sampleFilterDefinition.filterDescription.parameters,
            aCoefficients: []
          }
        }
      })
    ).rejects.toThrow();

    await expect(
      iirFilterDesign({
        ...sampleFilterDefinition,
        filterDescription: {
          ...sampleFilterDefinition.filterDescription,
          parameters: {
            ...sampleFilterDefinition.filterDescription.parameters,
            bCoefficients: []
          }
        }
      })
    ).rejects.toThrow();

    await expect(
      iirFilterDesign({
        ...sampleFilterDefinition,
        filterDescription: {
          ...sampleFilterDefinition.filterDescription,
          parameters: {
            ...sampleFilterDefinition.filterDescription.parameters,
            aCoefficients: [1],
            bCoefficients: [1, 2]
          }
        }
      })
    ).rejects.toThrow();

    await expect(iirFilterDesign(undefined)).rejects.toThrow();
  });

  test('check iirFilterApply error conditions', async () => {
    await expect(iirFilterApply(undefined, new Float64Array())).rejects.toThrow();
  });
});
