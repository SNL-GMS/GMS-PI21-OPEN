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

class ChannelMonitorTypeQuietPeriodCanceledBuilderTest {

  @Test
  void testChannelMonitorTypeQuietPeriodCanceledBuilder() {
    String stationName = "stationA";
    String channelName = "channelA";
    SohMonitorType monitorType = SohMonitorType.ENV_AMPLIFIER_SATURATION_DETECTED;
    String userName = "jshmoe";  // TODO: find out how to get userName

    SystemMessage sm = new ChannelMonitorTypeQuietPeriodCanceledBuilder(stationName, channelName,
      monitorType, userName)
      .build();

    assertNotNull(sm);
    assertEquals(
      String.format(
        "Station stationA Channel channelA Amplifier Saturation Detected quiet period canceled by user %s",
        userName),
      sm.getMessage());
    assertEquals(SystemMessageType.CHANNEL_MONITOR_TYPE_QUIET_PERIOD_CANCELED, sm.getType());
    assertEquals(SystemMessageSeverity.INFO, sm.getSeverity());
    assertEquals(SystemMessageCategory.SOH, sm.getCategory());
    assertEquals(SystemMessageSubCategory.USER, sm.getSubCategory());
    assertTrue(sm.getMessageTags().containsKey("station"));
    assertTrue(sm.getMessageTags().containsValue(stationName));
    assertTrue(sm.getMessageTags().containsKey("channel"));
    assertTrue(sm.getMessageTags().containsValue(channelName));
    assertTrue(sm.getMessageTags().containsKey("monitorType"));
    assertTrue(sm.getMessageTags().containsValue(monitorType));
  }
}
