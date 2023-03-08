package gms.shared.frameworks.osd.coi.signaldetection;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;

import java.io.Serializable;

/**
 * Enumeration for types of {@link FeatureMeasurement}
 */

// This is used to deserialize references of FeatureMeasurementType into particular implementing classes based on name.
@JsonTypeIdResolver(FeatureMeasurementTypeIdResolver.class)
public interface FeatureMeasurementType<T> extends Serializable {

  /**
   * Gets the class of the feature measurement.
   *
   * @return the class
   */
  @JsonIgnore
  Class<T> getMeasurementValueType();

  /**
   * Gets the name of the feature measurement type.
   *
   * @return the name
   */
  String getFeatureMeasurementTypeName();
}
