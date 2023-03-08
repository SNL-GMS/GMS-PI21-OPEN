import type { Distance, PhaseType, TimeRange } from '../../common/types';
import type {
  FeatureMeasurement,
  FeatureMeasurementType,
  FeatureMeasurementValue
} from '../../signal-detection/types';

// ***************************************
// Subscriptions
// ***************************************

export interface EventUpdatedSubscription {
  eventsUpdated: {
    id: string;
    status: string;
    activeAnalysts: {
      userName: string;
    }[];
  }[];
}

export interface EventsCreatedSubscription {
  eventsCreated: Event[];
}

// ***************************************
// Queries
// ***************************************

export interface EventsInTimeRangeQueryArgs {
  timeRange: TimeRange;
}

export interface EventsByIdQueryArgs {
  eventId: string;
}

// ***************************************
// Model
// ***************************************

export enum EventStatus {
  ReadyForRefinement = 'ReadyForRefinement',
  OpenForRefinement = 'OpenForRefinement',
  AwaitingReview = 'AwaitingReview',
  Complete = 'Complete'
}

export interface SignalDetectionEventAssociation {
  id: string;
  rejected: boolean;
  signalDetectionHypothesis: {
    id: string;
    rejected: boolean;
    parentSignalDetectionId: string;
  };
  eventHypothesisId: string;
}

export interface EventHypothesis {
  id: string;
  rejected: boolean;
  event: {
    id: string;
    status: EventStatus;
  };
  associationsMaxArrivalTime: number;
  signalDetectionAssociations: SignalDetectionEventAssociation[];
  // Not in OSD, only has a single set and is called locationSolutions
  locationSolutionSets: LocationSolutionSet[];
  preferredLocationSolution: PreferredLocationSolution;
}

export interface Ellipse {
  scalingFactorType: ScalingFactorType;
  kWeight: number;
  confidenceLevel: number;
  majorAxisLength: string;
  majorAxisTrend: number;
  minorAxisLength: string;
  minorAxisTrend: number;
  depthUncertainty: number;
  timeUncertainty: string;
}

export interface Ellipsoid {
  scalingFactorType: ScalingFactorType;
  kWeight: number;
  confidenceLevel: number;
  majorAxisLength: string;
  majorAxisTrend: number;
  majorAxisPlunge: number;
  intermediateAxisLength: number;
  intermediateAxisTrend: number;
  intermediateAxisPlunge: number;
  minorAxisLength: string;
  minorAxisTrend: number;
  minorAxisPlunge: number;
  depthUncertainty: number;
  timeUncertainty: string;
}

/**
 * Represents a preference relationship between an event hypothesis and a location solution.
 * Creation information is included in order to capture provenance of the preference.
 */
export interface PreferredLocationSolution {
  locationSolution: LocationSolution;
}
/**
 * Location Uncertainty for event Location Solution
 */
export interface LocationUncertainty {
  xy: number;
  xz: number;
  xt: number;
  yy: number;
  yz: number;
  yt: number;
  zz: number;
  zt: number;
  tt: number;
  stDevOneObservation: number;
  ellipses: Ellipse[];
  ellipsoids: Ellipsoid[];
}

/**
 * UI specific distance to source object, which has only the fields the UI needs
 */
export interface LocationToStationDistance {
  distance: Distance;
  azimuth: number;
  stationId: string;
}

/**
 * Represents an estimate of the location of an event, defined as latitude, longitude,
 * depth, and time. A location solution is often determined by a location algorithm that
 * minimizes the difference between feature measurements (usually arrival time, azimuth,
 * and slowness) and corresponding feature predictions.
 */
export interface LocationSolution {
  id: string;
  locationType: string;
  location: EventLocation;
  featurePredictions: FeaturePrediction[];
  locationRestraint: LocationRestraint;
  locationUncertainty?: LocationUncertainty;
  locationBehaviors: LocationBehavior[];
  networkMagnitudeSolutions: NetworkMagnitudeSolution[];
  locationToStationDistances: LocationToStationDistance[];
  snapshots: SignalDetectionSnapshot[];
}

/**
 * Location Solution Set
 * defines a list of location solutions for an event hypothesis
 * including a snapshot of association when solutions were created
 */
export interface LocationSolutionSet {
  id: string;
  count: number;
  locationSolutions: LocationSolution[];
}

/**
 * Snapshot of state of associations when location solution was created
 */
export interface SignalDetectionSnapshot {
  signalDetectionId: string;
  signalDetectionHypothesisId: string;
  stationName: string;
  channelName: string;
  phase: PhaseType;
  time: EventSignalDetectionAssociationValues;
  slowness: EventSignalDetectionAssociationValues;
  azimuth: EventSignalDetectionAssociationValues;
  aFiveAmplitude?: AmplitudeSnapshot;
  aLRAmplitude?: AmplitudeSnapshot;
}

/**
 * Generic interface for snapshot values of a signal detection association
 */
export interface EventSignalDetectionAssociationValues {
  defining: boolean;
  observed: number;
  residual: number;
  correction: number;
}

/**
 * Helper interface to contain amplitude and period from amplitude fm's
 */
export interface AmplitudeSnapshot {
  period: number;
  amplitudeValue: number;
}

/**
 * Location Behavior for event Location Solution
 */
export interface LocationBehavior {
  residual: number;
  weight: number;
  defining: boolean;
  featureMeasurementType: FeatureMeasurementType;
  signalDetectionId?: string;
}

/**
 * Location Restraint for event Location Solution
 */
export interface LocationRestraint {
  depthRestraintType: DepthRestraintType;
  depthRestraintKm: number;
  latitudeRestraintType: RestraintType;
  latitudeRestraintDegrees?: number;
  longitudeRestraintType: RestraintType;
  longitudeRestraintDegrees?: number;
  timeRestraintType: RestraintType;
  timeRestraint?: string;
}
/**
 *
 * Event Location definition
 */
export interface EventLocation {
  latitudeDegrees: number;
  longitudeDegrees: number;
  depthKm: number;
  time: number;
}
/**
 * Feature Prediction - part of preferred event hypothesis
 */
export interface FeaturePrediction {
  predictedValue: FeatureMeasurementValue;
  predictionType: FeatureMeasurementType;
  phase: string;
  channelName?: string;
  stationName?: string;
}

export interface PreferredEventHypothesis {
  processingStage: {
    id: string;
  };
  eventHypothesis: EventHypothesis;
}

export interface Event {
  id: string;
  status: EventStatus;
  modified: boolean;
  hasConflict: boolean;
  currentEventHypothesis: PreferredEventHypothesis;
  conflictingSdIds: string[];
}

/**
 * RestraintType for Location Restraint
 */
export enum RestraintType {
  UNRESTRAINED = 'UNRESTRAINED',
  FIXED = 'FIXED'
}

/**
 * DepthRestraintType for Location Restraint
 */
export enum DepthRestraintType {
  UNRESTRAINED = 'UNRESTRAINED',
  FIXED_AT_DEPTH = 'FIXED_AT_DEPTH',
  FIXED_AT_SURFACE = 'FIXED_AT_SURFACE'
}

/**
 * Makes depth restraint type to a more human readable format
 */
export enum PrettyDepthRestraint {
  UNRESTRAINED = 'Unrestrained',
  FIXED_AT_DEPTH = 'Fixed at depth',
  FIXED_AT_SURFACE = 'Fixed at surface'
}
/**
 * ScalingFactorType in  Ellipse and Ellipsoid
 */
export enum ScalingFactorType {
  CONFIDENCE = 'CONFIDENCE',
  COVERAGE = 'COVERAGE',
  K_WEIGHTED = 'K_WEIGHTED'
}

/**
 * Enumerated type of magnitude solution (surface wave, body wave, local, etc.)
 */
export enum MagnitudeType {
  MB = 'MB',
  MBMLE = 'MBMLE',
  // MBREL = 'MBREL',
  MS = 'MS',
  MSMLE = 'MSMLE'
}

/**
 * Magnitude types
 */
export enum MagnitudeModel {
  RICHTER = 'RICHTER',
  VEITH_CLAWSON = 'VEITH_CLAWSON',
  REZAPOUR_PEARCE = 'REZAPOUR_PEARCE',
  NUTTLI = 'NUTTLI',
  QFVC = 'QFVC',
  QFVC1 = 'QFVC1'
}

/**
 * Station magnitude solution
 */
export interface StationMagnitudeSolution {
  type: MagnitudeType;
  model: MagnitudeModel;
  stationName: string;
  phase: PhaseType;
  magnitude: number;
  magnitudeUncertainty: number;
  modelCorrection: number;
  stationCorrection: number;
  featureMeasurement: FeatureMeasurement;
}

/**
 * Network magnitude behavior
 */
export interface NetworkMagnitudeBehavior {
  defining: boolean;
  stationMagnitudeSolution: StationMagnitudeSolution;
  residual: number;
  weight: number;
}

/**
 * Represents an estimate of an event's magnitude based on detections from multiple stations.
 */
export interface NetworkMagnitudeSolution {
  uncertainty: number;
  magnitudeType: MagnitudeType;
  magnitude: number;
  networkMagnitudeBehaviors: NetworkMagnitudeBehavior[];
}

export interface ComputeNetworkMagnitudeInput {
  eventHypothesisId: string;
  magnitudeType: MagnitudeType;
  stationNames: string[];
  defining: boolean;
  locationSolutionSetId: string;
}
