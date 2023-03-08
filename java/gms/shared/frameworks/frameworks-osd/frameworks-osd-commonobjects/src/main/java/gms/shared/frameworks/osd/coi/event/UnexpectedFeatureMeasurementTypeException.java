package gms.shared.frameworks.osd.coi.event;

/**
 * Encountered an unexpected FeatureMeasurementType
 */
public class UnexpectedFeatureMeasurementTypeException extends RuntimeException {

  /**
   * Message string may indicate FeatureMeasurementTypes encountered and expected.
   */
  public UnexpectedFeatureMeasurementTypeException(String message) {
    super(message);
  }
}
