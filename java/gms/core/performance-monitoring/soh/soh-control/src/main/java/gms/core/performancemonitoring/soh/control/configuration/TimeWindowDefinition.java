package gms.core.performancemonitoring.soh.control.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.time.Duration;

/**
 * Container for the calculation interval and back off duration configuration
 */
@AutoValue
public abstract class TimeWindowDefinition {

  /**
   * @return calculation interval
   */
  public abstract Duration getCalculationInterval();

  /**
   * @return back off duration
   */
  public abstract Duration getBackOffDuration();

  @JsonCreator
  public static TimeWindowDefinition create(
    @JsonProperty("calculationInterval") Duration calculationInterval,
    @JsonProperty("backOffDuration") Duration backOffDuration
  ) {

    return new AutoValue_TimeWindowDefinition(
      calculationInterval,
      backOffDuration
    );
  }
}
