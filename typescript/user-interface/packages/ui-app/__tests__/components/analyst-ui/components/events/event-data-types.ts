import { EventTypes } from '@gms/common-model';
import { Units } from '@gms/common-model/lib/common/types';
import { SECONDS_IN_HOUR } from '@gms/common-util';
import type { EventsFetchResult } from '@gms/ui-state';

const eventId = 'eventID';
const hypothesisId = 'hypothesisID';
const locationSolutionId = 'locationSolutionID';
const workflowDefinitionId = { name: 'AL1', effectiveTime: 0 };

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

export const rejectedEventHypothesis: EventTypes.EventHypothesis = {
  id: eventHypothesisId,
  rejected: true,
  parentEventHypotheses: [eventHypothesis],
  associatedSignalDetectionHypotheses: [],
  preferredLocationSolution: null,
  locationSolutions: [],
  name: 'rejected event hypothesis'
};

export const preferredEventHypothesisRejected: EventTypes.PreferredEventHypothesis = {
  preferredBy: 'preferredAnalyst',
  stage: workflowDefinitionId,
  preferred: rejectedEventHypothesis
};

export const event: EventTypes.Event = {
  id: eventId,
  rejectedSignalDetectionAssociations: [],
  monitoringOrganization: 'testOrg',
  overallPreferred: eventHypothesis,
  eventHypotheses: [eventHypothesis],
  preferredEventHypothesisByStage: [preferredEventHypothesis],
  finalEventHypothesisHistory: [],
  name: 'test event'
};

export const rejectedEvent: EventTypes.Event = {
  id: eventId,
  rejectedSignalDetectionAssociations: [],
  monitoringOrganization: 'testOrg',
  overallPreferred: rejectedEventHypothesis,
  eventHypotheses: [eventHypothesis, rejectedEventHypothesis],
  preferredEventHypothesisByStage: [preferredEventHypothesisRejected],
  finalEventHypothesisHistory: [],
  name: 'test event'
};

export const eventResults: EventsFetchResult = {
  fulfilled: 1,
  isError: true,
  isLoading: false,
  pending: 0,
  rejected: 0,
  data: [event]
};

export const eventResultsWithRejected: EventsFetchResult = {
  fulfilled: 1,
  isError: true,
  isLoading: false,
  pending: 0,
  rejected: 0,
  data: [event, rejectedEvent]
};
