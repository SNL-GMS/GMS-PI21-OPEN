package gms.core.performancemonitoring.uimaterializedview;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class UiStationSohCapabilityStatus {
  /**
   * group name
   */
  public abstract String getGroupName();

  /**
   * station name
   */
  public abstract String getStationName();

  /**
   * Capability Soh Status
   */
  public abstract UiSohStatus getSohStationCapability();

  /**
   * Generates a new UiStationSohCapabilityStatus object.
   */
  @JsonCreator
  public static UiStationSohCapabilityStatus from(
    @JsonProperty("groupName") String groupName,
    @JsonProperty("stationName") String stationName,
    @JsonProperty("sohStationCapability") UiSohStatus sohStationCapability
  ) {

    return new AutoValue_UiStationSohCapabilityStatus(
      groupName, stationName, sohStationCapability
    );
  }

  /**
   * Generates a new UiStationSohCapabilityStatus object.
   * <p>
   * create() methods are used to create an entirely new object.
   */
  public static UiStationSohCapabilityStatus create(
    String groupName,
    String stationName,
    UiSohStatus sohStationCapability
  ) {
    return new AutoValue_UiStationSohCapabilityStatus(
      groupName, stationName, sohStationCapability
    );
  }
}