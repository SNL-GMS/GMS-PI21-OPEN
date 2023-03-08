import type {
  ArrivalTimeMeasurementValue,
  FeatureMeasurement,
  SignalDetection,
  SignalDetectionHypothesis
} from './types';
import { FeatureMeasurementType } from './types';

/**
 * Get the current Hypothesis from the set of Hypotheses. This will be the last entry in the set if there is one
 * Returns undefined on empty arrays
 *
 * @param hypotheses the set of Hypotheses.
 * @return the current Hypothesis
 */
export function getCurrentHypothesis(
  hypotheses: SignalDetectionHypothesis[]
): SignalDetectionHypothesis {
  return hypotheses?.slice(-1)[0];
}

/**
 * Searches Feature Measurements for the desired Feature Measurement
 *
 * @param featureMeasurements List of feature measurements
 * @param featureMeasurementType Enum of desired Feature Measurement desired
 *
 * @returns FeatureMeasurement or undefined if not found
 */
export function findFeatureMeasurementByType(
  featureMeasurements: FeatureMeasurement[],
  featureMeasurementType: FeatureMeasurementType
): FeatureMeasurement | undefined {
  if (featureMeasurements && featureMeasurementType) {
    return featureMeasurements.find(fm => fm?.featureMeasurementType === featureMeasurementType);
  }
  return undefined;
}

/**
 * Searches a SignalDetection for the ArrivalTime Feature Measurement
 *
 * @param signalDetection to search for ArrivalTime
 *
 * @returns ArrivalTime FeatureMeasurement or undefined if not found
 */
export function findArrivalTimeFeatureMeasurementUsingSignalDetection(
  signalDetection: SignalDetection
): FeatureMeasurement | undefined {
  const currentHypo = getCurrentHypothesis(signalDetection.signalDetectionHypotheses);
  if (!currentHypo) {
    return undefined;
  }
  return findFeatureMeasurementByType(
    currentHypo.featureMeasurements,
    FeatureMeasurementType.ARRIVAL_TIME
  );
}

/**
 * Searches Feature Measurements for the ArrivalTime Feature Measurement
 *
 * @param featureMeasurements List of feature measurements
 *
 * @returns ArrivalTime FeatureMeasurement or undefined if not found
 */
export function findArrivalTimeFeatureMeasurement(
  featureMeasurements: FeatureMeasurement[]
): FeatureMeasurement | undefined {
  return findFeatureMeasurementByType(featureMeasurements, FeatureMeasurementType.ARRIVAL_TIME);
}

/**
 * Searches Feature Measurements for the ArrivalTime Feature Measurement Value
 *
 * @param featureMeasurements List of feature measurements
 *
 * @returns ArrivalTime FeatureMeasurementValue or undefined if not found
 */
export function findArrivalTimeFeatureMeasurementValue(
  featureMeasurements: FeatureMeasurement[]
): ArrivalTimeMeasurementValue | undefined {
  const fm = findArrivalTimeFeatureMeasurement(featureMeasurements);
  return fm ? (fm.measurementValue as ArrivalTimeMeasurementValue) : undefined;
}
