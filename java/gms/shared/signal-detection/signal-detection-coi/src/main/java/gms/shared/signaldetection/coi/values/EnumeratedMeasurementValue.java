package gms.shared.signaldetection.coi.values;

import java.io.Serializable;
import java.time.Instant;
import java.util.Optional;

/**
 * An enumerated measurement is a measurement that has a value that is an enumerated type and has a
 * confidence on that selection of that value.
 *
 * @param <E> the type of Enum
 */
public interface EnumeratedMeasurementValue<E extends Enum<E>> extends Serializable {

  /**
   * Gets the value of this enumerated measurement.
   *
   * @return the value
   */
  E getValue();

  /**
   * Gets the confidence level of this enumerated measurement.
   *
   * @return the confidence level
   */
  Optional<Double> getConfidence();

  /**
   * Reference time corresponding to a sample time for where in the
   * ChannelSegment the measurement was made
   *
   * @return reference time instant
   */
  Optional<Instant> getReferenceTime();
}
