package gms.utilities.waveformreader;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;

class Sun2FormatWaveformReaderTest {
  private final WaveformReaderInterface reader = new Sun2FormatWaveformReader();
  private final String WFE1_FILE = "/css/WFS4/S2Test.w";
  private final int SAMPLES_TO_READ = 10;
  private final int SAMPLES_TO_SKIP = 0;
  private final double[] REF_SAMPLES = {14590.0, 15102.0, 15102.0, 14846.0, 14590.0, 13310.0, 13310.0, 13566.0, 15358.0, 15870.0};

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