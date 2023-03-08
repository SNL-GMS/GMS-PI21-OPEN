package gms.shared.utilities.javautilities.objectmapper;

import gms.shared.frameworks.systemconfig.SystemConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class OracleLivenessCheckTest {

  @Test
  void testCreateValidation() {
    NullPointerException exception = assertThrows(NullPointerException.class, () -> OracleLivenessCheck.create(null));
    assertEquals("SystemConfig cannot be null", exception.getMessage());
  }

  @Test
  void testCreate() {
    OracleLivenessCheck livenessCheck = assertDoesNotThrow(() -> OracleLivenessCheck.create(mock(SystemConfig.class)));
    assertNotNull(livenessCheck);
  }
}