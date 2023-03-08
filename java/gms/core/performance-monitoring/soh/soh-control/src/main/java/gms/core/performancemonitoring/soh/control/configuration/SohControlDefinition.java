package gms.core.performancemonitoring.soh.control.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.time.Duration;

/**
 * Configuration values for use by the SohControl class.
 */
@AutoValue
public abstract class SohControlDefinition {

  /**
   * @return the reprocessing period.
   */
  public abstract Duration getReprocessingPeriod();

  @JsonCreator
  public static SohControlDefinition create(
    @JsonProperty("reprocessingPeriod") Duration reprocessingPeriod) {

    return new AutoValue_SohControlDefinition(
      reprocessingPeriod
    );
  }
}
