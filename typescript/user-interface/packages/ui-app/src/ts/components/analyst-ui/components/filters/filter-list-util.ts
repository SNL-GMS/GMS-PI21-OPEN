import type { FilterListTypes } from '@gms/common-model';
import { getDuplicates, isUnique } from '@gms/common-util';
import { UILogger } from '@gms/ui-util';

const logger = UILogger.create('FILTER_LIST_LOGGER', process.env.GMS_FILTER_LIST_LOGGER);

/**
 * Checks filter lists for uniqueness, and @throws if filters with duplicate names are found
 */
export const checkForUniqueness = (filterLists: FilterListTypes.FilterList[]): boolean => {
  if (filterLists && !isUnique(filterLists?.map(fl => fl.name))) {
    const duplicates = getDuplicates(filterLists.map(fl => fl.name)).reduce(
      (str: string, n: string) => (str.length > 0 ? `${str}, ${n}` : n),
      ''
    );
    logger.error(`Filter list names must be unique. Duplicate name(s) found: ${duplicates}`);
    return false;
  }
  return true;
};

export const getFilterName = (filter: FilterListTypes.Filter): string =>
  filter.filterDefinition?.name ?? filter.namedFilter ?? (filter.unfiltered ? 'Unfiltered' : '???');
