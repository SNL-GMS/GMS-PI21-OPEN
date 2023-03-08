package gms.shared.frameworks.osd.coi.systemmessages.util;

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

class StationGroupCapabilityStatusChangedBuilderTest {

  @Test
  void testStationGroupCapabilityStatusChangedBuilder() {
    String stationGroupName = "stationGroupA";
    SohStatus previousStatus = SohStatus.MARGINAL;
    SohStatus currentStatus = SohStatus.BAD;

    SystemMessage sm = new StationGroupCapabilityStatusChangedBuilder(stationGroupName,
      previousStatus, currentStatus).build();

    assertNotNull(sm);
    assertEquals("Station Group stationGroupA capability status changed from MARGINAL to BAD",
      sm.getMessage());
    assertEquals(SystemMessageType.STATION_GROUP_CAPABILITY_STATUS_CHANGED, sm.getType());
    assertEquals(SystemMessageSeverity.WARNING, sm.getSeverity());
    assertEquals(SystemMessageCategory.SOH, sm.getCategory());
    assertEquals(SystemMessageSubCategory.CAPABILITY, sm.getSubCategory());
    assertTrue(sm.getMessageTags().containsKey("stationGroup"));
    assertTrue(sm.getMessageTags().containsValue(stationGroupName));
  }
}
