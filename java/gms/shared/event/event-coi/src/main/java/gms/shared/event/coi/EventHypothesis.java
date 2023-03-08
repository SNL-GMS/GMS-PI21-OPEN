package gms.shared.event.coi;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import gms.shared.signaldetection.coi.detection.SignalDetectionHypothesis;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Represents a possible hypothesis for an Event.
 */
@AutoValue
@JsonSerialize(as = EventHypothesis.class)
@JsonDeserialize(builder = AutoValue_EventHypothesis.Builder.class)
public abstract class EventHypothesis {

  /**
   * Creates a rejected {@link EventHypothesis}
   *
   * @param eventUUID {@link UUID} of the orid associated with the rejected {@link EventHypothesis}
   * @param rejectedEhUUID {@link UUID} of the rejected {@link EventHypothesis}
   * @param rejectedParentEh {@link UUID} of the parent {@link EventHypothesis} associated with the rejected {@link EventHypothesis}
   * @return A rejected {@link EventHypothesis}
   */
  public static EventHypothesis createRejectedEventHypothesis(UUID eventUUID, UUID rejectedEhUUID,
    UUID rejectedParentEh) {
    Preconditions.checkNotNull(eventUUID);
    Preconditions.checkNotNull(rejectedEhUUID);
    Preconditions.checkNotNull(rejectedParentEh);

    return builder()
      .setId(Id.from(eventUUID, rejectedEhUUID))
      .setData(Data.builder()
        .setParentEventHypotheses(List.of(
          EventHypothesis.builder()
            .setId(EventHypothesis.Id.from(eventUUID, rejectedParentEh))
            .build()))
        .setRejected(true)
        .build())
      .build();
  }

  public abstract Id getId();

  @JsonUnwrapped
  public abstract Optional<Data> getData();

  public static EventHypothesis createEntityReference(@JsonProperty("id") Id id) {
    return builder().setId(checkNotNull(id)).build();
  }

  public EventHypothesis toEntityReference() {
    return builder().setId(getId()).build();
  }

  /**
   * AutoValue builder for the main Event class
   */
  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public interface Builder {
    EventHypothesis.Builder setId(Id id);

    @JsonUnwrapped
    EventHypothesis.Builder setData(@Nullable EventHypothesis.Data data);

    EventHypothesis autobuild();

    default EventHypothesis build() {
      return autobuild();
    }

  }

  public static EventHypothesis.Builder builder() {
    return new AutoValue_EventHypothesis.Builder();
  }

  public abstract EventHypothesis.Builder toBuilder();

  @AutoValue
  public abstract static class Id {
    public abstract UUID getEventId();

    public abstract UUID getHypothesisId();

    @JsonCreator
    public static Id from(@JsonProperty("eventId") UUID eventId, @JsonProperty("hypothesisId") UUID hypothesisId) {
      return new AutoValue_EventHypothesis_Id(eventId, hypothesisId);
    }
  }


  /*********************************
   * DATA OBJECT
   *********************************/

  @AutoValue
  @JsonSerialize(as = EventHypothesis.Data.class)
  @JsonDeserialize(builder = AutoValue_EventHypothesis_Data.Builder.class)
  public abstract static class Data {

    public abstract EventHypothesis.Data.Builder toBuilder();

    public static EventHypothesis.Data.Builder builder() {
      return new AutoValue_EventHypothesis_Data.Builder();
    }

    public abstract ImmutableSet<EventHypothesis> getParentEventHypotheses();

    public abstract boolean isRejected();

    public abstract ImmutableSet<LocationSolution> getLocationSolutions();

    public Stream<LocationSolution> locationSolutions() {
      return getLocationSolutions().stream();
    }

    public abstract Optional<LocationSolution> getPreferredLocationSolution();

    public abstract ImmutableSet<SignalDetectionHypothesis> getAssociatedSignalDetectionHypotheses();

    public Stream<SignalDetectionHypothesis> associatedSignalDetectionHypotheses() {
      return getAssociatedSignalDetectionHypotheses().stream();
    }

    @AutoValue.Builder
    @JsonPOJOBuilder(withPrefix = "set")
    public abstract static class Builder {

      public abstract Builder setParentEventHypotheses(Collection<EventHypothesis> parentEventHypotheses);

      abstract ImmutableSet.Builder<EventHypothesis> parentEventHypothesesBuilder();

      public Builder addParentEventHypothesis(EventHypothesis parentEventHypothesis) {
        parentEventHypothesesBuilder().add(parentEventHypothesis);
        return this;
      }

      public abstract Builder setRejected(boolean rejected);


      public abstract Builder setLocationSolutions(Collection<LocationSolution> locationSolutions);

      abstract ImmutableSet.Builder<LocationSolution> locationSolutionsBuilder();

      public Builder addLocationSolution(LocationSolution locationSolution) {
        locationSolutionsBuilder().add(locationSolution);
        return this;
      }

      public abstract Builder setPreferredLocationSolution(@Nullable LocationSolution preferredLocationSolution);

      public abstract Builder setAssociatedSignalDetectionHypotheses(
        Collection<SignalDetectionHypothesis> associatedSignalDetectionHypotheses);

      abstract ImmutableSet.Builder<SignalDetectionHypothesis> associatedSignalDetectionHypothesesBuilder();

      public Builder addAssociation(SignalDetectionHypothesis association) {
        associatedSignalDetectionHypothesesBuilder().add(association);
        return this;
      }

      abstract Data autoBuild();

      /**
       * Returns an Event.Data object only if all the fields are properly set.
       * This intentionally returns null if the getRejected is not set.
       * This is to support deserialization of a faceted Location Solution using Jackson.
       *
       * @return EventHypothesis.Data
       */
      @Nullable
      public Data build() {

        EventHypothesis.Data data;

        // The reason for the try/catch is that if this fails to build it would mean
        // that isRejected was not set, and therefore null should be returned to support
        // deserializing faceted objects
        try {
          data = autoBuild();

        } catch (IllegalStateException e) {
          return null;
        }

        if (data.isRejected()) {
          checkState(data.getLocationSolutions().isEmpty(), "locationSolutions must be empty when hypothesis is rejected");
          checkState(data.getPreferredLocationSolution().isEmpty(), "preferredLocationSolution must be empty when hypothesis is rejected");
        } else {
          checkState(data.getPreferredLocationSolution().isPresent(), "preferredLocationSolution must be present when hypothesis is not rejected");

          data.getPreferredLocationSolution().ifPresent(ls ->
            checkState(data.getLocationSolutions().stream().map(LocationSolution::getId).collect(Collectors.toList())
              .contains(ls.getId()), "locationSolutions must contain preferredLocationSolution"));
        }

        return data;
      }

    }
  }
}