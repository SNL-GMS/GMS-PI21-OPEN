package gms.shared.signaldetection.coi.types;


import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.io.Serializable;

/**
 * Enumeration for types of {@link gms.shared.signaldetection.coi.detection.FeatureMeasurement}
 * NOTE: Json Serialization has been customized to produce a single value string for the name. Since deserialization
 * relies on {@link FeatureMeasurementTypes}, custom measurement types will not deserialize properly, as they lack
 * the id info necessary to assume the type.
 */

// This is used to deserialize references of FeatureMeasurementType into particular implementing classes based on name.
@JsonDeserialize(using = FeatureMeasurementTypeDeserializer.class)
public interface FeatureMeasurementType<T> extends Serializable {

  /**
   * Gets the class of the feature measurement.
   *
   * @return the class
   */
  Class<T> getMeasurementValueType();

  /**
   * Gets the name of the feature measurement type.
   *
   * @return the name
   */
  @JsonValue
  String getFeatureMeasurementTypeName();
}
