package gms.shared.frameworks.osd.coi.stationreference;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

@AutoValue
@JsonSerialize(as = ReferenceStationMembership.class)
@JsonDeserialize(builder = AutoValue_ReferenceStationMembership.Builder.class)
public abstract class ReferenceStationMembership {

  public abstract UUID getId();

  public abstract String getComment();

  public abstract Instant getActualChangeTime();

  public abstract Instant getSystemChangeTime();

  public abstract UUID getStationId();

  public abstract UUID getSiteId();

  public abstract StatusType getStatus();

  public abstract Builder toBuilder();

  public static Builder builder() {
    return new AutoValue_ReferenceStationMembership.Builder();
  }

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public interface Builder {

    Builder setId(UUID id);

    Builder setComment(String comment);

    Builder setActualChangeTime(Instant actualChangeTime);

    Builder setSystemChangeTime(Instant systemChangeTime);

    Builder setStationId(UUID stationId);

    Builder setSiteId(UUID siteId);

    Builder setStatus(StatusType statusType);

    ReferenceStationMembership build();
  }

  /**
   * @return a new ReferenceStationMembership object
   */
  public static ReferenceStationMembership create(String comment, Instant actualChangeTime,
    Instant systemChangeTime, UUID stationId, UUID siteId,
    StatusType status) {
    UUID id = UUID.nameUUIDFromBytes((stationId.toString() + siteId
      + status + actualChangeTime).getBytes(StandardCharsets.UTF_16LE));

    return ReferenceStationMembership.builder()
      .setId(id)
      .setComment(comment)
      .setActualChangeTime(actualChangeTime)
      .setSystemChangeTime(systemChangeTime)
      .setStationId(stationId)
      .setSiteId(siteId)
      .setStatus(status)
      .build();
  }

  /**
   * @return a new ReferenceStationMembership object from existing data
   */
  public static ReferenceStationMembership from(UUID id, String comment,
    Instant actualChangeTime, Instant systemChangeTime,
    UUID stationId, UUID siteId,
    StatusType status) {
    return ReferenceStationMembership.builder()
      .setId(id)
      .setComment(comment)
      .setActualChangeTime(actualChangeTime)
      .setSystemChangeTime(systemChangeTime)
      .setStationId(stationId)
      .setSiteId(siteId)
      .setStatus(status)
      .build();
  }
}
