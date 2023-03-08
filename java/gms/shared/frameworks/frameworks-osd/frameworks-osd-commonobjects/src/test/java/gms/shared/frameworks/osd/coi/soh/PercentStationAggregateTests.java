package gms.shared.frameworks.osd.coi.soh;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.stream.Stream;

import static gms.shared.frameworks.osd.coi.SohTestFixtures.MISSING_STATION_AGGREGATE;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PercentStationAggregateTests {

  @Test
  void testSerialization() throws IOException {
    TestUtilities
      .testSerialization(MISSING_STATION_AGGREGATE, StationAggregate.class);
  }

  @Test
  void testJsonHasConvenienceFieldSohValueType() throws JsonProcessingException {
    String json = TestUtilities.getJsonObjectMapper().writeValueAsString(MISSING_STATION_AGGREGATE);

    Assertions.assertTrue(
      json.contains("\"stationValueType\":\"PERCENT\"")
    );
  }

  @ParameterizedTest
  @MethodSource("validateConstructionTestProvider")
  void testValidateFrom(
    Double doubleValue,
    StationAggregateType stationAggregateType,
    Class<? extends Throwable> expectedException) {
    if (expectedException != null) {
      assertThrows(expectedException,
        () -> PercentStationAggregate.from(doubleValue, stationAggregateType));
    } else {
      PercentStationAggregate.from(doubleValue, stationAggregateType);
    }
  }

  static Stream<Arguments> validateConstructionTestProvider() {
    return Stream.of(
      Arguments.arguments(
        0.0,
        StationAggregateType.MISSING,
        null),
      Arguments.arguments(
        null,
        StationAggregateType.MISSING,
        null),
      Arguments.arguments(
        0.0,
        StationAggregateType.MISSING,
        null,
        NullPointerException.class),
      Arguments.arguments(
        0.0,
        null,
        NullPointerException.class),
      Arguments.arguments(
        0.0,
        StationAggregateType.LAG,
        IllegalStateException.class),
      Arguments.arguments(
        -1.0,
        StationAggregateType.MISSING,
        IllegalArgumentException.class),
      Arguments.arguments(
        101.0,
        StationAggregateType.MISSING,
        IllegalArgumentException.class));
  }
}
