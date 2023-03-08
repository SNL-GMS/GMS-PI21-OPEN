package gms.shared.frameworks.osd.coi.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableSet;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkState;
import static java.util.UUID.randomUUID;

/**
 * Represents a possible hypothesis for an Event.
 */
@AutoValue
@JsonSerialize(as = EventHypothesis.class)
@JsonDeserialize(builder = AutoValue_EventHypothesis.Builder.class)
@Deprecated(forRemoval = true)
public abstract class EventHypothesis {

  public abstract UUID getId();

  public abstract UUID getEventId();

  public abstract ImmutableSet<UUID> getParentEventHypotheses();

  public abstract boolean isRejected();

  public abstract ImmutableSet<LocationSolution> getLocationSolutions();

  public abstract Optional<PreferredLocationSolution> getPreferredLocationSolution();

  public abstract ImmutableSet<SignalDetectionEventAssociation> getAssociations();

  public abstract Builder toBuilder();

  public static Builder builder() {
    return new AutoValue_EventHypothesis.Builder();
  }

  public Builder withLocationSolutions(Collection<LocationSolution> locationSolutions) {
    return toBuilder().setLocationSolutions(locationSolutions);
  }

  /**
   * Creates an instance of an EventHypothesis
   *
   * @param id UUID assigned to the new EventHypothesis, not null
   * @param eventId UUID of the associated Event, not null
   * @param parentEventHypotheses {@code Set<UUID> of the ParentEventHypotheses, unmodifiable }
   * @param isRejected boolean
   * @param locationSolutions {@code Set<LocationSolution>, unmodifiable}
   * @param preferredLocationSolution The single PreferredLocationSolution associated with the
   * EventHypothesis, not null
   * @param associations {@code Set<SignalDetectionEventAssociation>, unmodifiable }
   * @return an Event Hypothesis
   */
  public static EventHypothesis from(UUID id, UUID eventId, Set<UUID> parentEventHypotheses,
    boolean isRejected, Set<LocationSolution> locationSolutions,
    PreferredLocationSolution preferredLocationSolution,
    Set<SignalDetectionEventAssociation> associations) {
    return builder()
      .setId(id)
      .setEventId(eventId)
      .setParentEventHypotheses(parentEventHypotheses)
      .setRejected(isRejected)
      .setLocationSolutions(locationSolutions)
      .setPreferredLocationSolution(Optional.ofNullable(preferredLocationSolution))
      .setAssociations(associations)
      .build();
  }

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public abstract static class Builder {

    public abstract Builder setId(UUID id);

    public Builder generatedId() {
      return setId(randomUUID());
    }

    public abstract Builder setEventId(UUID eventId);

    abstract Builder setParentEventHypotheses(ImmutableSet<UUID> parentEventHypotheses);

    public Builder setParentEventHypotheses(Collection<UUID> parentEventHypotheses) {
      return setParentEventHypotheses(ImmutableSet.copyOf(parentEventHypotheses));
    }

    abstract ImmutableSet.Builder<UUID> parentEventHypothesesBuilder();

    public Builder addParentEventHypothesis(UUID parentEventHypothesis) {
      parentEventHypothesesBuilder().add(parentEventHypothesis);
      return this;
    }

    public abstract Builder setRejected(boolean rejected);

    abstract Builder setLocationSolutions(ImmutableSet<LocationSolution> locationSolutions);

    public Builder setLocationSolutions(Collection<LocationSolution> locationSolutions) {
      return setLocationSolutions(ImmutableSet.copyOf(locationSolutions));
    }

    abstract ImmutableSet.Builder<LocationSolution> locationSolutionsBuilder();

    public Builder addLocationSolution(LocationSolution locationSolution) {
      locationSolutionsBuilder().add(locationSolution);
      return this;
    }

    @JsonProperty("preferredLocationSolution")
    public abstract Builder setPreferredLocationSolution(
      Optional<PreferredLocationSolution> preferredLocationSolution);

    public abstract Builder setPreferredLocationSolution(
      PreferredLocationSolution preferredLocationSolution);

    abstract Builder setAssociations(ImmutableSet<SignalDetectionEventAssociation> associations);

    public Builder setAssociations(Collection<SignalDetectionEventAssociation> associations) {
      return setAssociations(ImmutableSet.copyOf(associations));
    }

    abstract ImmutableSet.Builder<SignalDetectionEventAssociation> associationsBuilder();

    public Builder addAssociation(SignalDetectionEventAssociation association) {
      associationsBuilder().add(association);
      return this;
    }

    abstract EventHypothesis autoBuild();

    public EventHypothesis build() {
      EventHypothesis eventHypothesis = autoBuild();

      if (eventHypothesis.isRejected()) {
        checkState(eventHypothesis.getLocationSolutions().isEmpty(),
          "Expected locationSolutions to be empty when isRejected=true");
        checkState(!eventHypothesis.getPreferredLocationSolution().isPresent(),
          "Expected preferredLocationSolution to be empty when isRejected=true");
      } else {
        checkState(eventHypothesis.getPreferredLocationSolution().isPresent(),
          "Expected non-empty preferredLocationSolution when EventHypothesis is not rejected");

        eventHypothesis.getPreferredLocationSolution()
          .map(PreferredLocationSolution::getLocationSolution)
          .ifPresent(ls -> checkState(eventHypothesis.getLocationSolutions().contains(ls),
            "Expected locationSolutions to contain preferredLocationSolution"));
      }

      eventHypothesis.getAssociations().stream()
        .forEach(association ->
          checkState(association.getEventHypothesisId().equals(eventHypothesis.getId()),
            "Expected SignalDetectionEventAssociations to reference the current EventHypothesis"));

      return eventHypothesis;
    }

  }
}
