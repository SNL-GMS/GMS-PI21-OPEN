package gms.shared.frameworks.osd.coi.stationreference;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

@AutoValue
@JsonSerialize(as = ReferenceDigitizerMembership.class)
@JsonDeserialize(builder = AutoValue_ReferenceDigitizerMembership.Builder.class)
public abstract class ReferenceDigitizerMembership {

  public abstract UUID getId();

  public abstract String getComment();

  public abstract Instant getActualChangeTime();

  public abstract Instant getSystemChangeTime();

  public abstract UUID getDigitizerId();

  public abstract UUID getChannelId();

  public abstract StatusType getStatus();

  public abstract Builder toBuilder();

  public static Builder builder() {
    return new AutoValue_ReferenceDigitizerMembership.Builder();
  }

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public interface Builder {

    Builder setId(UUID id);

    Builder setComment(String comment);

    Builder setActualChangeTime(Instant actualChangeTime);

    Builder setSystemChangeTime(Instant systemChangeTime);

    Builder setDigitizerId(UUID digitizerId);

    Builder setChannelId(UUID channelId);

    Builder setStatus(StatusType statusType);

    ReferenceDigitizerMembership build();
  }

  /**
   * Create a new ReferenceDigitizerMembership object
   *
   * @param comment Comments
   * @param actualChangeTime The date and time time the information was originally generated
   * @param systemChangeTime The date and time time the information was entered into the system
   * @param digitizerId The member digitizer's id
   * @param channelId The member channel's id
   * @return A new ReferenceDigitizerMembership object
   */
  public static ReferenceDigitizerMembership create(String comment, Instant actualChangeTime,
    Instant systemChangeTime, UUID digitizerId, UUID channelId,
    StatusType status) {
    UUID id = UUID.nameUUIDFromBytes(
      (channelId.toString() + digitizerId + status + actualChangeTime)
        .getBytes(StandardCharsets.UTF_16LE));
    return ReferenceDigitizerMembership.builder()
      .setId(id)
      .setComment(comment)
      .setActualChangeTime(actualChangeTime)
      .setSystemChangeTime(systemChangeTime)
      .setDigitizerId(digitizerId)
      .setChannelId(channelId)
      .setStatus(status)
      .build();
  }

  /**
   * Create a ReferenceDigitizerMembership object from existing data
   *
   * @param id The object's id
   * @param comment Comments
   * @param actualChangeTime The date and time time the information was originally generated
   * @param systemChangeTime The date and time time the information was entered into the system
   * @param digitizerId The member digitizer's id
   * @param channelId The member channel's id
   * @param status
   * @return A new ReferenceDigitizerMembership object from existing data
   * @throws NullPointerException
   */
  public static ReferenceDigitizerMembership from(UUID id, String comment,
    Instant actualChangeTime, Instant systemChangeTime,
    UUID digitizerId, UUID channelId,
    StatusType status) {
    return ReferenceDigitizerMembership.builder()
      .setId(id)
      .setComment(comment)
      .setActualChangeTime(actualChangeTime)
      .setSystemChangeTime(systemChangeTime)
      .setDigitizerId(digitizerId)
      .setChannelId(channelId)
      .setStatus(status)
      .build();
  }

}
