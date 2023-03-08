package gms.shared.frameworks.osd.coi.provenance;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.time.Instant;
import java.util.Objects;

/**
 * Information Source captures reference details for information added to the System. E.g. if the
 * System records a seismometer's location based on a value published in a network guidebook then an
 * Information Source object would describe the originating organization that published the book,
 * the date of publication is the information time, and additional details on how others can access
 * the guidebook appear in reference.
 */
@AutoValue
public abstract class InformationSource {

  public abstract String getOriginatingOrganization();

  public abstract Instant getInformationTime();

  public abstract String getReference();

  @JsonCreator
  public static InformationSource from(
    @JsonProperty("originatingOrganization") String originatingOrganization,
    @JsonProperty("informationTime") Instant informationTime,
    @JsonProperty("reference") String reference) {

    Objects.requireNonNull(originatingOrganization,
      "Cannot create InformationSource from null originatingOrganization");
    Objects.requireNonNull(informationTime,
      "Cannot create InformationSource from null informationTime");
    Objects.requireNonNull(reference,
      "Cannot create InformationSource from null reference");
    return new AutoValue_InformationSource(originatingOrganization, informationTime, reference);
  }


}
