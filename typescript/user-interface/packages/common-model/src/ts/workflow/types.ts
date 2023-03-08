// ***************************************
// Workflow interfaces
// ***************************************

import type { Faceted } from '../faceted';

// TODO remove once events in ui-state (data slice)
export const stageNames = ['Auto Network', 'AL1', 'Auto Post-AL1', 'AL2'];

export enum StageMode {
  INTERACTIVE = 'INTERACTIVE',
  AUTOMATIC = 'AUTOMATIC'
}

export interface WorkflowDefinitionId {
  name: string;
  effectiveTime?: number;
}

export interface Stage {
  name: string;
  mode: StageMode;
  duration: number;
}

export interface ProcessingStep {
  name: string;
}

export interface ProcessingSequence {
  name: string;
  steps: ProcessingStep[];
}

export interface AutomaticProcessingStage extends Stage {
  sequences: ProcessingSequence[];
}

export enum AnalysisMode {
  EVENT_REVIEW = 'EVENT_REVIEW',
  SCAN = 'SCAN'
}

export interface StationGroup extends Faceted {
  effectiveAt: number;
  name: string;
  description: string;
}

export interface Activity {
  name: string;
  analysisMode: AnalysisMode;
  stationGroup: StationGroup;
}

export interface InteractiveAnalysisStage extends Stage {
  activities: Activity[];
}

export interface Workflow {
  name: string;
  stages: Stage[];
}

// ***************************************
// Workflow Interval interfaces
// ***************************************
export enum IntervalStatus {
  NOT_STARTED = 'NOT_STARTED',
  IN_PROGRESS = 'IN_PROGRESS',
  NOT_COMPLETE = 'NOT_COMPLETE',
  COMPLETE = 'COMPLETE',
  FAILED = 'FAILED',
  SKIPPED = 'SKIPPED'
}

export interface IntervalId {
  startTime: number;
  definitionId: WorkflowDefinitionId;
}
export interface StageMetrics {
  eventCount: number;
  associatedSignalDetectionCount: number;
  unassociatedSignalDetectionCount: number;
  maxMagnitude: number;
}

export interface Interval {
  intervalId: IntervalId;
  name: string;
  comment: string;
  startTime: number;
  endTime: number;
  status: IntervalStatus;
  storageTime: number;
  modificationTime: number;
  percentAvailable: number;
  processingStartTime: number;
  processingEndTime: number;
}

export interface StageInterval extends Interval {
  stageMetrics: StageMetrics;
  stageMode: StageMode;
}

export interface ProcessingSequenceInterval extends Interval {
  stageName: string;
  percentComplete: number;
  lastExecutedStepName: string;
}

export interface ActivityInterval extends Interval {
  stageName: string;
  activeAnalysts: string[];
}

export interface AutomaticProcessingStageInterval extends StageInterval {
  sequenceIntervals: ProcessingSequenceInterval[];
}

export interface InteractiveAnalysisStageInterval extends StageInterval {
  activityIntervals: ActivityInterval[];
}

// ***************************************
// Type Utilities
// ***************************************

/**
 * Checks if stage  is InteractiveAnalysisStage
 *
 * @param object Stage
 * @returns boolean
 */
export function isInteractiveAnalysisStage(object: Stage): object is InteractiveAnalysisStage {
  return object && object.mode === StageMode.INTERACTIVE;
}

/**
 * Checks if stage interval is AutomaticProcessingStage
 *
 * @param object Stage
 * @returns boolean
 */
export function isAutomaticProcessingStage(object: Stage): object is AutomaticProcessingStage {
  return object && object.mode === StageMode.AUTOMATIC;
}

/**
 * Checks if stage interval is InteractiveAnalysisStageInterval
 *
 * @param object Stage Interval
 * @returns boolean
 */
export function isInteractiveAnalysisStageInterval(
  object: StageInterval
): object is InteractiveAnalysisStageInterval {
  return object && object.stageMode === StageMode.INTERACTIVE;
}

/**
 * Checks if stage interval is AutomaticProcessingStageInterval
 *
 * @param object Stage Interval
 * @returns boolean
 */
export function isAutomaticProcessingStageInterval(
  object: StageInterval
): object is AutomaticProcessingStageInterval {
  return object && object.stageMode === StageMode.AUTOMATIC;
}

/**
 * Checks if stage interval is ProcessingSequenceInterval
 *
 * @param object Interval & Partial<ProcessingSequenceInterval>
 * @returns boolean
 */
export function isProcessingSequenceInterval(
  object: Interval & Partial<ProcessingSequenceInterval>
): object is ProcessingSequenceInterval {
  return (
    object && object.lastExecutedStepName !== undefined && object.percentComplete !== undefined
  );
}

/**
 * Checks if interval is StageInterval
 *
 * @param object Interval
 * @returns boolean
 */
export function isStageInterval(
  object: Interval & Partial<StageInterval>
): object is StageInterval {
  return object && object.stageMode !== undefined;
}

/**
 * Checks if interval is ActivityInterval
 *
 * @param object Interval
 * @returns boolean
 */
export function isActivityInterval(
  object: Interval & Partial<ActivityInterval>
): object is ActivityInterval {
  return object && object.stageName !== undefined && object.activeAnalysts !== undefined;
}
