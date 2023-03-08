package gms.dataacquisition.data.preloader;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Duration;
import java.time.Instant;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class DatasetGeneratorOptionsTest {

  /**
   * --dataType=(ACEI_ANALOG|ACEI_BOOLEAN|ROLLUP|RSDF|STATION_SOH) --startTime=<start_time>
   * --sampleDuration=<duration-of-sample> --duration=<duration> --stationGroups=<set-of-station-groups>
   * [--receptionDelay=<reception_time_seed>]
   */

  @ParameterizedTest
  @ValueSource(strings = {
    "--dataType=RSDF --startTime=2007-12-03T10:15:30.00Z --sampleDuration=PT10s --duration=PT1h",
    "--dataType=RSDF --startTime=2007-12-03T10:15:30.00Z --sampleDuration=PT10s --stationGroups=TEST",
    "--dataType=RSDF --sampleDuration=PT10s --duration=PT1h --stationGroups=TEST",
    "--startTime=2007-12-03T10:15:30.00Z --sampleDuration=PT10s --duration=PT1h --stationGroups=TEST"
  })
  void testMissingRequiredArgumentsThrowsIllegalArgument(String argsString) {
    String[] args = argsString.split(" ");
    assertThrows(IllegalArgumentException.class, () -> DatasetGeneratorOptions.parse(args));
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "--dataType=RSDF --startTime=2007-12-03T10:15:30.00Z --sampleDuration=PT10s --duration=PT1h --stationGroups=TEST"
  })
  void testMissingOptionalArgumentsDoesNotThrow(String argsString) {
    String[] args = argsString.split(" ");
    assertDoesNotThrow(() -> DatasetGeneratorOptions.parse(args));
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "--dataType=FAKE --startTime=2007-12-03T10:15:30.00Z --sampleDuration=PT10s --duration=PT1h --stationGroups=TEST --receptionDelay=PT1s",
    "--dataType=RSDF --startTime=FAKE --sampleDuration=PT10s --duration=PT1h --stationGroups=TEST --receptionDelay=PT1s",
    "--dataType=RSDF --startTime=2007-12-03T10:15:30.00Z --sampleDuration=FAKE --duration=PT1h --stationGroups=TEST --receptionDelay=PT1s",
    "--dataType=RSDF --startTime=2007-12-03T10:15:30.00Z --sampleDuration=PT10s --duration=FAKE --stationGroups=TEST --receptionDelay=FAKE"
  })
  void testBadParseThrowsIllegalArgument(String argsString) {
    String[] args = argsString.split(" ");
    assertThrows(IllegalArgumentException.class, () -> DatasetGeneratorOptions.parse(args));
  }

  @ParameterizedTest
  @MethodSource("specArguments")
  void testParse(String argsString, GenerationSpec expected) {
    String[] args = argsString.split(" ");
    GenerationSpec actual = assertDoesNotThrow(() -> DatasetGeneratorOptions.parse(args));
    assertEquals(expected, actual);
  }

  private static Stream<Arguments> specArguments() {
    GenerationType type = GenerationType.ACQUIRED_CHANNEL_ENV_ISSUE_BOOLEAN;
    Instant startTime = Instant.parse("2007-12-03T10:15:30.00Z");
    Duration sampleDuration = Duration.parse("PT10s");
    Duration duration = Duration.parse("PT1h");
    String stationGroups = "TEST";
    Duration receptionDelay = Duration.parse("PT1s");

    GenerationSpec template = GenerationSpec.builder()
      .setType(type)
      .setStartTime(startTime)
      .setSampleDuration(sampleDuration)
      .setDuration(duration)
      .addInitialCondition(InitialCondition.STATION_GROUPS, stationGroups)
      .setBatchSize(100)
      .build();

    GenerationSpec rsdf = template.toBuilder()
      .setType(GenerationType.RAW_STATION_DATA_FRAME)
      .addInitialCondition(InitialCondition.RECEPTION_DELAY, receptionDelay.toString())
      .build();

    return Stream.of(
      arguments(formatArgs(template), template),
      arguments(formatArgs(rsdf), rsdf)
    );
  }

  private static String formatArgs(GenerationSpec spec) {
    String args = format(
      "--dataType=%s --startTime=%s --sampleDuration=%s --duration=%s",
      spec.getType(), spec.getStartTime(), spec.getSampleDuration(),
      spec.getDuration());

    String conditions = spec.initialConditions()
      .map(e -> format(" --%s=%s", e.getKey(), e.getValue()))
      .collect(Collectors.joining());

    return args.concat(conditions);
  }
}
