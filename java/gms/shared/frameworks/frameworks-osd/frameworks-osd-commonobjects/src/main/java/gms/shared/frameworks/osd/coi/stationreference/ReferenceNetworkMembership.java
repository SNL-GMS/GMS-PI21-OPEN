package gms.shared.frameworks.osd.coi.stationreference;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

@AutoValue
@JsonSerialize(as = ReferenceNetworkMembership.class)
@JsonDeserialize(builder = AutoValue_ReferenceNetworkMembership.Builder.class)
public abstract class ReferenceNetworkMembership {

  public abstract UUID getId();

  public abstract String getComment();

  public abstract Instant getActualChangeTime();

  public abstract Instant getSystemChangeTime();

  public abstract UUID getNetworkId();

  public abstract UUID getStationId();

  public abstract StatusType getStatus();

  public abstract Builder toBuilder();

  public static Builder builder() {
    return new AutoValue_ReferenceNetworkMembership.Builder();
  }

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public interface Builder {

    Builder setId(UUID id);

    Builder setComment(String comment);

    Builder setActualChangeTime(Instant actualChangeTime);

    Builder setSystemChangeTime(Instant systemChangeTime);

    Builder setNetworkId(UUID networkId);

    Builder setStationId(UUID stationId);

    Builder setStatus(StatusType statusType);

    ReferenceNetworkMembership build();

  }

  /**
   * @return ReferenceNetworkMembership object
   */
  public static ReferenceNetworkMembership create(String comment, Instant actualChangeTime,
    Instant systemChangeTime, UUID networkId, UUID stationId, StatusType status) {
    var uuid = UUID.nameUUIDFromBytes((networkId.toString() + stationId + status + actualChangeTime)
      .getBytes(StandardCharsets.UTF_16LE));
    return ReferenceNetworkMembership.builder()
      .setId(uuid)
      .setComment(comment)
      .setActualChangeTime(actualChangeTime)
      .setSystemChangeTime(systemChangeTime)
      .setNetworkId(networkId)
      .setStationId(stationId)
      .setStatus(status)
      .build();
  }

  /**
   * @return a new ReferenceNetworkMembership object
   */
  public static ReferenceNetworkMembership from(UUID id, String comment,
    Instant actualChangeTime, Instant systemChangeTime,
    UUID networkId, UUID stationId, StatusType status) {
    return ReferenceNetworkMembership.builder()
      .setId(id)
      .setComment(comment)
      .setActualChangeTime(actualChangeTime)
      .setSystemChangeTime(systemChangeTime)
      .setNetworkId(networkId)
      .setStationId(stationId)
      .setStatus(status)
      .build();
  }

}
