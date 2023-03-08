/* eslint-disable @typescript-eslint/no-magic-numbers */
/**
 * @jest-environment node
 */

/**
 * !!! Super important info about returning array values
 * https://stackoverflow.com/questions/17883799/how-to-handle-passing-returning-array-pointers-to-emscripten-compiled-code
 */

import type { VectorDouble } from '../../../src/ts/filter-processor/gms-filters/types/vector-double';
import type { GmsFiltersModule } from '../../../src/ts/ui-wasm';
// ! IGNORED TO SUPPORT ESLINT CHECKS WITHOUT REQUIRING TO BUILD THE WASM
// eslint-disable-next-line @typescript-eslint/ban-ts-comment
// @ts-ignore
// eslint-disable-next-line import/no-unresolved
import gmsFilters from '../../../src/ts/wasm/gms-filters/gms-filters.mjs';

describe('GMS Filters Vector Double Test', () => {
  test('vector double exists and can be created', async () => {
    const gmsFiltersModule: GmsFiltersModule = await gmsFilters();
    expect(gmsFiltersModule).toBeDefined();

    expect(gmsFiltersModule.VectorDouble).toBeDefined();

    let vectorDouble: VectorDouble;

    try {
      vectorDouble = new gmsFiltersModule.VectorDouble();
      vectorDouble.push_back(1.1);
      vectorDouble.push_back(2.2);
      vectorDouble.push_back(3.3);

      expect(vectorDouble).not.toBeNull();
      expect(vectorDouble.size).toEqual(3);
      expect(vectorDouble.get(0)).toEqual(1.1);
      expect(vectorDouble.get(1)).toEqual(2.2);
      expect(vectorDouble.get(1)).toEqual(3.3);

      vectorDouble.set(1, 9.9);
      expect(vectorDouble.get(1)).toEqual(9.9);
    } catch (e) {
      // eslint-disable-next-line no-underscore-dangle
      gmsFiltersModule._free(vectorDouble as any);
    }
  });
});
