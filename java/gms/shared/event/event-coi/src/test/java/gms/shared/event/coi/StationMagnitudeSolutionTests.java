package gms.shared.event.coi;

import gms.shared.signaldetection.coi.detection.FeatureMeasurement;
import gms.shared.signaldetection.coi.types.PhaseType;
import gms.shared.signaldetection.coi.values.AmplitudeMeasurementValue;
import gms.shared.signaldetection.testfixtures.SignalDetectionTestFixtures;
import gms.shared.stationdefinition.coi.utils.DoubleValue;
import gms.shared.stationdefinition.coi.utils.Units;
import gms.shared.stationdefinition.testfixtures.UtilsTestFixtures;
import gms.shared.utilities.test.TestUtilities;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class StationMagnitudeSolutionTests {
  private static final MagnitudeType TYPE = MagnitudeType.MB;
  private static final MagnitudeModel MODEL = MagnitudeModel.VEITH_CLAWSON;
  private static final PhaseType PHASE = PhaseType.P;
  private static final DoubleValue MAGNITUDE = DoubleValue.from(1.0, Optional.empty(), Units.MAGNITUDE);
  private static final Optional<Double> MODEL_CORRECTION = Optional.of(1.0);
  private static final Optional<Double> STATION_CORRECTION = Optional.of(1.0);

  private static final FeatureMeasurement<AmplitudeMeasurementValue> MEASUREMENT =
    SignalDetectionTestFixtures.AMPLITUDE_FEATURE_MEASUREMENT;

  @ParameterizedTest
  @MethodSource("handlerSerialization")
  void testSerialization(Double modelCorrection, Double stationCorrection) throws IOException {
    var expectedBuilder = StationMagnitudeSolution.builder()
      .setType(TYPE)
      .setModel(MODEL)
      .setStation(UtilsTestFixtures.STATION_FACET)
      .setPhase(PHASE)
      .setMagnitude(MAGNITUDE)
      .setMeasurement(MEASUREMENT);

    if (modelCorrection != null) {
      // Want to flex the helper method setModelCorrection(double)
      expectedBuilder.setModelCorrection(modelCorrection);
    } else {
      expectedBuilder.setModelCorrection(Optional.empty());
    }

    if (stationCorrection != null) {
      // Want to flex the helper method setStationCorrection(double)
      expectedBuilder.setModelCorrection(stationCorrection);
    } else {
      expectedBuilder.setModelCorrection(Optional.empty());
    }

    TestUtilities.assertSerializes(expectedBuilder.build(), StationMagnitudeSolution.class);
  }

  private static Stream<Arguments> handlerSerialization() {
    return Stream.of(
      Arguments.arguments(1.0, 1.0),
      Arguments.arguments(1.0, null),
      Arguments.arguments(null, 1.0),
      Arguments.arguments(null, null)
    );
  }

  @ParameterizedTest
  @MethodSource("handlerInvalidArguments")
  void testBuildInvalidArguments(DoubleValue magnitude, Optional<Double> modelCorrection,
    Optional<Double> stationCorrection, FeatureMeasurement<AmplitudeMeasurementValue> measurement, boolean shouldFail) {
    var smsBuilder = StationMagnitudeSolution.builder()
      .setType(TYPE)
      .setModel(MODEL)
      .setStation(UtilsTestFixtures.STATION_FACET)
      .setPhase(PHASE)
      .setMagnitude(magnitude)
      .setModelCorrection(modelCorrection)
      .setStationCorrection(stationCorrection)
      .setMeasurement(measurement);

    if (shouldFail) {
      assertThrows(IllegalStateException.class, smsBuilder::build);
    } else {
      assertDoesNotThrow(smsBuilder::build);
    }
  }

  private static Stream<Arguments> handlerInvalidArguments() {
    return Stream.of(
      arguments(DoubleValue.from(-10.0, Optional.empty(), Units.MAGNITUDE), MODEL_CORRECTION, STATION_CORRECTION, MEASUREMENT, true),
      arguments(DoubleValue.from(51.0, Optional.empty(), Units.MAGNITUDE), MODEL_CORRECTION, STATION_CORRECTION, MEASUREMENT, true),
      arguments(MAGNITUDE, Optional.of(-1.0), STATION_CORRECTION, MEASUREMENT, true),
      arguments(MAGNITUDE, MODEL_CORRECTION, Optional.of(-1.0), MEASUREMENT, true),
      arguments(MAGNITUDE, MODEL_CORRECTION, Optional.empty(), MEASUREMENT, false),
      arguments(MAGNITUDE, Optional.empty(), STATION_CORRECTION, MEASUREMENT, false),
      arguments(MAGNITUDE, MODEL_CORRECTION, STATION_CORRECTION, MEASUREMENT, false)
    );
  }
}