package gms.shared.frameworks.osd.coi.stationreference;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;
import gms.shared.frameworks.osd.coi.provenance.InformationSource;

import java.time.Instant;
import java.util.UUID;

/**
 * Define a class which represents a digitizer
 */
@AutoValue
@JsonSerialize(as = ReferenceDigitizer.class)
@JsonDeserialize(builder = AutoValue_ReferenceDigitizer.Builder.class)
public abstract class ReferenceDigitizer {

  /**
   * The id of the entity
   */
  public abstract UUID getEntityId();

  /**
   * The id of the version of the entity
   */
  public abstract UUID getVersionId();

  /**
   * The name of the digitizer
   */
  public abstract String getName();

  /**
   * Digitizer manufacturer
   */
  public abstract String getManufacturer();

  /**
   * Digitizer model
   */
  public abstract String getModel();

  /**
   * Digitizer serialNumber
   */
  public abstract String getSerialNumber();

  /**
   * The date and time time the information was originally generated
   */
  public abstract Instant getActualChangeTime();

  /**
   * The date and time time the information was entered into the system
   */
  public abstract Instant getSystemChangeTime();

  /**
   * The source of this information
   */
  public abstract InformationSource getInformationSource();

  public abstract String getDescription();

  public abstract String getComment();

  public abstract Builder toBuilder();

  public static Builder builder() {
    return new AutoValue_ReferenceDigitizer.Builder();
  }

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public interface Builder {

    Builder setEntityId(UUID entityId);

    Builder setVersionId(UUID versionId);

    Builder setName(String name);

    Builder setManufacturer(String manufacturer);

    Builder setModel(String model);

    Builder setSerialNumber(String serialNumber);

    Builder setActualChangeTime(Instant actualChangeTime);

    Builder setSystemChangeTime(Instant systemChangeTime);

    Builder setInformationSource(InformationSource informationSource);

    Builder setDescription(String description);

    Builder setComment(String comment);

    ReferenceDigitizer build();
  }
}
