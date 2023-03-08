package gms.shared.event.coi;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import gms.shared.signaldetection.coi.detection.SignalDetection;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkState;

/**
 * Define an Event class
 */
@AutoValue
@JsonSerialize(as = Event.class)
@JsonDeserialize(builder = AutoValue_Event.Builder.class)
public abstract class Event {

  public abstract UUID getId();

  @JsonUnwrapped
  public abstract Optional<Data> getData();

  private Event.Data getDataOrThrow() {
    return getData().orElseThrow(() -> new IllegalStateException("Only contains ID facet"));
  }

  public static Event createEntityReference(UUID id) {
    return builder()
      .setId(id)
      .build();
  }

  public Event toEntityReference() {
    return builder()
      .setId(getId())
      .build();
  }

  /**
   * AutoValue builder for the main Event class
   */
  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public interface Builder {
    Event.Builder setId(UUID id);

    @JsonUnwrapped
    Event.Builder setData(@Nullable Event.Data data);

    Event autobuild();

    default Event build() {
      var build = autobuild();

      build.getData().ifPresent(data ->
        data.getEventHypotheses().forEach(hypothesis -> checkState(build.getId().equals(hypothesis.getId().getEventId()),
          "EventHypothesis has eventId not matching parent Event: event.id %s, EventHypothesis.eventId %s",
          build.getId(), hypothesis.getId().getEventId())));

      return build;
    }

  }

  public static Event.Builder builder() {
    return new AutoValue_Event.Builder();
  }

  public abstract Event.Builder toBuilder();


  /*********************************
   * DATA OBJECT
   *********************************/

  @AutoValue
  @JsonSerialize(as = Event.Data.class)
  @JsonDeserialize(builder = AutoValue_Event_Data.Builder.class)
  public abstract static class Data {

    public abstract Event.Data.Builder toBuilder();

    public static Event.Data.Builder builder() {
      return new AutoValue_Event_Data.Builder();
    }

    public abstract ImmutableSet<SignalDetection> getRejectedSignalDetectionAssociations();

    public abstract String getMonitoringOrganization();

    public abstract ImmutableSet<EventHypothesis> getEventHypotheses();

    public Stream<EventHypothesis> eventHypotheses() {
      return getEventHypotheses().stream();
    }

    public abstract ImmutableSet<PreferredEventHypothesis> getPreferredEventHypothesisByStage();

    public Stream<PreferredEventHypothesis> preferredEventHypothesisByStage() {
      return getPreferredEventHypothesisByStage().stream();
    }

    public abstract Optional<EventHypothesis> getOverallPreferred();

    public abstract ImmutableList<EventHypothesis> getFinalEventHypothesisHistory();

    @AutoValue.Builder
    @JsonPOJOBuilder(withPrefix = "set")
    public abstract static class Builder {

      abstract ImmutableSet.Builder<SignalDetection> rejectedSignalDetectionAssociationsBuilder();

      public abstract Event.Data.Builder setRejectedSignalDetectionAssociations(
        Collection<SignalDetection> rejectedSignalDetectionAssociations);

      public abstract Event.Data.Builder setMonitoringOrganization(String monitoringOrganization);

      abstract Optional<String> getMonitoringOrganization();

      abstract ImmutableSet.Builder<EventHypothesis> eventHypothesesBuilder();

      public abstract Event.Data.Builder setEventHypotheses(Collection<EventHypothesis> hypotheses);

      public Event.Data.Builder addEventHypothesis(EventHypothesis hypothesis) {
        eventHypothesesBuilder().add(hypothesis);
        return this;
      }

      public Event.Data.Builder addAllEventHypotheses(Collection<EventHypothesis> hypothesis) {
        eventHypothesesBuilder().addAll(hypothesis);
        return this;
      }

      public abstract ImmutableSet<EventHypothesis> getEventHypotheses();

      abstract ImmutableSet.Builder<PreferredEventHypothesis> preferredEventHypothesisByStageBuilder();

      public abstract Event.Data.Builder setPreferredEventHypothesisByStage(
        Collection<PreferredEventHypothesis> preferredEventHypothesisByStage);

      public Event.Data.Builder addPreferredEventHypothesis(PreferredEventHypothesis preferredEventHypothesis) {
        preferredEventHypothesisByStageBuilder().add(preferredEventHypothesis);
        setOverallPreferred(preferredEventHypothesis.getPreferred());
        return this;
      }

      public abstract Event.Data.Builder setOverallPreferred(@Nullable EventHypothesis overallPreferred);

      abstract ImmutableList.Builder<EventHypothesis> finalEventHypothesisHistoryBuilder();

      public abstract Event.Data.Builder setFinalEventHypothesisHistory(
        Collection<EventHypothesis> finalEventHypothesisHistory);

      public Event.Data.Builder addFinalEventHypothesis(EventHypothesis finalEventHypothesis) {
        finalEventHypothesisHistoryBuilder().add(finalEventHypothesis);
        return this;
      }

      abstract ImmutableList<EventHypothesis> getFinalEventHypothesisHistory();

      protected abstract Event.Data autoBuild();

      /**
       * Returns an Event.Data object only if all the fields are properly set.
       * This intentionally returns null if the MonitoringOrganization is not set.
       * This is to support deserialization of a faceted Location Solution using Jackson.
       *
       * @return Event.Data
       */
      @Nullable
      public Event.Data build() {

        //This check is for allowing deserialization of a faceted Event to be created
        //where the data is empty. This build must return null in this case
        List<Optional<?>> allFields = List.of(getMonitoringOrganization());
        var numPresentFields = allFields.stream()
          .filter(Optional::isPresent)
          .count();
        if (numPresentFields == 0) {
          return null;
        }

        var build = autoBuild();

        checkState(!build.getMonitoringOrganization().isEmpty() &&
            !build.getMonitoringOrganization().isBlank(),
          "Cannot create an event with empty or blank monitoringOrganization");
        checkState(!build.getEventHypotheses().isEmpty(), "An event must have at least one hypothesis");
        checkState(!build.getPreferredEventHypothesisByStage().isEmpty(), "An event must have at least one preferred event hypothesis");

        var finalEventsNotPresent = build.getFinalEventHypothesisHistory().stream()
          .filter(eh -> !build.getEventHypotheses().contains(eh))
          .map(EventHypothesis::getId)
          .collect(Collectors.toList());
        checkState(finalEventsNotPresent.isEmpty(),
          "Final Event Hypothesis history contains hypotheses not present in Event's hypotheses: %s",
          finalEventsNotPresent);

        var preferredHypothesesNotPresent = build.preferredEventHypothesisByStage()
          .filter(preferred ->
            !build.getEventHypotheses().stream()
              .map(EventHypothesis::getId)
              .collect(Collectors.toSet())
              .contains(preferred.getPreferred().getId())
          )
          .map(preferred -> String.format("[Stage: %s, EH ID: %s]", preferred.getStage(), preferred.getPreferred().getId()))
          .collect(Collectors.toList());
        checkState(preferredHypothesesNotPresent.isEmpty(),
          "Preferred event hypotheses by stage contains hypotheses not present in Event's hypotheses: %s",
          preferredHypothesesNotPresent);
        return build;
      }
    }
  }

  /**
   * Gets the (currently) final hypothesis.
   *
   * @return the final hypothesis, or empty if no hypothesis has (ever) been marked final
   */
  @JsonIgnore
  public Optional<EventHypothesis> getFinal() {
    var finalEventHypothesisHistory = this.getDataOrThrow().getFinalEventHypothesisHistory();
    if (finalEventHypothesisHistory.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(finalEventHypothesisHistory.get(
      finalEventHypothesisHistory.size() - 1));
  }
}
