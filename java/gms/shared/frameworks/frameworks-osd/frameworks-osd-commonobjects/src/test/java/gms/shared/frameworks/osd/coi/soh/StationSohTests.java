package gms.shared.frameworks.osd.coi.soh;

import gms.shared.frameworks.osd.coi.signaldetection.Station;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static gms.shared.frameworks.osd.coi.SohTestFixtures.BAD_LAG_MISSING_CHANNEL_SOH;
import static gms.shared.frameworks.osd.coi.SohTestFixtures.GOOD_MISSING_SOH_MONITOR_VALUE_AND_STATUS;
import static gms.shared.frameworks.osd.coi.SohTestFixtures.LAG_STATION_AGGREGATE;
import static gms.shared.frameworks.osd.coi.SohTestFixtures.MARGINAL_LAG_SOH_MONITOR_VALUE_AND_STATUS;
import static gms.shared.frameworks.osd.coi.SohTestFixtures.MARGINAL_MISSING_SOH_MONITOR_VALUE_AND_STATUS;
import static gms.shared.frameworks.osd.coi.SohTestFixtures.MARGINAL_STATION_SOH;
import static gms.shared.frameworks.osd.coi.SohTestFixtures.MISSING_STATION_AGGREGATE;
import static gms.shared.frameworks.osd.coi.test.utils.UtilsTestFixtures.CHANNEL;
import static gms.shared.frameworks.osd.coi.test.utils.UtilsTestFixtures.STATION;

class StationSohTests {

  @Test
  void testSerialization() throws IOException {

    TestUtilities.testSerialization(MARGINAL_STATION_SOH, StationSoh.class);
  }

  @ParameterizedTest
  @MethodSource("validationTestSource")
  void testValidations(
    Instant time,
    Station station,
    Set<SohMonitorValueAndStatus<?>> sohMonitorValueAndStatuses,
    SohStatus sohMonitorStatusRollup,
    Set<StationAggregate<?>> stationAggregates,
    Set<ChannelSoh> channelSohs,
    Class<? extends Throwable> expectedThrowable,
    String messageAfterMethodName
  ) {

    if (expectedThrowable == null && messageAfterMethodName != null
      || expectedThrowable != null && messageAfterMethodName == null
    ) {
      throw new IllegalArgumentException(
        "testValidation: expectedThrowable and messageAfterMethodName: both must be null or " +
          "both must be non-null");
    }

    Executable createExecutable = () -> StationSoh.create(
      time,
      station.getName(),
      sohMonitorValueAndStatuses,
      sohMonitorStatusRollup,
      channelSohs,
      stationAggregates
    );

    Executable fromExecutable = () -> StationSoh.from(
      UUID.randomUUID(),
      time,
      station.getName(),
      sohMonitorValueAndStatuses,
      sohMonitorStatusRollup,
      channelSohs,
      stationAggregates
    );

    if (expectedThrowable != null) {
      Throwable createThrowable = Assertions.assertThrows(expectedThrowable, createExecutable);
      Assertions.assertEquals(messageAfterMethodName,
        createThrowable.getMessage());

      Throwable fromThrowable = Assertions.assertThrows(expectedThrowable, fromExecutable);
      Assertions
        .assertEquals(messageAfterMethodName, fromThrowable.getMessage());
    } else {
      Assertions.assertDoesNotThrow(createExecutable);
      Assertions.assertDoesNotThrow(fromExecutable);
    }
  }

  static Stream<Arguments> validationTestSource() {

    return Stream.of(
      //
      // ALL GOOD - rollup monitor type set and station soh set are equal
      //
      Arguments.arguments(
        Instant.EPOCH,
        STATION,
        Set.of(MARGINAL_MISSING_SOH_MONITOR_VALUE_AND_STATUS,
          MARGINAL_LAG_SOH_MONITOR_VALUE_AND_STATUS),
        SohStatus.MARGINAL,
        Set.of(MISSING_STATION_AGGREGATE,
          LAG_STATION_AGGREGATE),
        Set.of(BAD_LAG_MISSING_CHANNEL_SOH),
        null,
        null
      ),

      //
      // ALL GOOD - rollup monitor type set subset of station soh set
      //
      Arguments.arguments(
        Instant.EPOCH,
        STATION,
        Set.of(GOOD_MISSING_SOH_MONITOR_VALUE_AND_STATUS,
          MARGINAL_LAG_SOH_MONITOR_VALUE_AND_STATUS),
        SohStatus.MARGINAL,
        Set.of(
          MISSING_STATION_AGGREGATE,
          LAG_STATION_AGGREGATE
        ),
        Set.of(
          BAD_LAG_MISSING_CHANNEL_SOH,
          ChannelSoh.from(
            "different channel name",
            SohStatus.BAD,
            Set.of(
              MARGINAL_LAG_SOH_MONITOR_VALUE_AND_STATUS,
              GOOD_MISSING_SOH_MONITOR_VALUE_AND_STATUS
            )
          )
        ),
        null,
        null
      ),
      // Should fail because two of the same channel Names are the same
      Arguments.arguments(
        Instant.EPOCH,
        STATION,
        Set.of(GOOD_MISSING_SOH_MONITOR_VALUE_AND_STATUS,
          MARGINAL_LAG_SOH_MONITOR_VALUE_AND_STATUS),
        SohStatus.MARGINAL,
        Set.of(
          MISSING_STATION_AGGREGATE,
          LAG_STATION_AGGREGATE
        ),
        Set.of(
          BAD_LAG_MISSING_CHANNEL_SOH,
          ChannelSoh.from(
            CHANNEL.getName(),
            SohStatus.BAD,
            Set.of(
              MARGINAL_LAG_SOH_MONITOR_VALUE_AND_STATUS,
              GOOD_MISSING_SOH_MONITOR_VALUE_AND_STATUS
            )
          )
        ),
        IllegalArgumentException.class,
        "The Set of ChannelSohs must only have one unique Channel Name per Set"
      )
    );
  }
}
