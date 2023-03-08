import gms.core.performancemonitoring.ssam.control.api.DecimationRequestParams;
import gms.shared.frameworks.osd.coi.soh.SohMonitorType;
import gms.shared.utilities.test.TestUtilities;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Instant;
import java.util.stream.Stream;

class DecimationRequestParamsTest {

  @ParameterizedTest
  @MethodSource("testDecimationRequestParamsSource")
  void testDecimationRequestParams(Instant startTime, Instant endtime, String stationName, int samplePerChannel,
    SohMonitorType monitorType, boolean shouldThrowExcpetion) {

    if (shouldThrowExcpetion) {
      Assertions.assertThrows(IllegalArgumentException.class, () ->
        DecimationRequestParams.create(startTime, endtime, samplePerChannel, stationName, monitorType));
    } else {
      Assertions.assertDoesNotThrow(() ->
        DecimationRequestParams.create(startTime, endtime, samplePerChannel, stationName, monitorType));
    }

  }

  private static Stream<Arguments> testDecimationRequestParamsSource() {
    return Stream.of(Arguments.arguments(Instant.EPOCH.plusSeconds(500), Instant.EPOCH, "station", 1, SohMonitorType.MISSING, true),
      Arguments.arguments(Instant.EPOCH, Instant.EPOCH.plusSeconds(500), "station", 0, SohMonitorType.MISSING, true),
      Arguments.arguments(Instant.EPOCH, Instant.EPOCH.plusSeconds(500), "", 1, SohMonitorType.MISSING, true),
      Arguments.arguments(Instant.EPOCH, Instant.EPOCH.plusSeconds(500), "station", 1, SohMonitorType.ENV_AMPLIFIER_SATURATION_DETECTED, true),
      Arguments.arguments(Instant.EPOCH, Instant.EPOCH.plusSeconds(500), "station", 1, SohMonitorType.MISSING, false));
  }

  @Test
  void testDecimationRequestParamsSerialization() {
    var decimationRequestParams = DecimationRequestParams.create(Instant.EPOCH, Instant.EPOCH.plusSeconds(500), 100, "blah", SohMonitorType.MISSING);
    TestUtilities.assertSerializes(decimationRequestParams, DecimationRequestParams.class);
  }
}
