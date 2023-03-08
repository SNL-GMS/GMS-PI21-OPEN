package gms.shared.frameworks.osd.coi.signaldetection;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;
import gms.shared.frameworks.osd.coi.channel.ChannelSegment;

import java.io.Serializable;
import java.time.Instant;

@AutoValue
@JsonSerialize(as = MeasuredChannelSegmentDescriptor.class)
@JsonDeserialize(builder = AutoValue_MeasuredChannelSegmentDescriptor.Builder.class)
public abstract class MeasuredChannelSegmentDescriptor implements Serializable {

  public abstract String getChannelName();

  public abstract Instant getMeasuredChannelSegmentStartTime();

  public abstract Instant getMeasuredChannelSegmentEndTime();

  public abstract Instant getMeasuredChannelSegmentCreationTime();

  public static Builder builder() {
    return new AutoValue_MeasuredChannelSegmentDescriptor.Builder();
  }

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public interface Builder {

    Builder setChannelName(String channelName);

    Builder setMeasuredChannelSegmentStartTime(Instant measuredChannelSegmentStartTime);

    Builder setMeasuredChannelSegmentEndTime(Instant measuredChannelSegmentEndTime);

    Builder setMeasuredChannelSegmentCreationTime(Instant measuredChannelSegmentCreationTime);

    MeasuredChannelSegmentDescriptor build();
  }

  public static MeasuredChannelSegmentDescriptor from(ChannelSegment channelSegment) {
    return MeasuredChannelSegmentDescriptor.builder()
      .setChannelName(channelSegment.getName())
      .setMeasuredChannelSegmentStartTime(channelSegment.getStartTime())
      .setMeasuredChannelSegmentEndTime(channelSegment.getEndTime())
      .setMeasuredChannelSegmentCreationTime(Instant.now())
      .build();
  }
}
