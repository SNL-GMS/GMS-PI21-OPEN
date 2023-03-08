package gms.utilities.waveformreader;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;

class SunSinglePrecisionRealTest {

  private final WaveformReaderInterface reader = new SunSinglePrecisionReal();
  private final String WFE1_FILE = "/css/WFS4/T4Test.w";
  private final int SAMPLES_TO_READ = 10;
  private final int SAMPLES_TO_SKIP = 0;
  private final double[] REF_SAMPLES = {-1760.7513427734375, -1567.1885986328125, -1015.978759765625, -126.05416107177734,
    922.51416015625, 1922.5946044921875, 2803.912841796875, 3522.66943359375, 3931.1787109375, 3839.6103515625};

  @Test
  void testReadTestData() throws Exception {
    WaveformReaderTestUtil
      .testReadTestData(reader, this.getClass().getResourceAsStream(WFE1_FILE),
        SAMPLES_TO_READ, SAMPLES_TO_SKIP, REF_SAMPLES);
  }

  @Test
  void testReadTestData_skipError() throws Exception {
    assertThrows(IOException.class, () -> WaveformReaderTestUtil
      .testReadTestData(reader, this.getClass().getResourceAsStream(WFE1_FILE),
        SAMPLES_TO_READ, -2, new double[]{}));
  }
}
