package gms.shared.frameworks.osd.api.util;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;
import gms.shared.frameworks.osd.coi.waveforms.ChannelSegmentDescriptor;

import java.time.Instant;

@AutoValue
@JsonSerialize(as = ChannelTimeRangeRequest.class)
@JsonDeserialize(builder = AutoValue_ChannelTimeRangeRequest.Builder.class)
public abstract class ChannelTimeRangeRequest {

  public abstract String getChannelName();

  @JsonUnwrapped
  public abstract TimeRangeRequest getTimeRange();

  public static ChannelTimeRangeRequest create(
    String channelName,
    Instant startTime,
    Instant endTime) {
    return builder().setChannelName(channelName)
      .setTimeRange(TimeRangeRequest.create(startTime, endTime))
      .build();
  }

  public static ChannelTimeRangeRequest from(ChannelSegmentDescriptor descriptor) {
    return builder().setChannelName(descriptor.getChannelName())
      .setTimeRange(TimeRangeRequest.create(descriptor.getStartTime(), descriptor.getEndTime()))
      .build();
  }

  public static Builder builder() {
    return new AutoValue_ChannelTimeRangeRequest.Builder();
  }

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public interface Builder {

    Builder setChannelName(String channelName);

    @JsonUnwrapped
    Builder setTimeRange(TimeRangeRequest timeRange);

    ChannelTimeRangeRequest build();
  }
}
