package gms.shared.event.coi.featureprediction;

/**
 * Literals for different types of derivatives.
 */
public enum FeaturePredictionDerivativeType {
  TRAVEL_TIME_WITH_RESPECT_TO_DISTANCE,         // HORIZONTAL SLOWNESS
  TRAVEL_TIME_WITH_RESPECT_TO_DEPTH,
  SECOND_TRAVEL_TIME_WITH_RESPECT_TO_DISTANCE,  // HORIZONTAL SLOWNESS wrt DISTANCE
  SECOND_TRAVEL_TIME_WITH_RESPECT_TO_DEPTH,
  HORIZONTAL_SLOWNESS_WITH_RESPECT_TO_DEPTH     // TRAVEL TIME wrt DISTANCE, then wrt to DEPTH
}
