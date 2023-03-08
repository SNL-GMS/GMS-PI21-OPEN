package gms.shared.frameworks.osd.api.stationreference.util;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.UUID;

@AutoValue
public abstract class ReferenceStationMembershipRequest {
  public abstract UUID getStationId();

  public abstract UUID getSiteId();

  @JsonCreator
  public static ReferenceStationMembershipRequest create(
    @JsonProperty("stationId") UUID stationId,
    @JsonProperty("siteId") UUID siteId) {
    return new AutoValue_ReferenceStationMembershipRequest(stationId, siteId);
  }
}
