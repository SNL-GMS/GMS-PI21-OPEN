package gms.core.performancemonitoring.soh.control.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

/**
 * Thresholds for percentage values
 */
@AutoValue
public abstract class PercentSohMonitorStatusThresholdDefinition implements
  SohMonitorStatusThresholdDefinition<Double> {

  // hiding public default constructor
  PercentSohMonitorStatusThresholdDefinition() {
  }

  @JsonCreator
  public static PercentSohMonitorStatusThresholdDefinition create(
    @JsonProperty("goodThreshold") double goodThreshold,
    @JsonProperty("marginalThreshold") double marginalThreshold
  ) {

    return new AutoValue_PercentSohMonitorStatusThresholdDefinition(
      goodThreshold,
      marginalThreshold
    );
  }
}
