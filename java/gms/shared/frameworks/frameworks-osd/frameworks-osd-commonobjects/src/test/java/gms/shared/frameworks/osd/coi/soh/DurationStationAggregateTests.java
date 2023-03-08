package gms.shared.frameworks.osd.coi.soh;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Streams;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.time.Duration;
import java.util.stream.Stream;

import static gms.shared.frameworks.osd.coi.SohTestFixtures.LAG_STATION_AGGREGATE;
import static gms.shared.frameworks.osd.coi.SohTestFixtures.NULL_LAG_STATION_AGGREGATE;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DurationStationAggregateTests {

  @Test
  void testSerializationNullLatency() throws IOException {
    TestUtilities.testSerialization(NULL_LAG_STATION_AGGREGATE,
      StationAggregate.class);
  }

  @Test
  void testSerialization() throws IOException {
    TestUtilities
      .testSerialization(LAG_STATION_AGGREGATE,
        StationAggregate.class);
  }

  //
  // Test that our "convenience field" stationValueType is in the JSON string.
  //
  @Test
  void testJsonHasConvenienceFieldStationAggregateType() throws JsonProcessingException {

    String json = TestUtilities.getJsonObjectMapper().writeValueAsString(LAG_STATION_AGGREGATE);
    //
    // Note: Assumption here on the format of the output json string (no whitespace between
    // key and value)
    //
    Assertions.assertTrue(
      json.contains("\"stationValueType\":\"DURATION\"")
    );
  }

  @ParameterizedTest
  @MethodSource("validateConstructionTestProvider")
  void testValidateFrom(
    Duration duration,
    StationAggregateType stationAggregateType,
    Class<? extends Throwable> expectedException) {

    if (expectedException != null && duration != null) {
      assertThrows(expectedException,
        () -> DurationStationAggregate
          .from(duration, stationAggregateType));
    } else {
      DurationStationAggregate
        .from(duration, stationAggregateType);
    }
  }

  static Stream<Arguments> validateConstructionTestProvider() {

    return
      Streams.concat(
        Stream.of(
          Arguments.arguments(
            Duration.ZERO,
            StationAggregateType.LAG,
            null),
          Arguments.arguments(
            null,
            StationAggregateType.LAG,
            NullPointerException.class),
          Arguments.arguments(
            Duration.ZERO,
            null,
            NullPointerException.class)
        ));
  }
}
