package gms.shared.frameworks.osd.api.stationreference.util;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.UUID;

@AutoValue
public abstract class NetworkMembershipRequest {
  public abstract UUID getNetworkId();

  public abstract UUID getStationId();

  @JsonCreator
  public static NetworkMembershipRequest from(
    @JsonProperty("networkId") UUID networkId,
    @JsonProperty("stationId") UUID stationId) {
    return new AutoValue_NetworkMembershipRequest(networkId, stationId);
  }
}
