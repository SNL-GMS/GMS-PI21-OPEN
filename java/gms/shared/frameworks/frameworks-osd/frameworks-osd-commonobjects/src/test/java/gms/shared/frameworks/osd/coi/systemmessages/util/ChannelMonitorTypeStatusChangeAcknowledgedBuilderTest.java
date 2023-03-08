package gms.shared.frameworks.osd.coi.systemmessages.util;

import gms.shared.frameworks.osd.coi.soh.SohMonitorType;
import gms.shared.frameworks.osd.coi.systemmessages.SystemMessage;
import gms.shared.frameworks.osd.coi.systemmessages.SystemMessageCategory;
import gms.shared.frameworks.osd.coi.systemmessages.SystemMessageSeverity;
import gms.shared.frameworks.osd.coi.systemmessages.SystemMessageSubCategory;
import gms.shared.frameworks.osd.coi.systemmessages.SystemMessageType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChannelMonitorTypeStatusChangeAcknowledgedBuilderTest {

  @Test
  void testChannelMonitorTypeStatusChangeAcknowledgedBuilderWithoutComment() {
    String stationName = "stationA";
    String channelName = "channelA";
    SohMonitorType sohMonitorType = SohMonitorType.ENV_AMPLIFIER_SATURATION_DETECTED;
    String userName = "gms";

    SystemMessage sm = new ChannelMonitorTypeStatusChangeAcknowledgedBuilder(stationName,
      channelName, sohMonitorType, userName, null)
      .build();

    assertNotNull(sm);
    assertEquals(
      "Station stationA Channel channelA Amplifier Saturation Detected status change acknowledged by user gms",
      sm.getMessage());
    assertEquals(SystemMessageType.CHANNEL_MONITOR_TYPE_STATUS_CHANGE_ACKNOWLEDGED, sm.getType());
    assertEquals(SystemMessageSeverity.INFO, sm.getSeverity());
    assertEquals(SystemMessageCategory.SOH, sm.getCategory());
    assertEquals(SystemMessageSubCategory.USER, sm.getSubCategory());
    assertTrue(sm.getMessageTags().containsKey("station"));
    assertTrue(sm.getMessageTags().containsValue(stationName));
    assertTrue(sm.getMessageTags().containsKey("channel"));
    assertTrue(sm.getMessageTags().containsValue(channelName));
    assertTrue(sm.getMessageTags().containsKey("monitorType"));
    assertTrue(sm.getMessageTags().containsValue(sohMonitorType));
  }

  @Test
  void testChannelMonitorTypeStatusChangeAcknowledgedBuilderWithComment() {
    String stationName = "stationA";
    String channelName = "channelA";
    SohMonitorType sohMonitorType = SohMonitorType.ENV_AMPLIFIER_SATURATION_DETECTED;
    String userName = "gms";
    String comment = "Acknowledged the change";

    SystemMessage sm = new ChannelMonitorTypeStatusChangeAcknowledgedBuilder(stationName,
      channelName, sohMonitorType, userName, comment)
      .build();

    assertNotNull(sm);
    assertEquals(
      "Station stationA Channel channelA Amplifier Saturation Detected status change acknowledged by user gms with comment 'Acknowledged the change'",
      sm.getMessage());
    assertEquals(SystemMessageType.CHANNEL_MONITOR_TYPE_STATUS_CHANGE_ACKNOWLEDGED, sm.getType());
    assertEquals(SystemMessageSeverity.INFO, sm.getSeverity());
    assertEquals(SystemMessageCategory.SOH, sm.getCategory());
    assertEquals(SystemMessageSubCategory.USER, sm.getSubCategory());
    assertTrue(sm.getMessageTags().containsKey("station"));
    assertTrue(sm.getMessageTags().containsValue(stationName));
    assertTrue(sm.getMessageTags().containsKey("channel"));
    assertTrue(sm.getMessageTags().containsValue(channelName));
    assertTrue(sm.getMessageTags().containsKey("monitorType"));
    assertTrue(sm.getMessageTags().containsValue(sohMonitorType));
  }
}
