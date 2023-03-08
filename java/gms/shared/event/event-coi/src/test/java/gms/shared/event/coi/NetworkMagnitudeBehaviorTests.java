package gms.shared.event.coi;

import gms.shared.signaldetection.coi.types.PhaseType;
import gms.shared.signaldetection.testfixtures.SignalDetectionTestFixtures;
import gms.shared.stationdefinition.coi.utils.DoubleValue;
import gms.shared.stationdefinition.coi.utils.Units;
import gms.shared.stationdefinition.testfixtures.UtilsTestFixtures;
import gms.shared.utilities.test.TestUtilities;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class NetworkMagnitudeBehaviorTests {
  private static final boolean DEFINING = true;
  private static final double RESIDUAL = 1.0;
  private static final StationMagnitudeSolution STATION_MAGNITUDE_SOLUTION =
    StationMagnitudeSolution
      .builder()
      .setType(MagnitudeType.MB)
      .setModel(MagnitudeModel.VEITH_CLAWSON)
      .setStation(UtilsTestFixtures.STATION_FACET)
      .setPhase(PhaseType.P)
      .setMagnitude(DoubleValue.from(RESIDUAL, Optional.empty(), Units.MAGNITUDE))
      .setModelCorrection(RESIDUAL)
      .setStationCorrection(RESIDUAL)
      .setMeasurement(SignalDetectionTestFixtures.AMPLITUDE_FEATURE_MEASUREMENT)
      .build();
  private static final double WEIGHT = 1.0;

  @Test
  void testSerialization() {
    var expected = NetworkMagnitudeBehavior.builder()
      .setDefining(DEFINING)
      .setResidual(RESIDUAL)
      .setWeight(WEIGHT)
      .setStationMagnitudeSolution(STATION_MAGNITUDE_SOLUTION)
      .build();

    TestUtilities.assertSerializes(expected, NetworkMagnitudeBehavior.class);
  }

  @ParameterizedTest
  @MethodSource("handlerInvalidArguments")
  void testBuildInvalidArguments(boolean isDefining, double residual, double weight, boolean shouldFail) {
    var nmbBuilder = NetworkMagnitudeBehavior.builder()
      .setDefining(isDefining)
      .setResidual(residual)
      .setWeight(weight)
      .setStationMagnitudeSolution(STATION_MAGNITUDE_SOLUTION);

    if (shouldFail) {
      assertThrows(IllegalStateException.class, nmbBuilder::build);
    } else {
      assertDoesNotThrow(nmbBuilder::build);
    }
  }

  private static Stream<Arguments> handlerInvalidArguments() {
    return Stream.of(
      arguments(DEFINING, -11, WEIGHT, true),
      arguments(DEFINING, 11, WEIGHT, true),
      arguments(DEFINING, RESIDUAL, -1, true),
      arguments(false, RESIDUAL, -1, true),
      arguments(false, RESIDUAL, 1, false),
      arguments(false, RESIDUAL, 0.0, false),
      arguments(DEFINING, RESIDUAL, WEIGHT, false)
    );
  }
}