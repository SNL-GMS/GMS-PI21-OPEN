import flatMap from 'lodash/flatMap';
import React from 'react';

import type { AsyncFetchHistory, AsyncFetchHistoryEntry, AsyncFetchResult } from '../query';
import { AsyncActionStatus } from '../query';

/**
 * A hook that can be used to return the current status of retrieving the async fetch history.
 * This includes the following information:
 *  - the async fetch status of all the async requests
 *  - the `data`: the history of the query
 *
 * @returns returns the current status of retrieving a query.
 */
export function useFetchHistoryStatus<T>(
  history: AsyncFetchHistory<T>
): AsyncFetchResult<AsyncFetchHistory<T>> {
  return React.useMemo(() => {
    const statuses: AsyncFetchHistoryEntry<T>[] = flatMap(
      Object.values(history).map(entry => Object.values(entry))
    );

    const status = statuses.reduce(
      (prev, current) => ({
        idle: current.status === AsyncActionStatus.idle ? prev.idle + 1 : prev.idle,
        pending: current.status === AsyncActionStatus.pending ? prev.pending + 1 : prev.pending,
        fulfilled:
          current.status === AsyncActionStatus.fulfilled ? prev.fulfilled + 1 : prev.fulfilled,
        rejected: current.status === AsyncActionStatus.rejected ? prev.rejected + 1 : prev.rejected
      }),
      { idle: 0, pending: 0, fulfilled: 0, rejected: 0 }
    );

    return {
      pending: status.pending,
      fulfilled: status.fulfilled,
      rejected: status.rejected,
      isLoading: (status.idle > 0 || status.pending > 0) && status.fulfilled < 1,
      // flag an error if there at least one rejected request and no pending/fulfilled requests
      isError: status.pending < 1 && status.fulfilled < 1 && status.rejected > 0,
      data: history
    };
  }, [history]);
}
