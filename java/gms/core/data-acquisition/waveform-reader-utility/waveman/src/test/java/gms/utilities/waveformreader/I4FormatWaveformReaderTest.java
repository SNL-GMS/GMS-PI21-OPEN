package gms.utilities.waveformreader;


import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;

class I4FormatWaveformReaderTest {
  private final WaveformReaderInterface reader = new I4FormatWaveformReader();
  private final String WFE1_FILE = "/css/WFS4/i4.w";
  private final int SAMPLES_TO_READ = 10;
  private final int SAMPLES_TO_SKIP = 0;
  private final double[] REF_SAMPLES = {-456.0, -454.0, -454.0, -455.0, -456.0, -461.0, -461.0, -460.0, -453.0, -451.0};

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