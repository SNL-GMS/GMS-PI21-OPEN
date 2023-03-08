package gms.shared.frameworks.osd.coi.signaldetection;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import gms.shared.frameworks.osd.coi.InstantValue;
import gms.shared.frameworks.osd.coi.signaldetection.EnumeratedMeasurementValue.PhaseTypeMeasurementValue;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * See {@link SignalDetection} and {@link FeatureMeasurement} for detailed description of
 * SignalDetectionHypothesis.
 */
@AutoValue
public abstract class SignalDetectionHypothesis {

  public abstract UUID getId();

  public abstract UUID getParentSignalDetectionId();

  public abstract String getMonitoringOrganization();

  public abstract String getStationName();

  public abstract Optional<UUID> getParentSignalDetectionHypothesisId();

  public abstract boolean isRejected();

  @JsonIgnore
  abstract ImmutableMap<FeatureMeasurementType<?>, FeatureMeasurement<?>> getFeatureMeasurementsByType();

  public Collection<FeatureMeasurement<?>> getFeatureMeasurements() {
    return getFeatureMeasurementsByType().values().asList();
  }


  /**
   * Recreation factory method (sets the SignalDetection entity identity). Handles parameter
   * validation. Used for deserialization and recreating from persistence.
   *
   * @param id The {@link UUID} id assigned to the new SignalDetection.
   * @param parentSignalDetectionId This hypothesis' parent SignalDetection object.
   * @param isRejected Determines if this is a valid hypothesis
   * @param featureMeasurements The measurements used to make this hypothesis
   * @throws IllegalArgumentException if any of the parameters are null or featureMeasurements does
   * not contain an arrival time.
   */
  @JsonCreator
  public static SignalDetectionHypothesis from(
    @JsonProperty("id") UUID id,
    @JsonProperty("parentSignalDetectionId") UUID parentSignalDetectionId,
    @JsonProperty("monitoringOrganization") String monitoringOrganization,
    @JsonProperty("stationName") String stationName,
    @JsonProperty("parentSignalDetectionHypothesisId") UUID parentSignalDetectionHypothesisId,
    @JsonProperty("rejected") boolean isRejected,
    @JsonProperty("featureMeasurements") Collection<FeatureMeasurement<?>> featureMeasurements) {

    // Validate that phase and arrival time measurements are provided
    var hasArrival = false;
    var hasPhase = false;
    for (FeatureMeasurement fm : featureMeasurements) {
      hasArrival |= fm.getFeatureMeasurementType() == FeatureMeasurementTypes.ARRIVAL_TIME;
      hasPhase |= fm.getFeatureMeasurementType() == FeatureMeasurementTypes.PHASE;
    }
    if (!(hasArrival && hasPhase)) {
      throw new IllegalArgumentException(
        "Feature Measurements must contain an Arrival Time and Phase");
    }

    final var builder = builder(id,
      parentSignalDetectionId,
      monitoringOrganization,
      stationName,
      parentSignalDetectionHypothesisId,
      isRejected);
    featureMeasurements.forEach(builder::addMeasurement);
    return builder.build();
  }

  public static SignalDetectionHypothesis create(UUID parentSignalDetectionId,
    String monitoringOrganization,
    String stationName,
    UUID parentSignalDetectionHypothesisId,
    FeatureMeasurement<InstantValue> arrivalTimeMeasurement,
    FeatureMeasurement<PhaseTypeMeasurementValue> phaseMeasurement) {

    return create(parentSignalDetectionId,
      monitoringOrganization,
      stationName,
      parentSignalDetectionHypothesisId,
      List.of(arrivalTimeMeasurement, phaseMeasurement));
  }

  public static SignalDetectionHypothesis create(UUID parentSignalDetectionId,
    String monitoringOrganization,
    String stationName,
    UUID parentSignalDetectionHypothesisId,
    Collection<FeatureMeasurement<?>> measurements) {
    return from(UUID.randomUUID(),
      parentSignalDetectionId,
      monitoringOrganization,
      stationName,
      parentSignalDetectionHypothesisId,
      false,
      measurements);
  }

  /**
   * Returns a particular type of feature measurement, if it exists.
   *
   * @param type the type of the measurement
   * @return the measurement if it is present, otherwise Optional.empty.
   */
  @JsonIgnore
  @SuppressWarnings("unchecked")
  public <T> Optional<FeatureMeasurement<T>> getFeatureMeasurement(
    FeatureMeasurementType<T> type) {
    Objects.requireNonNull(type, "Cannot get feature measurement by null type");
    // Cast is safe by virtue of FeatureMeasurementTypesStaticChecking
    return Optional.ofNullable((FeatureMeasurement<T>) getFeatureMeasurementsByType().get(type));
  }

  public static Builder builder(UUID id,
    UUID parentSignalDetectionId,
    String monitoringOrganization,
    String stationName,
    UUID parentSignalDetectionHypothesisId,
    boolean isRejected) {
    return new AutoValue_SignalDetectionHypothesis.Builder()
      .setId(id)
      .setParentSignalDetectionId(parentSignalDetectionId)
      .setMonitoringOrganization(monitoringOrganization)
      .setStationName(stationName)
      .setParentSignalDetectionHypothesisId(Optional.ofNullable(parentSignalDetectionHypothesisId))
      .setRejected(isRejected);
  }

  public abstract Builder toBuilder();

  public Builder withMeasurements(Collection<FeatureMeasurementType<?>> types) {
    return toBuilder().setFeatureMeasurementsByType(
      ImmutableMap.copyOf(Maps.filterKeys(getFeatureMeasurementsByType(), types::contains)));
  }

  public Builder withoutMeasurements(Collection<FeatureMeasurementType<?>> types) {
    return toBuilder().setFeatureMeasurementsByType(
      ImmutableMap
        .copyOf(Maps.filterKeys(getFeatureMeasurementsByType(), key -> !types.contains(key))));
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public Builder generateId() {
      return setId(UUID.randomUUID());
    }

    protected abstract Builder setId(UUID id);

    public abstract Builder setParentSignalDetectionId(UUID parentSignalDetectionId);

    public abstract Builder setMonitoringOrganization(String monitoringOrganization);

    public abstract Builder setStationName(String stationName);

    public abstract Builder setParentSignalDetectionHypothesisId(Optional<UUID> parentSignalDetectionHypothesisId);

    public abstract Builder setRejected(boolean rejected);

    protected abstract Builder setFeatureMeasurementsByType(
      ImmutableMap<FeatureMeasurementType<?>, FeatureMeasurement<?>> featureMeasurementsByType);

    protected abstract ImmutableMap.Builder<FeatureMeasurementType<?>, FeatureMeasurement<?>> featureMeasurementsByTypeBuilder();

    public <T> Builder addMeasurement(FeatureMeasurement<T> measurement) {
      Objects.requireNonNull(measurement, "Cannot add null measurement");
      featureMeasurementsByTypeBuilder().put(measurement.getFeatureMeasurementType(), measurement);
      return this;
    }

    public abstract SignalDetectionHypothesis autobuild();

    public SignalDetectionHypothesis build() {
      SignalDetectionHypothesis hypothesis = autobuild();

      hypothesis.getFeatureMeasurements()
        .forEach(measurement -> Preconditions.checkState(hypothesis.getStationName().equals(measurement.getChannel().getStation()),
          String.format("Cannot create SignalDetectionHypothesis with FeatureMeasurements on Channels " +
              "from a different station (hypothesis station is %s, but channel station is %s)",
            hypothesis.getStationName(), measurement.getChannel().getStation())));

      return hypothesis;
    }
  }
}
