package gms.shared.frameworks.osd.coi.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.frameworks.osd.coi.DoubleValue;
import gms.shared.frameworks.osd.coi.PhaseType;
import gms.shared.frameworks.osd.coi.Units;
import gms.shared.frameworks.osd.coi.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.frameworks.osd.coi.signaldetection.AmplitudeMeasurementValue;
import gms.shared.frameworks.osd.coi.signaldetection.FeatureMeasurement;
import gms.shared.frameworks.osd.coi.signaldetection.FeatureMeasurementTypes;
import gms.shared.frameworks.osd.coi.test.utils.UtilsTestFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class NetworkMagnitudeBehaviorTests {

  private static final boolean DEFINING = true;
  private static final double RESIDUAL = 1.0;
  private static final FeatureMeasurement<AmplitudeMeasurementValue> AMPLITUDE_FEATURE_MEASUREMENT = FeatureMeasurement
    .from(UtilsTestFixtures.CHANNEL, UtilsTestFixtures.DESCRIPTOR,
      FeatureMeasurementTypes.AMPLITUDE_A5_OVER_2.getFeatureMeasurementTypeName(),
      AmplitudeMeasurementValue.from(
        Instant.EPOCH, Duration.ofSeconds(1), DoubleValue.from(0.0, 0.0, Units.DEGREES)));
  private static final StationMagnitudeSolution STATION_MAGNITUDE_SOLUTION =
    StationMagnitudeSolution
      .builder()
      .setType(MagnitudeType.MB)
      .setModel(MagnitudeModel.VEITH_CLAWSON)
      .setStationName(UUID.randomUUID().toString())
      .setPhase(PhaseType.P)
      .setMagnitude(RESIDUAL)
      .setMagnitudeUncertainty(RESIDUAL)
      .setModelCorrection(RESIDUAL)
      .setStationCorrection(RESIDUAL)
      .setMeasurement(AMPLITUDE_FEATURE_MEASUREMENT)
      .build();
  private static final double WEIGHT = 1.0;

  @Test
  void testJsonDeserialize() throws IOException {
    ObjectMapper objectMapper = CoiObjectMapperFactory.getJsonObjectMapper();
    NetworkMagnitudeBehavior expected = NetworkMagnitudeBehavior.builder()
      .setDefining(DEFINING)
      .setResidual(RESIDUAL)
      .setWeight(WEIGHT)
      .setStationMagnitudeSolution(STATION_MAGNITUDE_SOLUTION)
      .build();

    assertEquals(expected, objectMapper
      .readValue(objectMapper.writeValueAsString(expected), NetworkMagnitudeBehavior.class));
  }

  private static Stream<Arguments> handlerInvalidArguments() {
    return Stream.of(
      arguments(-11, WEIGHT),
      arguments(11, WEIGHT),
      arguments(RESIDUAL, -1)
    );
  }

  @ParameterizedTest
  @MethodSource("handlerInvalidArguments")
  void testBuildInvalidArguments(double residual, double weight) {
    assertThrows(IllegalStateException.class,
      () -> NetworkMagnitudeBehavior.builder()
        .setDefining(DEFINING)
        .setResidual(residual)
        .setWeight(weight)
        .setStationMagnitudeSolution(STATION_MAGNITUDE_SOLUTION)
        .build());
  }
}
