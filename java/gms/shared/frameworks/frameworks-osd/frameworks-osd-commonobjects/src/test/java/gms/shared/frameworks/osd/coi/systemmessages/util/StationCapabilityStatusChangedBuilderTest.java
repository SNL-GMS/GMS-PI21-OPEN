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

class StationCapabilityStatusChangedBuilderTest {

  @Test
  void testStationCapabilityStatusChangedBuilder() {
    String stationName = "stationA";
    String stationGroupName = "stationGroupB";
    SohStatus previousStatus = SohStatus.BAD;
    SohStatus currentStatus = SohStatus.GOOD;

    SystemMessage sm = new StationCapabilityStatusChangedBuilder(stationName, stationGroupName,
      previousStatus, currentStatus).build();

    assertNotNull(sm);
    assertEquals(
      "Station stationA capability status for Station Group stationGroupB changed from BAD to GOOD",
      sm.getMessage());
    assertEquals(SystemMessageType.STATION_CAPABILITY_STATUS_CHANGED, sm.getType());
    assertEquals(SystemMessageSeverity.WARNING, sm.getSeverity());
    assertEquals(SystemMessageCategory.SOH, sm.getCategory());
    assertEquals(SystemMessageSubCategory.CAPABILITY, sm.getSubCategory());
    assertTrue(sm.getMessageTags().containsKey("station"));
    assertTrue(sm.getMessageTags().containsValue(stationName));
    assertTrue(sm.getMessageTags().containsKey("stationGroup"));
    assertTrue(sm.getMessageTags().containsValue(stationGroupName));
  }
}
