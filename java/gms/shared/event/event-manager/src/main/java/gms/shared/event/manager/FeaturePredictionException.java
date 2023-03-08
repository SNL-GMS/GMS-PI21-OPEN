package gms.shared.event.manager;

/**
 * Thrown when a request body provided to {@link gms.shared.event.coi.featureprediction.FeaturePrediction}
 * service throws an exception.
 */
class FeaturePredictionException extends Exception {

  public FeaturePredictionException(String message) {
    super(message);
  }
}
