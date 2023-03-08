/* eslint-disable @typescript-eslint/no-magic-numbers */
/**
 * @jest-environment node
 */

/**
 * !!! Super important info about returning array values
 * https://stackoverflow.com/questions/17883799/how-to-handle-passing-returning-array-pointers-to-emscripten-compiled-code
 */

import { Constants } from '../../../src/ts/filter-processor/gms-filters/types/constants';
import type { GmsFiltersModule } from '../../../src/ts/ui-wasm';
// ! IGNORED TO SUPPORT ESLINT CHECKS WITHOUT REQUIRING TO BUILD THE WASM
// eslint-disable-next-line @typescript-eslint/ban-ts-comment
// @ts-ignore
// eslint-disable-next-line import/no-unresolved
import gmsFilters from '../../../src/ts/wasm/gms-filters/gms-filters.mjs';

describe('GMS Filters Constants Test', () => {
  test('constants and thresholds are defined and equal', async () => {
    const gmsFiltersModule: GmsFiltersModule = await gmsFilters();
    expect(gmsFiltersModule).toBeDefined();

    expect(gmsFiltersModule.MAX_NAME_SIZE).toBeDefined();
    expect(gmsFiltersModule.MAX_COMMENT_SIZE).toBeDefined();
    expect(gmsFiltersModule.MAX_FILTER_ORDER).toBeDefined();
    expect(gmsFiltersModule.MAX_POLES).toBeDefined();
    expect(gmsFiltersModule.MAX_SOS).toBeDefined();
    expect(gmsFiltersModule.MAX_TRANSFER_FUNCTION).toBeDefined();
    expect(gmsFiltersModule.MAX_FILTER_DESCRIPTIONS).toBeDefined();

    expect(gmsFiltersModule.MAX_NAME_SIZE).toEqual(Constants.MAX_NAME_SIZE);
    expect(gmsFiltersModule.MAX_COMMENT_SIZE).toEqual(Constants.MAX_COMMENT_SIZE);
    expect(gmsFiltersModule.MAX_FILTER_ORDER).toEqual(Constants.MAX_FILTER_ORDER);
    expect(gmsFiltersModule.MAX_POLES).toEqual(Constants.MAX_POLES);
    expect(gmsFiltersModule.MAX_SOS).toEqual(Constants.MAX_SOS);
    expect(gmsFiltersModule.MAX_TRANSFER_FUNCTION).toEqual(Constants.MAX_TRANSFER_FUNCTION);
    expect(gmsFiltersModule.MAX_FILTER_DESCRIPTIONS).toEqual(Constants.MAX_FILTER_DESCRIPTIONS);
  });
});
