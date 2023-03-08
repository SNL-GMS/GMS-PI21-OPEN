package gms.shared.frameworks.osd.coi.soh;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import gms.shared.frameworks.osd.coi.soh.SohMonitorType.SohValueType;
import org.apache.commons.lang3.Validate;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents a monitor value of type Duration, such as lag.
 */
@AutoValue
public abstract class DurationSohMonitorValueAndStatus implements
  SohMonitorValueAndStatus<Duration> {

  // hiding public default constructor
  DurationSohMonitorValueAndStatus() {
  }


  /**
   * Create a new DurationMonitorValueAndStatus object
   *
   * @param value monitor value
   * @param status status
   * @param monitorType monitor type
   */
  @JsonCreator
  public static DurationSohMonitorValueAndStatus from(
    @JsonProperty("value") Duration value,
    @JsonProperty("status") SohStatus status,
    @JsonProperty("monitorType") SohMonitorType monitorType
  ) {

    Objects.requireNonNull(monitorType);
    Validate.isTrue(monitorType.getSohValueType() == SohValueType.DURATION,
      "DurationSohMonitorValueAndStatus: monitorType is of SohValueType "
        + monitorType.getSohValueType() + ".  Must be of type SohValueType.DURATION");

    return new AutoValue_DurationSohMonitorValueAndStatus(
      Optional.ofNullable(value),
      status,
      monitorType
    );
  }

}
