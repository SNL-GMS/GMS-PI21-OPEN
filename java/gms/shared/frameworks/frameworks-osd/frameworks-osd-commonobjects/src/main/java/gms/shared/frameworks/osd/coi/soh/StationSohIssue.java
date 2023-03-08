package gms.shared.frameworks.osd.coi.soh;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.time.Instant;

@AutoValue
public abstract class StationSohIssue {

  /**
   * The acknowledged status of a station issue.
   *
   * @return a boolean indicating if the issue requires acknowledgement
   */
  public abstract boolean getRequiresAcknowledgement();

  /**
   * The time at which a station issue was acknowledged.
   *
   * @return the time at which the issue was acknowledged
   */
  @Nullable
  public abstract Instant getAcknowledgedAt();

  public abstract Builder toBuilder();

  /**
   * Call AutoValue and obtain Builder for StationSohIssue which is then returned.
   *
   * @return builder for SohIssue
   */
  public static Builder builder() {
    return new AutoValue_StationSohIssue.Builder();

  }

  /**
   * Abstract class that defines the Builder Object for StationSoHIssue. This needs to be an
   * abstract class, so the S1610 finding in SolarQube is invalid.
   */
  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setRequiresAcknowledgement(boolean requiresAcknowledgement);

    public abstract Builder setAcknowledgedAt(Instant acknowledgedAt);

    abstract StationSohIssue autoBuild();

    public StationSohIssue build() {
      return autoBuild();
    }

  }

  /**
   * Recreates a StationSohIssue from a serialized form.
   *
   * @param requiresAcknowledgement indicates if an issue requires acknowledgement
   * @param acknowledgedAt indicates the time at which an issue was acknowledged
   * @return a StationSohIssue
   */
  @JsonCreator
  public static StationSohIssue from(
    @JsonProperty("requiresAcknowledgement") boolean requiresAcknowledgement,
    @JsonProperty("acknowledgedAt") Instant acknowledgedAt) {
    return StationSohIssue.builder()
      .setRequiresAcknowledgement(requiresAcknowledgement)
      .setAcknowledgedAt(acknowledgedAt)
      .build();
  }
}


