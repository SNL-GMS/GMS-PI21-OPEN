package gms.shared.frameworks.osd.coi.soh;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@AutoValue
public abstract class CapabilitySohRollup {

  /**
   * @return the id.
   */
  public abstract UUID getId();

  /**
   * @return the time when the CapabilitySohRollup was computed.
   */
  public abstract Instant getTime();

  /**
   * @return the StationGroup's capability rollup.
   */
  public abstract SohStatus getGroupRollupSohStatus();

  /**
   * @return the StationGroup for which the CapabilitySohRollup was computed.  Over time, each
   * StationGroup can be associated with many CapabilitySohRollups that were each computed at a
   * different time.
   */
  public abstract String getForStationGroup();

  /**
   * @return a collection of StationSoh IDs for all of the StationSoh objects containing information
   * used to calculate the CapabilitySohRollup.
   */
  public abstract Set<UUID> getBasedOnStationSohs();

  /**
   * @return a map of Station IDs to their rollup SohStatus.
   */
  public abstract Map<String, SohStatus> getRollupSohStatusByStation();

  /**
   * Create a new CapabilitySohRollup object.
   *
   * @param id the Id.
   * @param time the time.
   * @return a CapabilitySohRollup object.
   */
  @JsonCreator
  public static CapabilitySohRollup create(
    @JsonProperty("id") UUID id,
    @JsonProperty("time") Instant time,
    @JsonProperty("groupRollupSohStatus") SohStatus groupRollupSohStatus,
    @JsonProperty("forStationGroup") String forStationGroup,
    @JsonProperty("basedOnStationSohs") Set<UUID> basedOnStationSohs,
    @JsonProperty("rollupSohStatusByStation") Map<String, SohStatus> rollupSohStatusByStation
  ) {

    return new AutoValue_CapabilitySohRollup(
      id, time, groupRollupSohStatus, forStationGroup, basedOnStationSohs, rollupSohStatusByStation
    );
  }

}
