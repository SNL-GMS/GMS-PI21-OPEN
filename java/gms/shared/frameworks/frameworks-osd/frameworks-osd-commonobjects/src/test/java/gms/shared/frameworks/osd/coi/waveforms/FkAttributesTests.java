package gms.shared.frameworks.osd.coi.waveforms;

import gms.shared.frameworks.osd.coi.fk.FkTestFixtures;
import gms.shared.frameworks.osd.coi.util.TestUtilities;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FkAttributesTests {

  //TODO: More sane defaults.
  private final double azimuth = 1;
  private final double slowness = 1;
  private final double azimuthUncertainty = 1;
  private final double slownessUncertainty = 1;
  private final double peakFStat = 0.04;

  @Test
  void testSerialization() throws Exception {
    TestUtilities.testSerialization(FkTestFixtures.fkAttributes(), FkAttributes.class);
  }

  @Test
  void testCreate() throws Exception {

    FkAttributes fkAttributes = FkAttributes
      .from(azimuth, slowness, azimuthUncertainty, slownessUncertainty, peakFStat);

    assertTrue(fkAttributes.getAzimuth() == azimuth &&
      fkAttributes.getSlowness() == slowness &&
      fkAttributes.getAzimuthUncertainty() == azimuthUncertainty &&
      fkAttributes.getSlownessUncertainty() == slownessUncertainty &&
      fkAttributes.getPeakFStat() == peakFStat);
  }

  @Test
  void testFrom() throws Exception {

    FkAttributes fkAttributes = FkAttributes
      .from(azimuth, slowness, azimuthUncertainty, slownessUncertainty, peakFStat);

    assertTrue(fkAttributes.getAzimuth() == azimuth &&
      fkAttributes.getSlowness() == slowness &&
      fkAttributes.getAzimuthUncertainty() == azimuthUncertainty &&
      fkAttributes.getSlownessUncertainty() == slownessUncertainty &&
      fkAttributes.getPeakFStat() == peakFStat);
  }

  @Test
  void testFieldBadBounds() throws Exception {

    assertThrows(IllegalArgumentException.class, () -> FkAttributes
      .from(azimuth, slowness, -1, slownessUncertainty, peakFStat));


    assertThrows(IllegalArgumentException.class, () -> FkAttributes
      .from(azimuth, slowness, azimuthUncertainty, -1, peakFStat));
  }
}
