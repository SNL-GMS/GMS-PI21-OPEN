import isEmpty from 'lodash/isEmpty';
import isNull from 'lodash/isNull';
import isUndefined from 'lodash/isUndefined';

/**
 * Return number of entries in the Record if undefined returns 0
 *
 * @param record Record
 * @returns number of entries
 */
export function recordLength(record: Record<string | number | symbol, unknown>): number {
  if (isUndefined(isEmpty(record)) || isNull(isEmpty(record)) || isEmpty(record)) {
    return 0;
  }
  return Object.keys(record).length;
}
