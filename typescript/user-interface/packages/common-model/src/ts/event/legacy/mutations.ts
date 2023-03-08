// ***************************************
// Mutations
// ***************************************

import type { DataPayload } from '../../cache/types';
import type { ComputeNetworkMagnitudeInput, LocationBehavior } from './types';

export interface UpdateEventsInput {
  processingStageId: string;
  status?: 'ReadyForRefinement' | 'OpenForRefinement' | 'AwaitingReview' | 'Complete';
  preferredHypothesisId?: string;
}

export interface UpdateEventsMutationArgs {
  eventIds: string[];
  input: UpdateEventsInput;
}

export interface UpdateEventsMutationData {
  updateEvents: DataPayload;
}

export interface UpdateEventsMutationResult {
  data: UpdateEventsMutationData;
}

/**
 * Locate Event Mutation Args
 */
export interface LocateEventMutationArgs {
  eventHypothesisId: string;
  preferredLocationSolutionId: string;
  locationBehaviors: LocationBehavior[];
}

export interface LocateEventMutationResult {
  data: {
    locateEvent: DataPayload;
  };
}

export interface UpdateFeaturePredictionsMutationResult {
  data: {
    updateFeaturePredictions: DataPayload;
  };
}

export interface UpdateFeaturePredictionsMutationArgs {
  eventId: string;
}

/**
 * Locate Event Mutation Args
 */
export interface SaveEventMutationArgs {
  eventId: string;
}

export interface SaveEventMutationData {
  saveEvent: DataPayload;
}

export interface SaveEventMutationResult {
  data: SaveEventMutationData;
}

export interface ChangeSignalDetectionAssociationsMutationArgs {
  eventHypothesisId: string;
  signalDetectionIds: string[];
  associate: boolean;
}

export interface ChangeSignalDetectionAssociationsMutationData {
  changeSignalDetectionAssociations: DataPayload;
}

export interface ChangeSignalDetectionAssociationsMutationResult {
  data: ChangeSignalDetectionAssociationsMutationData;
}

export interface CreateEventMutationArgs {
  signalDetectionIds: string[];
}

export interface CreateEventMutationData {
  createEvent: DataPayload;
}

export interface CreateEventMutationResult {
  data: CreateEventMutationData;
}

export interface ComputeNetworkMagnitudeSolutionMutationArgs {
  computeNetworkMagnitudeSolutionInput: ComputeNetworkMagnitudeInput;
}

export interface ComputeNetworkMagnitudeSolutionMutationData {
  computeNetworkMagnitudeSolution: {
    status: {
      stationId: string;
      rational: string;
    };
    dataPayload: DataPayload;
  };
}

export interface ComputeNetworkMagnitudeSolutionMutationResult {
  data: ComputeNetworkMagnitudeSolutionMutationData;
}
