import type { FilterList } from '@gms/common-model/lib/filter-list/types';

import { checkForUniqueness } from '../../../../../src/ts/components/analyst-ui/components/filters/filter-list-util';

const filterList: FilterList = {
  defaultFilterIndex: 0,
  name: 'test-0',
  filters: [
    {
      withinHotKeyCycle: true,
      unfiltered: true,
      namedFilter: null,
      filterDefinition: null
    },
    {
      withinHotKeyCycle: false,
      unfiltered: null,
      namedFilter: 'HYDRO - for testing',
      filterDefinition: null
    },
    {
      withinHotKeyCycle: true,
      unfiltered: null,
      namedFilter: 'DETECTION',
      filterDefinition: null
    },
    {
      withinHotKeyCycle: true,
      unfiltered: null,
      namedFilter: 'ONSET',
      filterDefinition: null
    },
    {
      withinHotKeyCycle: false,
      unfiltered: null,
      namedFilter: 'FK',
      filterDefinition: null
    }
  ]
};

describe('Filter-list-utils', () => {
  describe('checkForUniqueness', () => {
    it('returns true if given an empty list', () => {
      expect(checkForUniqueness([])).toBe(true);
    });
    it('returns true if given a list of one', () => {
      expect(checkForUniqueness([filterList])).toBe(true);
    });
    it('returns false if given two identical entries', () => {
      expect(checkForUniqueness([filterList, filterList])).toBe(false);
    });
    it('returns true if given a unique list of two entries', () => {
      expect(
        checkForUniqueness([
          filterList,
          {
            defaultFilterIndex: 1,
            filters: [
              {
                withinHotKeyCycle: true,
                unfiltered: true,
                namedFilter: null,
                filterDefinition: null
              }
            ],
            name: 'test-2'
          }
        ])
      ).toBe(true);
    });
  });
});
