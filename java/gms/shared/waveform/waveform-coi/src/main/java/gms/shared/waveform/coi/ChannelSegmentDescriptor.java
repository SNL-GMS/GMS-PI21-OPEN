package gms.shared.waveform.coi;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import gms.shared.stationdefinition.coi.channel.Channel;

import java.time.Instant;
import java.util.Objects;

@AutoValue
public abstract class ChannelSegmentDescriptor {
  public abstract Channel getChannel();

  public abstract Instant getStartTime();

  public abstract Instant getEndTime();

  public abstract Instant getCreationTime();

  @JsonCreator
  public static ChannelSegmentDescriptor from(
    @JsonProperty("channel") Channel channel,
    @JsonProperty("startTime") Instant startTime,
    @JsonProperty("endTime") Instant endTime,
    @JsonProperty("creationTime") Instant creationTime) {
    Objects.requireNonNull(channel);
    Objects.requireNonNull(startTime);
    Objects.requireNonNull(endTime);
    Objects.requireNonNull(creationTime);
    return new AutoValue_ChannelSegmentDescriptor(
      channel, startTime, endTime, creationTime);
  }
}
