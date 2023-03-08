package gms.shared.event.coi;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.Collection;

/**
 * Represents the update information used in a update {@link Event} status request
 */
@AutoValue
public abstract class EventStatusInfo {

  /**
   * Gets the {@link EventStatus} for an update {@link Event} request
   *
   * @return The {@link EventStatus} to update to
   */
  public abstract EventStatus getEventStatus();

  /**
   * Gets the {@link Collection} of active analysts for an update {@link Event} request
   *
   * @return A {@link Collection} of active analysts
   */
  public abstract Collection<String> getActiveAnalystIds();

  /**
   * Creates and returns a {@link EventStatusInfo} for use in update {@link Event} requests
   *
   * @param eventStatus The {@link EventStatus} for the update {@link Event} request
   * @param activeAnalystIds The List of active analysis Ids for the update {@link Event} request
   * @return A newly created {@link EventStatusInfo}
   */
  @JsonCreator
  public static EventStatusInfo from(
    @JsonProperty("eventStatus") EventStatus eventStatus,
    @JsonProperty("activeAnalystIds") Collection<String> activeAnalystIds) {
    return new AutoValue_EventStatusInfo(eventStatus, activeAnalystIds);
  }

}
