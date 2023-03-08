package gms.shared.event.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableSet;
import gms.shared.event.coi.Event;
import gms.shared.signaldetection.api.response.SignalDetectionsWithChannelSegments;

import java.util.Collection;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY;

/**
 * Defines the response body for the findEventsWithDetectionsAndSegmentsByTime operations. Associates a Set of Events
 * with a SignalDetectionsWithChannelSegments.
 */
@AutoValue
@JsonSerialize(as = EventsWithDetectionsAndSegments.class)
@JsonDeserialize(builder = AutoValue_EventsWithDetectionsAndSegments.Builder.class)
public abstract class EventsWithDetectionsAndSegments {

  /**
   * Gets the Events associated with the SignalDetectionsWithChannelSegments
   *
   * @return Set of Events
   */
  @JsonProperty("events")
  public abstract ImmutableSet<Event> getEvents();

  /**
   * Gets the SignalDetectionsWithChannelSegments associated with the Events
   *
   * @return SignalDetectionsWithChannelSegments
   */
  @JsonUnwrapped
  @JsonProperty(access = READ_ONLY)
  public abstract SignalDetectionsWithChannelSegments getDetectionsWithChannelSegments();

  public static Builder builder() {
    return new AutoValue_EventsWithDetectionsAndSegments.Builder();
  }

  public abstract Builder toBuilder();

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public interface Builder {
    default Builder setEvents(Collection<Event> events) {
      setEvents(ImmutableSet.copyOf(events));
      return this;
    }

    Builder setEvents(ImmutableSet<Event> events);

    ImmutableSet.Builder<Event> eventsBuilder();

    default EventsWithDetectionsAndSegments.Builder addEvent(Event event) {
      eventsBuilder().add(event);
      return this;
    }

    @JsonUnwrapped
    Builder setDetectionsWithChannelSegments(SignalDetectionsWithChannelSegments signalDetectionsWithChannelSegments);

    EventsWithDetectionsAndSegments autoBuild();

    default EventsWithDetectionsAndSegments build() {
      return autoBuild();
    }
  }
}
