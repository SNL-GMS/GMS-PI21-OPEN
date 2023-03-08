package gms.core.performancemonitoring.uimaterializedview;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import gms.shared.frameworks.osd.coi.soh.SohMonitorType;
import gms.shared.frameworks.osd.coi.soh.SohStatus;

@AutoValue
public abstract class UiSohContributor {
  /**
   * value of SOH Contributor
   */
  public abstract Double getValue();

  /**
   * is value present (was the value set in the SohMonitorValueAndStatus)
   */
  public abstract boolean getValuePresent();

  /**
   * status of SOH Contributor
   */
  public abstract SohStatus getStatusSummary();

  /**
   * is contributing to Soh Rollup
   */
  public abstract boolean isContributing();

  /**
   * monitor type of SOH Contributor
   */
  public abstract SohMonitorType getType();

  /**
   * Generates a new UiSohContributor object.
   */
  @JsonCreator
  public static UiSohContributor from(
    @JsonProperty("value") Double value,
    @JsonProperty("valuePresent") boolean valuePresent,
    @JsonProperty("statusSummary") SohStatus statusSummary,
    @JsonProperty("contributing") boolean contributing,
    @JsonProperty("type") SohMonitorType type
  ) {

    return new AutoValue_UiSohContributor(
      value, valuePresent, statusSummary, contributing, type
    );
  }
}