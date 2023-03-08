package gms.shared.frameworks.osd.api.stationreference.util;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.UUID;

@AutoValue
public abstract class ReferenceSiteMembershipRequest {
  public abstract UUID getSiteId();

  public abstract String getChannelName();

  @JsonCreator
  public static ReferenceSiteMembershipRequest create(
    @JsonProperty("siteId") UUID siteId,
    @JsonProperty("channelName") String channelName) {
    return new AutoValue_ReferenceSiteMembershipRequest(siteId, channelName);
  }
}
