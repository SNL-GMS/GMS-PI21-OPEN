package gms.shared.frameworks.osd.coi.stationreference;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

@AutoValue
@JsonSerialize(as = ReferenceSiteMembership.class)
@JsonDeserialize(builder = AutoValue_ReferenceSiteMembership.Builder.class)
public abstract class ReferenceSiteMembership {

  public abstract UUID getId();

  public abstract String getComment();

  public abstract Instant getActualChangeTime();

  public abstract Instant getSystemChangeTime();

  public abstract UUID getSiteId();

  public abstract String getChannelName();

  public abstract StatusType getStatus();

  public abstract Builder toBuilder();

  static Builder builder() {
    return new AutoValue_ReferenceSiteMembership.Builder();
  }

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public interface Builder {

    Builder setId(UUID id);

    Builder setComment(String comment);

    Builder setActualChangeTime(Instant actualChangeTime);

    Builder setSystemChangeTime(Instant systemChangeTime);

    Builder setSiteId(UUID siteId);

    Builder setChannelName(String channelName);

    Builder setStatus(StatusType status);

    ReferenceSiteMembership build();

  }

  /**
   * @param systemChangeTime The date and time time the information was entered into the system
   * @return a new ReferenceSiteMembership object
   */
  public static ReferenceSiteMembership create(String comment, Instant actualChangeTime,
    Instant systemChangeTime, UUID siteId, String channelName,
    StatusType status) {

    UUID id = UUID.nameUUIDFromBytes((siteId.toString() + channelName + status + actualChangeTime)
      .getBytes(StandardCharsets.UTF_16LE));
    return ReferenceSiteMembership.builder()
      .setId(id)
      .setComment(comment)
      .setActualChangeTime(actualChangeTime)
      .setSystemChangeTime(systemChangeTime)
      .setSiteId(siteId)
      .setChannelName(channelName)
      .setStatus(status)
      .build();
  }

  public static ReferenceSiteMembership from(UUID id, String comment,
    Instant actualChangeTime, Instant systemChangeTime,
    UUID siteId, String channelName,
    StatusType status) {
    return ReferenceSiteMembership.builder()
      .setId(id)
      .setComment(comment)
      .setActualChangeTime(actualChangeTime)
      .setSystemChangeTime(systemChangeTime)
      .setSiteId(siteId)
      .setChannelName(channelName)
      .setStatus(status)
      .build();
  }
}
