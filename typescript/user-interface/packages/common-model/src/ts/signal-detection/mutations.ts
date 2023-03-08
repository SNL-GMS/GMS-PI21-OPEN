import type { DataPayload } from '../cache/types';
import type { AmplitudeMeasurementValue } from './types';

// ***************************************
// Mutations
// ***************************************

/**
 * Signal Detection Timing. Input object that groups ArrivalTime and AmplitudeMeasurement
 */
export interface SignalDetectionTimingInput {
  // The detection time (seconds since epoch) to assign to the new detection's initial hypothesis
  arrivalTime: number;

  // The uncertainty (seconds) associated with the time input
  timeUncertaintySec: number;

  // The Amplitude Measurement Value
  amplitudeMeasurement?: AmplitudeMeasurementValue;
}

/**
 * Input used to create a new signal detection with an initial hypothesis
 * and time feature measurement
 */
export interface NewDetectionInput {
  stationId: string;
  phase: string;

  // Signal Detection Timing Input for ArrivalTime and AmplitudeMeasurement
  signalDetectionTiming: SignalDetectionTimingInput;
  eventId?: string;
}

export interface CreateDetectionMutationArgs {
  input: NewDetectionInput;
}

export interface CreateDetectionMutationData {
  createDetection: DataPayload;
}

export interface CreateDetectionMutationResult {
  data: CreateDetectionMutationData;
}

/**
 * Input used to update an existing signal detection
 */
export interface UpdateDetectionInput {
  phase?: string;

  // Signal Detection Timing Input for ArrivalTime and AmplitudeMeasurement
  signalDetectionTiming?: SignalDetectionTimingInput;
}

export interface UpdateDetectionsMutationArgs {
  detectionIds: string[];
  input: UpdateDetectionInput;
}

export interface UpdateDetectionsMutationData {
  updateDetections: DataPayload;
}

export interface UpdateDetectionsMutationResult {
  data: UpdateDetectionsMutationData;
}

export interface RejectDetectionsMutationArgs {
  detectionIds: string[];
}

export interface RejectDetectionsMutationData {
  rejectDetections: DataPayload;
}

export interface RejectDetectionsMutationResult {
  data: RejectDetectionsMutationData;
}

export interface MarkAmplitudeMeasurementReviewedMutationArgs {
  signalDetectionIds: string[];
}

export interface MarkAmplitudeMeasurementReviewedMutationData {
  markAmplitudeMeasurementReviewed: DataPayload;
}

export interface MarkAmplitudeMeasurementReviewedMutationResult {
  data: MarkAmplitudeMeasurementReviewedMutationData;
}
