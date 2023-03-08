package gms.shared.stationdefinition.api.station.util;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import gms.shared.stationdefinition.api.util.Request;

import java.time.Instant;
import java.util.Collection;

@AutoValue
@JsonSerialize(as = StationGroupsTimeRequest.class)
@JsonDeserialize(builder = AutoValue_StationGroupsTimeRequest.Builder.class)
public abstract class StationGroupsTimeRequest implements Request {

  @Override
  @JsonIgnore
  public ImmutableList<String> getNames() {
    return getStationGroupNames();
  }

  public abstract ImmutableList<String> getStationGroupNames();

  public abstract Instant getEffectiveTime();

  public static StationGroupsTimeRequest.Builder builder() {
    return new AutoValue_StationGroupsTimeRequest.Builder();
  }

  public abstract StationGroupsTimeRequest.Builder toBuilder();

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public interface Builder {

    StationGroupsTimeRequest.Builder setStationGroupNames(ImmutableList<String> stationGroupNames);

    default StationGroupsTimeRequest.Builder setStationGroupNames(Collection<String> stationGroupNames) {
      return setStationGroupNames(ImmutableList.copyOf(stationGroupNames));
    }

    ImmutableList.Builder<String> stationGroupNamesBuilder();

    default StationGroupsTimeRequest.Builder addStationGroupName(String stationGroupName) {
      stationGroupNamesBuilder().add(stationGroupName);
      return this;
    }

    StationGroupsTimeRequest.Builder setEffectiveTime(Instant effectiveTime);

    StationGroupsTimeRequest build();
  }
}
