package gms.shared.signaldetection.coi.detection;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;
import com.google.auto.value.extension.memoized.Memoized;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import gms.shared.signaldetection.coi.types.FeatureMeasurementType;
import gms.shared.signaldetection.coi.types.FeatureMeasurementTypes;
import gms.shared.stationdefinition.coi.channel.Channel;
import gms.shared.stationdefinition.coi.station.Station;
import org.apache.commons.lang3.Validate;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

/**
 * See {@link SignalDetection} and {@link FeatureMeasurement} for detailed description of
 * SignalDetectionHypothesis.
 */
@AutoValue
@JsonSerialize(as = SignalDetectionHypothesisBase.class)
@JsonDeserialize(builder = AutoValue_SignalDetectionHypothesis.Builder.class)
public abstract class SignalDetectionHypothesis extends SignalDetectionHypothesisBase {

  public abstract SignalDetectionHypothesisId getId();

  public String getMonitoringOrganization() {
    return getDataOrThrow().getMonitoringOrganization();
  }

  public Station getStation() {
    return getDataOrThrow().getStation();
  }

  public Optional<SignalDetectionHypothesis> getParentSignalDetectionHypothesis() {
    return getDataOrThrow().getParentSignalDetectionHypothesis();
  }

  public boolean isRejected() {
    return getDataOrThrow().isRejected();
  }

  @JsonUnwrapped
  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  public abstract Optional<SignalDetectionHypothesis.Data> getData();

  private SignalDetectionHypothesis.Data getDataOrThrow() {
    return getDataOptional().orElseThrow(() -> new IllegalStateException("Only contains ID facet"));
  }

  Optional<SignalDetectionHypothesis.Data> getDataOptional() {
    return getData();
  }

  @JsonIgnore
  public boolean isPresent() {
    return getData().isPresent();
  }

  /**
   * Reach-through method to allow for ease of testing and associating parent IDs.
   *
   * @return signal detection parent hypothesis id
   */
  @JsonIgnore
  public Optional<SignalDetectionHypothesisId> getParentSignalDetectionHypothesisId() {
    final var parentSignalDetectionHypothesis =
      getDataOrThrow().getParentSignalDetectionHypothesis();

    if (parentSignalDetectionHypothesis.isPresent()) {
      return Optional.of(parentSignalDetectionHypothesis.orElseThrow().getId());
    } else {
      return Optional.empty();
    }
  }

  /**
   * Another reachthrough method for testing and associating featureMeasurements.
   *
   * @return list of feature measurements
   */
  @JsonIgnore
  public Collection<FeatureMeasurement<?>> getFeatureMeasurements() {
    return getDataOrThrow().getFeatureMeasurements();
  }

  public static SignalDetectionHypothesis createEntityReference(UUID signalDetectionId, UUID id) {
    SignalDetectionHypothesisId hid = SignalDetectionHypothesisId.from(signalDetectionId, id);
    return SignalDetectionHypothesis.create(hid, Optional.empty());
  }

  public SignalDetectionHypothesis toEntityReference() {
    return SignalDetectionHypothesis.create(getId(), Optional.empty());
  }

  /**
   * Recreation factory method (sets the SignalDetection entity identity). Handles parameter
   * validation. Used for deserialization and recreating from persistence.
   *
   * @param id The {@link UUID} id assigned to the new SignalDetectionHypothesis.
   * @param data The data object holding all pertinent information for SignalDetectionHypothesis.
   * @throws IllegalArgumentException if any of the parameters are null or featureMeasurements does
   * not contain an arrival time.
   */
  public static SignalDetectionHypothesis from(
    SignalDetectionHypothesisId id,
    Optional<SignalDetectionHypothesis.Data> data) {

    Objects.requireNonNull(id, "Cannot create SignalDetectionHypothesis from a null id");
    Objects.requireNonNull(data,
      "Cannot create SignalDetectionHypothesis from a null data object");

    return create(id, data);
  }

  /**
   * Create a SignalDetectionHypothesis object with an empty data Optional, as an EntityReference...
   */
  public static SignalDetectionHypothesis create(SignalDetectionHypothesisId id, Optional<Data> data) {
    Objects.requireNonNull(id, "Cannot create a SignalDetectionHypothesis with a null id");
    return SignalDetectionHypothesis.builder()
      .setId(id)
      .setData(data).build();
  }

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public interface Builder {

    Builder setId(SignalDetectionHypothesisId id);

    Builder setData(Optional<SignalDetectionHypothesis.Data> data);

    @JsonUnwrapped
    default SignalDetectionHypothesis.Builder setData(SignalDetectionHypothesis.Data data) {
      return setData(Optional.ofNullable(data));
    }

    SignalDetectionHypothesis autoBuild();

    default SignalDetectionHypothesis build() {
      return autoBuild();
    }
  }

  public static Builder builder() {
    return new AutoValue_SignalDetectionHypothesis.Builder();
  }

  public abstract Builder toBuilder();

  /***********************************
   * DATA CLASS
   ***********************************/

  @AutoValue
  @JsonSerialize(as = SignalDetectionHypothesis.Data.class)
  @JsonDeserialize(builder = AutoValue_SignalDetectionHypothesis_Data.Builder.class)
  public abstract static class Data extends SignalDetectionHypothesisBase.Data {

    public abstract SignalDetectionHypothesis.Data.Builder toBuilder();

    public static SignalDetectionHypothesis.Data.Builder builder() {
      return new AutoValue_SignalDetectionHypothesis_Data.Builder();
    }

    public SignalDetectionHypothesis.Data.Builder withMeasurements(Collection<FeatureMeasurementType<?>> types) {
      return toBuilder().setFeatureMeasurements(
        ImmutableSet.copyOf(Maps.filterKeys(getFeatureMeasurementsByType(), types::contains).values()));
    }

    public SignalDetectionHypothesis.Data.Builder withoutMeasurements(Collection<FeatureMeasurementType<?>> types) {
      return toBuilder().setFeatureMeasurements(
        ImmutableSet
          .copyOf(Maps.filterKeys(getFeatureMeasurementsByType(), key -> !types.contains(key)).values()));
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

    @Memoized
    @JsonIgnore
    public ImmutableMap<FeatureMeasurementType<?>, FeatureMeasurement<?>> getFeatureMeasurementsByType() {
      return getFeatureMeasurements().stream()
        .collect(ImmutableMap.toImmutableMap(FeatureMeasurement::getFeatureMeasurementType, Function.identity()));
    }

    @AutoValue.Builder
    @JsonPOJOBuilder(withPrefix = "set")
    public interface Builder {

      SignalDetectionHypothesis.Data.Builder setMonitoringOrganization(String monitoringOrganization);

      Optional<String> getMonitoringOrganization();

      SignalDetectionHypothesis.Data.Builder setStation(Station station);

      Optional<Station> getStation();

      Optional<SignalDetectionHypothesis> getParentSignalDetectionHypothesis();

      SignalDetectionHypothesis.Data.Builder setRejected(boolean rejected);

      Optional<Boolean> isRejected();

      default SignalDetectionHypothesis.Data.Builder setParentSignalDetectionHypothesis(
        SignalDetectionHypothesis signalDetectionHypothesis) {
        setParentSignalDetectionHypothesis(Optional.ofNullable(signalDetectionHypothesis));
        return this;
      }

      public abstract SignalDetectionHypothesis.Data.Builder setParentSignalDetectionHypothesis(
        Optional<SignalDetectionHypothesis> signalDetectionHypothesisIdOptional);

      default SignalDetectionHypothesis.Data.Builder setFeatureMeasurements(
        Set<FeatureMeasurement<?>> featureMeasurements) {
        return setFeatureMeasurements(ImmutableSet.copyOf(featureMeasurements));
      }

      SignalDetectionHypothesis.Data.Builder setFeatureMeasurements(
        ImmutableSet<FeatureMeasurement<?>> featureMeasurements);

      Optional<ImmutableSet<FeatureMeasurement<?>>> getFeatureMeasurements();

      SignalDetectionHypothesis.Data autoBuild();

      default SignalDetectionHypothesis.Data build() {

        final List<Optional<?>> requiredFields = List.of(getFeatureMeasurements(), getStation(), getMonitoringOrganization());
        final long numberOfPresentFields = requiredFields.stream().filter(Optional::isPresent).count();

        if (numberOfPresentFields == 0) {
          return null;
        } else if (requiredFields.size() == numberOfPresentFields) {
          if (isRejected().isEmpty()) {
            throw new IllegalStateException("Must set rejected on signal detection hypothesis");
          }

          if (getStation().isEmpty()) {
            throw new IllegalStateException("Must set station on signal detection hypothesis");
          }

          Validate.notEmpty(getFeatureMeasurements().orElseThrow(),
            "Must set feature measurements on signal detection hypothesis");

          Validate.notEmpty(getMonitoringOrganization().orElseThrow(),
            "Must set monitoring organization on signal detection hypothesis");

          boolean hasArrival = false;
          boolean hasPhase = false;

          for (FeatureMeasurement<?> fm : getFeatureMeasurements().orElseThrow()) {
            hasArrival |= fm.getFeatureMeasurementType() == FeatureMeasurementTypes.ARRIVAL_TIME;
            hasPhase |= fm.getFeatureMeasurementType() == FeatureMeasurementTypes.PHASE;
          }

          checkArgument((hasArrival && hasPhase),
            "Feature Measurements must contain an Arrival Time and Phase");

          getFeatureMeasurements().orElseThrow()
            .forEach(measurement -> {

              // Ensure FeatureMeasurement.Channel is fully populated or version reference
              Channel measurementChannel = measurement.getChannel();
              checkState(measurementChannel.isPresent() || measurementChannel.getEffectiveAt().isPresent(),
                "SignalDetectionHypothesis FeatureMeasurement channel %s must be a version reference or fully populated",
                measurementChannel.getName());

              // Check Hypothesis.Station matches the FeatureMeasurement.Channel station name
              if (measurementChannel.isPresent()) {
                checkState(getStation().get().getName().equals(measurementChannel.getStation().getName()),
                  String.format("Cannot create SignalDetectionHypothesis with FeatureMeasurements on Channels " +
                      "from a different station (hypothesis station is %s, but channel station is %s)",
                    getStation(), measurementChannel.getStation()));
              }

              // Compare ChannelSegment.Channel and FeatureMeasurement.Channel
              checkState(measurement.getMeasuredChannelSegment().getId().getChannel().toEntityReference().equals(measurementChannel.toEntityReference()),
                String.format("Cannot create SignalDetectionHypothesis with FeatureMeasurements on ChannelSegments " +
                    "from a different channel (channel segment channel is %s, but channel is %s)",
                  measurement.getMeasuredChannelSegment().getId().getChannel(), measurementChannel));
            });

          return autoBuild();
        }

        throw new IllegalStateException(
          "If not an entity reference, station, monitoring organization, " +
            "and arrival time and phase feature measurements must be populated");
      }
    }

    public abstract Optional<SignalDetectionHypothesis> getParentSignalDetectionHypothesis();

    @Override
    SignalDetectionHypothesisReference getParentSignalDetectionHypothesisReference() {
      var parentSignalDectionHypothesis = getParentSignalDetectionHypothesis();

      if (parentSignalDectionHypothesis.isEmpty()) {
        return null;
      }

      return SignalDetectionHypothesisReference.builder()
        .setId(parentSignalDectionHypothesis.orElseThrow().getId())
        .build();
    }
  }
}
