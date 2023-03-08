package gms.dataacquisition.data.preloader;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GenerationTypeTest {

  @ParameterizedTest
  @EnumSource(GenerationType.class)
  void testParse(GenerationType expected) {
    assertEquals(expected, GenerationType.parseType(expected.toString()));
  }

  @ParameterizedTest
  @EnumSource(GenerationType.class)
  void testHasConditions(GenerationType expected) {
    assertTrue(expected.hasConditions(expected.getInitialConditions()));
  }


}