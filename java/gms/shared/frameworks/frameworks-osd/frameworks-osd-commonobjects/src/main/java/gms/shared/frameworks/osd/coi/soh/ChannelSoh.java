package gms.shared.frameworks.osd.coi.soh;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;
import com.google.auto.value.extension.memoized.Memoized;
import com.google.common.collect.ImmutableSet;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.lang3.Validate;

/**
 * Represents state-of-health of a single channel.
 */
@AutoValue
@JsonSerialize(as = ChannelSoh.class)
@JsonDeserialize(builder = AutoValue_ChannelSoh.Builder.class)
public abstract class ChannelSoh {

  /**
   * @return the channel with this state-of-health
   */
  public abstract String getChannelName();

  /**
   * Worst of the statuses that were monitored
   */
  public abstract SohStatus getSohStatusRollup();

  /**
   * @return Types, values, and status of the monitored SOH of the channel.
   */
  public abstract ImmutableSet<SohMonitorValueAndStatus<?>> getAllSohMonitorValueAndStatuses();

  /**
   * @return Map of SohMonitorType to SohMonitorValueAndStatus.
   */
  @Memoized
  public Map<SohMonitorType, SohMonitorValueAndStatus<?>> getSohMonitorValueAndStatusMap() {

    return getAllSohMonitorValueAndStatuses().stream()
      .collect(Collectors.toMap(
        SohMonitorValueAndStatus::getMonitorType,
        Function.identity()
      ));
  }

  /**
   * Creates a Channel SOH object
   *
   * @param channelName channel that has this state-of-health
   * @param sohStatusRollup Rolled-up status of the channel (see getSohStatusRollup)
   * @param allSohMonitorValueAndStatuses All of the status that were monitored for this channel.
   * @return new ChannelSoh object
   */
  @JsonCreator
  public static ChannelSoh from(
    @JsonProperty("channelName") String channelName,
    @JsonProperty("sohStatusRollup") SohStatus sohStatusRollup,
    @JsonProperty("allSohMonitorValueAndStatuses") Set<SohMonitorValueAndStatus<?>> allSohMonitorValueAndStatuses
  ) {
    return ChannelSoh.builder()
      .setChannelName(channelName)
      .setSohStatusRollup(sohStatusRollup)
      .setAllSohMonitorValueAndStatuses(allSohMonitorValueAndStatuses)
      .build();
  }

  public abstract ChannelSoh.Builder toBuilder();

  public static ChannelSoh.Builder builder() {
    return new AutoValue_ChannelSoh.Builder();
  }

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public abstract static class Builder {

    public abstract Builder setChannelName(String channelName);

    public abstract Builder setSohStatusRollup(SohStatus sohStatusRollup);

    abstract Builder setAllSohMonitorValueAndStatuses(
      ImmutableSet<SohMonitorValueAndStatus<?>> allSohMonitorValueAndStatuses);

    public Builder setAllSohMonitorValueAndStatuses(
      Collection<SohMonitorValueAndStatus<?>> sohMonitorValueAndStatuses) {
      return setAllSohMonitorValueAndStatuses(ImmutableSet.copyOf(sohMonitorValueAndStatuses));
    }

    abstract ImmutableSet.Builder<SohMonitorValueAndStatus<?>> allSohMonitorValueAndStatusesBuilder();

    public Builder allSohMonitorValueAndStatuses(SohMonitorValueAndStatus<?> sohMonitorValueAndStatus) {
      allSohMonitorValueAndStatusesBuilder().add(sohMonitorValueAndStatus);
      return this;
    }

    public abstract ChannelSoh autoBuild();

    public ChannelSoh build() {
      var channelSoh = autoBuild();

      validate(channelSoh.getAllSohMonitorValueAndStatuses());

      return channelSoh;
    }

    private static void validate(
      Set<SohMonitorValueAndStatus<?>> sohMonitorValueAndStatuses) {

      var duplicateSmvs = sohMonitorValueAndStatuses.stream().collect(Collectors.groupingBy(SohMonitorValueAndStatus::getMonitorType, Collectors.counting()));
      Validate.isTrue(duplicateSmvs.values().stream().noneMatch(count -> count > 1), "The Set of sohMonitorValueAndStatuses must only have one unique MonitorType per Set");
    }
  }
}
