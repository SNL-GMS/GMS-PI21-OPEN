package gms.shared.frameworks.osd.api.util;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

@AutoValue
@JsonSerialize(as = StationsTimeRangeRequest.class)
@JsonDeserialize(builder = AutoValue_StationsTimeRangeRequest.Builder.class)
public abstract class StationsTimeRangeRequest {

  public abstract ImmutableList<String> getStationNames();

  @JsonUnwrapped
  public abstract TimeRangeRequest getTimeRange();

  public static StationsTimeRangeRequest create(
    List<String> stationNames,
    Instant startTime,
    Instant endTime) {
    return builder().setStationNames(stationNames)
      .setTimeRange(TimeRangeRequest.create(startTime, endTime))
      .build();
  }

  public static Builder builder() {
    return new AutoValue_StationsTimeRangeRequest.Builder();
  }

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public abstract static class Builder {

    abstract Builder setStationNames(ImmutableList<String> stationNames);

    public Builder setStationNames(Collection<String> stationNames) {
      return setStationNames(ImmutableList.copyOf(stationNames));
    }

    abstract ImmutableList.Builder<String> stationNamesBuilder();

    public Builder addStationName(String stationName) {
      stationNamesBuilder().add(stationName);
      return this;
    }

    @JsonUnwrapped
    public abstract Builder setTimeRange(TimeRangeRequest timeRange);

    public abstract StationsTimeRangeRequest build();
  }
}
