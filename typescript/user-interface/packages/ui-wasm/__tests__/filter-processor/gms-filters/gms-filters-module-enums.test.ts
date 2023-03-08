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
import { FilterDesignModel } from '../../../src/ts/filter-processor/gms-filters/types/filter-design-model';
import type { GmsFiltersModule } from '../../../src/ts/ui-wasm';
// ! IGNORED TO SUPPORT ESLINT CHECKS WITHOUT REQUIRING TO BUILD THE WASM
// eslint-disable-next-line @typescript-eslint/ban-ts-comment
// @ts-ignore
// eslint-disable-next-line import/no-unresolved
import gmsFilters from '../../../src/ts/wasm/gms-filters/gms-filters.mjs';

describe('GMS Filters Enums Test', () => {
  test('enum FilterComputationType is defined and equal', async () => {
    const gmsFiltersModule: GmsFiltersModule = await gmsFilters();
    expect(gmsFiltersModule).toBeDefined();

    expect(gmsFiltersModule.FilterComputationType).toBeDefined();
    expect(gmsFiltersModule.FilterComputationType.FIR.value).toEqual(FilterComputationType.FIR);
    expect(gmsFiltersModule.FilterComputationType.IIR.value).toEqual(FilterComputationType.IIR);
    expect(gmsFiltersModule.FilterComputationType.AR.value).toEqual(FilterComputationType.AR);
    expect(gmsFiltersModule.FilterComputationType.PM.value).toEqual(FilterComputationType.PM);
  });

  test('enum FilterDesignModel is defined and equal', async () => {
    const gmsFiltersModule: GmsFiltersModule = await gmsFilters();
    expect(gmsFiltersModule).toBeDefined();

    expect(gmsFiltersModule.FilterDesignModel).toBeDefined();
    expect(gmsFiltersModule.FilterDesignModel.BUTTERWORTH.value).toEqual(
      FilterDesignModel.BUTTERWORTH
    );
    expect(gmsFiltersModule.FilterDesignModel.CHEBYSHEV_I.value).toEqual(
      FilterDesignModel.CHEBYSHEV_I
    );
    expect(gmsFiltersModule.FilterDesignModel.CHEBYSHEV_II.value).toEqual(
      FilterDesignModel.CHEBYSHEV_II
    );
    expect(gmsFiltersModule.FilterDesignModel.ELLIPTIC.value).toEqual(FilterDesignModel.ELLIPTIC);
  });

  test('enum FilterBandType is defined and equal', async () => {
    const gmsFiltersModule: GmsFiltersModule = await gmsFilters();
    expect(gmsFiltersModule).toBeDefined();

    expect(gmsFiltersModule.FilterBandType).toBeDefined();
    expect(gmsFiltersModule.FilterBandType.LOW_PASS.value).toEqual(FilterBandType.LOW_PASS);
    expect(gmsFiltersModule.FilterBandType.HIGH_PASS.value).toEqual(FilterBandType.HIGH_PASS);
    expect(gmsFiltersModule.FilterBandType.BAND_PASS.value).toEqual(FilterBandType.BAND_PASS);
    expect(gmsFiltersModule.FilterBandType.BAND_REJECT.value).toEqual(FilterBandType.BAND_REJECT);
  });
});
