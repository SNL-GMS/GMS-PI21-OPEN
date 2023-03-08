import isEqual from 'lodash/isEqual';

import type { AsyncFetchHistoryEntry } from './types';
import { AsyncActionStatus } from './types';

/**
 * Returns true if the new request (arg) has already been requested; false otherwise
 * which indicates that the requests has not been issued (a new request).
 *
 * @param requests the requests that have already been issued, to check against
 * @param arg the new request to check to see if it has already been issued
 */
export function hasAlreadyBeenRequested<ArgType>(
  requests: Record<string, AsyncFetchHistoryEntry<ArgType>>,
  arg: ArgType
): boolean {
  let result = false;
  Object.keys(requests).forEach(requestId => {
    const request = requests[requestId];
    if (!result && request.status !== AsyncActionStatus.idle) {
      result ||= isEqual(arg, request.arg);
    }
  });
  return result;
}
