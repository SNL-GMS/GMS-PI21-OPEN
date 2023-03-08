package gms.shared.stationdefinition.api.channel.util;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import gms.shared.stationdefinition.api.util.Request;
import gms.shared.stationdefinition.api.util.TimeRangeRequest;

import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

@AutoValue
@JsonSerialize(as = ResponseTimeRangeRequest.class)
@JsonDeserialize(builder = AutoValue_ResponseTimeRangeRequest.Builder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class ResponseTimeRangeRequest implements Request {

  @Override
  @JsonIgnore
  public ImmutableList<String> getNames() {
    return ImmutableList.copyOf(getResponseIds().stream().map(UUID::toString).collect(Collectors.toList()));
  }

  public abstract ImmutableList<UUID> getResponseIds();

  @JsonUnwrapped
  public abstract TimeRangeRequest getTimeRange();

  public static ResponseTimeRangeRequest.Builder builder() {
    return new AutoValue_ResponseTimeRangeRequest.Builder();
  }

  public abstract ResponseTimeRangeRequest.Builder toBuilder();

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public interface Builder {

    ResponseTimeRangeRequest.Builder setResponseIds(ImmutableList<UUID> responseIds);

    default ResponseTimeRangeRequest.Builder setResponseIds(Collection<UUID> responseIds) {
      return setResponseIds(ImmutableList.copyOf(responseIds));
    }

    ImmutableList.Builder<UUID> responseIdsBuilder();

    default ResponseTimeRangeRequest.Builder addResponseId(UUID responseId) {
      responseIdsBuilder().add(responseId);
      return this;
    }

    @JsonUnwrapped
    ResponseTimeRangeRequest.Builder setTimeRange(TimeRangeRequest timeRange);

    ResponseTimeRangeRequest build();
  }
}
