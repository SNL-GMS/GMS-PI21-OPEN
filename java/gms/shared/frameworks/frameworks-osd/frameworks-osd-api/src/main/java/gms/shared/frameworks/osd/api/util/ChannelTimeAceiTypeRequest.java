package gms.shared.frameworks.osd.api.util;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableMap;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssue.AcquiredChannelEnvironmentIssueType;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@AutoValue
@JsonSerialize(as = ChannelTimeAceiTypeRequest.class)
@JsonDeserialize(builder = AutoValue_ChannelTimeAceiTypeRequest.Builder.class)
public abstract class ChannelTimeAceiTypeRequest {

  public abstract AcquiredChannelEnvironmentIssueType getType();

  public abstract ImmutableMap<String, Set<Instant>> getChannelNamesToTime();

  public static ChannelTimeAceiTypeRequest create(
    AcquiredChannelEnvironmentIssueType type,
    Map<String, Set<Instant>> channelNamesToTime
  ) {

    return ChannelTimeAceiTypeRequest.builder()
      .setType(type)
      .setChannelNamesToTime(channelNamesToTime).build();
  }

  public static ChannelTimeAceiTypeRequest.Builder builder() {
    return new AutoValue_ChannelTimeAceiTypeRequest.Builder()
      .setChannelNamesToTime(new HashMap<>());
  }

  public abstract ChannelTimeAceiTypeRequest.Builder toBuilder();

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public abstract static class Builder {

    public abstract Builder setType(AcquiredChannelEnvironmentIssueType type);

    abstract ChannelTimeAceiTypeRequest.Builder setChannelNamesToTime(
      ImmutableMap<String, Set<Instant>> channelNamesToTime);

    public ChannelTimeAceiTypeRequest.Builder setChannelNamesToTime(
      Map<String, Set<Instant>> channelNamesToTime) {
      return setChannelNamesToTime(ImmutableMap.copyOf(channelNamesToTime));
    }

    abstract ImmutableMap<String, Set<Instant>> getChannelNamesToTime();

    public Builder addTime(String channelName, Instant time) {
      var channelNamesToTime = new HashMap<>(getChannelNamesToTime());
      var times = channelNamesToTime.computeIfAbsent(channelName, key -> new HashSet<>());
      if (times.add(time)) {
        setChannelNamesToTime(channelNamesToTime);
      }
      return this;
    }

    abstract ChannelTimeAceiTypeRequest autoBuild();

    public ChannelTimeAceiTypeRequest build() {
      return autoBuild();
    }
  }
}
