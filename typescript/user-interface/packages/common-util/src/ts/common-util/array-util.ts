import filter from 'lodash/filter';
import includes from 'lodash/includes';
import uniq from 'lodash/uniq';

/**
 * @returns whether the two lists are unique
 */
export const isUnique = (a: unknown[] | undefined): boolean => a && uniq(a).length === a.length;

/** Gets a list the items that are duplicates */
export const getDuplicates = (arr: unknown[]): unknown[] =>
  filter(arr, (val, i: number, iteratee) => includes(iteratee, val, i + 1));
