import type {
  ChannelSegment,
  ChannelSegmentDescriptor,
  TimeSeries
} from '../channel-segment/types';
import type { PhaseType, Units } from '../common/types';
import type { VersionReference } from '../faceted';
import type { Channel } from '../station-definitions/channel-definitions/channel-definitions';
import type { Station } from '../station-definitions/station-definitions/station-definitions';

// ***************************************
// Model
// ***************************************

/**
 * Represents a measurement of a signal detection feature,
 * including arrival time, azimuth, slowness and phase
 */
export interface FeatureMeasurement {
  channel: VersionReference<Channel> | Channel;
  measuredChannelSegment: {
    id: ChannelSegmentDescriptor;
  };
  measurementValue: FeatureMeasurementValue;
  featureMeasurementType: FeatureMeasurementType;
  snr?: DoubleValue; // Signal to Noise Ratio as a DoubleValue
}

/**
 * Represents Feature Measurement Value (fields are dependent on type of FM)
 */
// eslint-disable-next-line @typescript-eslint/no-empty-interface
export interface FeatureMeasurementValue {
  // no common members
}

/**
 * Generic value object which are the foundational building blocks to
 * the FeatureMeasurementValue definition
 */
// eslint-disable-next-line @typescript-eslint/no-empty-interface
export interface ValueType {
  // no common members
}
/**
 * Represents Feature Measurement Value for a double type.
 */
export interface DoubleValue extends ValueType {
  value: number;
  standardDeviation: number;
  units: Units;
}

export interface DurationValue extends ValueType {
  value: number;
  standardDeviation: number;
  units: Units;
}

export interface InstantValue extends ValueType {
  value: number;
  standardDeviation: number;
}

/**
 * Represents Feature Measurement Value for a amplitude type.
 */
export interface AmplitudeMeasurementValue extends FeatureMeasurementValue {
  startTime: number;
  period: number;
  amplitude: DoubleValue;
}

/**
 * Represents Feature Measurement Value for Arrival Time FM Type.
 */
export interface ArrivalTimeMeasurementValue extends FeatureMeasurementValue {
  arrivalTime: InstantValue;
  travelTime: DurationValue;
}

/**
 * Represents Feature Measurement Value representing a duration feature measurement value
 */
export interface DurationMeasurementValue extends FeatureMeasurementValue {
  startTime: InstantValue;
  duration: DurationValue;
}

/**
 * Represents Feature Measurement Value for a numeric type.
 */
export interface NumericMeasurementValue extends FeatureMeasurementValue {
  measuredValue: DoubleValue;
  referenceTime?: number;
}

/**
 * Represents Feature Measurement Value for a phase type.
 */
export interface PhaseTypeMeasurementValue extends FeatureMeasurementValue {
  value: PhaseType;
  confidence: number;
  referenceTime: number;
}

/**
 * Represents Feature Measurement Value for Rectilinearity
 */
export interface RectilinearityMeasurementValue extends FeatureMeasurementValue {
  measuredValue: DoubleValue;
  referenceTime: number;
}

/**
 * Represents Feature Measurement Value for EmergenceAngle
 */
export interface EmergenceAngleMeasurementValue extends FeatureMeasurementValue {
  measuredValue: DoubleValue;
  referenceTime: number;
}

/**
 * Represents Feature Measurement Value for LongPeriodFirstMotion
 */
export interface LongPeriodFirstMotionMeasurementValue extends FeatureMeasurementValue {
  value: string;
  confidence: number;
  referenceTime: number;
}
/**
 * Represents Feature Measurement Value for ShortPeriodFirstMotion
 */
export interface ShortPeriodFirstMotionMeasurementValue extends FeatureMeasurementValue {
  value: string;
  confidence: number;
  referenceTime: number;
}

/**
 * Represents Feature Measurement Value for first motion.
 */
export interface FirstMotionMeasurementValue extends FeatureMeasurement {
  value: string;
  confidence: number;
  referenceTime: number;
}
/**
 * Represents Feature Measurement Value for a numeric type.
 */
export interface StringMeasurementValue extends FeatureMeasurementValue {
  strValue: string;
}

export enum AmplitudeType {
  AMPLITUDE_A5_OVER_2 = 'AMPLITUDE_A5_OVER_2',
  AMPLITUDE_A5_OVER_2_OR = 'AMPLITUDE_A5_OVER_2_OR',
  AMPLITUDE_ALR_OVER_2 = 'AMPLITUDE_ALR_OVER_2',
  AMPLITUDEh_ALR_OVER_2 = 'AMPLITUDEh_ALR_OVER_2',
  AMPLITUDE_ANL_OVER_2 = 'AMPLITUDE_ANL_OVER_2',
  AMPLITUDE_SBSNR = 'AMPLITUDE_SBSNR',
  AMPLITUDE_FKSNR = 'AMPLITUDE_FKSNR'
}

/**
 * Enumeration of feature measurement type names
 */
export enum FeatureMeasurementType {
  ARRIVAL_TIME = 'ARRIVAL_TIME',
  RECEIVER_TO_SOURCE_AZIMUTH = 'RECEIVER_TO_SOURCE_AZIMUTH',
  SOURCE_TO_RECEIVER_AZIMUTH = 'SOURCE_TO_RECEIVER_AZIMUTH',
  SLOWNESS = 'SLOWNESS',
  PHASE = 'PHASE',
  EMERGENCE_ANGLE = 'EMERGENCE_ANGLE',
  PERIOD = 'PERIOD',
  RECTILINEARITY = 'RECTILINEARITY',
  SNR = 'SNR',
  AMPLITUDE = 'AMPLITUDE',
  AMPLITUDE_A5_OVER_2 = 'AMPLITUDE_A5_OVER_2',
  AMPLITUDE_A5_OVER_2_OR = 'AMPLITUDE_A5_OVER_2_OR',
  AMPLITUDE_ALR_OVER_2 = 'AMPLITUDE_ALR_OVER_2',
  AMPLITUDEh_ALR_OVER_2 = 'AMPLITUDEh_ALR_OVER_2',
  AMPLITUDE_ANL_OVER_2 = 'AMPLITUDE_ANL_OVER_2',
  AMPLITUDE_SBSNR = 'AMPLITUDE_SBSNR',
  AMPLITUDE_FKSNR = 'AMPLITUDE_FKSNR',
  FILTERED_BEAM = 'FILTERED_BEAM',
  LONG_PERIOD_FIRST_MOTION = 'LONG_PERIOD_FIRST_MOTION',
  SHORT_PERIOD_FIRST_MOTION = 'SHORT_PERIOD_FIRST_MOTION',
  SOURCE_TO_RECEIVER_DISTANCE = 'SOURCE_TO_RECEIVER_DISTANCE'
}

/**
 * Signal detection hypothesis id interface
 */
export interface SignalDetectionHypothesisId {
  id: string;
  signalDetectionId: string;
}

/**
 * Faceted Signal Detection Hypotheses
 */
export interface SignalDetectionHypothesisFaceted {
  id: SignalDetectionHypothesisId;
}

/**
 * Signal detection hypothesis interface used in Signal detection
 */
export interface SignalDetectionHypothesis extends SignalDetectionHypothesisFaceted {
  monitoringOrganization: string;
  rejected: boolean;
  featureMeasurements: FeatureMeasurement[];
  parentSignalDetectionHypothesis: SignalDetectionHypothesis | null;
}

/**
 * Represents a Signal detection
 */
export interface SignalDetection {
  id: string;
  monitoringOrganization: string;
  station: VersionReference<Station> | Station;
  signalDetectionHypotheses: SignalDetectionHypothesis[];
}

export interface SignalDetectionsWithChannelSegments {
  signalDetections: SignalDetection[];
  channelSegments: ChannelSegment<TimeSeries>[];
}

/**
 * Basic info for a hypothesis
 */
export interface ConflictingSdHypData {
  eventId: string;
  phase: PhaseType;
  arrivalTime: number;
  stationName?: string;
  eventTime?: number;
}
