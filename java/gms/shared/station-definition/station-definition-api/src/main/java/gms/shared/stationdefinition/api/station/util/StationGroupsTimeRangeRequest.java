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
@JsonSerialize(as = StationGroupsTimeRangeRequest.class)
@JsonDeserialize(builder = AutoValue_StationGroupsTimeRangeRequest.Builder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class StationGroupsTimeRangeRequest implements Request {

  @Override
  @JsonIgnore
  public ImmutableList<String> getNames() {
    return getStationGroupNames();
  }

  public abstract ImmutableList<String> getStationGroupNames();

  @JsonUnwrapped
  public abstract TimeRangeRequest getTimeRange();

  public static StationGroupsTimeRangeRequest.Builder builder() {
    return new AutoValue_StationGroupsTimeRangeRequest.Builder();
  }

  public abstract StationGroupsTimeRangeRequest.Builder toBuilder();

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public interface Builder {

    StationGroupsTimeRangeRequest.Builder setStationGroupNames(ImmutableList<String> stationGroupNames);

    default StationGroupsTimeRangeRequest.Builder setStationGroupNames(Collection<String> stationGroupNames) {
      return setStationGroupNames(ImmutableList.copyOf(stationGroupNames));
    }

    ImmutableList.Builder<String> stationGroupNamesBuilder();

    default StationGroupsTimeRangeRequest.Builder addStationGroupName(String stationGroupName) {
      stationGroupNamesBuilder().add(stationGroupName);
      return this;
    }

    @JsonUnwrapped
    StationGroupsTimeRangeRequest.Builder setTimeRange(TimeRangeRequest timeRange);

    StationGroupsTimeRangeRequest build();
  }
}
