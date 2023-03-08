package gms.core.performancemonitoring.soh.control.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.time.Duration;

/**
 * Thresholds for Duration values
 */
@AutoValue
public abstract class DurationSohMonitorStatusThresholdDefinition implements
  SohMonitorStatusThresholdDefinition<Duration> {

  // hiding public default constructor
  DurationSohMonitorStatusThresholdDefinition() {
  }

  @JsonCreator
  public static DurationSohMonitorStatusThresholdDefinition create(
    @JsonProperty("goodThreshold") Duration goodThreshold,
    @JsonProperty("marginalThreshold") Duration marginalThreshold
  ) {

    return new AutoValue_DurationSohMonitorStatusThresholdDefinition(
      goodThreshold,
      marginalThreshold
    );
  }
}
