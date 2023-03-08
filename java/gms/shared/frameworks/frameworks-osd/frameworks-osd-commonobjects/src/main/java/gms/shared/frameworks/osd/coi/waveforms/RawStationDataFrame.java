package gms.shared.frameworks.osd.coi.waveforms;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static java.util.UUID.randomUUID;

/**
 * Represents a frame of data from a station; could be received via various protocols. It includes
 * the start/end time of the data, a reference by ID to the channel the data is for, the time it was
 * received, a raw payload (bytes) - this represents the whole raw frame, and the status of its
 * authentication.
 */
@AutoValue
@JsonSerialize(as = RawStationDataFrame.class)
@JsonDeserialize(builder = AutoValue_RawStationDataFrame.Builder.class)
public abstract class RawStationDataFrame {

  public abstract UUID getId();

  public abstract RawStationDataFrameMetadata getMetadata();

  public abstract Optional<byte[]> getRawPayload();

  public static Builder builder() {
    return new AutoValue_RawStationDataFrame.Builder();
  }

  public abstract Builder toBuilder();

  /**
   * Enum for the status of authentication of a frame.
   */
  public enum AuthenticationStatus {
    NOT_APPLICABLE,
    AUTHENTICATION_FAILED,
    AUTHENTICATION_SUCCEEDED,
    NOT_YET_AUTHENTICATED
  }

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public interface Builder {

    Builder setId(UUID id);

    default Builder generatedId() {
      return setId(randomUUID());
    }

    Builder setMetadata(RawStationDataFrameMetadata metadata);

    RawStationDataFrameMetadata getMetadata();

    @JsonProperty
    Builder setRawPayload(byte[] rawPayload);

    Builder setRawPayload(Optional<byte[]> rawPayload);

    RawStationDataFrame build();
  }

  // Compares the state and the raw payloads of two RSDF objects
  public boolean hasSameStateAndRawPayload(RawStationDataFrame otherRsdf) {
    if (!this.getMetadata().hasSameState(otherRsdf.getMetadata())) {
      return false;
    }
    var optRawPayload = this.getRawPayload();
    var optOtherRawPayload = otherRsdf.getRawPayload();
    if (optRawPayload.isPresent() && optOtherRawPayload.isPresent()) {
      return Arrays.equals(optRawPayload.get(), optOtherRawPayload.get());
    } else {
      return optRawPayload.isEmpty() && optOtherRawPayload.isEmpty();
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj instanceof RawStationDataFrame) {
      RawStationDataFrame that = (RawStationDataFrame) obj;
      return this.getId().equals(that.getId())
        && this.getMetadata().equals(that.getMetadata())
        && hasSameStateAndRawPayload(that);
    }
    return false;
  }
}
