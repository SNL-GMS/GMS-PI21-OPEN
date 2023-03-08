package gms.shared.frameworks.osd.coi.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.frameworks.osd.coi.datatransferobjects.CoiObjectMapperFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.Collections;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class NetworkMagnitudeSolutionTests {

  private static final MagnitudeType TYPE = MagnitudeType.MB;
  private static final double MAGNITUDE = 1.0;
  private static final double MAGNITUDE_UNCERTAINTY = 1.0;

  @Test
  void testJsonDeserialize() throws IOException {
    ObjectMapper objectMapper = CoiObjectMapperFactory.getJsonObjectMapper();
    NetworkMagnitudeSolution expected = NetworkMagnitudeSolution.builder()
      .setMagnitudeType(TYPE)
      .setMagnitude(MAGNITUDE)
      .setUncertainty(MAGNITUDE_UNCERTAINTY)
      .setNetworkMagnitudeBehaviors(Collections.emptyList())
      .build();

    assertEquals(expected, objectMapper
      .readValue(objectMapper.writeValueAsString(expected), NetworkMagnitudeSolution.class));
  }

  private static Stream<Arguments> handlerInvalidArguments() {
    return Stream.of(
      arguments(11, MAGNITUDE_UNCERTAINTY),
      arguments(MAGNITUDE, -1),
      arguments(MAGNITUDE, 11)
    );
  }

  @ParameterizedTest
  @MethodSource("handlerInvalidArguments")
  void testBuildInvalidArguments(double magnitude, double magnitudeUncertainty) {
    NetworkMagnitudeSolution.Builder netMagBuilder = NetworkMagnitudeSolution.builder()
      .setMagnitudeType(TYPE)
      .setMagnitude(magnitude)
      .setUncertainty(magnitudeUncertainty)
      .setNetworkMagnitudeBehaviors(Collections.emptyList());
    assertThrows(IllegalStateException.class, () -> netMagBuilder.build());
  }

}
