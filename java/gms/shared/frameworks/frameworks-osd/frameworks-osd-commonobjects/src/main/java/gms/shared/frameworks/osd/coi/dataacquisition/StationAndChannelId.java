package gms.shared.frameworks.osd.coi.dataacquisition;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class StationAndChannelId {

  public abstract String getStationId();

  public abstract String getChannelId();

  @JsonCreator
  public static StationAndChannelId from(
    @JsonProperty("stationId") String stationId,
    @JsonProperty("channelId") String channelId) {
    return new AutoValue_StationAndChannelId(stationId, channelId);
  }

}

