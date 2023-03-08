package gms.dataacquisition.stationreceiver.cd11.parser;

import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssue;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssue.AcquiredChannelEnvironmentIssueType;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssueAnalog;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssueBoolean;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class Cd11AcquiredChannelEnvironmentIssuesParserTests {

  private static final String LBTB1_CHANNEL_NAME = "LBTB.LBTB1.SHZ";
  private static final Instant EXPECTED_START_TIME = Instant.parse("2019-06-06T17:26:00Z");
  private static final Instant EXPECTED_TIME_SERIES_END_TIME = Instant
    .parse("2019-06-06T17:26:09.975Z");
  private static final byte[] ALL_FALSE_CHANNEL_STATUSES = {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
  private static final byte[] ALL_TRUE_CHANNEL_STATUSES = {1, 15, 31, 31, 3, 0, 0, 0, 0, 0, 0, 0,
    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
  private static final byte[] TRUE_AND_FALSE_CHANNEL_STATUSES = {1, 5, 5, 5, 1, 0, 0, 0, 0, 0, 0, 0,
    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
  private static final byte[] NON_ZERO_CLOCK_DIFFERENTIAL_CHANNEL_STATUSES = {1, 0, 0, 0, 0, 0, 0,
    0, 0, 0, 0, 0,
    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 10, 0, 0, 0};
  private static final List<AcquiredChannelEnvironmentIssueType> ACQUIRED_CHANNEL_SOH_TYPES_SET_TRUE = List
    .of(
      AcquiredChannelEnvironmentIssueType.DEAD_SENSOR_CHANNEL,
      AcquiredChannelEnvironmentIssueType.CLIPPED,
      AcquiredChannelEnvironmentIssueType.EQUIPMENT_HOUSING_OPEN,
      AcquiredChannelEnvironmentIssueType.VAULT_DOOR_OPENED,
      AcquiredChannelEnvironmentIssueType.CLOCK_DIFFERENTIAL_TOO_LARGE,
      AcquiredChannelEnvironmentIssueType.GPS_RECEIVER_UNLOCKED,
      AcquiredChannelEnvironmentIssueType.MAIN_POWER_FAILURE
    );

  @ParameterizedTest
  @MethodSource("testToChannelStatusListProvider")
  void testToChannelStatusList(String channelName, byte[] channelStatusData, Instant startTime,
    Instant endTime) {
    List<AcquiredChannelEnvironmentIssue<?>> acquiredChannelEnvironmentIssues = Cd11AcquiredChannelEnvironmentIssuesParser
      .parseAcquiredChannelSoh(channelStatusData, channelName,
        startTime, endTime);
    // All Statuses False
    if (channelStatusData[1] == 0) {
      assertFalse(getBooleanChannelSohStatuses(acquiredChannelEnvironmentIssues)
        .map(AcquiredChannelEnvironmentIssue::getStatus).collect(
          Collectors.toList()).contains(true));
    }
    // True and False Statuses
    if (channelStatusData[1] == 5) {
      for (AcquiredChannelEnvironmentIssueBoolean acquiredChannelSohBoolean : getBooleanChannelSohStatuses(
        acquiredChannelEnvironmentIssues).collect(
        Collectors.toList())) {
        boolean acquiredChannelSohBooleanStatus = acquiredChannelSohBoolean.getStatus();
        if (ACQUIRED_CHANNEL_SOH_TYPES_SET_TRUE.contains(acquiredChannelSohBoolean.getType())) {
          assertTrue(acquiredChannelSohBooleanStatus);
        } else {
          assertFalse(acquiredChannelSohBooleanStatus);
        }
      }
    }
    // All Statuses True
    if (channelStatusData[1] == 15) {
      assertFalse(getBooleanChannelSohStatuses(acquiredChannelEnvironmentIssues)
        .map(AcquiredChannelEnvironmentIssue::getStatus).collect(
          Collectors.toList()).contains(false));
    }
    // CLOCK_DIFFERENTIAL_IN_MICROSECONDS greater than 0
    if (channelStatusData[28] > 0) {
      AcquiredChannelEnvironmentIssueAnalog acquiredChannelSohAnalog = (AcquiredChannelEnvironmentIssueAnalog) acquiredChannelEnvironmentIssues
        .get(acquiredChannelEnvironmentIssues.size() - 1);
      assertTrue(acquiredChannelSohAnalog.getStatus() > 0);
    }
  }

  static Stream<Arguments> testToChannelStatusListProvider() {

    return Stream.of(
      Arguments.arguments(
        LBTB1_CHANNEL_NAME,
        ALL_FALSE_CHANNEL_STATUSES,
        EXPECTED_START_TIME,
        EXPECTED_TIME_SERIES_END_TIME
      ),
      Arguments.arguments(
        LBTB1_CHANNEL_NAME,
        ALL_TRUE_CHANNEL_STATUSES,
        EXPECTED_START_TIME,
        EXPECTED_TIME_SERIES_END_TIME
      ),
      Arguments.arguments(
        LBTB1_CHANNEL_NAME,
        TRUE_AND_FALSE_CHANNEL_STATUSES,
        EXPECTED_START_TIME,
        EXPECTED_TIME_SERIES_END_TIME
      ),
      Arguments.arguments(
        LBTB1_CHANNEL_NAME,
        NON_ZERO_CLOCK_DIFFERENTIAL_CHANNEL_STATUSES,
        EXPECTED_START_TIME,
        EXPECTED_TIME_SERIES_END_TIME
      )
    );
  }

  @Test
  void testToChannelStatusListWithNonCd11DataStillParse() {
    // expected status array with leading 1, due to CD1.1 Spec, but received array with leading 0;
    // should still parse
    byte[] channelStatusData = new byte[32];
    Arrays.fill(channelStatusData, (byte) 0);

    List<AcquiredChannelEnvironmentIssue<?>> issues = Cd11AcquiredChannelEnvironmentIssuesParser
      .parseAcquiredChannelSoh(channelStatusData, LBTB1_CHANNEL_NAME, EXPECTED_START_TIME,
        EXPECTED_TIME_SERIES_END_TIME);

    //All issue statuses should be false or 0, since status array is zero-filled
    for (AcquiredChannelEnvironmentIssue<?> issue : issues) {
      if (issue.getStatus() instanceof Boolean) {
        assertFalse((Boolean) issue.getStatus());
      } else if (issue.getStatus() instanceof Double) {
        assertEquals(0.0, (Double) issue.getStatus());
      }
    }
  }


  @Test
  void testToChannelStatusListNotEnoughData() {
    // expected 32 bytes, received 31
    byte[] channelStatusData = new byte[31];
    Arrays.fill(channelStatusData, (byte) 0);
    assertEquals(Collections.EMPTY_LIST, Cd11AcquiredChannelEnvironmentIssuesParser
      .parseAcquiredChannelSoh(channelStatusData, LBTB1_CHANNEL_NAME, EXPECTED_START_TIME,
        EXPECTED_TIME_SERIES_END_TIME));
  }

  private Stream<AcquiredChannelEnvironmentIssueBoolean> getBooleanChannelSohStatuses(
    List<AcquiredChannelEnvironmentIssue<?>> acquiredChannelEnvironmentIssues) {
    return acquiredChannelEnvironmentIssues
      .stream()
      .filter(
        acquiredChannelSoh -> acquiredChannelSoh instanceof AcquiredChannelEnvironmentIssueBoolean)
      .map(acquiredChannelSoh -> (AcquiredChannelEnvironmentIssueBoolean) acquiredChannelSoh);
  }

}
