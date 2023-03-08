package gms.shared.stationdefinition.api.station.util;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import gms.shared.stationdefinition.api.util.Request;
import gms.shared.stationdefinition.coi.station.Station;

import java.time.Instant;

@AutoValue
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class StationChangeTimesRequest implements Request {
  @Override
  @JsonIgnore
  public ImmutableList<String> getNames() {
    return ImmutableList.of(getStation().getName());
  }

  public abstract Station getStation();

  public abstract Instant getStartTime();

  public abstract Instant getEndTime();

  @JsonCreator
  public static StationChangeTimesRequest create(
    @JsonProperty("station") Station station,
    @JsonProperty("startTime") Instant startTime,
    @JsonProperty("endTime") Instant endTime) {

    Preconditions.checkState(startTime.isBefore(endTime), "Start time must be before end time");
    return new AutoValue_StationChangeTimesRequest(station, startTime, endTime);
  }

}
