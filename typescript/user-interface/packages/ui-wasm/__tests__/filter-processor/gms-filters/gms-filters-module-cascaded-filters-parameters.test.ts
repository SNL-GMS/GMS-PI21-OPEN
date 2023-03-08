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
import type { GmsFiltersModule } from '../../../src/ts/ui-wasm';
// ! IGNORED TO SUPPORT ESLINT CHECKS WITHOUT REQUIRING TO BUILD THE WASM
// eslint-disable-next-line @typescript-eslint/ban-ts-comment
// @ts-ignore
// eslint-disable-next-line import/no-unresolved
import gmsFilters from '../../../src/ts/wasm/gms-filters/gms-filters.mjs';

describe('GMS Filters Cascaded Filters Parameters Test', () => {
  test('CascadedFiltersParameters is defined and can be created', async () => {
    const gmsFiltersModule: GmsFiltersModule = await gmsFilters();
    expect(gmsFiltersModule).toBeDefined();

    expect(gmsFiltersModule.CascadedFiltersParameters).toBeDefined();
    expect(gmsFiltersModule.CascadedFiltersParameters.build).toBeDefined();

    let cascadedFiltersParameters: CascadedFiltersParameters;

    try {
      cascadedFiltersParameters = gmsFiltersModule.CascadedFiltersParameters.build(
        'my comments',
        false,
        1,
        2,
        3
      );

      expect(cascadedFiltersParameters).toBeDefined();
      expect(cascadedFiltersParameters.delete).toBeDefined();
      expect(cascadedFiltersParameters.comments).toBeDefined();
      expect(cascadedFiltersParameters.comments).toEqual('my comments');
      expect(cascadedFiltersParameters.isCausal).toBeDefined();
      expect(cascadedFiltersParameters.isCausal).toEqual(false);
      expect(cascadedFiltersParameters.sampleRate).toBeDefined();
      expect(cascadedFiltersParameters.sampleRate).toEqual(1);
      expect(cascadedFiltersParameters.sampleRateTolerance).toBeDefined();
      expect(cascadedFiltersParameters.sampleRateTolerance).toEqual(2);
      expect(cascadedFiltersParameters.groupDelay).toBeDefined();
      expect(cascadedFiltersParameters.groupDelay).toEqual(3);
    } finally {
      cascadedFiltersParameters.delete();
      gmsFiltersModule._free(cascadedFiltersParameters as any);
    }
  });
});
