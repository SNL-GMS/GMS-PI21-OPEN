import { FilterType } from '@gms/common-model/lib/filter-list/types';

import {
  getFilterBandType,
  getFilterComputationType,
  getFilterDesignModel
} from '../../../src/ts/filter-processor/gms-filters/util';

describe('GMS Filter Processor Util', () => {
  test('exists', () => {
    expect(getFilterBandType).toBeDefined();
    expect(getFilterDesignModel).toBeDefined();
    expect(getFilterComputationType).toBeDefined();
  });

  test('should throw', () => {
    expect(() => {
      getFilterDesignModel(FilterType.CASCADE);
    }).toThrow();

    expect(() => {
      getFilterComputationType(FilterType.CASCADE);
    }).toThrow();
  });
});
