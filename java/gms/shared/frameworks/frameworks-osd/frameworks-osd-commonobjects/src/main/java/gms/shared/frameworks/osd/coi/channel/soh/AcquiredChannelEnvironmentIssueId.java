package gms.shared.frameworks.osd.coi.channel.soh;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssue.AcquiredChannelEnvironmentIssueType;

import java.time.Instant;

@AutoValue
public abstract class AcquiredChannelEnvironmentIssueId {

  public abstract String getChannelName();

  public abstract AcquiredChannelEnvironmentIssueType getType();

  public abstract Instant getStartTime();

  @JsonCreator
  public static AcquiredChannelEnvironmentIssueId create(
    @JsonProperty("channelName") String channelName,
    @JsonProperty("type") AcquiredChannelEnvironmentIssueType type,
    @JsonProperty("startTime") Instant startTime) {
    return new AutoValue_AcquiredChannelEnvironmentIssueId(channelName, type, startTime);
  }

}
