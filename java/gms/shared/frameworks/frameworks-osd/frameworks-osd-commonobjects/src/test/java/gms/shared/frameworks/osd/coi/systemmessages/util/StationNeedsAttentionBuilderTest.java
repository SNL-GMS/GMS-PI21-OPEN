package gms.shared.frameworks.osd.coi.systemmessages.util;

import gms.shared.frameworks.osd.coi.systemmessages.SystemMessage;
import gms.shared.frameworks.osd.coi.systemmessages.SystemMessageCategory;
import gms.shared.frameworks.osd.coi.systemmessages.SystemMessageSeverity;
import gms.shared.frameworks.osd.coi.systemmessages.SystemMessageSubCategory;
import gms.shared.frameworks.osd.coi.systemmessages.SystemMessageType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StationNeedsAttentionBuilderTest {

  @Test
  void testStationNeedsAttentionBuilder() {
    String stationName = "stationA";

    SystemMessage sm = new StationNeedsAttentionBuilder(stationName).build();

    assertNotNull(sm);
    assertEquals("Station stationA needs attention", sm.getMessage());
    assertEquals(SystemMessageType.STATION_NEEDS_ATTENTION, sm.getType());
    assertEquals(SystemMessageSeverity.CRITICAL, sm.getSeverity());
    assertEquals(SystemMessageCategory.SOH, sm.getCategory());
    assertEquals(SystemMessageSubCategory.STATION, sm.getSubCategory());
    assertTrue(sm.getMessageTags().containsKey("station"));
    assertTrue(sm.getMessageTags().containsValue(stationName));
  }
}
