package gms.shared.event.coi.type;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DepthMethodTests {

  @Test
  void testNewValueMap() {

    assertEquals("a", DepthMethod.A.label);
    assertEquals("f", DepthMethod.F.label);
    assertEquals("d", DepthMethod.D.label);
    assertEquals("r", DepthMethod.R.label);
    assertEquals("g", DepthMethod.G.label);
    assertEquals("-", DepthMethod.UNKNOWN.label);
    assertEquals("A", DepthMethod.valueOfLabel("a").name());
    assertEquals("F", DepthMethod.valueOfLabel("f").name());
    assertEquals("D", DepthMethod.valueOfLabel("d").name());
    assertEquals("R", DepthMethod.valueOfLabel("r").name());
    assertEquals("G", DepthMethod.valueOfLabel("g").name());
    assertEquals("UNKNOWN", DepthMethod.valueOfLabel("-").name());
    assertEquals(DepthMethod.A, DepthMethod.valueOfLabel("a"));
    assertEquals(DepthMethod.F, DepthMethod.valueOfLabel("f"));
    assertEquals(DepthMethod.D, DepthMethod.valueOfLabel("d"));
    assertEquals(DepthMethod.R, DepthMethod.valueOfLabel("r"));
    assertEquals(DepthMethod.G, DepthMethod.valueOfLabel("g"));
    assertEquals(DepthMethod.UNKNOWN, DepthMethod.valueOfLabel("-"));
  }

  @Test
  void testInvalidValueOfLabel() {
    assertThrows(NullPointerException.class, () -> DepthMethod.valueOfLabel(null));
    assertThrows(IllegalArgumentException.class, () -> DepthMethod.valueOfLabel(""));
    assertThrows(IllegalArgumentException.class, () -> DepthMethod.valueOfLabel("    "));
  }
}