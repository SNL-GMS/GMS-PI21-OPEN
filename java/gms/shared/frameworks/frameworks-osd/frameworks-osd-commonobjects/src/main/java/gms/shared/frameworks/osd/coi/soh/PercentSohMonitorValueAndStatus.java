package gms.shared.frameworks.osd.coi.soh;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import gms.shared.frameworks.osd.coi.soh.SohMonitorType.SohValueType;
import org.msgpack.core.Preconditions;

import java.util.Optional;

/**
 * Represents a monitor value that is a percentage, such as the percentage of missing data.
 */
@AutoValue
public abstract class PercentSohMonitorValueAndStatus implements SohMonitorValueAndStatus<Double> {

  // hiding public default constructor
  PercentSohMonitorValueAndStatus() {
  }


  /**
   * Create a new PercentMonitorValueAndStatus object
   *
   * @param value monitor value
   * @param status status
   * @param monitorType monitor type
   */
  @JsonCreator
  public static PercentSohMonitorValueAndStatus from(
    @JsonProperty("value") Double value,
    @JsonProperty("status") SohStatus status,
    @JsonProperty("monitorType") SohMonitorType monitorType
  ) {
    Preconditions.checkState(monitorType.getSohValueType() == SohValueType.PERCENT,
      "PercentSohMonitorValueAndStatus: monitorType is of SohValueType "
        + monitorType.getSohValueType() + ".  Must be of type SohValueType.PERCENT");

    // The idea here would be that only check if the value is not a NaN, NaNs would be interpreted
    // as Marginal eventually so they should be allowed
    if (value != null && !Double.isNaN(value)) {
      Preconditions.checkArgument(value >= 0 && value <= 100,
        "PercentSohMonitorValueAndStatus requires a value between 0 and 100");
    }

    return new AutoValue_PercentSohMonitorValueAndStatus(
      Optional.ofNullable(value),
      status,
      monitorType
    );
  }
}
