package gms.shared.frameworks.osd.coi.stationreference;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;

import java.time.Instant;
import java.util.UUID;

@AutoValue
@JsonSerialize(as = ReferenceAlias.class)
@JsonDeserialize(builder = AutoValue_ReferenceAlias.Builder.class)
public abstract class ReferenceAlias {

  public abstract UUID getId();

  public abstract String getName();

  public abstract StatusType getStatus();

  public abstract String getComment();

  public abstract Instant getActualChangeTime();

  public abstract Instant getSystemChangeTime();

  public abstract Builder toBuilder();

  public static Builder builder() {
    return new AutoValue_ReferenceAlias.Builder();
  }

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public interface Builder {

    Builder setId(UUID id);

    Builder setName(String name);

    Builder setStatus(StatusType statusType);

    Builder setComment(String comment);

    Builder setActualChangeTime(Instant actualChangeTime);

    Builder setSystemChangeTime(Instant systemChangeTime);

    ReferenceAlias build();
  }

  /**
   * Create a new ReferenceSiteAlias.
   *
   * @param name The alias name.
   * @param status Status value, if null defaults to ACTIVE.
   * @param comment Comment about the alias.
   * @param actualChangeTime actual change time
   * @param systemChangeTime system change time
   * @return The new ReferenceSiteAlias object.
   */
  public static ReferenceAlias create(String name, StatusType status,
    String comment, Instant actualChangeTime, Instant systemChangeTime) {
    return ReferenceAlias.builder()
      .setId(UUID.randomUUID())
      .setName(name)
      .setStatus(status)
      .setComment(comment)
      .setActualChangeTime(actualChangeTime)
      .setSystemChangeTime(systemChangeTime)
      .build();
  }

  /**
   * Recreate a ReferenceSiteAlias from existing data.
   *
   * @param id The assigned UUID.
   * @param name The alias name.
   * @param status Status value.
   * @param comment Comment about the alias.
   * @param actualChangeTime The actual change time.
   * @param systemChangeTime The system change time.
   * @return The ReferenceSiteAlias object.
   */
  public static ReferenceAlias from(UUID id, String name, StatusType status,
    String comment, Instant actualChangeTime, Instant systemChangeTime) {
    return ReferenceAlias.builder()
      .setId(id)
      .setName(name)
      .setStatus(status)
      .setComment(comment)
      .setActualChangeTime(actualChangeTime)
      .setSystemChangeTime(systemChangeTime)
      .build();
  }

}
