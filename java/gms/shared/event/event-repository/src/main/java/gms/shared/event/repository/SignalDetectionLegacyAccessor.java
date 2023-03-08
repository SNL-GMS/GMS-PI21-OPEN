package gms.shared.event.repository;

import gms.shared.signaldetection.api.SignalDetectionAccessorInterface;
import gms.shared.signaldetection.coi.detection.SignalDetectionHypothesis;
import gms.shared.signaldetection.coi.detection.SignalDetectionHypothesisId;
import gms.shared.signaldetection.repository.utils.SignalDetectionHypothesisAssocIdComponents;
import gms.shared.signaldetection.repository.utils.SignalDetectionIdUtility;
import gms.shared.workflow.coi.WorkflowDefinitionId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * Provides convenience methods for retrieving {@link SignalDetectionHypothesis} COI objects by their legacy identifiers
 */
@Component
public class SignalDetectionLegacyAccessor {

  private static final Logger logger = LoggerFactory.getLogger(SignalDetectionLegacyAccessor.class);

  private final SignalDetectionIdUtility signalDetectionIdUtility;
  private final SignalDetectionAccessorInterface signalDetectionAccessor;

  /**
   * @param signalDetectionIdUtility Converts legacy identifiers to COI identifiers
   * @param signalDetectionAccessor Retrieves {@link SignalDetectionHypothesis} COI objects
   */
  @Autowired
  public SignalDetectionLegacyAccessor(
    SignalDetectionIdUtility signalDetectionIdUtility,
    @Qualifier("requestCachingSignalDetectionAccessor") SignalDetectionAccessorInterface signalDetectionAccessor) {

    this.signalDetectionIdUtility = requireNonNull(signalDetectionIdUtility);
    this.signalDetectionAccessor = requireNonNull(signalDetectionAccessor);
  }

  /**
   * Retrieves a single {@link SignalDetectionHypothesis} for the provided stageId and arid
   *
   * @param stageId {@link WorkflowDefinitionId} containing the SignalDetectionHypothesis
   * @param arid arid of the SignalDetectionHypothesis
   * @return SignalDetectionHypothesis
   * @throws IllegalStateException if multiple SignalDetectionHypotheses were found
   */
  public Optional<SignalDetectionHypothesis> findHypothesisByStageIdAridAndOrid(WorkflowDefinitionId stageId, long arid,
    long orid) {

    requireNonNull(stageId);

    var legacyStageName = stageIdToLegacyDatabaseAccount(stageId);
    var signalDetectionHypothesisUuid = signalDetectionIdUtility.getOrCreateSignalDetectionHypothesisIdFromAridOridAndStageId(arid, orid, legacyStageName);
    var signalDetectionUuid = signalDetectionIdUtility.getOrCreateSignalDetectionIdfromArid(arid);
    var signalDetectionHypothesisId = SignalDetectionHypothesisId.from(signalDetectionUuid, signalDetectionHypothesisUuid);

    var signalDetectionHypotheses = new ArrayList<SignalDetectionHypothesis>();
    try {
      signalDetectionHypotheses.addAll(
        signalDetectionAccessor.findHypothesesByIds(List.of(signalDetectionHypothesisId)).stream()
          .filter(sdh -> signalDetectionHypothesisId.equals(sdh.getId()))
          .collect(Collectors.toList())
      );
    } catch (IllegalStateException e) {
      logger.warn("Unable to retrieve requested SignalDetectionHypothesis with orid[{}], arid[{}], stage[{}] caused by {}", orid, arid, stageId.getName(), e);
    }

    if (signalDetectionHypotheses.size() == 1) {
      return Optional.of(signalDetectionHypotheses.get(0));
    } else if (signalDetectionHypotheses.isEmpty()) {
      return Optional.empty();
    } else {
      throw new IllegalStateException(String.format("Expected 1 or 0 SignalDetectionHypothesis for [stage, legacyStage, arid, orid] [%s, %s, %d, %d], but got %d",
        stageId.getName(), legacyStageName, arid, orid, signalDetectionHypotheses.size()));
    }
  }

  /**
   * Retrieves the {@link SignalDetectionHypothesisAssocIdComponents} from the {@link SignalDetectionIdUtility} IgniteCache,
   * keyed by the provided {@link SignalDetectionHypothesis} ids
   *
   * @param sdHypotheses A collection of SignalDetectionHypotheses
   * @return A set of AssocIdComponents associated with the provided SignalDetectionHypotheses
   */
  public Set<SignalDetectionHypothesisAssocIdComponents> getSignalDetectionHypothesesAssocIdComponents(
    Collection<SignalDetectionHypothesis> sdHypotheses) {
    return sdHypotheses.stream()
      .map(SignalDetectionHypothesis::getId)
      .map(SignalDetectionHypothesisId::getId)
      .map(signalDetectionIdUtility::getAssocIdComponentsFromSignalDetectionHypothesisId)
      .filter(Objects::nonNull)
      .collect(Collectors.toSet());
  }

  private String stageIdToLegacyDatabaseAccount(WorkflowDefinitionId stageId) {
    if ("Auto Network".equals(stageId.getName()))
      return "soccpro";
    else if ("AL1".equals(stageId.getName()))
      return "al1";
    else if ("AL2".equals(stageId.getName()))
      return "al2";
    else
      throw new IllegalArgumentException(String.format("stageId [%s] could not be converted to a legacy database account", stageId.getName()));
  }

  public static WorkflowDefinitionId legacyDatabaseAccountToStageId(String legacyDatabaseAccount) {
    switch (legacyDatabaseAccount) {
      case "soccpro":
        return WorkflowDefinitionId.from("Auto Network");
      case "al1":
        return WorkflowDefinitionId.from("AL1");
      case "al2":
        return WorkflowDefinitionId.from("AL2");
      default:
        throw new IllegalArgumentException(String.format("legacyDatabaseAccount [%s] could not be converted to a stageId", legacyDatabaseAccount));
    }
  }
}