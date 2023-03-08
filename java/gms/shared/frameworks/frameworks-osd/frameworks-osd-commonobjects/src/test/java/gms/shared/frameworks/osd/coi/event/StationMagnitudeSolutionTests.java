package gms.shared.frameworks.osd.coi.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.frameworks.osd.coi.DoubleValue;
import gms.shared.frameworks.osd.coi.PhaseType;
import gms.shared.frameworks.osd.coi.Units;
import gms.shared.frameworks.osd.coi.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.frameworks.osd.coi.signaldetection.AmplitudeMeasurementValue;
import gms.shared.frameworks.osd.coi.signaldetection.FeatureMeasurement;
import gms.shared.frameworks.osd.coi.signaldetection.FeatureMeasurementTypes;
import gms.shared.frameworks.osd.coi.signaldetection.MeasuredChannelSegmentDescriptor;
import gms.shared.frameworks.osd.coi.test.utils.UtilsTestFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class StationMagnitudeSolutionTests {

  private static final MagnitudeType TYPE = MagnitudeType.MB;
  private static final MagnitudeModel MODEL = MagnitudeModel.VEITH_CLAWSON;
  private static final String STATION_NAME = "station";
  private static final PhaseType PHASE = PhaseType.P;
  private static final double MAGNITUDE = 1.0;
  private static final double MAGNITUDE_UNCERTAINTY = 1.0;
  private static final double MODEL_CORRECTION = 1.0;
  private static final double STATION_CORRECTION = 1.0;
  private static final MeasuredChannelSegmentDescriptor descriptor =
    MeasuredChannelSegmentDescriptor.builder()
      .setChannelName(UtilsTestFixtures.CHANNEL.getName())
      .setMeasuredChannelSegmentStartTime(Instant.EPOCH)
      .setMeasuredChannelSegmentEndTime(Instant.EPOCH.plusSeconds(5))
      .setMeasuredChannelSegmentCreationTime(Instant.EPOCH.plusSeconds(6))
      .build();

  private static final FeatureMeasurement<AmplitudeMeasurementValue> MEASUREMENT = FeatureMeasurement
    .from(UtilsTestFixtures.CHANNEL, UtilsTestFixtures.DESCRIPTOR, FeatureMeasurementTypes.AMPLITUDE_A5_OVER_2,
      AmplitudeMeasurementValue.from(
        Instant.EPOCH, Duration.ofSeconds(1), DoubleValue.from(0.0, 0.0, Units.DEGREES)));

  @Test
  void testJsonDeserialize() throws IOException {
    ObjectMapper objectMapper = CoiObjectMapperFactory.getJsonObjectMapper();
    StationMagnitudeSolution expected = StationMagnitudeSolution.builder()
      .setType(TYPE)
      .setModel(MODEL)
      .setStationName(STATION_NAME)
      .setPhase(PHASE)
      .setMagnitude(MAGNITUDE)
      .setMagnitudeUncertainty(MAGNITUDE_UNCERTAINTY)
      .setModelCorrection(MODEL_CORRECTION)
      .setStationCorrection(STATION_CORRECTION)
      .setMeasurement(MEASUREMENT)
      .build();

    assertEquals(expected, objectMapper
      .readValue(objectMapper.writeValueAsString(expected), StationMagnitudeSolution.class));
  }

  private static Stream<Arguments> handlerInvalidArguments() {
    return Stream.of(
      arguments(-10, MAGNITUDE_UNCERTAINTY, MODEL_CORRECTION, STATION_CORRECTION, MEASUREMENT),
      arguments(51, MAGNITUDE_UNCERTAINTY, MODEL_CORRECTION, STATION_CORRECTION, MEASUREMENT),
      arguments(MAGNITUDE, -1, MODEL_CORRECTION, STATION_CORRECTION, MEASUREMENT),
      arguments(MAGNITUDE, 0, MODEL_CORRECTION, STATION_CORRECTION, MEASUREMENT),
      arguments(MAGNITUDE, MAGNITUDE_UNCERTAINTY, -1, STATION_CORRECTION, MEASUREMENT),
      arguments(MAGNITUDE, MAGNITUDE_UNCERTAINTY, MODEL_CORRECTION, -1, MEASUREMENT)
    );
  }

  @ParameterizedTest
  @MethodSource("handlerInvalidArguments")
  void testBuildInvalidArguments(double magnitude, double magnitudeUncertainty,
    double modelCorrection,
    double stationCorrection, FeatureMeasurement<AmplitudeMeasurementValue> measurement) {
    StationMagnitudeSolution.Builder staMagBuilder = StationMagnitudeSolution.builder()
      .setType(TYPE)
      .setModel(MODEL)
      .setStationName(STATION_NAME)
      .setPhase(PHASE)
      .setMagnitude(magnitude)
      .setMagnitudeUncertainty(magnitudeUncertainty)
      .setModelCorrection(modelCorrection)
      .setStationCorrection(stationCorrection)
      .setMeasurement(measurement);
    assertThrows(IllegalStateException.class, () -> staMagBuilder.build());
  }

}