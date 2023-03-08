package gms.shared.frameworks.osd.coi.signaldetection;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import gms.shared.frameworks.osd.coi.ParameterValidation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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
public class SignalDetection {

  private final UUID id;
  private final String monitoringOrganization;
  private final String stationName;
  private final List<SignalDetectionHypothesis> signalDetectionHypotheses;

  /**
   * Obtains an instance from SignalDetection.
   *
   * @param id The {@link UUID} id assigned to the new SignalDetection.
   * @param stationName The station detecting this signal
   * @param signalDetectionHypotheses The set of {@link SignalDetectionHypothesis} objects assigned
   * to the new SignalDetection.
   */
  private SignalDetection(UUID id, String monitoringOrganization, String stationName,
    List<SignalDetectionHypothesis> signalDetectionHypotheses) {

    this.id = id;
    this.monitoringOrganization = monitoringOrganization;
    this.stationName = stationName;

    // Making a copy of the signal detection array so it is immutable.
    this.signalDetectionHypotheses = new ArrayList<>(signalDetectionHypotheses);
  }

  /**
   * Recreation factory method (sets the SignalDetection entity identity). Handles parameter
   * validation. Used for deserialization and recreating from persistence.
   *
   * @param id The {@link UUID} id assigned to the new SignalDetection.
   * @param stationName The station detecting this signal
   * @param signalDetectionHypotheses The set of {@link SignalDetectionHypothesis} objects assigned
   * to the new SignalDetection.
   * @return QcMask representing all the input parameters.
   * @throws IllegalArgumentException if any of the parameters are null
   */
  @JsonCreator
  public static SignalDetection from(
    @JsonProperty("id") UUID id,
    @JsonProperty("monitoringOrganization") String monitoringOrganization,
    @JsonProperty("stationName") String stationName,
    @JsonProperty("signalDetectionHypotheses") List<SignalDetectionHypothesis> signalDetectionHypotheses) {
    Objects.requireNonNull(id, "Cannot create SignalDetection from a null id");
    Objects.requireNonNull(monitoringOrganization,
      "Cannot create SignalDetection from a null monitoringOrganization");
    Objects.requireNonNull(stationName, "Cannot create SignalDetection from a null stationName");
    Objects.requireNonNull(signalDetectionHypotheses,
      "Cannot create SignalDetection from null SignalDetectionHypothesis");

    var onlyContainsRejected = true;
    for (SignalDetectionHypothesis sdh : signalDetectionHypotheses) {

      ParameterValidation.requireTrue(id::equals, sdh.getParentSignalDetectionId(),
        "SignalDetectionHypothesis with parent id=" + sdh.getParentSignalDetectionId() +
          "cannot be assigned to SignalDetection with id=" + id);

      if (!sdh.isRejected()) {
        onlyContainsRejected = false;
      }
    }

    if (!signalDetectionHypotheses.isEmpty() && onlyContainsRejected) {
      throw new IllegalArgumentException("Cannot create a SignalDetection containing only rejected"
        + " SignalDetectionHypotheses");
    }

    List<UUID> hypothesisIds = signalDetectionHypotheses.stream()
      .map(SignalDetectionHypothesis::getId)
      .collect(Collectors.toList());

    long noParentCount = signalDetectionHypotheses.stream()
      .map(SignalDetectionHypothesis::getParentSignalDetectionHypothesisId)
      .filter(parentId -> !parentId.isPresent())
      .count();
    Preconditions.checkState(noParentCount <= 1,
      "SignalDetection can only contain one hypothesis without a parent hypothesis, found {}", noParentCount);

    for (var i = 0; i < signalDetectionHypotheses.size(); i++) {
      SignalDetectionHypothesis hypothesis = signalDetectionHypotheses.get(i);
      Optional<UUID> possibleParentID = hypothesis.getParentSignalDetectionHypothesisId();
      if (possibleParentID.isPresent()) {
        Preconditions.checkState(hypothesisIds.indexOf(possibleParentID.get()) >= 0,
          "Cannot create a SignalDetection with a hypothesis whose parent is not contained in the detection");
        Preconditions.checkState(hypothesisIds.indexOf(possibleParentID.get()) < i,
          "Cannot create a SignalDetection with a hypothesis whose parent was created after the child");
      }
    }

    return new SignalDetection(id, monitoringOrganization, stationName, signalDetectionHypotheses);
  }

  /**
   * Creates SignalDetection with an unrejected SignalDetectionHypothesis.
   */
  public static SignalDetection create(String monitoringOrganization, String stationName,
    List<FeatureMeasurement<?>> featureMeasurements) {

    var signalDetection = SignalDetection
      .from(UUID.randomUUID(), monitoringOrganization, stationName,
        Collections.emptyList());

    signalDetection.addSignalDetectionHypothesis(featureMeasurements);

    return signalDetection;
  }

  /**
   * Creates a new rejected {@link SignalDetectionHypothesis} for this SignalDetection by copying an
   * existing SignalDetectionHypothesis.
   *
   * @throws IllegalArgumentException if any parameters are null
   */
  public void reject(UUID signalDetectionHypothesisIdToClone) {
    Objects.requireNonNull(signalDetectionHypothesisIdToClone);

    List<SignalDetectionHypothesis> signalDetectionHypothesesToClone =
      signalDetectionHypotheses.stream()
        .filter(s -> signalDetectionHypothesisIdToClone.equals(s.getId()))
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
    signalDetectionHypotheses.add(sdh.toBuilder().setRejected(true).build());
  }

  public UUID getId() {
    return id;
  }

  public String getMonitoringOrganization() {
    return monitoringOrganization;
  }

  public String getStationName() {
    return stationName;
  }

  /**
   * Returns an unmodifiable list of signal detection hypotheses.
   */
  public List<SignalDetectionHypothesis> getSignalDetectionHypotheses() {
    return Collections.unmodifiableList(signalDetectionHypotheses);
  }

  public void addSignalDetectionHypothesis(List<FeatureMeasurement<?>> featureMeasurements) {
    Preconditions.checkState(getSignalDetectionHypotheses().isEmpty(),
      "Cannot add a signal detection hypothesis without a parent when signal detection " +
        "hypotheses is not empty");

    var signalDetectionHypothesis = SignalDetectionHypothesis.create(getId(),
      getMonitoringOrganization(),
      getStationName(),
      null,
      featureMeasurements);
    signalDetectionHypotheses.add(signalDetectionHypothesis);
  }

  /**
   * Adds an unrejected SignalDetectionHypothesis to this SignalDetection
   */
  public void addSignalDetectionHypothesis(UUID parentSignalDetectionHypothesisId,
    List<FeatureMeasurement<?>> featureMeasurements) {
    var signalDetectionHypothesis = SignalDetectionHypothesis.create(getId(),
      getMonitoringOrganization(),
      getStationName(),
      parentSignalDetectionHypothesisId,
      featureMeasurements);

    List<UUID> hypothesisIds = signalDetectionHypotheses.stream()
      .map(SignalDetectionHypothesis::getId)
      .collect(Collectors.toList());
    Preconditions.checkState(hypothesisIds.contains(parentSignalDetectionHypothesisId),
      "Cannot add SignalDetectionHypothesis with a parent hypothesis not contained by the SignalDetection");

    signalDetectionHypotheses.add(signalDetectionHypothesis);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SignalDetection that = (SignalDetection) o;
    return id.equals(that.id) &&
      monitoringOrganization.equals(that.monitoringOrganization) &&
      stationName.equals(that.stationName) &&
      signalDetectionHypotheses.equals(that.signalDetectionHypotheses);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, monitoringOrganization, stationName, signalDetectionHypotheses);
  }

  @Override
  public String toString() {
    return "SignalDetection{" +
      "id=" + id +
      ", monitoringOrganization='" + monitoringOrganization + '\'' +
      ", stationId=" + stationName +
      ", signalDetectionHypotheses=" + signalDetectionHypotheses +
      '}';
  }
}
