package gms.core.performancemonitoring.uimaterializedview;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import gms.shared.frameworks.osd.coi.soh.SohStatus;
import gms.shared.frameworks.osd.coi.soh.StationSoh;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@AutoValue
public abstract class UiStationSoh {
  /**
   * Id of this object
   */
  public abstract String getId();

  /**
   * UUID of this object
   */
  public abstract UUID getUuid();

  /**
   * SOH Status for station SOH
   */
  public abstract SohStatus getSohStatusSummary();

  /**
   * Does station need acknowledgement (dirty dot on display)
   *
   * @return boolean
   */
  public abstract boolean getNeedsAcknowledgement();

  /**
   * Does station need attention
   */
  public abstract boolean getNeedsAttention();

  /**
   * Soh Contributors
   */
  public abstract List<UiSohContributor> getStatusContributors();

  /**
   * Station Group Capability Rollup Statuses
   */
  public abstract List<UiStationSohCapabilityStatus> getStationGroups();


  /**
   * Time this stated of health was generated.
   */
  public abstract long getTime();

  /**
   * The station whose state of health is being represented.
   */
  public abstract String getStationName();

  /**
   * Set of UiChannelSoh objects representing individual channel SOHs
   */
  public abstract Set<UiChannelSoh> getChannelSohs();

  /**
   * Set of Station Aggregates for this station
   *
   * @return
   */
  public abstract Set<UiStationAggregate> getAllStationAggregates();

  /**
   * Generates a new StationSOH object.
   */
  @JsonCreator
  public static UiStationSoh from(
    @JsonProperty("id") String id,
    @JsonProperty("uuid") UUID uuid,
    @JsonProperty("sohStatusSummary") SohStatus sohStatusSummary,
    @JsonProperty("needsAcknowledgement") boolean needsAcknowledgement,
    @JsonProperty("needsAttention") boolean needsAttention,
    @JsonProperty("statusContributors") List<UiSohContributor> statusContributors,
    @JsonProperty("stationGroups") List<UiStationSohCapabilityStatus> stationGroups,
    @JsonProperty("time") long time,
    @JsonProperty("stationName") String stationName,
    @JsonProperty("channelSohs") Set<UiChannelSoh> channelSohs,
    @JsonProperty("allStationAggregates") Set<UiStationAggregate> allStationAggregates
  ) {

    return new AutoValue_UiStationSoh(
      id,
      uuid,
      sohStatusSummary,
      needsAcknowledgement,
      needsAttention,
      statusContributors,
      stationGroups,
      time,
      stationName,
      channelSohs,
      allStationAggregates
    );
  }

  /**
   * Generates a new StationSoh object.
   * <p>
   * create() methods are used to create an entirely new object. In this case, the create method
   * generates its own unique UUID.
   */
  public static UiStationSoh create(
    StationSoh stationSoh,
    boolean needsAcknowledgement,
    boolean needsAttention,
    List<UiSohContributor> statusContributors,
    List<UiStationSohCapabilityStatus> stationGroups,
    Set<UiChannelSoh> channelSohs
  ) {

    // Convert to UiStationAggregates
    Set<UiStationAggregate> allStationAggregates =
      stationSoh.getAllStationAggregates().stream().map(stationAgg -> UiStationAggregate.from(stationAgg)).collect(
        Collectors.toSet());
    return new AutoValue_UiStationSoh(
      stationSoh.getStationName(),
      stationSoh.getId(),
      stationSoh.getSohStatusRollup(),
      needsAcknowledgement,
      needsAttention,
      statusContributors,
      stationGroups,
      stationSoh.getTime().toEpochMilli(),
      stationSoh.getStationName(),
      channelSohs,
      allStationAggregates
    );
  }
}