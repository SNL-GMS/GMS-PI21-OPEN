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

class StationSohStatusChangedBuilderTest {

  @Test
  void testStationSohStatusChangedBuilder() {
    String stationName = "stationA";
    SohStatus previousStatus = SohStatus.MARGINAL;
    SohStatus currentStatus = SohStatus.BAD;

    SystemMessage sm = new StationSohStatusChangedBuilder(stationName, previousStatus,
      currentStatus).build();

    assertNotNull(sm);
    assertEquals("Station stationA SOH status changed from MARGINAL to BAD", sm.getMessage());
    assertEquals(SystemMessageType.STATION_SOH_STATUS_CHANGED, sm.getType());
    assertEquals(SystemMessageSeverity.INFO, sm.getSeverity());
    assertEquals(SystemMessageCategory.SOH, sm.getCategory());
    assertEquals(SystemMessageSubCategory.STATION, sm.getSubCategory());
    assertTrue(sm.getMessageTags().containsKey("station"));
    assertTrue(sm.getMessageTags().containsValue(stationName));
  }
}
