package gms.shared.frameworks.osd.coi.soh;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.stream.Stream;

import static gms.shared.frameworks.osd.coi.SohTestFixtures.MARGINAL_MISSING_SOH_MONITOR_VALUE_AND_STATUS;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PercentSohMonitorValueAndStatusTests {

  @Test
  void testSerialization() throws IOException {
    TestUtilities
      .testSerialization(MARGINAL_MISSING_SOH_MONITOR_VALUE_AND_STATUS,
        SohMonitorValueAndStatus.class);
  }

  //
  // Test that our "convenience field" sohValueType is in the JSON string.
  //
  @Test
  void testJsonHasConvenienceFieldSohValueType() throws JsonProcessingException {

    String json = TestUtilities.getJsonObjectMapper().writeValueAsString(MARGINAL_MISSING_SOH_MONITOR_VALUE_AND_STATUS);

    //
    // Note: Assumption here on the format of the output json string (no whitespace between
    // key and value)
    //
    Assertions.assertTrue(
      json.contains("\"sohValueType\":\"PERCENT\"")
    );
  }

  @ParameterizedTest
  @MethodSource("validateConstructionTestProvider")
  void testValidateFrom(
    Double doubleValue,
    SohStatus sohStatus,
    SohMonitorType sohMonitorType,
    Class<? extends Throwable> expectedException) {

    if (expectedException != null) {
      assertThrows(expectedException,
        () -> PercentSohMonitorValueAndStatus
          .from(doubleValue, sohStatus, sohMonitorType));
    } else {
      PercentSohMonitorValueAndStatus
        .from(doubleValue, sohStatus, sohMonitorType);
    }
  }

  static Stream<Arguments> validateConstructionTestProvider() {

    return Stream.of(
      Arguments.arguments(
        0.0,
        SohStatus.GOOD,
        SohMonitorType.ENV_AUTHENTICATION_SEAL_BROKEN,
        null),
      Arguments.arguments(
        null,
        SohStatus.GOOD,
        SohMonitorType.ENV_AUTHENTICATION_SEAL_BROKEN,
        null),
      Arguments.arguments(
        0.0,
        null,
        SohMonitorType.ENV_AUTHENTICATION_SEAL_BROKEN,
        NullPointerException.class),
      Arguments.arguments(
        0.0,
        SohStatus.GOOD,
        null,
        NullPointerException.class),
      Arguments.arguments(
        0.0,
        SohStatus.GOOD,
        SohMonitorType.LAG,
        IllegalStateException.class),
      Arguments.arguments(
        -1.0,
        SohStatus.GOOD,
        SohMonitorType.MISSING,
        IllegalArgumentException.class),
      Arguments.arguments(
        101.0,
        SohStatus.GOOD,
        SohMonitorType.MISSING,
        IllegalArgumentException.class)
    );
  }
}
