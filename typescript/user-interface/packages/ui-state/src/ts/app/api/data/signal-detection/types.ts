import type { SignalDetectionTypes, StationTypes, WorkflowTypes } from '@gms/common-model';
import type { EntityReference } from '@gms/common-model/lib/faceted';

import type { AsyncFetchHistory } from '../../../query';

/**
 * The interface required to make a signal detection query by stations.
 */
export interface GetSignalDetectionsAndSegmentsByStationsAndTimeQueryArgs {
  /**
   * Entity references contain only the station name.
   */
  stations: EntityReference<StationTypes.Station>[];

  /**
   * In seconds. This will get converted into a UTC time string by the AxiosTransformers.
   */
  startTime: number;

  /**
   * In seconds. This will get converted into a UTC time string by the AxiosTransformers.
   */
  endTime: number;

  /**
   * The stage for which to request signal detections by station.
   */
  stageId: WorkflowTypes.WorkflowDefinitionId;

  /**
   * A list of signal detections to exclude from the result (so a request
   * with the same times could return newer results, in the case of late-arriving
   * data for example).
   */
  excludedSignalDetections?: SignalDetectionTypes.SignalDetection[];
}

/**
 * The interface required to make a signal detection query by a single station.
 */
export type GetSignalDetectionsWithSegmentsByStationAndTimeQueryArgs = Omit<
  GetSignalDetectionsAndSegmentsByStationsAndTimeQueryArgs & {
    station: EntityReference<StationTypes.Station>;
  },
  'stations'
>;

/**
 * Defines the history record type for the getSignalDetectionsAndSegmentsByStationAndTime query
 */
export type GetSignalDetectionsWithSegmentsByStationAndTimeHistory = AsyncFetchHistory<
  GetSignalDetectionsWithSegmentsByStationAndTimeQueryArgs
>;
