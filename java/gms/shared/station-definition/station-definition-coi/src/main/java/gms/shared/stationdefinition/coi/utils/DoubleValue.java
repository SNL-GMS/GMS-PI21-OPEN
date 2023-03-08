package gms.shared.stationdefinition.coi.utils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.Optional;

/**
 * A value class for storing a double along with its units and uncertainty
 */
@AutoValue
public abstract class DoubleValue {

  public abstract double getValue();

  public abstract Optional<Double> getStandardDeviation();

  public abstract Units getUnits();

  @JsonCreator
  public static DoubleValue from(
    @JsonProperty("value") double value,
    @JsonProperty("standardDeviation") Optional<Double> standardDeviation,
    @JsonProperty("units") Units units) {
    return new AutoValue_DoubleValue(value, standardDeviation, units);
  }

}
