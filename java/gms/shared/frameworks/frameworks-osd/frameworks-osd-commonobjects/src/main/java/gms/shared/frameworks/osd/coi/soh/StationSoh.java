package gms.shared.frameworks.osd.coi.soh;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableSet;
import gms.shared.frameworks.osd.coi.channel.Channel;
import gms.shared.frameworks.osd.coi.signaldetection.Station;
import org.apache.commons.lang3.Validate;

import java.time.Instant;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Represents the state of health of a station.
 */
@AutoValue
@JsonSerialize(as = StationSoh.class)
@JsonDeserialize(builder = AutoValue_StationSoh.Builder.class)
public abstract class StationSoh {

  /**
   * Id of this object
   */
  public abstract UUID getId();

  /**
   * Time this stated of health was generated.
   */
  public abstract Instant getTime();

  /**
   * The station whose state of health is being represented.
   */
  public abstract String getStationName();

  /**
   * Worst of the statuses that were monitored
   */
  public abstract SohStatus getSohStatusRollup();

  /**
   * Set of Monitor status and values for this station
   */
  public abstract ImmutableSet<SohMonitorValueAndStatus<?>> getSohMonitorValueAndStatuses();

  /**
   * Set of ChannelSoh objects representing individual channel SOHs
   */
  public abstract ImmutableSet<ChannelSoh> getChannelSohs();

  /**
   * Set of Station Aggregates for this station
   */
  public abstract ImmutableSet<StationAggregate<?>> getAllStationAggregates();

  @JsonCreator
  public static StationSoh from(
    @JsonProperty("id") UUID id,
    @JsonProperty("time") Instant time,
    @JsonProperty("stationName") String stationName,
    @JsonProperty("sohMonitorValueAndStatuses") Set<SohMonitorValueAndStatus<?>> sohMonitorValueAndStatuses,
    @JsonProperty("sohStatusRollup") SohStatus sohStatusRollup,
    @JsonProperty("channelSohs") Set<ChannelSoh> channelSohs,
    @JsonProperty("allStationAggregates") Set<StationAggregate<?>> allStationAggregates
  ) {

    return StationSoh.builder()
      .setId(id)
      .setTime(time)
      .setStationName(stationName)
      .setSohStatusRollup(sohStatusRollup)
      .setSohMonitorValueAndStatuses(sohMonitorValueAndStatuses)
      .setChannelSohs(channelSohs)
      .setAllStationAggregates(allStationAggregates)
      .build();
  }

  /**
   * Generates a new StationSoh object.
   * <p>
   * create() methods are used to create an entirely new object. In this case, the create method generates its own
   * unique UUID.
   */
  public static StationSoh create(
    Instant time,
    String stationName,
    Set<SohMonitorValueAndStatus<?>> sohMonitorValueAndStatuses,
    SohStatus sohStatusRollup,
    Set<ChannelSoh> channelSohs,
    Set<StationAggregate<?>> allStationAggregates
  ) {
    return StationSoh.builder()
      .generateId()
      .setTime(time)
      .setStationName(stationName)
      .setSohStatusRollup(sohStatusRollup)
      .setSohMonitorValueAndStatuses(sohMonitorValueAndStatuses)
      .setChannelSohs(channelSohs)
      .setAllStationAggregates(allStationAggregates)
      .build();
  }

  public abstract Builder toBuilder();

  public static Builder builder() {
    return new AutoValue_StationSoh.Builder();
  }

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public abstract static class Builder {

    public abstract Builder setId(UUID id);

    public Builder generateId() {
      return setId(UUID.randomUUID());
    }

    public abstract Builder setTime(Instant time);

    public abstract Builder setStationName(String stationName);

    public abstract Builder setSohStatusRollup(SohStatus sohStatusRollup);

    abstract Builder setAllStationAggregates(ImmutableSet<StationAggregate<?>> allStationAggregates);

    public Builder setAllStationAggregates(Collection<StationAggregate<?>> allStationAggregates) {
      return setAllStationAggregates(ImmutableSet.copyOf(allStationAggregates));
    }

    abstract ImmutableSet.Builder<StationAggregate<?>> allStationAggregatesBuilder();

    abstract Builder setSohMonitorValueAndStatuses(
      ImmutableSet<SohMonitorValueAndStatus<?>> sohMonitorValueAndStatuses);

    public Builder setSohMonitorValueAndStatuses(
      Collection<SohMonitorValueAndStatus<?>> sohMonitorValueAndStatuses) {
      return setSohMonitorValueAndStatuses(ImmutableSet.copyOf(sohMonitorValueAndStatuses));
    }

    abstract ImmutableSet.Builder<SohMonitorValueAndStatus<?>> sohMonitorValueAndStatusesBuilder();

    public Builder sohMonitorValueAndStatus(SohMonitorValueAndStatus<?> sohMonitorValueAndStatus) {
      sohMonitorValueAndStatusesBuilder().add(sohMonitorValueAndStatus);
      return this;
    }

    abstract Builder setChannelSohs(ImmutableSet<ChannelSoh> channelSohs);

    public Builder setChannelSohs(Collection<ChannelSoh> channelSohs) {
      return setChannelSohs(ImmutableSet.copyOf(channelSohs));
    }

    abstract ImmutableSet.Builder<ChannelSoh> channelSohsBuilder();

    public Builder addChannelSoh(ChannelSoh channelSoh) {
      channelSohsBuilder().add(channelSoh);
      return this;
    }

    public abstract StationSoh autoBuild();

    public StationSoh build() {
      StationSoh stationSoh = autoBuild();

      validate(
        stationSoh.getSohMonitorValueAndStatuses(),
        stationSoh.getChannelSohs()
      );

      return stationSoh;
    }

    private static void validate(
      Set<SohMonitorValueAndStatus<?>> sohMonitorValueAndStatuses,
      Set<ChannelSoh> channelSohs) {

      Validate.notEmpty(sohMonitorValueAndStatuses,
        "StationSoh requires at least one SohMonitorValueAndStatus");

      Validate.notEmpty(channelSohs, "StationSoh requires at least one channel SOH");
      
      var duplicateChannels = channelSohs.stream().collect(Collectors.groupingBy(ChannelSoh::getChannelName, Collectors.counting()));
      Validate.isTrue(duplicateChannels.values().stream().noneMatch(count -> count > 1), "The Set of ChannelSohs must only have one unique Channel Name per Set");
    }
    

    /**
     * Verifies that the set of channels inside a set of ChannelSoh objects are a subset of the channels belonging to a
     * station
     *
     * @param station station containing superset of channels
     * @param channelSohSet ChannelSoh objects
     */
    //TODO - tpf - this isn't used anywhere, should we be verifying this?
    static boolean channelSohChannelsSubsetStationChannels(
      Station station,
      Set<ChannelSoh> channelSohSet
    ) {

      return station.getChannels()
        .stream()
        .map(Channel::getName)
        .collect(Collectors.toSet())
        .containsAll(
          channelSohSet.stream().map(ChannelSoh::getChannelName).collect(Collectors.toSet())
        );
    }
  }
}
