import type { ChannelSegment, TimeSeries } from '../channel-segment/types';
import type { Distance, PhaseType } from '../common/types';
import type { Faceted, VersionReference } from '../faceted';
import type {
  DoubleValue,
  FeatureMeasurement,
  FeatureMeasurementType,
  FeatureMeasurementValue,
  SignalDetection,
  SignalDetectionHypothesisFaceted,
  ValueType
} from '../signal-detection';
import type {
  Channel,
  Location
} from '../station-definitions/channel-definitions/channel-definitions';
import type { Station } from '../station-definitions/station-definitions/station-definitions';
import type { WorkflowDefinitionId } from '../workflow/types';

/**
 * Enumerated Restraint Type
 */
export enum RestraintType {
  UNRESTRAINED = 'UNRESTRAINED',
  FIXED = 'FIXED'
}

/**
 * Enumerated Scaling Factor Type
 */
export enum ScalingFactorType {
  CONFIDENCE = 'CONFIDENCE',
  COVERAGE = 'COVERAGE',
  K_WEIGHTED = 'K_WEIGHTED'
}

/**
 * UI specific distance to source object, which has only the fields the UI needs
 */
export interface LocationDistance {
  distance: Distance;
  azimuth: number;
  id: string;
}

/**
 * Event status options
 */
export enum EventStatus {
  IN_PROGRESS = 'IN_PROGRESS',
  COMPLETE = 'COMPLETE',
  NOT_STARTED = 'NOT_STARTED',
  NOT_COMPLETE = 'NOT_COMPLETE'
}

/**
 * Filter status options
 */
export enum FilterStatus {
  IN_PROGRESS = 'IN_PROGRESS',
  COMPLETE = 'COMPLETE',
  NOT_STARTED = 'NOT_STARTED',
  NOT_COMPLETE = 'NOT_COMPLETE'
}

/**
 * Event association status options
 */
export enum AssociationStatus {
  OPEN_ASSOCIATED = 'Open',
  COMPLETE_ASSOCIATED = 'Completed',
  OTHER_ASSOCIATED = 'Other',
  UNASSOCIATED = 'Unassociated'
}

/**
 * Enumerated Depth Restraint Reason
 */
export enum DepthRestraintReason {
  FIXED_AT_DEPTH_FOUND_USING_DEPTH_PHASE_MEASUREMENTS = 'FIXED_AT_DEPTH_FOUND_USING_DEPTH_PHASE_MEASUREMENTS',
  FIXED_AS_STANDARD_DEPTH_FOR_ECM = 'FIXED_AT_STANDARD_DEPTH_FOR_ECM',
  FIXED_AT_SURFACE = 'FIXED_AT_SURFACE',
  FIXED_BY_ANALYST = 'FIXED_BY_ANALYST',
  OTHER = 'OTHER'
}

/**
 * Enumerated Feature Prediction Derivative Type
 */
export enum FeaturePredictionDerivativeType {
  D_DX = 'D_DX',
  D_DY = 'D_DY',
  D_DZ = 'D_DZ',
  D_DT = 'D_DT'
}

/**
 * Enumerated Magnitude Type
 */
export enum MagnitudeType {
  MB = 'MB',
  MB_CODA = 'MB_CODA',
  MB_MB = 'MB_MB',
  MB_MLE = 'MB_MLE',
  MB_PG = 'MB_PG',
  MB_REL_T = 'MB_REL_T',
  ML = 'ML',
  MS = 'MS',
  MS_MLE = 'MS_MLE',
  MS_VMAX = 'MS_VMAX',
  MW_CODA = 'MW_CODA'
}

/**
 * Magnitude Model Enum
 */
export enum MagnitudeModel {
  NUTTLI = 'NUTTLI',
  P_FACTOR = 'P_FACTOR',
  REZAPOUR_PEARCE = 'REZAPOUR_PEARCE',
  RICHTER = 'RICHTER',
  UNKNOWN = 'UNKNOWN',
  VEITH_CLAWSON = 'VEITH_CLAWSON'
}

/**
 * Prediction Component Type Enum
 */
export enum PredictionComponentType {
  BASEMODEL_PREDICTION = 'BASEMODEL_PREDICTION',
  BULK_STATIC_STATION_CORRECTION = 'BASE_STATIC_STATION_CORRECTION',
  ELEVATION_CORRECTION = 'ELEVATION_CORRECTION',
  ELLIPTICITY_CORRECTION = 'ELLIPTICITY_CORRECTION',
  SOURCE_DEPENDENT_CORRECTION = 'SOURCE_DEPENDENT_CORRECTION'
}

/**
 * Event status info for event status update
 */
export interface EventStatusInfo {
  readonly eventStatus: EventStatus;
  readonly activeAnalystIds: string[];
}

/**
 * Filter status info for event status update
 */
export interface FilterStatusInfo {
  readonly filterStatus: FilterStatus;
  readonly activeAnalystIds: string[];
}

/**
 * Ellipse
 */
export interface Ellipse {
  scalingFactorType: ScalingFactorType;
  kWeight: number;
  confidenceLevel: number;
  semiMajorAxisLengthKm: number;
  semiMajorAxisTrendDeg: number;
  semiMinorAxisLengthKm: number;
  depthUncertaintyKm: number;
  timeUncertainty: string;
}

/**
 * Ellipsoid
 */
export interface Ellipsoid {
  scalingFactorType: ScalingFactorType;
  kWeight: number;
  confidenceLevel: number;
  semiMajorAxisLengthKm: number;
  semiMajorAxisTrendDeg: number;
  semiMajorAxisPlungeDeg: number;
  semiIntermediateAxisLengthKm: number;
  semiIntermediateAxisTrendDeg: number;
  semiIntermediateAxisPlungeDeg: number;
  semiMinorAxisLengthKm: number;
  semiMinorAxisTrendDeg: number;
  semiMinorAxisPlungeDeg: number;
  timeUncertainty: string;
}

/**
 * Location Uncertainty
 */
export interface LocationUncertainty {
  xx: number;
  xy: number;
  xz: number;
  xt: number;
  yy: number;
  yz: number;
  yt: number;
  zz: number;
  zt: number;
  tt: number;
  stdDevOneObservation: number;
  ellipses: Ellipse[];
  ellipsoids: Ellipsoid[];
}

/**
 * Station magnitude solution
 */
export interface StationMagnitudeSolution {
  type: MagnitudeType;
  model: MagnitudeModel;
  station: Station;
  phase: PhaseType;
  magnitude: DoubleValue;
  measurement: FeatureMeasurement;
}

/**
 * Network magnitude behavior
 */
export interface NetworkMagnitudeBehavior {
  isDefining: boolean;
  stationMagnitudeSolution: StationMagnitudeSolution;
  residual: number;
  weight: number;
}

/**
 * Network Magnitude Solution
 */
export interface NetworkMagnitudeSolution {
  magnitude: DoubleValue;
  magnitudeBehaviors: NetworkMagnitudeBehavior[];
  type: MagnitudeType;
}

/**
 * Feature Prediction Component
 */
export interface FeaturePredictionComponent {
  value: FeatureMeasurementValue | ValueType;
  extrapolated: boolean;
  predictionComponentType: PredictionComponentType;
}

export interface PredictionValue {
  featureMeasurementType: FeatureMeasurementType;
  predictedValue: FeatureMeasurementValue;
  derivativeMap?: Map<FeaturePredictionDerivativeType, ValueType>;
  featurePredictionComponentSet: FeaturePredictionComponent[];
}

/**
 * Feature prediction
 */
export interface FeaturePrediction {
  phase: PhaseType;
  predictionValue: PredictionValue;
  sourceLocation: EventLocation;
  receiverLocation: Location;
  channel?: VersionReference<Channel> | Channel;
  predictionChannelSegment?: ChannelSegment<TimeSeries>;
  predictionType: FeatureMeasurementType;
}

/**
 * Location Restraint
 */
export interface LocationRestraint {
  depthRestraintType: RestraintType;
  depthRestraintReason?: DepthRestraintReason;
  depthRestraintKm?: number;
  positionRestraintType: RestraintType;
  latitudeRestraintDegrees?: number;
  longitudeRestraintDegrees?: number;
  timeRestraintType: RestraintType;
  timeRestraint?: number;
}

/**
 * Event Location
 */
export interface EventLocation {
  latitudeDegrees: number;
  longitudeDegrees: number;
  depthKm: number;
  time: number;
}

/**
 * Location Behavior
 */
export interface LocationBehavior {
  residual: number;
  weight: number;
  defining: boolean;
  prediction: FeaturePrediction;
  measurement: FeatureMeasurement;
}

/**
 * Location Solution
 */
export interface LocationSolution extends Faceted {
  id: string;
  networkMagnitudeSolutions: NetworkMagnitudeSolution[];
  featurePredictions: {
    featurePredictions: FeaturePrediction[];
  };
  locationUncertainty?: LocationUncertainty;
  locationBehaviors: LocationBehavior[];
  location: EventLocation;
  locationRestraint: LocationRestraint;
}

/**
 * Event Hypothesis ID
 */
export interface EventHypothesisId {
  eventId: string;
  hypothesisId: string;
}

/**
 * Event Hypothesis
 */
export interface EventHypothesis extends Faceted {
  id: EventHypothesisId;
  rejected: boolean;
  parentEventHypotheses: EventHypothesis[];
  associatedSignalDetectionHypotheses: SignalDetectionHypothesisFaceted[];
  preferredLocationSolution?: { id: string };
  locationSolutions: LocationSolution[];
}

/**
 * Preferred Event Hypothesis
 */
export interface PreferredEventHypothesis {
  preferredBy: string;
  stage: WorkflowDefinitionId;
  preferred: EventHypothesis;
}

/**
 * Event
 */
export interface Event extends Faceted {
  id: string;
  rejectedSignalDetectionAssociations: SignalDetection[];
  monitoringOrganization: string;
  overallPreferred?: EventHypothesis;
  eventHypotheses: EventHypothesis[];
  preferredEventHypothesisByStage: PreferredEventHypothesis[];
  finalEventHypothesisHistory: EventHypothesis[];
}
