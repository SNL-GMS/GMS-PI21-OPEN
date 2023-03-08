package gms.shared.frameworks.osd.coi.waveforms;

import gms.shared.frameworks.osd.coi.fk.FkTestFixtures;
import gms.shared.frameworks.osd.coi.util.TestUtilities;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FkSpectrumTests {

  @Test
  void testFkSpectrum() {
    double[][] power = new double[][]{{0.1, 0.1, 0.1}, {0.1, 0.5, 0.1}, {0.1, 0.1, 0.1}};
    double[][] fstat = new double[][]{{0.1, 0.1, 0.1}, {0.1, 0.1, 0.1}, {0.1, 0.1, 0.1}};
    int quality = 1;
    FkAttributes attributes = FkAttributes.from(0.1, 0.2,
      0.3, 0.4, 0.5);

    FkSpectrum fk = FkSpectrum.from(power, fstat, quality);

    assertTrue(Arrays.deepEquals(power, fk.getPower().copyOf()));
    assertTrue(Arrays.deepEquals(fstat, fk.getFstat().copyOf()));
    assertEquals(quality, fk.getQuality());

    fk = FkSpectrum.from(power, fstat, quality, List.of(attributes));
    assertTrue(Arrays.deepEquals(power, fk.getPower().copyOf()));
    assertTrue(Arrays.deepEquals(fstat, fk.getFstat().copyOf()));
    assertEquals(quality, fk.getQuality());
    assertEquals(1, fk.getAttributes().size());
    assertEquals(attributes, fk.getAttributes().get(0));

  }

  @Test
  void testValidation() {

    assertAll("Empty array validation",
      () -> assertThrows(IllegalArgumentException.class,
        () -> FkSpectrum.from(new double[][]{}, new double[][]{{0.1}}, 1),
        "Expected exception with empty power array"),
      () -> assertThrows(IllegalArgumentException.class,
        () -> FkSpectrum.from(new double[][]{{0.1}}, new double[][]{}, 1),
        "Expected exception with empty fstat array"));

    assertThrows(IllegalStateException.class,
      () -> FkSpectrum.from(new double[][]{{0.1}},
        new double[][]{{0.1, 0.1}, {0.1, 0.1}}, 1),
      "Expected exception when power and fstat dimensions do not match.");
  }

  @Test
  void testSerialization() throws Exception {
    TestUtilities.testSerialization(FkTestFixtures.fkSpectrum, FkSpectrum.class);
  }

}
