package gms.shared.frameworks.osd.coi.systemmessages.util;

import gms.shared.frameworks.osd.coi.soh.PercentSohMonitorValueAndStatus;
import gms.shared.frameworks.osd.coi.soh.SohMonitorType;
import gms.shared.frameworks.osd.coi.soh.SohMonitorValueAndStatus;
import gms.shared.frameworks.osd.coi.soh.SohStatus;
import gms.shared.frameworks.osd.coi.systemmessages.SystemMessage;
import gms.shared.frameworks.osd.coi.systemmessages.SystemMessageCategory;
import gms.shared.frameworks.osd.coi.systemmessages.SystemMessageSeverity;
import gms.shared.frameworks.osd.coi.systemmessages.SystemMessageSubCategory;
import gms.shared.frameworks.osd.coi.systemmessages.SystemMessageType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChannelMonitorTypeStatusChangedBuilderTest {

  @Test
  void testChannelMonitorTypeStatusChangedBuilder() {
    String stationName = "stationA";
    String channelName = "channelA";
    double previousValue = 12.3;
    double currentValue = 99.9;
    SohMonitorType monitorType = SohMonitorType.MISSING;
    SohMonitorValueAndStatus<Double> previousStatus = PercentSohMonitorValueAndStatus
      .from(previousValue, SohStatus.MARGINAL, monitorType);
    SohMonitorValueAndStatus<Double> currentStatus = PercentSohMonitorValueAndStatus
      .from(currentValue, SohStatus.BAD, monitorType);

    SystemMessage sm = new ChannelMonitorTypeStatusChangedBuilder(stationName, channelName,
      monitorType, previousStatus, currentStatus)
      .build();

    assertNotNull(sm);
    assertEquals("Station stationA Channel channelA Missing status changed from "
      + previousValue + "% (MARGINAL) to " + currentValue + "% (BAD)", sm.getMessage());
    assertEquals(SystemMessageType.CHANNEL_MONITOR_TYPE_STATUS_CHANGED, sm.getType());
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
