import { EventTypes } from '@gms/common-model';
import { Units } from '@gms/common-model/lib/common/types';
import type { SignalDetectionHypothesisFaceted } from '@gms/common-model/lib/signal-detection/types';
import { SECONDS_IN_HOUR } from '@gms/common-util/lib/common-util/time-util';
import type { EventsFetchResult } from 'src/ts/ui-state';

const eventId = 'eventID';
const hypothesisId = 'hypothesisID';
const locationSolutionId = 'locationSolutionID';
const workflowDefinitionId = { name: 'AL1', effectiveTime: 0 };

export const signalDetectionHypothesisFaceted: SignalDetectionHypothesisFaceted = {
  id: {
    id: '20cc9505-efe3-3068-b7d5-59196f37992c',
    signalDetectionId: '012de1b9-8ae3-3fd4-800d-58665c3152cc'
  }
};

export const eventHypothesisId: EventTypes.EventHypothesisId = {
  eventId,
  hypothesisId
};

export const networkMagnitudeSolutionMB: EventTypes.NetworkMagnitudeSolution = {
  magnitude: { value: 1.2, standardDeviation: 0, units: Units.MAGNITUDE },
  magnitudeBehaviors: [],
  type: EventTypes.MagnitudeType.MB
};

export const location: EventTypes.EventLocation = {
  latitudeDegrees: 1.1,
  longitudeDegrees: 2.2,
  depthKm: 3.3,
  time: SECONDS_IN_HOUR
};

export const locationSolution: EventTypes.LocationSolution = {
  id: locationSolutionId,
  networkMagnitudeSolutions: [networkMagnitudeSolutionMB],
  featurePredictions: { featurePredictions: [] },
  locationBehaviors: [],
  location,
  locationRestraint: undefined,
  name: ''
};

export const eventHypothesis: EventTypes.EventHypothesis = {
  id: eventHypothesisId,
  rejected: false,
  parentEventHypotheses: [],
  associatedSignalDetectionHypotheses: [],
  preferredLocationSolution: locationSolution,
  locationSolutions: [locationSolution],
  name: 'event hypothesis'
};

export const preferredEventHypothesis: EventTypes.PreferredEventHypothesis = {
  preferredBy: 'preferredAnalyst',
  stage: workflowDefinitionId,
  preferred: eventHypothesis
};

export const eventHypothesisWithAssociatedSignalDetectionHypotheses: EventTypes.EventHypothesis = {
  id: eventHypothesisId,
  rejected: false,
  parentEventHypotheses: [],
  associatedSignalDetectionHypotheses: [signalDetectionHypothesisFaceted],
  preferredLocationSolution: locationSolution,
  locationSolutions: [locationSolution],
  name: 'event hypothesis'
};

export const eventWithHypothesis: EventTypes.Event = {
  id: eventId,
  rejectedSignalDetectionAssociations: [],
  monitoringOrganization: 'testOrg',
  overallPreferred: eventHypothesisWithAssociatedSignalDetectionHypotheses,
  eventHypotheses: [eventHypothesis],
  preferredEventHypothesisByStage: [preferredEventHypothesis],
  finalEventHypothesisHistory: [],
  name: 'test event'
};

export const eventResultsWithOverallPreferredHypothesis: EventsFetchResult = {
  fulfilled: 1,
  isError: true,
  isLoading: false,
  pending: 0,
  rejected: 0,
  data: [eventWithHypothesis]
};
