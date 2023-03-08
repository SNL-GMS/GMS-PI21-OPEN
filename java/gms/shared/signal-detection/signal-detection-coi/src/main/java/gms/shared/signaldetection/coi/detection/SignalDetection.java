package gms.shared.signaldetection.coi.detection;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import gms.shared.stationdefinition.coi.station.Station;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

/**
 * A Signal Detection represents the recording of the arrival of energy at a station (Figure 19).
 * Determining a consistent solution for a Signal Detection (e.g., its phase identification and
 * arrival time) is often an iterative process. A Signal Detection Hypothesis represents a proposed
 * explanation for a Signal Detection (e.g., it is a P phase at a particular time). A Signal
 * Detection can have multiple Signal Detection Hypotheses: for example, a computer algorithm could
 * create the original Signal Detection Hypothesis, assigning a phase and an arrival time; an
 * analyst reviewing that hypothesis may then choose to change the phase and/or the arrival time,
 * hence creating a new Signal Detection Hypothesis. Signal Detection has an ordered list of its
 * Signal Detection Hypothesis tracking how the Signal Detection was updated over time. Signal
 * Detection Hypothesis also includes an attribute to track Signal Detection Hypotheses that were
 * rejected during a particular processing stage (is rejected), in order to prevent their
 * re-creation in subsequent processing stages. Note that processing stage is tracked through the
 * Creation Info class attached to Signal Detection Hypothesis.
 */
@AutoValue
@JsonSerialize(as = SignalDetection.class)
@JsonDeserialize(builder = AutoValue_SignalDetection.Builder.class)
public abstract class SignalDetection {

  public abstract UUID getId();

  @JsonUnwrapped
  public abstract Optional<SignalDetection.Data> getData();

  private SignalDetection.Data getDataOrThrow() {
    return getData().orElseThrow(() -> new IllegalStateException("Only contains ID facet"));
  }

  @JsonIgnore
  public boolean isPresent() {
    return getData().isPresent();
  }

  @JsonIgnore
  public Station getStation() {
    return this.getDataOrThrow().getStation();
  }

  @JsonIgnore
  public String getMonitoringOrganization() {
    return this.getDataOrThrow().getMonitoringOrganization();
  }

  /**
   * Returns an unmodifiable list of signal detection hypotheses.
   */
  @JsonIgnore
  public List<SignalDetectionHypothesis> getSignalDetectionHypotheses() {
    return Collections.unmodifiableList(this.getDataOrThrow().getSignalDetectionHypotheses());
  }

  /**
   * Recreation factory method (sets the SignalDetection entity identity). Handles parameter
   * validation. Used for deserialization and recreating from persistence.
   *
   * @param id The {@link UUID} id assigned to the new SignalDetection.
   * @param data The data Object for faceting
   * @return SignalDetection object representing all the input parameters.
   * @throws IllegalArgumentException if any of the parameters are null
   */
  @JsonCreator
  public static SignalDetection from(
    @JsonProperty("id") UUID id,
    @JsonProperty("data") Optional<SignalDetection.Data> data) {

    data.ifPresent(dataVal -> {
      for (SignalDetectionHypothesis sdh : dataVal.getSignalDetectionHypotheses()) {
        checkState(id.equals(sdh.getId().getSignalDetectionId()),
          "SignalDetectionHypothesis with UUID=%s " +
            "cannot be assigned to SignalDetection with id=%s",
          sdh.getId().getSignalDetectionId(), id);
      }
    });

    return new AutoValue_SignalDetection.Builder()
      .setId(id)
      .setData(data)
      .build();
  }

  public static SignalDetection createEntityReference(UUID id) {
    return new AutoValue_SignalDetection.Builder()
      .setId(id)
      .build();
  }

  public SignalDetection toEntityReference() {
    return new AutoValue_SignalDetection.Builder()
      .setId(getId())
      .build();
  }

  /**
   * AutoValue Builder for the main SignalDetection class
   */
  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public interface Builder {
    SignalDetection.Builder setId(UUID id);

    @JsonUnwrapped
    default SignalDetection.Builder setData(SignalDetection.Data data) {
      return setData(Optional.ofNullable(data));
    }

    SignalDetection.Builder setData(Optional<SignalDetection.Data> data);

    SignalDetection autobuild();

    default SignalDetection build() {
      return autobuild();
    }
  }

  public static Builder builder() {
    return new AutoValue_SignalDetection.Builder();
  }

  public abstract Builder toBuilder();

  /*********************************
   * DATA OBJECT
   *********************************/

  @AutoValue
  @JsonSerialize(as = SignalDetection.Data.class)
  @JsonDeserialize(builder = AutoValue_SignalDetection_Data.Builder.class)
  public abstract static class Data {

    public abstract SignalDetection.Data.Builder toBuilder();

    public static SignalDetection.Data.Builder builder() {
      return new AutoValue_SignalDetection_Data.Builder();
    }

    public abstract Station getStation();

    public abstract String getMonitoringOrganization();

    public abstract ImmutableList<SignalDetectionHypothesis> getSignalDetectionHypotheses();

    @AutoValue.Builder
    @JsonPOJOBuilder(withPrefix = "set")
    public abstract static class Builder {

      public abstract SignalDetection.Data.Builder setMonitoringOrganization(String monitoringOrganization);

      abstract Optional<String> getMonitoringOrganization();

      public abstract SignalDetection.Data.Builder setStation(Station station);

      abstract Optional<Station> getStation();

      abstract ImmutableList.Builder<SignalDetectionHypothesis> signalDetectionHypothesesBuilder();

      abstract SignalDetection.Data.Builder setSignalDetectionHypotheses(
        ImmutableList<SignalDetectionHypothesis> signalDetectionHypotheses);

      public SignalDetection.Data.Builder setSignalDetectionHypotheses(
        Collection<SignalDetectionHypothesis> signalDetectionHypotheses) {
        return setSignalDetectionHypotheses(ImmutableList.copyOf(signalDetectionHypotheses));
      }

      public SignalDetection.Data.Builder addSignalDetectionHypothesis(
        SignalDetectionHypothesis signalDetectionHypothesis) {
        Objects.requireNonNull(signalDetectionHypothesis, "Cannot add null signal detection hypothesis");
        signalDetectionHypothesesBuilder().add(signalDetectionHypothesis);
        return this;
      }

      abstract Optional<ImmutableList<SignalDetectionHypothesis>> getSignalDetectionHypotheses();

      protected abstract SignalDetection.Data autobuild();

      public SignalDetection.Data build() {
        List<Optional<?>> allFields = List.of(getMonitoringOrganization(), getSignalDetectionHypotheses(), getStation());
        var numPresentFields = allFields.stream()
          .filter(Optional::isPresent)
          .count();
        if (numPresentFields == 0) {
          return null;
        } else if (numPresentFields == allFields.size()) {
          var data = autobuild();

          boolean onlyContainsRejected = checkForRejectedOnly(data);


          checkArgument((data.getSignalDetectionHypotheses().isEmpty() || !onlyContainsRejected),
            "Cannot create a SignalDetection containing only rejected " +
              "SignalDetectionHypotheses");

          long numHypothesesPopulated = data.getSignalDetectionHypotheses().stream()
            .filter(SignalDetectionHypothesis::isPresent)
            .count();

          // this ensures that we only check parents if there is data present in the hypotheses
          if (numHypothesesPopulated == data.getSignalDetectionHypotheses().size()) {
            List<UUID> hypothesisIds = data.getSignalDetectionHypotheses().stream()
              .map(SignalDetectionHypothesis::getId)
              .map(SignalDetectionHypothesisId::getId)
              .collect(Collectors.toList());

            long noParentCount = data.getSignalDetectionHypotheses().stream()
              .map(SignalDetectionHypothesis::getParentSignalDetectionHypothesisId)
              .filter(Optional::isEmpty)
              .count();
            checkState(noParentCount <= 1,
              "SignalDetection can only contain one hypothesis without a parent hypothesis, found %d", noParentCount);

            for (var i = 0; i < data.getSignalDetectionHypotheses().size(); i++) {
              SignalDetectionHypothesis hypothesis = data.getSignalDetectionHypotheses().get(i);
              Optional<SignalDetectionHypothesisId> possibleParentID = hypothesis.getParentSignalDetectionHypothesisId();
              if (possibleParentID.isPresent()) {

                checkState(hypothesisIds.indexOf(possibleParentID.get().getId()) < i,
                  "Cannot create a SignalDetection with a hypothesis whose parent was created after the child");
              }
            }
          }

          return data;
        } else {
          throw new IllegalStateException("Either all FacetedDataClass fields must be populated or none of them can be populated");
        }
      }

      /**
       * This method checks to see if the SignalDetectionHypotheses are all rejected, and returns true, if so.
       * @param data
       * @return true if all are rejected, false otherwise.
       */
      private static boolean checkForRejectedOnly(Data data) {
        for (SignalDetectionHypothesis sdh : data.getSignalDetectionHypotheses()) {

          Optional<SignalDetectionHypothesis.Data> sdhData = sdh.getData();
          if (sdh.getData().isPresent()) {
            if (sdhData.isPresent() && !sdhData.get().isRejected()) {
              return false;
            }
          } else {
            return false;
          }
        }
        return true;
      }
    }

  }

  /**
   * Creates a new rejected {@link SignalDetectionHypothesis} for this SignalDetection by copying an
   * existing SignalDetectionHypothesis.
   *
   * @throws IllegalArgumentException if any parameters are null
   */
  public SignalDetection reject(UUID signalDetectionHypothesisIdToClone) {

    Objects.requireNonNull(signalDetectionHypothesisIdToClone);

    List<SignalDetectionHypothesis> signalDetectionHypothesesToClone =
      getDataOrThrow().getSignalDetectionHypotheses().stream()
        .filter(hypothesis -> signalDetectionHypothesisIdToClone.equals(hypothesis.getId().getId()))
        .collect(Collectors.toList());

    if (signalDetectionHypothesesToClone.isEmpty()) {
      throw new IllegalArgumentException(
        "No SignalDetectionHypothesis exists for id=" + signalDetectionHypothesesToClone);
    }
    if (signalDetectionHypothesesToClone.size() != 1) {
      throw new IllegalArgumentException(
        signalDetectionHypothesesToClone.size() + " SignalDetectionHypotheses exist for id="
          + signalDetectionHypothesesToClone);
    }

    SignalDetectionHypothesis sdh = signalDetectionHypothesesToClone.get(0);
    var sdhData = sdh.getData().orElseThrow().toBuilder().setRejected(true)
      .setParentSignalDetectionHypothesis(Optional.ofNullable(sdh)).build();
    var sdh2 = SignalDetectionHypothesis.from(
      SignalDetectionHypothesisId.from(sdh.getId().getSignalDetectionId(), UUID.randomUUID()),
      Optional.of(sdhData));

    var builder = getDataOrThrow().toBuilder();
    List<SignalDetectionHypothesis> originalHypotheses = new ArrayList<>(getDataOrThrow().getSignalDetectionHypotheses());
    originalHypotheses.add(sdh2);
    var data = builder.setSignalDetectionHypotheses(originalHypotheses).build();
    return toBuilder().setData(data).build();
  }

}
