package gms.core.performancemonitoring.uimaterializedview;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class UiStationAndStationGroups {
  /**
   * @return the Station Group information and capability status
   */
  public abstract List<UiStationGroupSoh> getStationGroups();

  /**
   * @return the UI Station SOH information
   */
  public abstract List<UiStationSoh> getStationSoh();

  /**
   * @return Is this message an update from a Ack or Quiet notification
   */
  public abstract boolean getIsUpdateResponse();

  /**
   * Create a new UiStationAndStationGroups object.
   *
   * @param stationGroups
   * @param stationSoh list of UI Station SOH objects
   * @return a UiStationAndStationGroups object
   */
  @JsonCreator
  public static UiStationAndStationGroups create(
    @JsonProperty("stationGroups") List<UiStationGroupSoh> stationGroups,
    @JsonProperty("stationSoh") List<UiStationSoh> stationSoh,
    @JsonProperty("isUpdateResponse") boolean isUpdateResponse
  ) {
    return new AutoValue_UiStationAndStationGroups(stationGroups, stationSoh, isUpdateResponse);
  }
}
