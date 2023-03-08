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
import java.util.Arrays;
import java.util.stream.Stream;

import static gms.shared.frameworks.osd.coi.SohTestFixtures.MARGINAL_LAG_SOH_MONITOR_VALUE_AND_STATUS;
import static gms.shared.frameworks.osd.coi.SohTestFixtures.MARGINAL_NULL_LAG_SOH_MONITOR_VALUE_AND_STATUS;
import static gms.shared.frameworks.osd.coi.SohTestFixtures.MARGINAL_TIMELINESS_SOH_MONITOR_VALUE_AND_STATUS;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DurationSohMonitorValueAndStatusTests {

  @Test
  void testSerializationNullLag() throws IOException {
    TestUtilities.testSerialization(MARGINAL_NULL_LAG_SOH_MONITOR_VALUE_AND_STATUS,
      SohMonitorValueAndStatus.class);
  }

  @Test
  void testSerialization() throws IOException {
    TestUtilities
      .testSerialization(MARGINAL_LAG_SOH_MONITOR_VALUE_AND_STATUS,
        SohMonitorValueAndStatus.class);
  }

  //
  // Test that our "convenience field" sohValueType is in the JSON string.
  //
  @Test
  void testJsonHasConvenienceFieldSohValueType() throws JsonProcessingException {

    String json = TestUtilities.getJsonObjectMapper().writeValueAsString(
      MARGINAL_LAG_SOH_MONITOR_VALUE_AND_STATUS);

    //
    // Note: Assumption here on the format of the output json string (no whitespace between
    // key and value)
    //
    Assertions.assertTrue(
      json.contains("\"sohValueType\":\"DURATION\"")
    );

    json = TestUtilities.getJsonObjectMapper().writeValueAsString(
      MARGINAL_TIMELINESS_SOH_MONITOR_VALUE_AND_STATUS);

    Assertions.assertTrue(
      json.contains("\"sohValueType\":\"DURATION\"")
    );
  }


  @ParameterizedTest
  @MethodSource("testValidateFromWithExceptionsProvider")
  void testValidateFromWithExceptions(
    Duration duration,
    SohStatus sohStatus,
    SohMonitorType sohMonitorType,
    Class<? extends Throwable> expectedException) {

    assertThrows(expectedException,
      () -> DurationSohMonitorValueAndStatus.from(duration, sohStatus, sohMonitorType));
  }


  static Stream<Arguments> testValidateFromWithExceptionsProvider() {

    return
      Streams.concat(
        Stream.of(
          Arguments.arguments(
            Duration.ZERO,
            null,
            SohMonitorType.LAG,
            NullPointerException.class),
          Arguments.arguments(
            Duration.ZERO,
            SohStatus.GOOD,
            null,
            NullPointerException.class),
          Arguments.arguments(
            Duration.ZERO,
            null,
            SohMonitorType.TIMELINESS,
            NullPointerException.class)
        ),

        //
        // Test that we fail for any SohMonitorType except for LAG OR TIMELINESS.
        //
        // If another monitor type beside LAG or TIMELINESS is added that is usable for
        // DurationSohMonitorValueAndStatus, This test will need to be modified or it
        // will fail.
        //
        Arrays.stream(SohMonitorType.values()).filter(
          sohMonitorType -> sohMonitorType != SohMonitorType.LAG
            && sohMonitorType != SohMonitorType.TIMELINESS
        ).map(
          sohMonitorType -> Arguments.arguments(
            Duration.ZERO,
            SohStatus.GOOD,
            sohMonitorType,
            IllegalArgumentException.class,
            "DurationSohMonitorValueAndStatus can only be created with a LAG or TIMELINESS SohMonitorType, but was: "
              + sohMonitorType
          )
        ));
  }


  @ParameterizedTest
  @MethodSource("testValidateFromNoExceptionsProvider")
  void testValidateFromNoExceptions(
    Duration duration,
    SohStatus sohStatus,
    SohMonitorType sohMonitorType) {

    assertDoesNotThrow(() -> DurationSohMonitorValueAndStatus.from(duration, sohStatus, sohMonitorType));
  }


  static Stream<Arguments> testValidateFromNoExceptionsProvider() {

    return
      Streams.concat(
        Stream.of(
          Arguments.arguments(
            Duration.ZERO,
            SohStatus.GOOD,
            SohMonitorType.LAG),
          Arguments.arguments(
            null,
            SohStatus.GOOD,
            SohMonitorType.LAG),
          Arguments.arguments(
            Duration.ZERO,
            SohStatus.GOOD,
            SohMonitorType.TIMELINESS),
          Arguments.arguments(
            null,
            SohStatus.GOOD,
            SohMonitorType.TIMELINESS)
        ));
  }

}
