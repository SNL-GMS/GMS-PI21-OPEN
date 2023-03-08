import type { SerializedError } from '@reduxjs/toolkit';
import type { QueryDefinition } from '@reduxjs/toolkit/dist/query';
import type { QueryStatus, QuerySubState } from '@reduxjs/toolkit/dist/query/core/apiState';
import type { ResultTypeFrom } from '@reduxjs/toolkit/dist/query/endpointDefinitions';
import type { Id, Override } from '@reduxjs/toolkit/dist/query/tsHelpers';

/**
 * Defines the possible statuses for an async action (thunk).
 */
export enum AsyncActionStatus {
  /** the action is idle or has not started */
  idle = 'idle',
  /** the action is pending */
  pending = 'pending',
  /** the action has been fulfilled and completed */
  fulfilled = 'fulfilled',
  /** the action has been rejected or failed */
  rejected = 'rejected'
}

/**
 * Defines the base history entry object for an async fetch request.
 */
export interface AsyncFetchHistoryEntry<T> {
  /** the async request arguments */
  arg: T;
  /** the async action request status */
  status: AsyncActionStatus;
  /**
   * The serialized error from the rejected promise, but excluding any non-serializable parameters.
   * See @link https://redux-toolkit.js.org/api/createAsyncThunk#handling-thunk-errors
   */
  error: SerializedError;
}

/**
 * Defines the base history object for an async fetch request.
 */
export type AsyncFetchHistory<T> = Record<
  string /* unique string identifier */,
  Record<string /* request id */, AsyncFetchHistoryEntry<T>>
>;

/**
 * Defines the base result object for an async fetch request.
 */
export interface AsyncFetchResult<T> {
  /** the number of pending (active) fetch requests */
  pending: number;
  /** the number of fulfilled (completed) fetch requests */
  fulfilled: number;
  /** the number of rejected fetch requests */
  rejected: number;
  /** true if fetching is active or loading; false otherwise */
  isLoading: boolean;
  /** true if an error occurred while fetching; false otherwise */
  isError: boolean;
  /** the data of the result */
  data: T;
}

// TODO: RTK Query Type Issue: https://github.com/reduxjs/redux-toolkit/issues/1937
// ! Type inference appears to not work with the Redux Toolkit hook result type
// ! To work around this issue, the following types are copied over from Redux Toolkit
// ! Once fixed, update and use something like ReturnType<typeof useFetchMyDataQuery>
type UseQueryStateBaseResult<D extends QueryDefinition<any, any, any, any>> = QuerySubState<D> & {
  /**
   * Where `data` tries to hold data as much as possible, also re-using
   * data from the last arguments passed into the hook, this property
   * will always contain the received data from the query, for the current query arguments.
   */
  currentData?: ResultTypeFrom<D>;
  /**
   * Query has not started yet.
   */
  isUninitialized: boolean;
  /**
   * Query is currently loading for the first time. No data yet.
   */
  isLoading: boolean;
  /**
   * Query is currently fetching, but might have data from an earlier request.
   */
  isFetching: boolean;
  /**
   * Query has data from a successful load.
   */
  isSuccess: boolean;
  /**
   * Query is currently in "error" state.
   */
  isError: boolean;
};

export type UseQueryStateDefaultResult<D extends QueryDefinition<any, any, any, any>> = Id<
  | Override<
      Extract<UseQueryStateBaseResult<D>, { status: QueryStatus.uninitialized }>,
      { isUninitialized: true }
    >
  | Override<
      UseQueryStateBaseResult<D>,
      | { isLoading: true; isFetching: boolean; data: undefined }
      | ({
          isSuccess: true;
          isFetching: true;
          error: undefined;
        } & Required<Pick<UseQueryStateBaseResult<D>, 'data' | 'fulfilledTimeStamp'>>)
      | ({
          isSuccess: true;
          isFetching: false;
          error: undefined;
        } & Required<
          Pick<UseQueryStateBaseResult<D>, 'data' | 'fulfilledTimeStamp' | 'currentData'>
        >)
      | ({ isError: true } & Required<Pick<UseQueryStateBaseResult<D>, 'error'>>)
    >
> & {
  /**
   * @deprecated will be removed in the next version
   * please use the `isLoading`, `isFetching`, `isSuccess`, `isError`
   * and `isUninitialized` flags instead
   */
  status: QueryStatus;
};

export type UseQueryStateResult<ReturnType> = UseQueryStateDefaultResult<
  QueryDefinition<any, any, any, ReturnType>
>;
