import type { WorkspaceState } from '../common/types';
import type { Event } from '../event/legacy/types';
import type { QcMask } from '../qc-mask/types';
import type { SignalDetection } from '../signal-detection/types';

/**
 * Description of a user action preformed
 */
export enum UserActionDescription {
  UNKNOWN = 'Unknown',
  CHANGE_SIGNAL_DETECTION_ASSOCIATIONS_ASSOCIATE = 'Associate',
  CHANGE_SIGNAL_DETECTION_ASSOCIATIONS_ASSOCIATE_MULTIPLE = 'Associate multiple detections',
  CHANGE_SIGNAL_DETECTION_ASSOCIATIONS_UNASSOCIATE = 'Unassociate',
  CHANGE_SIGNAL_DETECTION_ASSOCIATIONS_UNASSOCIATE_MULTIPLE = 'Unassociate multiple detections',

  // signal detection actions
  CREATE_DETECTION = 'Create detection',
  REJECT_DETECTION = 'Reject detection',
  REJECT_MULTIPLE_DETECTIONS = 'Reject multiple detections',
  UPDATE_DETECTION_RE_TIME = 'Time',
  UPDATE_DETECTION_RE_PHASE = 'Phase',
  UPDATE_MULTIPLE_DETECTIONS_RE_PHASE = 'Phase multiple detections',
  UPDATE_DETECTION_AMPLITUDE = 'Update detection amplitude',
  UPDATE_DETECTION_REVIEW_AMPLITUDE = 'Update detection review amplitude',
  UPDATE_DETECTION = 'Update detection',
  COMPUTE_FK = 'Fk',
  COMPUTE_MULTIPLE_FK = 'Fk multiple detections',

  // event actions
  CREATE_EVENT = 'Create event',
  UPDATE_EVENT_LOCATE = 'Locate event',
  UPDATE_EVENT_FROM_SIGNAL_DETECTION_CHANGE = 'Update event from signal detection change',
  UPDATE_EVENT_FEATURE_PREDICTIONS = 'Update event feature predictions',
  UPDATE_EVENT_STATUS_OPEN_FOR_REFINEMENT = 'Event opened for refinement',
  UPDATE_EVENT_MARK_COMPLETE = 'Event marked complete',
  UPDATE_EVENT_PREFERRED_HYP = 'Update event preferred hypothesis',
  UPDATE_EVENT_MAGNITUDE = 'Change Magnitude defining settings',
  SAVE_EVENT = 'Save event'
}

/**
 * Defines the hypothesis type for a hypothesis change.
 */
export enum HypothesisType {
  EventHypothesis = 'EventHypothesis',
  SignalDetectionHypothesis = 'SignalDetectionHypothesis'
}

/**
 * Represents hypothesis information for a change.
 * Defines a mapping from a hypothesis id to the main
 * object id.
 */
export interface HypothesisChangeInformation {
  readonly id: string;
  readonly hypothesisId: string;
  readonly type: HypothesisType;
  readonly parentId: string;
  readonly userAction: string;
}

/**
 * A history change summary.
 */
export interface HistoryChange {
  readonly id: string;
  readonly active: boolean;
  readonly eventId: string;
  readonly conflictCreated: boolean;
  readonly hypothesisChangeInformation: HypothesisChangeInformation;
}

/**
 * History summary.
 */
export interface History {
  readonly id: string;
  readonly description: string;
  readonly changes: HistoryChange[];
  readonly redoPriorityOrder: number;
}

/**
 * Invalid data; typically data that has been deleted.
 */
export interface InvalidData {
  readonly eventIds: string[];
  readonly signalDetectionIds: string[];
}

export interface DataPayload {
  events: Event[];
  sds: SignalDetection[];
  qcMasks: QcMask[];
  invalid: InvalidData;
  workspaceState: WorkspaceState;
  history: History[];
}

// ***************************************
// Mutations
// ***************************************

/**
 * Undo history mutation data
 */
export interface UndoHistoryMutationData {
  undoHistory: DataPayload;
}

/**
 * Undo history mutation result
 */
export interface UndoHistoryMutationResult {
  data: UndoHistoryMutationData;
}

/**
 * Undo history mutation by id data
 */
export interface UndoHistoryByIdMutationData {
  undoHistoryById: DataPayload;
}

/**
 * Undo history mutation by id result
 */
export interface UndoHistoryByIdMutationResult {
  data: UndoHistoryByIdMutationData;
}

/**
 * Redo history mutation data
 */
export interface RedoHistoryMutationData {
  redoHistory: DataPayload;
}

/**
 * Redo history mutation result
 */
export interface RedoHistoryMutationResult {
  data: RedoHistoryMutationData;
}

/**
 * Redo history mutation by id data
 */
export interface RedoHistoryByIdMutationData {
  redoHistoryById: DataPayload;
}

/**
 * Redo history mutation by id result
 */
export interface RedoHistoryByIdMutationResult {
  data: RedoHistoryByIdMutationData;
}

/**
 * Undo event history mutation data
 */
export interface UndoEventHistoryMutationData {
  undoEventHistory: DataPayload;
}

/**
 * Undo event history mutation result
 */
export interface UndoEventHistoryMutationResult {
  data: UndoEventHistoryMutationData;
}

/**
 * Undo event history mutation by id data
 */
export interface UndoEventHistoryByIdMutationData {
  undoEventHistoryById: DataPayload;
}

/**
 * Undo event history mutation by id result
 */
export interface UndoEventHistoryByIdMutationResult {
  data: UndoEventHistoryByIdMutationData;
}

/**
 * Redo event history mutation data
 */
export interface RedoEventHistoryMutationData {
  redoEventHistory: DataPayload;
}

/**
 * Redo event history mutation result
 */
export interface RedoEventHistoryMutationResult {
  data: RedoEventHistoryMutationData;
}

/**
 * Redo event history mutation by id data
 */
export interface RedoEventHistoryByIdMutationData {
  redoEventHistoryById: DataPayload;
}

/**
 * Redo event history mutation by id result
 */
export interface RedoEventHistoryByIdMutationResult {
  data: RedoEventHistoryByIdMutationData;
}
// ***************************************
// Queries
// ***************************************

/**
 * Get event history query arguments.
 */
export interface EventHistoryQueryArgs {
  id: string;
}
