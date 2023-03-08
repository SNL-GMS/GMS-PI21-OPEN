package gms.shared.frameworks.osd.coi.waveforms;

import gms.shared.frameworks.osd.coi.util.TestUtilities;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Optional;

class RawStationDataFrameTests {

  @Test
  void testSerialization() throws IOException {
    RawStationDataFrame expected = WaveformTestFixtures.RAW_STATION_DATA_FRAME;
    TestUtilities.testSerialization(expected, RawStationDataFrame.class);
  }

  @Test
  void testEquals() {
    Assertions.assertTrue(WaveformTestFixtures.RAW_STATION_DATA_FRAME.hasSameStateAndRawPayload(WaveformTestFixtures.RAW_STATION_DATA_FRAME));
    Assertions.assertEquals(WaveformTestFixtures.RAW_STATION_DATA_FRAME, WaveformTestFixtures.RAW_STATION_DATA_FRAME);

    var otherRsdf = WaveformTestFixtures.RAW_STATION_DATA_FRAME.toBuilder().setRawPayload(Optional.empty()).build();
    Assertions.assertFalse(WaveformTestFixtures.RAW_STATION_DATA_FRAME.hasSameStateAndRawPayload(otherRsdf));
    Assertions.assertNotEquals(WaveformTestFixtures.RAW_STATION_DATA_FRAME, otherRsdf);
  }

}