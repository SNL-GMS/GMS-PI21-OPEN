package gms.shared.signaldetection.converter.detection;

import gms.shared.signaldetection.coi.detection.SignalDetectionHypothesis;
import gms.shared.signaldetection.coi.detection.SignalDetectionHypothesisConverterId;
import gms.shared.signaldetection.coi.types.FeatureMeasurementTypes;
import gms.shared.signaldetection.dao.css.AmplitudeDao;
import gms.shared.signaldetection.dao.css.ArrivalDao;
import gms.shared.signaldetection.dao.css.AssocDao;
import gms.shared.signaldetection.repository.utils.SignalDetectionIdUtility;
import gms.shared.signaldetection.testfixtures.SignalDetectionDaoTestFixtures;
import gms.shared.stationdefinition.coi.channel.Channel;
import gms.shared.stationdefinition.coi.station.Station;
import gms.shared.stationdefinition.coi.utils.DoubleValue;
import gms.shared.stationdefinition.coi.utils.Units;
import gms.shared.waveform.coi.ChannelSegment;
import gms.shared.waveform.coi.Timeseries;
import gms.shared.workflow.coi.WorkflowDefinitionId;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static gms.shared.signaldetection.repository.utils.SignalDetectionAccessorTestFixtures.ARRIVAL_MEASUREMENT_SPEC;
import static gms.shared.signaldetection.repository.utils.SignalDetectionAccessorTestFixtures.PHASE_MEASUREMENT_SPEC;
import static gms.shared.signaldetection.testfixtures.SignalDetectionDaoTestFixtures.AMPLITUDE_DAO_1;
import static gms.shared.signaldetection.testfixtures.SignalDetectionDaoTestFixtures.ARRIVAL_1;
import static gms.shared.signaldetection.testfixtures.SignalDetectionDaoTestFixtures.ASSOC_DAO_1;
import static gms.shared.signaldetection.testfixtures.SignalDetectionDaoTestFixtures.STAGE_1;
import static gms.shared.signaldetection.testfixtures.SignalDetectionTestFixtures.ARRIVAL_CHANNEL;
import static gms.shared.signaldetection.testfixtures.SignalDetectionTestFixtures.ARRIVAL_CHANNEL_SEGMENT;
import static gms.shared.signaldetection.testfixtures.SignalDetectionTestFixtures.ARRIVAL_TIME_FEATURE_MEASUREMENT;
import static gms.shared.signaldetection.testfixtures.SignalDetectionTestFixtures.MONITORING_ORG;
import static gms.shared.signaldetection.testfixtures.SignalDetectionTestFixtures.PHASE_FEATURE_MEASUREMENT;
import static gms.shared.signaldetection.testfixtures.SignalDetectionTestFixtures.SIGNAL_DETECTION_HYPOTHESIS_0;
import static gms.shared.signaldetection.testfixtures.SignalDetectionTestFixtures.SIGNAL_DETECTION_HYPOTHESIS_ENTITY_REFERENCE;
import static gms.shared.signaldetection.testfixtures.SignalDetectionTestFixtures.converterId;
import static gms.shared.stationdefinition.testfixtures.UtilsTestFixtures.STATION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SignalDetectionHypothesisConverterTest {

  private static final WorkflowDefinitionId stage1Id = WorkflowDefinitionId.from(STAGE_1);

  @Mock
  private FeatureMeasurementConverterInterface featureMeasurementConverter;

  @Mock
  private SignalDetectionIdUtility signalDetectionIdUtility;

  private SignalDetectionHypothesisConverter converter;

  @BeforeEach
  void setup() {
    converter = new SignalDetectionHypothesisConverter(featureMeasurementConverter, signalDetectionIdUtility);
  }

  @ParameterizedTest
  @MethodSource("getConvertToEntityReferenceValidationArguments")
  void testConvertToEntityReferenceValidation(Class<? extends Exception> expectedException,
    WorkflowDefinitionId stageId,
    UUID detectionId,
    ArrivalDao arrivalDao,
    Optional<AssocDao> assocDao) {

    assertThrows(expectedException,
      () -> converter.convertToEntityReference(stageId.getName(), detectionId, arrivalDao, assocDao));
  }


  @ParameterizedTest
  @MethodSource("getConvertValidationArguments")
  void testConvertValidation(Class<? extends Exception> expectedException,
    SignalDetectionHypothesisConverterId converterId,
    ArrivalDao arrivalDao,
    Optional<AssocDao> assocDao,
    Collection<AmplitudeDao> amplitudeDaos,
    String monitoringOrganization,
    Station station,
    Channel channel,
    ChannelSegment<? extends Timeseries> channelSegment) {

    assertThrows(expectedException,
      () -> converter.convert(converterId, arrivalDao, assocDao, amplitudeDaos,
        monitoringOrganization, station, channel, channelSegment));
  }

  static Stream<Arguments> getConvertToEntityReferenceValidationArguments() {
    return Stream.of(
      arguments(NullPointerException.class,
        null,
        UUID.randomUUID(),
        ARRIVAL_1,
        Optional.empty()),
      arguments(NullPointerException.class,
        stage1Id,
        null,
        ARRIVAL_1,
        Optional.empty()),
      arguments(NullPointerException.class,
        stage1Id,
        UUID.randomUUID(),
        null,
        Optional.empty()),
      arguments(IllegalStateException.class,
        stage1Id,
        UUID.randomUUID(),
        ARRIVAL_1,
        Optional.of(ASSOC_DAO_1))
    );
  }

  static Stream<Arguments> getConvertValidationArguments() {
    return Stream.of(
      arguments(NullPointerException.class,
        null,
        ARRIVAL_1,
        Optional.of(ASSOC_DAO_1),
        List.of(AMPLITUDE_DAO_1),
        MONITORING_ORG,
        STATION,
        ARRIVAL_CHANNEL,
        ARRIVAL_CHANNEL_SEGMENT),
      arguments(NullPointerException.class,
        converterId,
        null,
        Optional.of(ASSOC_DAO_1),
        List.of(AMPLITUDE_DAO_1),
        MONITORING_ORG,
        STATION,
        ARRIVAL_CHANNEL,
        ARRIVAL_CHANNEL_SEGMENT),
      arguments(NullPointerException.class,
        converterId,
        ARRIVAL_1,
        null,
        List.of(AMPLITUDE_DAO_1),
        MONITORING_ORG,
        STATION,
        ARRIVAL_CHANNEL,
        ARRIVAL_CHANNEL_SEGMENT),
      arguments(NullPointerException.class,
        converterId,
        ARRIVAL_1,
        Optional.of(ASSOC_DAO_1),
        null,
        MONITORING_ORG,
        STATION,
        ARRIVAL_CHANNEL,
        ARRIVAL_CHANNEL_SEGMENT),
      arguments(IllegalStateException.class,
        converterId,
        ARRIVAL_1,
        Optional.of(ASSOC_DAO_1),
        List.of(AMPLITUDE_DAO_1),
        null,
        STATION,
        ARRIVAL_CHANNEL,
        ARRIVAL_CHANNEL_SEGMENT),
      arguments(NullPointerException.class,
        converterId,
        ARRIVAL_1,
        Optional.of(ASSOC_DAO_1),
        List.of(AMPLITUDE_DAO_1),
        MONITORING_ORG,
        null,
        ARRIVAL_CHANNEL,
        ARRIVAL_CHANNEL_SEGMENT),
      arguments(NullPointerException.class,
        converterId,
        ARRIVAL_1,
        Optional.of(ASSOC_DAO_1),
        List.of(AMPLITUDE_DAO_1),
        MONITORING_ORG,
        STATION,
        null,
        ARRIVAL_CHANNEL_SEGMENT),
      arguments(NullPointerException.class,
        converterId,
        ARRIVAL_1,
        Optional.of(ASSOC_DAO_1),
        List.of(AMPLITUDE_DAO_1),
        MONITORING_ORG,
        STATION,
        ARRIVAL_CHANNEL,
        null));
  }

  @ParameterizedTest
  @MethodSource("getConvertReferenceArguments")
  void testConvertToEntityReferenceArguments(SignalDetectionHypothesis expected,
    Consumer<SignalDetectionIdUtility> sdiuMock,
    WorkflowDefinitionId stageId,
    UUID detectionId,
    ArrivalDao arrivalDao,
    Optional<AssocDao> assocDao) {

    sdiuMock.accept(signalDetectionIdUtility);

    Optional<SignalDetectionHypothesis> actual = converter.convertToEntityReference(stageId.getName(), detectionId, arrivalDao, assocDao);

    assertTrue(actual.isPresent());
    assertEquals(SIGNAL_DETECTION_HYPOTHESIS_ENTITY_REFERENCE, actual.get());
  }

  static Stream<Arguments> getConvertReferenceArguments() {

    long arid = 234;
    long orid = 932;
    Pair<ArrivalDao, AssocDao> arrivalAssocPair = SignalDetectionDaoTestFixtures.getArrivalAssocPair(arid, orid);

    Consumer<SignalDetectionIdUtility> signalDetectionIdUtilityConsumer1 = sdiu -> {
      when(sdiu.getOrCreateSignalDetectionHypothesisIdFromAridAndStageId(ARRIVAL_1.getId(), stage1Id.getName()))
        .thenReturn(SIGNAL_DETECTION_HYPOTHESIS_ENTITY_REFERENCE.getId().getId());
    };

    Consumer<SignalDetectionIdUtility> signalDetectionIdUtilityConsumer2 = sdiu -> {
      when(sdiu.getOrCreateSignalDetectionHypothesisIdFromAridOridAndStageId(arid, orid, stage1Id.getName()))
        .thenReturn(SIGNAL_DETECTION_HYPOTHESIS_ENTITY_REFERENCE.getId().getId());
    };

    return Stream.of(
      arguments(SIGNAL_DETECTION_HYPOTHESIS_ENTITY_REFERENCE,
        signalDetectionIdUtilityConsumer1,
        stage1Id,
        SIGNAL_DETECTION_HYPOTHESIS_ENTITY_REFERENCE.getId().getSignalDetectionId(),
        ARRIVAL_1,
        Optional.empty()),
      arguments(SIGNAL_DETECTION_HYPOTHESIS_ENTITY_REFERENCE,
        signalDetectionIdUtilityConsumer2,
        stage1Id,
        SIGNAL_DETECTION_HYPOTHESIS_ENTITY_REFERENCE.getId().getSignalDetectionId(),
        arrivalAssocPair.getLeft(),
        Optional.of(arrivalAssocPair.getRight())));
  }

  @ParameterizedTest
  @MethodSource("getConvertArguments")
  void testConvert(SignalDetectionHypothesis expected,
    List<Consumer<FeatureMeasurementConverterInterface>> setupMocks,
    Consumer<SignalDetectionIdUtility> sdiuMock,
    SignalDetectionHypothesisConverterId converterId,
    ArrivalDao arrivalDao,
    Optional<AssocDao> assocDao,
    List<AmplitudeDao> amplitudeDaos,
    String monitoringOrganization,
    Station station,
    Channel channel,
    ChannelSegment<? extends Timeseries> channelSegment) {

    setupMocks.forEach(mock -> mock.accept(featureMeasurementConverter));

    sdiuMock.accept(signalDetectionIdUtility);

    Optional<SignalDetectionHypothesis> actual = converter.convert(converterId,
      arrivalDao,
      assocDao,
      amplitudeDaos,
      monitoringOrganization,
      station,
      channel,
      channelSegment);

    assertTrue(actual.isPresent());
    assertEquals(expected, actual.get());
  }

  static Stream<Arguments> getConvertArguments() {

    long arid = 44;
    long orid = 25;
    Pair<ArrivalDao, AssocDao> arrivalAssocPair = SignalDetectionDaoTestFixtures.getArrivalAssocPair(arid, orid);

    // ArrivalTime FeatureMeasurement createMeasurementValueSpec setup
    Consumer<FeatureMeasurementConverterInterface> arrivalSpecSetup = fmConverter ->
      when(fmConverter.createMeasurementValueSpec(FeatureMeasurementTypes.ARRIVAL_TIME,
        ARRIVAL_1, Optional.empty(), Optional.empty()))
        .thenReturn(Stream.of(ARRIVAL_MEASUREMENT_SPEC));

    Consumer<FeatureMeasurementConverterInterface> arrivalSpecSetup2 = fmConverter ->
      when(fmConverter.createMeasurementValueSpec(FeatureMeasurementTypes.ARRIVAL_TIME,
        arrivalAssocPair.getLeft(), Optional.of(arrivalAssocPair.getRight()), Optional.empty()))
        .thenReturn(Stream.of(ARRIVAL_MEASUREMENT_SPEC));

    // Phase FeatureMeasurement createMeasurementValueSpec setup
    Consumer<FeatureMeasurementConverterInterface> phaseSpecSetup = fmConverter ->
      when(fmConverter.createMeasurementValueSpec(FeatureMeasurementTypes.PHASE,
        ARRIVAL_1, Optional.empty(), Optional.empty()))
        .thenReturn(Stream.of(PHASE_MEASUREMENT_SPEC));

    Consumer<FeatureMeasurementConverterInterface> phaseSpecSetup2 = fmConverter ->
      when(fmConverter.createMeasurementValueSpec(FeatureMeasurementTypes.PHASE,
        arrivalAssocPair.getLeft(), Optional.of(arrivalAssocPair.getRight()), Optional.empty()))
        .thenReturn(Stream.of(PHASE_MEASUREMENT_SPEC));

    // ArrivalTime FeatureMeasurement converter setup
    Consumer<FeatureMeasurementConverterInterface> singleArrivalSetup = fmConverter ->
      when(fmConverter.convert(ARRIVAL_MEASUREMENT_SPEC,
        ARRIVAL_CHANNEL, ARRIVAL_CHANNEL_SEGMENT,
        Optional.of(
          DoubleValue.from(ARRIVAL_1.getSnr(), Optional.empty(), Units.DECIBELS)
        )))
        .thenReturn(Optional.of(ARRIVAL_TIME_FEATURE_MEASUREMENT));

    Consumer<FeatureMeasurementConverterInterface> singleArrivalSetup2 = fmConverter ->
      when(fmConverter.convert(ARRIVAL_MEASUREMENT_SPEC,
        ARRIVAL_CHANNEL, ARRIVAL_CHANNEL_SEGMENT,
        Optional.of(
          DoubleValue.from(arrivalAssocPair.getLeft().getSnr(), Optional.empty(), Units.DECIBELS)
        )))
        .thenReturn(Optional.of(ARRIVAL_TIME_FEATURE_MEASUREMENT));

    // Phase FeatureMeasurement converter setup
    Consumer<FeatureMeasurementConverterInterface> singlePhaseSetup = fmConverter ->
      when(fmConverter.convert(PHASE_MEASUREMENT_SPEC,
        ARRIVAL_CHANNEL, ARRIVAL_CHANNEL_SEGMENT, Optional.empty()))
        .thenReturn(Optional.of(PHASE_FEATURE_MEASUREMENT));

    List<Consumer<FeatureMeasurementConverterInterface>> fmSetupList = List.of(
      arrivalSpecSetup,
      phaseSpecSetup,
      singleArrivalSetup,
      singlePhaseSetup);

    List<Consumer<FeatureMeasurementConverterInterface>> fmSetupList2 = List.of(
      arrivalSpecSetup2,
      phaseSpecSetup2,
      singleArrivalSetup2,
      singlePhaseSetup);

    Consumer<SignalDetectionIdUtility> signalDetectionIdUtilityConsumer1 = sdiu -> {
      when(sdiu.getOrCreateSignalDetectionHypothesisIdFromAridAndStageId(ARRIVAL_1.getId(), converterId.getLegacyDatabaseAccountId()))
        .thenReturn(SIGNAL_DETECTION_HYPOTHESIS_0.getId().getId());
    };

    Consumer<SignalDetectionIdUtility> signalDetectionIdUtilityConsumer2 = sdiu -> {
      when(sdiu.getOrCreateSignalDetectionHypothesisIdFromAridOridAndStageId(arid, orid, stage1Id.getName()))
        .thenReturn(SIGNAL_DETECTION_HYPOTHESIS_0.getId().getId());
    };

    return Stream.of(
      arguments(SIGNAL_DETECTION_HYPOTHESIS_0,
        fmSetupList,
        signalDetectionIdUtilityConsumer1,
        converterId,
        ARRIVAL_1,
        Optional.empty(),
        List.of(),
        MONITORING_ORG,
        STATION,
        ARRIVAL_CHANNEL,
        ARRIVAL_CHANNEL_SEGMENT),
      arguments(SIGNAL_DETECTION_HYPOTHESIS_0,
        fmSetupList2,
        signalDetectionIdUtilityConsumer2,
        converterId,
        arrivalAssocPair.getLeft(),
        Optional.of(arrivalAssocPair.getRight()),
        List.of(),
        MONITORING_ORG,
        STATION,
        ARRIVAL_CHANNEL,
        ARRIVAL_CHANNEL_SEGMENT));
  }
}
