package gms.dataacquisition.data.preloader;

import gms.dataacquisition.data.preloader.GenerationSpec.Builder;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssue.AcquiredChannelEnvironmentIssueType;
import gms.shared.frameworks.osd.coi.signaldetection.StationGroup;
import gms.shared.frameworks.osd.coi.test.utils.UtilsTestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static gms.dataacquisition.data.preloader.GenerationType.CAPABILITY_SOH_ROLLUP;
import static gms.dataacquisition.data.preloader.GenerationType.RAW_STATION_DATA_FRAME;
import static gms.dataacquisition.data.preloader.GenerationType.STATION_SOH;
import static org.junit.jupiter.api.Assertions.assertEquals;

class GenerationSpecTests {

  private Builder builder;

  @BeforeEach
  public void testSetup() {
    builder = GenerationSpec.builder();
  }

  @Test
  void testSerializationDeserialization() {
    final var generationType = STATION_SOH;
    final var startTime = Instant.EPOCH.minus(1, ChronoUnit.DAYS);
    final var sampleDuration = Duration.ofSeconds(20);
    final var duration = Duration.ofDays(1);
    GenerationSpec generationSpec = builder
      .setType(generationType)
      .setBatchSize(10)
      .setStartTime(startTime)
      .setSampleDuration(sampleDuration)
      .setDuration(duration)
      .addInitialCondition(InitialCondition.STATION_GROUPS,
        UtilsTestFixtures.STATION_GROUP.getName())
      .build();
    assertEquals(generationType, generationSpec.getType());
    assertEquals(startTime, generationSpec.getStartTime());
    assertEquals(duration, generationSpec.getDuration());
    assertEquals(sampleDuration, generationSpec.getSampleDuration());
    assertEquals(UtilsTestFixtures.STATION_GROUP.getName(),
      generationSpec.getInitialConditions().get(InitialCondition.STATION_GROUPS));
  }

  @Test
  void testSerializationDeserialization_initialConditionBuilder() {
    final var duration = Duration.ofDays(1);
    final var startTime = Instant.now().minus(duration);
    final var stationGroups = List.of(UtilsTestFixtures.STATION_GROUP);
    final var sampleDuration = Duration.ofSeconds(20);
    final var generationType = STATION_SOH;
    GenerationSpec generationSpec = builder
      .setType(generationType)
      .setBatchSize(10)
      .setDuration(duration)
      .setSampleDuration(sampleDuration)
      .setStartTime(startTime)
      .addInitialCondition(InitialCondition.STATION_GROUPS,
        stationGroups.stream().map(StationGroup::getName).collect(Collectors.joining(",")))
      .build();
    assertEquals(generationType, generationSpec.getType());
    assertEquals(duration, generationSpec.getDuration());
    assertEquals(sampleDuration, generationSpec.getSampleDuration());
    assertEquals(stationGroups.stream().map(StationGroup::getName).collect(Collectors.joining(",")),
      generationSpec.getInitialConditions().get(InitialCondition.STATION_GROUPS));
  }

  @Test
  void testSerializationDeserialization_curatedDataGeneration() {
    final var duration = Duration.ofDays(1);
    final var startTime = Instant.now().minus(duration);
    final var stationGroups = List.of(UtilsTestFixtures.STATION_GROUP);
    final var sampleDuration = Duration.ofSeconds(20);
    final var generationType = STATION_SOH;
    final var useCuratedDataGeneration = true;
    final var isCd11Station = true;
    final var meanHoursOfPersistence = 12.0;
    final var durationStdErr = 10.0;
    final var percentStdErr = 0.5;
    final Map<InitialCondition, Object> booleanStatusGeneratorParameters = Map
      .of(InitialCondition.BOOLEAN_INITIAL_STATUS, false, InitialCondition.DURATION_INCREMENT,
        Duration.ofHours(1), InitialCondition.MEAN_OCCURRENCES_PER_YEAR, 1.5e5);
    final var durationStatusGeneratorParameters = Map
      .of(InitialCondition.DURATION_ANALOG_STATUS_MAX, Double.MAX_VALUE,
        InitialCondition.DURATION_ANALOG_STATUS_MIN, 0.0,
        InitialCondition.DURATION_ANALOG_INITIAL_VALUE, 1.0, InitialCondition.DURATION_BETA0,
        5.0, InitialCondition.DURATION_BETA1, -0.1);
    final var percentStatusGeneratorParameters = Map
      .of(InitialCondition.PERCENT_ANALOG_STATUS_MAX, 1.0,
        InitialCondition.PERCENT_ANALOG_STATUS_MIN, 0.0,
        InitialCondition.PERCENT_ANALOG_INITIAL_VALUE, 0.5, InitialCondition.PERCENT_BETA0,
        0.5, InitialCondition.PERCENT_BETA1, -0.1);
    GenerationSpec generationSpec = builder
      .setType(generationType)
      .setBatchSize(10)
      .setDuration(duration)
      .setSampleDuration(sampleDuration)
      .setStartTime(startTime)
      .addInitialCondition(InitialCondition.STATION_GROUPS,
        stationGroups.stream().map(StationGroup::getName).collect(Collectors.joining(",")))
      .setUseCuratedDataGeneration(useCuratedDataGeneration)
      .setIsCd11Station(isCd11Station)
      .setAcquiredChannelEnvironmentIssueType(
        AcquiredChannelEnvironmentIssueType.VAULT_DOOR_OPENED)
      .setBooleanStatusGeneratorParameters(booleanStatusGeneratorParameters)
      .addBooleanStatusGeneratorParameter(InitialCondition.MEAN_HOURS_OF_PERSISTENCE,
        meanHoursOfPersistence)
      .setDurationStatusGeneratorParameters(durationStatusGeneratorParameters)
      .addDurationStatusGeneratorParameter(InitialCondition.DURATION_STDERR, durationStdErr)
      .setPercentStatusGeneratorParameters(percentStatusGeneratorParameters)
      .addPercentStatusGeneratorParameter(InitialCondition.PERCENT_STDERR, percentStdErr)
      .build();
    assertEquals(generationType, generationSpec.getType());
    assertEquals(duration, generationSpec.getDuration());
    assertEquals(sampleDuration, generationSpec.getSampleDuration());
    assertEquals(stationGroups.stream().map(StationGroup::getName).collect(Collectors.joining(",")),
      generationSpec.getInitialConditions().get(InitialCondition.STATION_GROUPS));
    assertEquals(stationGroups.stream().map(StationGroup::getName).collect(Collectors.joining(",")),
      generationSpec.getInitialCondition(InitialCondition.STATION_GROUPS).orElseThrow());
    assertEquals(useCuratedDataGeneration, generationSpec.getUseCuratedDataGeneration());
    assertEquals(isCd11Station, generationSpec.getIsCd11Station());
    generationSpec.booleanGeneratorParameters()
      .forEach(entry -> {
        final var key = entry.getKey();
        assertEquals(booleanStatusGeneratorParameters.getOrDefault(key, meanHoursOfPersistence),
          generationSpec.getBooleanStatusGeneratorParameter(key).orElseThrow());
      });
    generationSpec.durationGeneratorParameters()
      .forEach(entry -> {
        final var key = entry.getKey();
        assertEquals(durationStatusGeneratorParameters.getOrDefault(key, durationStdErr),
          generationSpec.getDurationStatusGeneratorParameter(key).orElseThrow());
      });
    generationSpec.percentGeneratorParameters()
      .forEach(entry -> {
        final var key = entry.getKey();
        assertEquals(percentStatusGeneratorParameters.getOrDefault(key, percentStdErr),
          generationSpec.getPercentStatusGeneratorParameter(key).orElseThrow());
      });
  }

  @Test
  void testUsingBuilderMultipleTimes() {
    final var dataTypes = List.of(CAPABILITY_SOH_ROLLUP, RAW_STATION_DATA_FRAME);

    final Function<GenerationType, GenerationSpec> stringGenerationSpecFunction = dt -> {
      final var startTime = Instant.now();
      return builder
        .setDuration(Duration.ofMinutes(1))
        .setSampleDuration(Duration.ofSeconds(5))
        .setBatchSize(2)
        .setInitialConditions(new HashMap<>())
        .setType(dt)
        .setStartTime(startTime)
        .build();
    };

    final var results = dataTypes.stream()
      .map(GenerationType::toString)
      .map(GenerationType::parseType)
      .map(stringGenerationSpecFunction)
      .collect(Collectors.toList());

    final var actual = results.stream()
      .map(GenerationSpec::getType)
      .collect(Collectors.toList());
    assertEquals(dataTypes, actual);
  }

}