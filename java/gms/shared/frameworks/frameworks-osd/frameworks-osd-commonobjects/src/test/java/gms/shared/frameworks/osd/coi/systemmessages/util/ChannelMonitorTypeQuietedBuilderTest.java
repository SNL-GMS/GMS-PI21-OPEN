package gms.shared.frameworks.osd.coi.systemmessages.util;

import gms.shared.frameworks.osd.coi.soh.SohMonitorType;
import gms.shared.frameworks.osd.coi.systemmessages.SystemMessage;
import gms.shared.frameworks.osd.coi.systemmessages.SystemMessageCategory;
import gms.shared.frameworks.osd.coi.systemmessages.SystemMessageSeverity;
import gms.shared.frameworks.osd.coi.systemmessages.SystemMessageSubCategory;
import gms.shared.frameworks.osd.coi.systemmessages.SystemMessageType;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static gms.shared.frameworks.osd.coi.systemmessages.SystemMessageTagNames.CHANNEL;
import static gms.shared.frameworks.osd.coi.systemmessages.SystemMessageTagNames.MONITOR_TYPE;
import static gms.shared.frameworks.osd.coi.systemmessages.SystemMessageTagNames.STATION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChannelMonitorTypeQuietedBuilderTest {

  @Test
  void testChannelMonitorTypeQuietedBuilderWithoutComment() {
    String stationName = "stationA";
    String channelName = "channelA";
    SohMonitorType sohMonitorType = SohMonitorType.MISSING;
    Duration timeInterval = Duration.ofDays(1);
    String userName = "gms";

    SystemMessage sm = new ChannelMonitorTypeQuietedBuilder(stationName, channelName,
      sohMonitorType, timeInterval, userName, null)
      .build();

    assertNotNull(sm);
    assertEquals(
      "Station stationA Channel channelA Missing quieted for 1 day by user gms",
      sm.getMessage());
    assertEquals(SystemMessageType.CHANNEL_MONITOR_TYPE_QUIETED, sm.getType());
    assertEquals(SystemMessageSeverity.WARNING, sm.getSeverity());
    assertEquals(SystemMessageCategory.SOH, sm.getCategory());
    assertEquals(SystemMessageSubCategory.USER, sm.getSubCategory());
    assertTrue(sm.getMessageTags().containsKey(STATION.getTagName()));
    assertTrue(sm.getMessageTags().containsValue(stationName));
    assertTrue(sm.getMessageTags().containsKey(CHANNEL.getTagName()));
    assertTrue(sm.getMessageTags().containsValue(channelName));
    assertTrue(sm.getMessageTags().containsKey(MONITOR_TYPE.getTagName()));
    assertTrue(sm.getMessageTags().containsValue(sohMonitorType));
  }

  @Test
  void testChannelMonitorTypeQuietedBuilderWithComment() {
    String stationName = "stationA";
    String channelName = "channelA";
    SohMonitorType sohMonitorType = SohMonitorType.MISSING;
    Duration timeInterval = Duration.ofDays(1);
    String userName = "gms";
    String comment = "Acknowledged the change";

    SystemMessage sm = new ChannelMonitorTypeQuietedBuilder(stationName, channelName,
      sohMonitorType, timeInterval, userName, comment).build();

    assertNotNull(sm);
    assertEquals(
      "Station stationA Channel channelA Missing quieted for 1 day by user gms with comment 'Acknowledged the change'",
      sm.getMessage());
    assertEquals(SystemMessageType.CHANNEL_MONITOR_TYPE_QUIETED, sm.getType());
    assertEquals(SystemMessageSeverity.WARNING, sm.getSeverity());
    assertEquals(SystemMessageCategory.SOH, sm.getCategory());
    assertEquals(SystemMessageSubCategory.USER, sm.getSubCategory());
    assertTrue(sm.getMessageTags().containsKey(STATION.getTagName()));
    assertTrue(sm.getMessageTags().containsValue(stationName));
    assertTrue(sm.getMessageTags().containsKey(CHANNEL.getTagName()));
    assertTrue(sm.getMessageTags().containsValue(channelName));
    assertTrue(sm.getMessageTags().containsKey(MONITOR_TYPE.getTagName()));
    assertTrue(sm.getMessageTags().containsValue(sohMonitorType));
  }
}
