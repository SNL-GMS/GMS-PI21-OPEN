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
@JsonSerialize(as = StationsTimeRequest.class)
@JsonDeserialize(builder = AutoValue_StationsTimeRequest.Builder.class)
public abstract class StationsTimeRequest implements Request {

  @Override
  @JsonIgnore
  public ImmutableList<String> getNames() {
    return getStationNames();
  }

  public abstract ImmutableList<String> getStationNames();

  public abstract Instant getEffectiveTime();

  public static StationsTimeRequest.Builder builder() {
    return new AutoValue_StationsTimeRequest.Builder();
  }

  public abstract StationsTimeRequest.Builder toBuilder();

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public interface Builder {

    StationsTimeRequest.Builder setStationNames(ImmutableList<String> stationNames);

    default StationsTimeRequest.Builder setStationNames(Collection<String> stationNames) {
      return setStationNames(ImmutableList.copyOf(stationNames));
    }

    ImmutableList.Builder<String> stationNamesBuilder();

    default StationsTimeRequest.Builder addStationName(String stationName) {
      stationNamesBuilder().add(stationName);
      return this;
    }

    StationsTimeRequest.Builder setEffectiveTime(Instant effectiveTime);

    StationsTimeRequest build();
  }
}
