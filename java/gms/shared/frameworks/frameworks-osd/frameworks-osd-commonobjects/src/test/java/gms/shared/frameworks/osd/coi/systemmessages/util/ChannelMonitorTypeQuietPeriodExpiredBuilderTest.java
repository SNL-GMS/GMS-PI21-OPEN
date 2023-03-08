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

class ChannelMonitorTypeQuietPeriodExpiredBuilderTest {

  @Test
  void testChannelMonitorTypeQuietPeriodExpiredBuilder() {
    String stationName = "stationA";
    String channelName = "channelA";
    SohMonitorType monitorType = SohMonitorType.ENV_AMPLIFIER_SATURATION_DETECTED;

    SystemMessage sm = new ChannelMonitorTypeQuietPeriodExpiredBuilder(stationName, channelName,
      monitorType)
      .build();

    assertNotNull(sm);
    assertEquals(
      "Station stationA Channel channelA Amplifier Saturation Detected quiet period expired",
      sm.getMessage());
    assertEquals(SystemMessageType.CHANNEL_MONITOR_TYPE_QUIET_PERIOD_EXPIRED, sm.getType());
    assertEquals(SystemMessageSeverity.INFO, sm.getSeverity());
    assertEquals(SystemMessageCategory.SOH, sm.getCategory());
    assertEquals(SystemMessageSubCategory.STATION, sm.getSubCategory());
    assertTrue(sm.getMessageTags().containsKey("station"));
    assertTrue(sm.getMessageTags().containsValue(stationName));
    assertTrue(sm.getMessageTags().containsKey("channel"));
    assertTrue(sm.getMessageTags().containsValue(channelName));
    assertTrue(sm.getMessageTags().containsKey("monitorType"));
    assertTrue(sm.getMessageTags().containsValue(monitorType));
  }
}
