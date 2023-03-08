package gms.shared.signaldetection.converter.detection;

import gms.shared.signaldetection.coi.detection.SignalDetection;
import gms.shared.signaldetection.dao.css.ArrivalDao;
import gms.shared.signaldetection.dao.css.AssocDao;
import gms.shared.signaldetection.repository.utils.SignalDetectionComponents;
import gms.shared.signaldetection.repository.utils.SignalDetectionIdUtility;
import gms.shared.signaldetection.testfixtures.SignalDetectionDaoTestFixtures;
import gms.shared.workflow.coi.WorkflowDefinitionId;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static gms.shared.signaldetection.testfixtures.SignalDetectionDaoTestFixtures.AMPLITUDE_DAO_1;
import static gms.shared.signaldetection.testfixtures.SignalDetectionDaoTestFixtures.ARRIVAL_1;
import static gms.shared.signaldetection.testfixtures.SignalDetectionDaoTestFixtures.STAGE_1;
import static gms.shared.signaldetection.testfixtures.SignalDetectionDaoTestFixtures.STAGE_2;
import static gms.shared.signaldetection.testfixtures.SignalDetectionDaoTestFixtures.STAGE_3;
import static gms.shared.signaldetection.testfixtures.SignalDetectionTestFixtures.DETECTION_FROM_ARRIVAL;
import static gms.shared.signaldetection.testfixtures.SignalDetectionTestFixtures.DETECTION_FROM_ARRIVAL_NO_HYPOTHESES;
import static gms.shared.signaldetection.testfixtures.SignalDetectionTestFixtures.DETECTION_FROM_BOTH_ARRIVALS;
import static gms.shared.signaldetection.testfixtures.SignalDetectionTestFixtures.DETECTION_FROM_PREVIOUS_STAGE;
import static gms.shared.signaldetection.testfixtures.SignalDetectionTestFixtures.HYPOTHESIS_FROM_ARRIVAL_1;
import static gms.shared.signaldetection.testfixtures.SignalDetectionTestFixtures.HYPOTHESIS_FROM_ASSOC_1;
import static gms.shared.signaldetection.testfixtures.SignalDetectionTestFixtures.HYPOTHESIS_FROM_ASSOC_2;
import static gms.shared.signaldetection.testfixtures.SignalDetectionTestFixtures.MONITORING_ORG;
import static gms.shared.stationdefinition.testfixtures.UtilsTestFixtures.STATION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SignalDetectionConverterTest {

  private static final WorkflowDefinitionId stage1Id = WorkflowDefinitionId.from(STAGE_1);
  private static final WorkflowDefinitionId stage2Id = WorkflowDefinitionId.from(STAGE_2);
  private static final WorkflowDefinitionId stage3Id = WorkflowDefinitionId.from(STAGE_3);
  private static final List<WorkflowDefinitionId> orderedStages = List.of(stage1Id, stage2Id);
  private static final Map<WorkflowDefinitionId, String> dbAccountStageMap = Map.of(stage1Id, stage1Id.getName(),
    stage2Id, stage2Id.getName());

  @Mock
  private SignalDetectionHypothesisConverterInterface signalDetectionHypothesisConverter;

  @Mock
  private SignalDetectionIdUtility signalDetectionIdUtility;

  private SignalDetectionConverter converter;

  @BeforeEach
  void setup() {
    converter = SignalDetectionConverter.create(signalDetectionHypothesisConverter,
      signalDetectionIdUtility,
      orderedStages,
      dbAccountStageMap);
  }

  @ParameterizedTest
  @MethodSource("getCreateArguments")
  void testCreateValidation(SignalDetectionHypothesisConverterInterface signalDetectionHypothesisConverter,
    SignalDetectionIdUtility signalDetectionIdUtility,
    List<WorkflowDefinitionId> orderedStages) {
    assertThrows(NullPointerException.class, () -> SignalDetectionConverter.create(signalDetectionHypothesisConverter,
      signalDetectionIdUtility,
      orderedStages,
      dbAccountStageMap));
  }

  static Stream<Arguments> getCreateArguments() {
    return Stream.of(
      arguments(null,
        mock(SignalDetectionIdUtility.class),
        orderedStages),
      arguments(mock(SignalDetectionHypothesisConverterInterface.class),
        null,
        orderedStages),
      arguments(mock(SignalDetectionHypothesisConverterInterface.class),
        mock(SignalDetectionIdUtility.class),
        null));
  }

  @Test
  void testCreate() {
    SignalDetectionConverter converter =
      Assertions.assertDoesNotThrow(() -> SignalDetectionConverter.create(signalDetectionHypothesisConverter,
        signalDetectionIdUtility,
        orderedStages,
        dbAccountStageMap));
    assertNotNull(converter);
  }

  @ParameterizedTest
  @MethodSource("getConvertValidationArguments")
  void testConvertValidation(Class<? extends Exception> expectedException,
    SignalDetectionComponents signalDetectionComponents) {

    assertThrows(expectedException,
      () -> converter.convert(signalDetectionComponents));
  }

  static Stream<Arguments> getConvertValidationArguments() {
    return Stream.of(
      arguments(NullPointerException.class,
        null));
  }

  @Test
  void testConverter_emptyStageArrivals() {

    when(signalDetectionIdUtility.getOrCreateSignalDetectionIdfromArid(ARRIVAL_1.getId())).thenReturn(DETECTION_FROM_ARRIVAL_NO_HYPOTHESES.getId());

    SignalDetectionComponents signalDetectionComponents =
      SignalDetectionComponents.builder()
        .setCurrentStage(stage3Id)
        .setPreviousStage(Optional.empty())
        .setCurrentArrival(ARRIVAL_1)
        .setPreviousArrival(Optional.empty())
        .setCurrentAssocs(List.of())
        .setPreviousAssocs(List.of())
        .setAmplitudeDaos(List.of(AMPLITUDE_DAO_1))
        .setStation(STATION)
        .setMonitoringOrganization(MONITORING_ORG)
        .build();

    Optional<SignalDetection> actual = converter.convert(signalDetectionComponents);

    assertTrue(actual.isPresent());
    assertEquals(DETECTION_FROM_ARRIVAL_NO_HYPOTHESES, actual.get());
  }

  @ParameterizedTest
  @MethodSource("getConvertArguments")
  void testConvert(SignalDetection expected,
    Consumer<SignalDetectionHypothesisConverterInterface> setupMocks,
    Consumer<SignalDetectionHypothesisConverterInterface> verifyMocks,
    Consumer<SignalDetectionIdUtility> sdiuMocks,
    SignalDetectionComponents signalDetectionComponents) {

    setupMocks.accept(signalDetectionHypothesisConverter);
    sdiuMocks.accept(signalDetectionIdUtility);

    Optional<SignalDetection> actual = converter.convert(signalDetectionComponents);

    assertTrue(actual.isPresent());
    assertEquals(expected, actual.get());

    verifyMocks.accept(signalDetectionHypothesisConverter);
  }

  static Stream<Arguments> getConvertArguments() {

    long arid = 32;
    long orid = 23;
    Pair<ArrivalDao, AssocDao> arrivalAssocPair = SignalDetectionDaoTestFixtures.getArrivalAssocPair(arid, orid);

    Consumer<SignalDetectionHypothesisConverterInterface> singleArrivalSetup = sdhConverter ->
      when(sdhConverter.convertToEntityReference(stage1Id.getName(), DETECTION_FROM_ARRIVAL.getId(), ARRIVAL_1, Optional.empty()))
        .thenReturn(Optional.ofNullable(HYPOTHESIS_FROM_ARRIVAL_1));

    Consumer<SignalDetectionHypothesisConverterInterface> singleArrivalVerification = sdhConverter -> {
      verify(sdhConverter).convertToEntityReference(stage1Id.getName(), DETECTION_FROM_ARRIVAL.getId(), ARRIVAL_1, Optional.empty());
      verifyNoMoreInteractions(sdhConverter);
    };

    Consumer<SignalDetectionIdUtility> idUtilityConsumer1 = idUtility ->
      when(idUtility.getOrCreateSignalDetectionIdfromArid(ARRIVAL_1.getId()))
        .thenReturn(DETECTION_FROM_ARRIVAL.getId());

    Consumer<SignalDetectionHypothesisConverterInterface> twoArrivalSetup = sdhConverter -> {
      when(sdhConverter.convertToEntityReference(stage1Id.getName(), DETECTION_FROM_BOTH_ARRIVALS.getId(), ARRIVAL_1, Optional.empty()))
        .thenReturn(Optional.ofNullable(HYPOTHESIS_FROM_ARRIVAL_1));
    };

    Consumer<SignalDetectionHypothesisConverterInterface> twoArrivalVerification = sdhConverter -> {
      verify(sdhConverter).convertToEntityReference(stage1Id.getName(), DETECTION_FROM_BOTH_ARRIVALS.getId(), ARRIVAL_1, Optional.empty());
      verifyNoMoreInteractions(sdhConverter);
    };

    Consumer<SignalDetectionIdUtility> idUtilityConsumer2 = idUtility ->
      when(idUtility.getOrCreateSignalDetectionIdfromArid(ARRIVAL_1.getId()))
        .thenReturn(DETECTION_FROM_BOTH_ARRIVALS.getId());

    Consumer<SignalDetectionHypothesisConverterInterface> previousArrivalSetup = sdhConverter -> {
      when(sdhConverter.convertToEntityReference(stage1Id.getName(), DETECTION_FROM_PREVIOUS_STAGE.getId(), arrivalAssocPair.getLeft(),
        Optional.of(arrivalAssocPair.getRight())))
        .thenReturn(Optional.ofNullable(HYPOTHESIS_FROM_ASSOC_1));

      when(sdhConverter.convertToEntityReference(stage2Id.getName(), DETECTION_FROM_PREVIOUS_STAGE.getId(), arrivalAssocPair.getLeft(),
        Optional.of(arrivalAssocPair.getRight())))
        .thenReturn(Optional.ofNullable(HYPOTHESIS_FROM_ASSOC_2));

      when(sdhConverter.convertToEntityReference(stage1Id.getName(), DETECTION_FROM_PREVIOUS_STAGE.getId(), arrivalAssocPair.getLeft(),
        Optional.empty()))
        .thenReturn(Optional.ofNullable(HYPOTHESIS_FROM_ARRIVAL_1));
    };

    Consumer<SignalDetectionHypothesisConverterInterface> previousVerification = sdhConverter -> {
      verify(sdhConverter).convertToEntityReference(stage1Id.getName(), DETECTION_FROM_PREVIOUS_STAGE.getId(), arrivalAssocPair.getLeft(),
        Optional.of(arrivalAssocPair.getRight()));

      verify(sdhConverter).convertToEntityReference(stage2Id.getName(), DETECTION_FROM_PREVIOUS_STAGE.getId(), arrivalAssocPair.getLeft(),
        Optional.of(arrivalAssocPair.getRight()));

      verify(sdhConverter).convertToEntityReference(stage1Id.getName(), DETECTION_FROM_PREVIOUS_STAGE.getId(), arrivalAssocPair.getLeft(),
        Optional.empty());

      verifyNoMoreInteractions(sdhConverter);
    };

    Consumer<SignalDetectionIdUtility> idUtilityConsumer3 = idUtility ->
      when(idUtility.getOrCreateSignalDetectionIdfromArid(arrivalAssocPair.getLeft().getId()))
        .thenReturn(DETECTION_FROM_PREVIOUS_STAGE.getId());

    return Stream.of(
      arguments(DETECTION_FROM_ARRIVAL,
        singleArrivalSetup,
        singleArrivalVerification,
        idUtilityConsumer1,
        SignalDetectionComponents.builder()
          .setCurrentStage(stage1Id)
          .setPreviousStage(Optional.empty())
          .setCurrentArrival(ARRIVAL_1)
          .setPreviousArrival(Optional.empty())
          .setCurrentAssocs(List.of())
          .setPreviousAssocs(List.of())
          .setAmplitudeDaos(List.of(AMPLITUDE_DAO_1))
          .setStation(STATION)
          .setMonitoringOrganization(MONITORING_ORG)
          .build()),
      arguments(DETECTION_FROM_ARRIVAL,
        twoArrivalSetup,
        twoArrivalVerification,
        idUtilityConsumer2,
        SignalDetectionComponents.builder()
          .setCurrentStage(stage1Id)
          .setPreviousStage(Optional.empty())
          .setCurrentArrival(ARRIVAL_1)
          .setPreviousArrival(Optional.of(ARRIVAL_1))
          .setCurrentAssocs(List.of())
          .setPreviousAssocs(List.of())
          .setAmplitudeDaos(List.of(AMPLITUDE_DAO_1))
          .setStation(STATION)
          .setMonitoringOrganization(MONITORING_ORG)
          .build()),
      arguments(DETECTION_FROM_PREVIOUS_STAGE,
        previousArrivalSetup,
        previousVerification,
        idUtilityConsumer3,
        SignalDetectionComponents.builder()
          .setCurrentStage(stage2Id)
          .setPreviousStage(Optional.of(stage1Id))
          .setCurrentArrival(arrivalAssocPair.getLeft())
          .setPreviousArrival(Optional.of(arrivalAssocPair.getLeft()))
          .setCurrentAssocs(List.of(arrivalAssocPair.getRight()))
          .setPreviousAssocs(List.of(arrivalAssocPair.getRight()))
          .setAmplitudeDaos(List.of(AMPLITUDE_DAO_1))
          .setStation(STATION)
          .setMonitoringOrganization(MONITORING_ORG)
          .build())

    );
  }
}