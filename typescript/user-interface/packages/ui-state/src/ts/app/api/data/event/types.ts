import type { AsyncFetchHistory } from '../../../query/types';

/**
 * The interface required by the events manager service to get events.
 */
export interface GetEventsWithDetectionsAndSegmentsByTimeQueryArgs {
  /**
   * In seconds. This will get converted into a UTC time string by the AxiosTransformers.
   */
  startTime: number;
  /**
   * In seconds. This will get converted into a UTC time string by the AxiosTransformers.
   */
  endTime: number;
  /**
   * The stage id
   */
  stageId: { name: string };
}

/**
 * Defines the history record type for the getEventsWithDetectionsAndSegmentsByTime query
 */
export type GetEventsWithDetectionsAndSegmentsByTimeHistory = AsyncFetchHistory<
  GetEventsWithDetectionsAndSegmentsByTimeQueryArgs
>;
