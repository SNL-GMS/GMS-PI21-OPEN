package gms.core.performancemonitoring.uimaterializedview;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class UiStationGroupSoh {

  /**
   * @return the name of the station group unique for using as Id
   */
  public abstract String getId();

  /**
   * @return the name of the group.
   */
  public abstract String getStationGroupName();

  /**
   * @return the time of creation of this object.
   */
  public abstract long getTime();

  /**
   * @return the capability rollup station of this group
   */
  public abstract UiSohStatus getGroupCapabilityStatus();

  /**
   * @return the time priority order in which to display StationGroups.
   */
  public abstract long getPriority();

  /**
   * Create a new UiStationGroupSoh object.
   *
   * @param id the Id.
   * @param time the time.
   * @param groupCapabilityStatus
   * @param stationGroupName
   * @param priority
   * @return a UiStationGroupSoh object.
   */
  @JsonCreator
  public static UiStationGroupSoh create(
    @JsonProperty("id") String id,
    @JsonProperty("stationGroupName") String stationGroupName,
    @JsonProperty("time") long time,  // epoch milliseconds
    @JsonProperty("groupCapabilityStatus") UiSohStatus groupCapabilityStatus,
    @JsonProperty("priority") int priority
  ) {
    return new AutoValue_UiStationGroupSoh(
      id, stationGroupName, time, groupCapabilityStatus, priority);
  }
}
