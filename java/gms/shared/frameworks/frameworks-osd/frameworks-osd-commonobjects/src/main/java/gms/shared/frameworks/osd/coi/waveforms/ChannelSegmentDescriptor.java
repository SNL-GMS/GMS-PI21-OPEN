package gms.shared.frameworks.osd.coi.waveforms;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import gms.shared.frameworks.osd.coi.channel.ChannelSegment;

import java.time.Instant;

/**
 * An Object used to describe a segment of Channel data. Used in place of return the actual segment
 * data itself.
 */
@AutoValue
public abstract class ChannelSegmentDescriptor {

  public abstract String getChannelName();

  public abstract Instant getStartTime();

  public abstract Instant getEndTime();

  @JsonCreator
  public static ChannelSegmentDescriptor from(
    @JsonProperty("channelName") String channelName,
    @JsonProperty("startTime") Instant startTime,
    @JsonProperty("endTime") Instant endTime) {
    return new AutoValue_ChannelSegmentDescriptor(channelName, startTime, endTime);
  }

  public static ChannelSegmentDescriptor from(ChannelSegment<? extends Timeseries> channelSegment) {
    return new AutoValue_ChannelSegmentDescriptor(channelSegment.getChannel().getName(),
      channelSegment.getStartTime(), channelSegment.getEndTime());
  }
}
