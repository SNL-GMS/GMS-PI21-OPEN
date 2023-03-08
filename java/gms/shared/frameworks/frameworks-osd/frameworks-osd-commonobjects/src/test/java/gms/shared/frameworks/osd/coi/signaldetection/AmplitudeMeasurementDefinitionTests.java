package gms.shared.frameworks.osd.coi.signaldetection;

import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.frameworks.osd.coi.datatransferobjects.CoiObjectMapperFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.time.Duration;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class AmplitudeMeasurementDefinitionTests {

  private static final Duration WINDOW_LENGTH = Duration.ofSeconds(5);
  private static final Duration MIN_PERIOD = Duration.ofSeconds(1);
  private static final Duration MAX_PERIOD = Duration.ofSeconds(4);

  @Test
  void testJsonDeserialize() throws IOException {
    ObjectMapper objectMapper = CoiObjectMapperFactory.getJsonObjectMapper();
    AmplitudeMeasurementDefinition expected = AmplitudeMeasurementDefinition.builder()
      .setAmplitudeMeasurementType(FeatureMeasurementTypes.AMPLITUDE_A5_OVER_2)
      .setPeakTroughType(PeakTroughType.MAX_PEAK_TO_TROUGH)
      .setArrivalTimeLag(Duration.ZERO)
      .setWindowLength(Duration.ofSeconds(5))
      .setMinPeriod(Duration.ofSeconds(1))
      .setMaxPeriod(Duration.ofSeconds(5))
      .build();

    assertEquals(expected, objectMapper.readValue(objectMapper.writeValueAsString(expected),
      AmplitudeMeasurementDefinition.class));
  }

  @ParameterizedTest
  @MethodSource("handlerInvalidArguments")
  void testInvalidArguments(Duration windowLength, Duration minPeriod, Duration maxPeriod) {
    AmplitudeMeasurementDefinition.Builder ampMeasBuilder =
      AmplitudeMeasurementDefinition.builder()
        .setAmplitudeMeasurementType(FeatureMeasurementTypes.AMPLITUDE_A5_OVER_2)
        .setPeakTroughType(PeakTroughType.FIRST_EXTREMUM)
        .setArrivalTimeLag(Duration.ZERO)
        .setWindowLength(windowLength)
        .setMinPeriod(minPeriod)
        .setMaxPeriod(maxPeriod);
    assertThrows(IllegalStateException.class, () -> ampMeasBuilder.build());
  }

  private static Stream<Arguments> handlerInvalidArguments() {
    return Stream.of(
      arguments(Duration.ofSeconds(-1), MIN_PERIOD, MAX_PERIOD),
      arguments(WINDOW_LENGTH, Duration.ofSeconds(-1), MAX_PERIOD),
      arguments(WINDOW_LENGTH, MAX_PERIOD, MIN_PERIOD)
    );
  }
}