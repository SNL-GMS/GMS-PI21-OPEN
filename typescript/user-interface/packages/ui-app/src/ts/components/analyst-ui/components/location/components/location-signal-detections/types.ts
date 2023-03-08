import type { CommonTypes, LegacyEventTypes } from '@gms/common-model';
import type { Row } from '@gms/ui-core-components';

import type { SignalDetectionSnapshotWithDiffs } from '../../types';

/**
 * Location Props
 */
export interface LocationSignalDetectionsProps {
  // Current open event
  event: LegacyEventTypes.Event;
  // Associated Signal Detections to open event
  signalDetectionDiffSnapshots: SignalDetectionSnapshotWithDiffs[];
  // Distance to the station for selected location
  distances: LegacyEventTypes.LocationToStationDistance[];
  // If true, the sd list is showing data that DOES NOT match the rest of the analyst workspace
  historicalMode: boolean;
  // The currently-selected signal detection IDs
  selectedSdIds: string[];

  rejectDetections: (args: any) => Promise<void>;
  updateDetections: (args: any) => Promise<void>;
  createEvent: (args: any) => Promise<void>;
  changeSignalDetectionAssociations: (args: any) => Promise<void>;

  setSelectedSdIds(ids: string[]): void;
  setSdIdsToShowFk(signalDetectionIds: string[]): void;
  setMeasurementModeEntries(entries: Record<string, boolean>): void;
  showSDContextMenu(selectedSdIds: string[], x: number, y: number);
  showSDDetails(sdId: string, x: number, y: number);
  setDefining(isDefining: boolean, definingType: DefiningTypes): void;
  updateIsDefining(
    definingType: DefiningTypes,
    signalDetectionId: string,
    setDefining: boolean
  ): void;
  toast(message: string): void;
}

export interface LocationSDRow extends Row {
  eventId: string;
  signalDetectionId: string;
  station: string;
  channel: string;
  phase: CommonTypes.PhaseType;
  distance?: number;
  timeObs: string;
  timeRes: number;
  timeCorr: number;
  slownessObs: number;
  slownessRes: number;
  slownessCorr: number;
  azimuthObs: number;
  azimuthRes: number;
  azimuthCorr: number;
  arrivalTimeDefining: boolean;
  slownessDefining: boolean;
  azimuthDefining: boolean;
  // Fields for highlighting if there is a diff between snapshot and state
  isAssociatedDiff: boolean;
  // Removed covers rejected and unassociated
  timeDefiningDiff: boolean;
  azimuthDefiningDiff: boolean;
  slownessDefiningDiff: boolean;
  rejectedOrUnassociated: boolean;
  // Value diffs
  channelNameDiff?: boolean;
  arrivalTimeDiff?: boolean;
  azimuthObsDiff?: boolean;
  slownessObsDiff?: boolean;
  phaseDiff?: boolean;

  historicalMode: boolean;
  updateIsDefining(
    definingType: DefiningTypes,
    signalDetectionHypothesisId: string,
    setDefining: boolean
  );
}

/**
 * DefiningTypes are the FeatureMeasurement Types that populate the Location SD table
 */
export enum DefiningTypes {
  AZIMUTH = 'AZIMUTH',
  SLOWNESS = 'SLOWNESS',
  ARRIVAL_TIME = 'ARRIVAL_TIME'
}
export enum DefiningChange {
  CHANGED_TO_TRUE,
  CHANGED_TO_FALSE,
  CHANGED_TO_OTHER,
  NO_CHANGE
}
/**
 * Describes state of (non)defining sd's
 */
export enum DefiningStates {
  ALL = 'ALL',
  NONE = 'NONE',
  SOME = 'SOME'
}
