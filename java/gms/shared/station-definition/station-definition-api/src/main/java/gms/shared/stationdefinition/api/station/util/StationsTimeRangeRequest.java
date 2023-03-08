package gms.shared.stationdefinition.api.station.util;

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

@AutoValue
@JsonSerialize(as = StationsTimeRangeRequest.class)
@JsonDeserialize(builder = AutoValue_StationsTimeRangeRequest.Builder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class StationsTimeRangeRequest implements Request {

  @Override
  @JsonIgnore
  public ImmutableList<String> getNames() {
    return getStationNames();
  }

  public abstract ImmutableList<String> getStationNames();

  @JsonUnwrapped
  public abstract TimeRangeRequest getTimeRange();

  public static StationsTimeRangeRequest.Builder builder() {
    return new AutoValue_StationsTimeRangeRequest.Builder();
  }

  public abstract StationsTimeRangeRequest.Builder toBuilder();

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public interface Builder {

    StationsTimeRangeRequest.Builder setStationNames(ImmutableList<String> stationNames);

    default StationsTimeRangeRequest.Builder setStationNames(Collection<String> stationNames) {
      return setStationNames(ImmutableList.copyOf(stationNames));
    }

    ImmutableList.Builder<String> stationNamesBuilder();

    default StationsTimeRangeRequest.Builder addStationName(String stationName) {
      stationNamesBuilder().add(stationName);
      return this;
    }

    @JsonUnwrapped
    StationsTimeRangeRequest.Builder setTimeRange(TimeRangeRequest timeRange);

    StationsTimeRangeRequest build();
  }
}
