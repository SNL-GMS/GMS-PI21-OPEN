package gms.core.performancemonitoring.uimaterializedview;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import gms.shared.frameworks.osd.coi.soh.quieting.SohStatusChange;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@AutoValue
public abstract class AcknowledgedSohStatusChange {
  /**
   * @return the id.
   */
  public abstract UUID getId();

  /**
   * @return the user who created the acknowledgement
   */
  public abstract String getAcknowledgedBy();

  /**
   * @return the time of the Acknowledgement.
   */
  public abstract Instant getAcknowledgedAt();

  /**
   * @return the user comment
   */
  public abstract Optional<String> getComment();

  /**
   * @return the list of SOH Status Changes
   */
  public abstract List<SohStatusChange> getAcknowledgedChanges();

  /**
   * @return the station name
   */
  public abstract String getAcknowledgedStation();


  /**
   * Create a new AcknowledgedSohStatusChange object.
   *
   * @param id the Id.
   * @param acknowledgedBy user who acknowledge
   * @param acknowledgedAt the time of acknowledged
   * @param comment by user at acknowledgement
   * @param acknowledgedChanges list of SOH changes acknowledged
   * @param acknowledgedStation station name
   * @return an AcknowledgedSohStatusChange object.
   */
  @JsonCreator
  public static AcknowledgedSohStatusChange from(
    @JsonProperty("id") UUID id,
    @JsonProperty("acknowledgedBy") String acknowledgedBy,
    @JsonProperty("acknowledgedAt") Instant acknowledgedAt,
    @JsonProperty("comment") Optional<String> comment,
    @JsonProperty("acknowledgedChanges") List<SohStatusChange> acknowledgedChanges,
    @JsonProperty("acknowledgedStation") String acknowledgedStation
  ) {

    return new AutoValue_AcknowledgedSohStatusChange(
      id, acknowledgedBy, acknowledgedAt, comment, acknowledgedChanges, acknowledgedStation
    );
  }
}
    