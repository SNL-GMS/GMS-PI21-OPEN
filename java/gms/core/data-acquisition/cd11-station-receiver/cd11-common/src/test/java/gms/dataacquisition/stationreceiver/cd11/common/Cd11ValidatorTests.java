package gms.dataacquisition.stationreceiver.cd11.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Cd11ValidatorTests {


  @Test
  void testValidPortNumber() {
    assertThrows(IllegalArgumentException.class, () -> Cd11Validator.validPortNumber(-1));
    assertDoesNotThrow(() -> Cd11Validator.validPortNumber(0));
    assertDoesNotThrow(() -> Cd11Validator.validPortNumber(1));
    assertDoesNotThrow(() -> Cd11Validator.validPortNumber(12345));
    assertDoesNotThrow(() -> Cd11Validator.validPortNumber(65535));
    assertThrows(IllegalArgumentException.class, () -> Cd11Validator.validPortNumber(65536));
  }

  @Test
  void testValidNonZeroPortNumber() {
    assertThrows(IllegalArgumentException.class, () -> Cd11Validator.validNonZeroPortNumber(-1));
    assertThrows(IllegalArgumentException.class, () -> Cd11Validator.validNonZeroPortNumber(0));
    assertDoesNotThrow(() -> Cd11Validator.validNonZeroPortNumber(1));
    assertDoesNotThrow(() -> Cd11Validator.validNonZeroPortNumber(12345));
    assertDoesNotThrow(() -> Cd11Validator.validNonZeroPortNumber(65535));
    assertThrows(IllegalArgumentException.class, () -> Cd11Validator.validNonZeroPortNumber(65536));
  }

  @Test
  void testValidServiceType() {
    assertThrows(NullPointerException.class, () -> Cd11Validator.validServiceType(null));
    assertThrows(IllegalArgumentException.class, () -> Cd11Validator.validServiceType(""));
    assertThrows(IllegalArgumentException.class, () -> Cd11Validator.validServiceType("  "));
    assertThrows(IllegalArgumentException.class, () -> Cd11Validator.validServiceType("   "));
    assertThrows(IllegalArgumentException.class, () -> Cd11Validator.validServiceType("    "));
    assertThrows(IllegalArgumentException.class, () -> Cd11Validator.validServiceType("UDP"));
    assertThrows(IllegalArgumentException.class, () -> Cd11Validator.validServiceType("SNMP"));
    assertEquals("TCP", Cd11Validator.validServiceType("TCP"));
  }

}
