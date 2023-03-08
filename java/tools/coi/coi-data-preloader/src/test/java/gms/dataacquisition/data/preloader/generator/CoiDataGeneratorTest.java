package gms.dataacquisition.data.preloader.generator;

import gms.dataacquisition.data.preloader.DatasetGeneratorOptions;
import gms.dataacquisition.data.preloader.GenerationSpec;
import gms.dataacquisition.data.preloader.GenerationType;
import gms.shared.frameworks.injector.Modifier;
import gms.shared.frameworks.osd.api.OsdRepositoryInterface;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssue.AcquiredChannelEnvironmentIssueType;
import gms.shared.frameworks.osd.coi.signaldetection.StationGroup;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static gms.shared.frameworks.osd.coi.station.StationTestFixtures.asar;
import static gms.shared.frameworks.osd.coi.station.StationTestFixtures.pdar;
import static gms.shared.frameworks.osd.coi.station.StationTestFixtures.txar;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

abstract class CoiDataGeneratorTest<G extends CoiDataGenerator<T, M>, T, M extends Modifier<?>> {

  private AutoCloseable openMocks;
  @Mock
  protected OsdRepositoryInterface sohRepository;

  protected GenerationSpec generationSpec;
  G dataGenerator;

  private final StationGroup test_station_group_1 = StationGroup.from("Test_Station_Group_1",
    "Test StationGroup 1", List.of(asar()));
  private final StationGroup test_station_group_2 = StationGroup.from("Test_Station_Group_2",
    "Test StationGroup 2", List.of(pdar(), txar()));
  private final StationGroup test_station_group_3 = StationGroup.from("Test_Station_Group_3",
    "Test StationGroup 3", List.of(asar(), pdar(), txar()));

  protected final List<StationGroup> stationGroups = List
    .of(test_station_group_1, test_station_group_2, test_station_group_3);
  private final List<String> stationGroupNames = stationGroups.stream()
    .map(StationGroup::getName)
    .distinct()
    .collect(Collectors.toList());

  protected boolean validateUniqueRecords;
  protected Instant seedTime;
  protected Instant receptionTime;

  public static final int BATCH_SIZE = 10;
  // TODO need to handle case where our total number of items generated is less than BATCH_SIZE
  // to match frequency in modifiers for now
  protected final Duration generationFrequency = Duration.ofSeconds(20);
  protected final Duration generationDuration = Duration.ofSeconds(200);
  protected final Duration receptionDelay = Duration.ofSeconds(20);
  protected final Instant startTime = Instant.now();

  private final AtomicInteger repositoryCallCounter = new AtomicInteger(0);

  @BeforeEach
  public void testSetup() {
    // We want to validate that we aren't trying to store the same records
    // under normal circumstances, but we won't check for uniqueness if we
    // re-try the storage of a collection after a failure because the Captor
    // retains all collections we attempt to store. We could also capture the
    // the exception is thrown on and remove it.
    validateUniqueRecords = true;
    repositoryCallCounter.set(0);
    openMocks = MockitoAnnotations.openMocks(this);
    buildGenerationSpec();

    when(sohRepository.retrieveStationGroups(stationGroupNames)).thenReturn(stationGroups);
    when(sohRepository.retrieveStationGroups(List.of("CD1.1"))).thenReturn(stationGroups);

    dataGenerator = getDataGenerator(generationSpec, sohRepository);
  }

  @AfterEach
  void endTest() throws Exception {
    openMocks.close();
  }

  private void buildGenerationSpec() {
    final var stationGroups = this.stationGroups.stream()
      .map(StationGroup::getName)
      .distinct()
      .reduce((s, s2) -> s + "," + s2)
      .orElseThrow();
    final var generatorDataType = getGeneratorDataType();
    final String[] args = String.format(
      "--duration=%s --dataType=%s --stationGroups=%s --startTime=%s --sampleDuration=%s --receptionDelay=%s",
      generationDuration, generatorDataType, stationGroups, startTime, generationFrequency,
      receptionDelay).split(" ");

    generationSpec = DatasetGeneratorOptions.parse(args).toBuilder().setBatchSize(BATCH_SIZE)
      .build();
    seedTime = generationSpec.getStartTime();
    receptionTime = generationSpec.getReceptionTime();
  }

  protected Stream<AcquiredChannelEnvironmentIssueType> getEnvironmentIssueTypes() {
    return Stream.of(
      AcquiredChannelEnvironmentIssueType.AUTHENTICATION_SEAL_BROKEN,
      AcquiredChannelEnvironmentIssueType.BACKUP_POWER_UNSTABLE,
      AcquiredChannelEnvironmentIssueType.CALIBRATION_UNDERWAY,
      AcquiredChannelEnvironmentIssueType.CLIPPED,
      AcquiredChannelEnvironmentIssueType.CLOCK_DIFFERENTIAL_TOO_LARGE,
      AcquiredChannelEnvironmentIssueType.DEAD_SENSOR_CHANNEL,
      AcquiredChannelEnvironmentIssueType.DIGITIZER_ANALOG_INPUT_SHORTED,
      AcquiredChannelEnvironmentIssueType.DIGITIZER_CALIBRATION_LOOP_BACK,
      AcquiredChannelEnvironmentIssueType.DIGITIZING_EQUIPMENT_OPEN,
      AcquiredChannelEnvironmentIssueType.EQUIPMENT_HOUSING_OPEN,
      AcquiredChannelEnvironmentIssueType.EQUIPMENT_MOVED,
      AcquiredChannelEnvironmentIssueType.GPS_RECEIVER_OFF,
      AcquiredChannelEnvironmentIssueType.GPS_RECEIVER_UNLOCKED,
      AcquiredChannelEnvironmentIssueType.MAIN_POWER_FAILURE,
      AcquiredChannelEnvironmentIssueType.VAULT_DOOR_OPENED,
      AcquiredChannelEnvironmentIssueType.ZEROED_DATA
    );
  }

  protected abstract G getDataGenerator(GenerationSpec generationSpec,
    OsdRepositoryInterface sohRepository);

  protected abstract GenerationType getGeneratorDataType();

  @Test
  void testGetSeedNames() {
    final var result = dataGenerator.getSeedNames();

    assertNotNull(result);
    final List<String> seedNames = getSeedNames();
    assertEquals(seedNames, result);
    verify(sohRepository, times(1)).retrieveStationGroups(stationGroupNames);
    verifyNoMoreInteractions(sohRepository);
  }


  protected abstract List<String> getSeedNames();

  @Test
  void testGenerateSeed_dependencyInteraction() {
    final var stationName = stationGroups.stream().flatMap(g -> g.getStations().stream())
      .findFirst().orElseThrow().getName();

    dataGenerator.generateSeed(stationName);

    verify(sohRepository, atLeast(0)).retrieveStationGroups(stationGroupNames);
    verifyNoMoreInteractions(sohRepository);
  }

  abstract void testGenerateSeed();

  @Test
  void testConsumeRecord_dependencyInteraction() {
    final var records = getRecordsToSend();

    dataGenerator.consumeRecords(records);

    verifyRepositoryInteraction(1);
    verifyNoMoreInteractions(sohRepository);
  }

  protected abstract List<T> getRecordsToSend();

  @ParameterizedTest
  @MethodSource("useCuratedDataGenerationParameterProvider")
  void testRun(boolean useCuratedDataGeneration) {
    generationSpec.toBuilder().setUseCuratedDataGeneration(useCuratedDataGeneration);
    dataGenerator.run();

    verifyRepositoryInteraction(getWantedNumberOfConsumeInvocations());
    verify(sohRepository, atMost(
      ((int) stationGroups.stream().flatMap(StationGroup::stations).distinct().count() + 1)))
      .retrieveStationGroups(anyList());
    verifyNoMoreInteractions(sohRepository);
  }

  protected abstract void verifyTimes(Collection<T> capturedRecords);

  @ParameterizedTest
  @MethodSource("useCuratedDataGenerationParameterProvider")
  void testRunWithFailures(boolean useCuratedDataGeneration) {
    generationSpec.toBuilder().setUseCuratedDataGeneration(useCuratedDataGeneration);
    final int wantedNumberOfConsumeInvocationsPlusRetry = getWantedNumberOfConsumeInvocations() + 1;
    validateUniqueRecords = false;
    mockIntermittentSohRepositoryFailure(1);

    dataGenerator.run();

    verifyRepositoryInteraction(wantedNumberOfConsumeInvocationsPlusRetry);
    verify(sohRepository, atMost(
      ((int) stationGroups.stream().flatMap(StationGroup::stations).distinct().count() + 1)))
      .retrieveStationGroups(anyList());
    verifyNoMoreInteractions(sohRepository);
  }

  static Stream<Arguments> useCuratedDataGenerationParameterProvider() {
    return Stream.of(arguments(true), arguments(false));
  }

  protected int getWantedNumberOfConsumeInvocations() {
    return (int) Math.ceil((double) getWantedNumberOfItemsGenerated() / BATCH_SIZE);
  }

  protected abstract void mockIntermittentSohRepositoryFailure(int failsOnNthCall);

  protected abstract int getWantedNumberOfItemsGenerated();

  protected abstract void verifyRepositoryInteraction(int wantedNumberOfInvocations);


  protected <I> void validateIds(List<I> ids) {
    if (validateUniqueRecords) {
      assertEquals(ids.stream().distinct().count(), ids.size());
    } else {
      Map<I, Long> idCount = ids.stream()
        .collect(Collectors.groupingBy(e -> e, Collectors.counting()));
      final var duplicates = idCount.values().stream()
        .filter(integer -> integer > 1)
        .collect(Collectors.toList());
      assertEquals(this.getExpectedDuplicateIdsSize(), duplicates.size());
      assertTrue(duplicates.stream().allMatch(count -> count == 2));
    }
  }

  protected int getExpectedDuplicateIdsSize() {
    return BATCH_SIZE;
  }

  protected <A> void assertEmpty(Collection<A> actual) {
    assertNotNull(actual);
    assertTrue(actual.isEmpty());
  }

  protected <A> void assertNotNullOrEmpty(Collection<A> actual) {
    assertNotNull(actual);
    assertFalse(actual.isEmpty());
  }

  protected <A, B> void assertNotNullOrEmpty(Map<A, B> actual) {
    assertNotNull(actual);
    assertFalse(actual.isEmpty());
  }

  protected synchronized Object throwErrorOnNthCall(int failsOnNthCall) {
    final var timesCalled = repositoryCallCounter.getAndIncrement();
    if (failsOnNthCall == timesCalled) {
      throw new IllegalStateException();
    } else {
      return null;
    }
  }
}
