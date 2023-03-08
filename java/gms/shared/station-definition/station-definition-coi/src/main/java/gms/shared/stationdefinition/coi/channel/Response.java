package gms.shared.stationdefinition.coi.channel;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static gms.shared.stationdefinition.coi.utils.StationDefinitionCoiUtils.RESPONSE_COMPARATOR;

/**
 * Periodically, the instrument corresponding to a Channel is calibrated to characterize the true
 * relationship between the underlying phenomenon the instrument is measuring and the actual output
 * of the instrument. As with the manufacturer-provided calibration information, this calibration
 * information is stored in the Calibration and Response classes. Response includes the full
 * response function across a range of periods/frequencies.
 */

@AutoValue
@JsonSerialize(as = Response.class)
@JsonDeserialize(builder = AutoValue_Response.Builder.class)
@JsonPropertyOrder(alphabetic = true)
public abstract class Response implements Comparable<Response> {

  public static Response.Builder builder() {
    return new AutoValue_Response.Builder();
  }

  public abstract Response.Builder toBuilder();

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public interface Builder {

    Response.Builder setId(UUID id);

    UUID getId();

    default Response.Builder setEffectiveAt(Instant effectiveAt) {
      return setEffectiveAt(Optional.ofNullable(effectiveAt));
    }

    Response.Builder setEffectiveAt(Optional<Instant> effectiveAt);

    @JsonUnwrapped
    default Response.Builder setData(Response.Data data) {
      return setData(Optional.ofNullable(data));
    }

    Response.Builder setData(Optional<Response.Data> data);

    Response autoBuild();

    default Response build() {
      return autoBuild();
    }
  }

  public static Response createEntityReference(UUID id) {
    return new AutoValue_Response.Builder()
      .setId(id)
      .build();
  }

  public static Response createEntityReference(Response other) {
    return new AutoValue_Response.Builder()
      .setId(other.getId())
      .build();
  }

  public static Response createVersionReference(UUID id, Instant effectiveAt) {
    return new AutoValue_Response.Builder()
      .setId(id)
      .setEffectiveAt(effectiveAt)
      .build();
  }

  public static Response createVersionReference(Response other) {
    return new AutoValue_Response.Builder()
      .setId(other.getId())
      .setEffectiveAt(other.getEffectiveAt())
      .build();
  }

  /**
   * Planning to add some sort of conversion between uuid and channelName.
   * This conversion is intended to be cached at the level of the accessor.
   */
  public abstract UUID getId();

  public abstract Optional<Instant> getEffectiveAt();

  @JsonIgnore
  public Calibration getCalibration() {
    return getDataOrThrow().getCalibration();
  }

  @JsonIgnore
  public FrequencyAmplitudePhase getFapResponse() {
    return getDataOrThrow().getFapResponse();
  }

  @JsonIgnore
  public Optional<Instant> getEffectiveUntil() {
    return getDataOrThrow().getEffectiveUntil();
  }

  @JsonIgnore
  public boolean isPresent() {
    return getData().isPresent();
  }

  @JsonUnwrapped
  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  public abstract Optional<Response.Data> getData();

  private Response.Data getDataOrThrow() {
    return getDataOptional().orElseThrow(() -> new IllegalStateException("Only contains ID facet"));
  }

  @JsonIgnore
  Optional<Response.Data> getDataOptional() {
    return getData();
  }

  @AutoValue
  @JsonSerialize(as = Response.Data.class)
  @JsonDeserialize(builder = AutoValue_Response_Data.Builder.class)
  @JsonPropertyOrder(alphabetic = true)
  public abstract static class Data {

    public static Response.Data.Builder builder() {
      return new AutoValue_Response_Data.Builder();
    }

    public abstract Response.Data.Builder toBuilder();

    @AutoValue.Builder
    @JsonPOJOBuilder(withPrefix = "set")
    public interface Builder {

      Response.Data.Builder setCalibration(Calibration calibration);

      Optional<Calibration> getCalibration();

      Response.Data.Builder setFapResponse(FrequencyAmplitudePhase fapResponse);

      Optional<FrequencyAmplitudePhase> getFapResponse();

      Response.Data.Builder setEffectiveUntil(Optional<Instant> effectiveUntil);

      default Response.Data.Builder setEffectiveUntil(Instant effectiveUntil) {
        return setEffectiveUntil(Optional.ofNullable(effectiveUntil));
      }

      Optional<Instant> getEffectiveUntil();

      Response.Data autoBuild();

      default Response.Data build() {
        final List<Optional<?>> allFields = List
          .of(getCalibration(), getFapResponse());
        final long numPresentFields = allFields.stream().filter(Optional::isPresent).count();

        if (0 == numPresentFields) {
          return null;
        } else if (allFields.size() == numPresentFields) {
          return autoBuild();
        }

        throw new IllegalStateException(
          "Either all FacetedDataClass fields must be populated or none of them can be populated");
      }
    }

    public abstract Calibration getCalibration();

    public abstract FrequencyAmplitudePhase getFapResponse();

    public abstract Optional<Instant> getEffectiveUntil();

  }

  @Override
  public int compareTo(Response otherResponse) {
    return RESPONSE_COMPARATOR.compare(this, otherResponse);
  }
}

