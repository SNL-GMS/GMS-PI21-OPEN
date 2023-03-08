package gms.shared.frameworks.osd.coi.systemmessages;

import gms.shared.frameworks.osd.coi.util.TestUtilities;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SystemMessageDefinitionTest {

  private static final SystemMessageDefinition SYSTEM_MESSAGE_DEFINITION =
    SystemMessageDefinition.from(
      SystemMessageType.STATION_SOH_STATUS_CHANGED,
      SystemMessageCategory.SOH,
      SystemMessageSubCategory.CAPABILITY,
      SystemMessageSeverity.INFO);

  @Test
  void testSerialization() throws IOException {
    TestUtilities.testSerialization(SYSTEM_MESSAGE_DEFINITION, SystemMessageDefinition.class);
  }

  @Test
  void testNullSystemMessageType() {
    assertThrows(NullPointerException.class, () -> SystemMessageDefinition.from(null));
  }

  @Test
  void testSystemMessageDefinitionFrom() {

    SystemMessageType type = SystemMessageType.CHANNEL_MONITOR_TYPE_QUIETED;
    SystemMessageDefinition systemMessageDefinition = SystemMessageDefinition.from(type);

    assertEquals(systemMessageDefinition.getSystemMessageType(), type);
    assertEquals(systemMessageDefinition.getSystemMessageCategory(), type.getCategory());
    assertEquals(systemMessageDefinition.getSystemMessageSubCategory(), type.getSubCategory());
    assertEquals(systemMessageDefinition.getSystemMessageSeverity(), type.getSeverity());
    assertEquals(systemMessageDefinition.getTemplate(), type.getMessageTemplate());
  }

}
