package gms.testtools.simulators.bridgeddatasourcesimulator.api;

import gms.shared.frameworks.osd.coi.util.TestUtilities;
import gms.testtools.simulators.bridgeddatasourcesimulator.api.util.BridgedDataSourceSimulatorSpec;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BridgedDataSourceSimulatorSpecTest {
  private static final String ERROR_FUTURE_START_SEED = "Seed Data Start Time has to be in the past.";
  private static final String ERROR_FUTURE_END_SEED = "Seed Data End Time has to be in the past.";
  private static final String ERROR_END_BEFORE_START = "Start Time has to be before End Time.";
  private static final String ERROR_NEGATIVE_CALIB = "A Calibration Update Frequency has to be set (in hours) as greater to 0.";
  private static final String ERROR_NEGATIVE_OP_TIME = "An Operational Time Period has to be set (in hours) as greater than 0.";

  private static final Instant FUTURE_INSTANT_OFFSET = Instant.now().plusMillis(100000);
  private static final Instant FUTURE_BIGGER_INSTANT_OFFSET = Instant.now().plusMillis(110000);
  private static final Instant VALID_INSTANT_1600 = Instant.parse("2010-05-20T16:00:00.00Z");
  private static final Instant VALID_INSTANT_1800 = Instant.parse("2010-05-20T18:00:00.00Z");

  private static final Duration VALID_OP_TIME = Duration.ofDays(45);
  private static final Duration VALID_CALIB_TIME = Duration.ofDays(4);
  private static final Duration INVALID_DURATION = Duration.ZERO.minus(Duration.ofDays(1));

  @Test
  void successfulValidation() {
    assertDoesNotThrow(() -> BridgedDataSourceSimulatorSpec.builder()
      .setSeedDataStartTime(VALID_INSTANT_1600)
      .setSeedDataEndTime(VALID_INSTANT_1800)
      .setSimulationStartTime(Instant.now())
      .setOperationalTimePeriod(VALID_OP_TIME)
      .setCalibUpdateFrequency(VALID_CALIB_TIME)
      .build());
  }

  @ParameterizedTest
  @MethodSource("createFaultySpecDataCheck")
  void testBridgeSimulatorSpec_validChecks(Class<? extends Exception> exception,
    Instant startSeedData, Instant stopSeedData, Instant simStartTime,
    Duration opTime, Duration calib, String message) {

    final Exception error = assertThrows(exception,
      () -> BridgedDataSourceSimulatorSpec.builder()
        .setSeedDataStartTime(startSeedData)
        .setSeedDataEndTime(stopSeedData)
        .setSimulationStartTime(simStartTime)
        .setOperationalTimePeriod(opTime)
        .setCalibUpdateFrequency(calib)
        .build());

    assertEquals(message, error.getMessage());
  }

  private static Stream<Arguments> createFaultySpecDataCheck() {
    return Stream.of(
      Arguments.of(IllegalArgumentException.class, VALID_INSTANT_1600, VALID_INSTANT_1600,
        VALID_INSTANT_1800, VALID_OP_TIME, VALID_CALIB_TIME, ERROR_END_BEFORE_START),
      Arguments.of(IllegalArgumentException.class, VALID_INSTANT_1800, VALID_INSTANT_1600,
        Instant.now(), VALID_OP_TIME, VALID_CALIB_TIME, ERROR_END_BEFORE_START),
      Arguments.of(IllegalArgumentException.class, VALID_INSTANT_1600, VALID_INSTANT_1800,
        Instant.now(), INVALID_DURATION, VALID_CALIB_TIME, ERROR_NEGATIVE_OP_TIME),
      Arguments.of(IllegalArgumentException.class, VALID_INSTANT_1600, VALID_INSTANT_1800,
        Instant.now(), VALID_OP_TIME, INVALID_DURATION, ERROR_NEGATIVE_CALIB),
      Arguments.of(IllegalArgumentException.class, FUTURE_INSTANT_OFFSET, FUTURE_BIGGER_INSTANT_OFFSET,
        Instant.now(), VALID_OP_TIME, VALID_CALIB_TIME, ERROR_FUTURE_START_SEED),
      Arguments.of(IllegalArgumentException.class, FUTURE_INSTANT_OFFSET, VALID_INSTANT_1800,
        Instant.now(), VALID_OP_TIME, VALID_CALIB_TIME, ERROR_FUTURE_START_SEED),
      Arguments.of(IllegalArgumentException.class, VALID_INSTANT_1600, FUTURE_INSTANT_OFFSET,
        Instant.now(), VALID_OP_TIME, VALID_CALIB_TIME, ERROR_FUTURE_END_SEED),
      Arguments.of(IllegalArgumentException.class, VALID_INSTANT_1600, FUTURE_INSTANT_OFFSET,
        Instant.now(), Duration.ZERO, VALID_CALIB_TIME, ERROR_FUTURE_END_SEED),
      Arguments.of(IllegalArgumentException.class, VALID_INSTANT_1600, FUTURE_INSTANT_OFFSET,
        Instant.now(), VALID_OP_TIME, Duration.ZERO, ERROR_FUTURE_END_SEED)
    );
  }

  @Test
  void testSerializationGood() throws IOException {
    BridgedDataSourceSimulatorSpec bridgedDataSourceSimulatorSpec = BridgedDataSourceSimulatorSpec.builder()
      .setSeedDataStartTime(VALID_INSTANT_1600)
      .setSeedDataEndTime(VALID_INSTANT_1800)
      .setSimulationStartTime(Instant.now())
      .setOperationalTimePeriod(VALID_OP_TIME)
      .setCalibUpdateFrequency(VALID_CALIB_TIME)
      .build();

    TestUtilities.testSerialization(bridgedDataSourceSimulatorSpec, BridgedDataSourceSimulatorSpec.class);
  }
}
