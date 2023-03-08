package gms.shared.frameworks.osd.api.channel.util;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import com.google.common.base.Preconditions;

import java.util.Collection;
import java.util.UUID;

@AutoValue
public abstract class ChannelSegmentsIdRequest {

  public abstract Collection<UUID> getChannelSegmentIds();

  public abstract boolean isWithTimeseries();

  @JsonCreator
  public static ChannelSegmentsIdRequest create(
    @JsonProperty("channelSegmentIds") Collection<UUID> channelSegmentIds,
    @JsonProperty("withTimeseries") boolean withTimeseries) {
    Preconditions.checkState(!channelSegmentIds.isEmpty());
    return new AutoValue_ChannelSegmentsIdRequest(channelSegmentIds, withTimeseries);
  }

}
