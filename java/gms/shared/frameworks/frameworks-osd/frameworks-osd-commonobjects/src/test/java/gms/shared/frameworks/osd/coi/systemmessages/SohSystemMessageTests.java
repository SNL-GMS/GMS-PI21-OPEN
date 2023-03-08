package gms.shared.frameworks.osd.coi.systemmessages;

import gms.shared.frameworks.osd.coi.SohTestFixtures;
import gms.shared.frameworks.osd.coi.util.TestUtilities;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SohSystemMessageTests {

  @Test
  void testSerialization() throws IOException {
    String needsAttention = String
      .format(SystemMessageType.STATION_NEEDS_ATTENTION.getMessageTemplate(), "stationA");

    assertEquals("Station stationA needs attention", needsAttention);

    SystemMessageType needsAttentionType = SystemMessageType.STATION_NEEDS_ATTENTION;
    assertEquals(SystemMessageSeverity.CRITICAL, needsAttentionType.getSeverity());
    assertEquals(SystemMessageCategory.SOH, needsAttentionType.getCategory());
    assertEquals(SystemMessageSubCategory.STATION, needsAttentionType.getSubCategory());

    SystemMessage create = SystemMessage.create(Instant.now(), needsAttention,
      needsAttentionType, needsAttentionType.getSeverity(), needsAttentionType.getCategory(),
      needsAttentionType.getSubCategory(),
      Map.of("station", "stationA"));

    TestUtilities.testSerialization(create, SystemMessage.class);

    String statusChanged = String
      .format(SystemMessageType.STATION_SOH_STATUS_CHANGED.getMessageTemplate(), "stationB",
        "GOOD", "BAD");

    assertEquals("Station stationB SOH status changed from GOOD to BAD", statusChanged);

    SystemMessageType stationStatusChangedType = SystemMessageType.STATION_SOH_STATUS_CHANGED;
    assertEquals(SystemMessageSeverity.INFO, stationStatusChangedType.getSeverity());
    assertEquals(SystemMessageCategory.SOH, stationStatusChangedType.getCategory());
    assertEquals(SystemMessageSubCategory.STATION, stationStatusChangedType.getSubCategory());

    SystemMessage from = SystemMessage
      .from(UUID.randomUUID(), Instant.now(), statusChanged,
        stationStatusChangedType, stationStatusChangedType.getSeverity(),
        stationStatusChangedType.getCategory(), stationStatusChangedType.getSubCategory(),
        Map.of("station", "stationB"));

    TestUtilities.testSerialization(from, SystemMessage.class);
  }

  @Test
  void testMessageTemplateParams() {
    String needsAttention = SohTestFixtures.STATION_NEEDS_ATTENTION_MESSAGE_TEMPLATE;
    assertNotNull(needsAttention);
    assertEquals(1, matches(needsAttention));

    String stationStatusChanged = SohTestFixtures.STATION_SOH_STATUS_CHANGED_MESSAGE_TEMPLATE;
    assertNotNull(stationStatusChanged);
    assertEquals(3, matches(stationStatusChanged));

    String stationCapabilityStatusChanged = SohTestFixtures.STATION_CAPABILITY_STATUS_CHANGED_MESSAGE_TEMPLATE;
    assertNotNull(stationCapabilityStatusChanged);
    assertEquals(4, matches(stationCapabilityStatusChanged));

    String stationGroupCapabilityStatusChanged = SohTestFixtures.STATION_GROUP_CAPABILITY_STATUS_CHANGED_MESSAGE_TEMPLATE;
    assertNotNull(stationGroupCapabilityStatusChanged);
    assertEquals(3, matches(stationGroupCapabilityStatusChanged));

    String channelMonitorTypeStatusChanged = SohTestFixtures.CHANNEL_MONITOR_TYPE_STATUS_CHANGED_MESSAGE_TEMPLATE;
    assertNotNull(channelMonitorTypeStatusChanged);
    assertEquals(7, matches(channelMonitorTypeStatusChanged));

    String channelMonitorTypeAcknowledged = SohTestFixtures.CHANNEL_MONITOR_TYPE_STATUS_CHANGE_ACKNOWLEDGED_MESSAGE_TEMPLATE;
    assertNotNull(channelMonitorTypeAcknowledged);

    String[] parts1 = channelMonitorTypeAcknowledged.split("\\|");
    assertNotNull(parts1);
    assertEquals(2, parts1.length);
    assertEquals(4, matches(parts1[0]));
    assertEquals(5, matches(parts1[1]));

    String channelMonitorTypeQuieted = SohTestFixtures.CHANNEL_MONITOR_TYPE_QUIETED_MESSAGE_TEMPLATE;
    assertNotNull(channelMonitorTypeQuieted);

    String[] parts2 = channelMonitorTypeQuieted.split("\\|");
    assertNotNull(parts2);
    assertEquals(2, parts2.length);
    assertEquals(5, matches(parts2[0]));
    assertEquals(6, matches(parts2[1]));

    String channelMonitorTypeQuietPeriodCanceled = SohTestFixtures.CHANNEL_MONITOR_TYPE_QUIET_PERIOD_CANCELED_MESSAGE_TEMPLATE;
    assertNotNull(channelMonitorTypeQuietPeriodCanceled);
    assertEquals(4, matches(channelMonitorTypeQuietPeriodCanceled));

    String channelMonitorTypeQuietPeriodExpired = SohTestFixtures.CHANNEL_MONITOR_TYPE_QUIET_PERIOD_EXPIRED_MESSAGE_TEMPLATE;
    assertNotNull(channelMonitorTypeQuietPeriodExpired);
    assertEquals(3, matches(channelMonitorTypeQuietPeriodExpired));
  }

  int matches(String text) {
    return StringUtils.countMatches(text, "%s");
  }
}
