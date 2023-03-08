package gms.utilities.waveformreader;


import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CanadianCompressedWaveformReaderTest {
  private final CanadianCompressedWaveformReader reader = new CanadianCompressedWaveformReader();
  private final String WFE1_FILE = "/css/WFS4/cc.w";
  private final int SAMPLES_TO_READ = 10;
  private final int SAMPLES_TO_SKIP = 0;
  private final double[] REF_SAMPLES = {774.0, 708.0, 637.0, 391.0, 128.0, -393.0, -957.0, -1974.0, -3056.0, -4155.0};

  @Test
  void testReadTestData() throws Exception {
    double[] actual = reader.read(this.getClass().getResourceAsStream(WFE1_FILE),
      SAMPLES_TO_READ,
      SAMPLES_TO_SKIP);
    IntStream.range(0, actual.length)
      .forEach(i -> assertEquals(REF_SAMPLES[i], actual[i], 1e-7));

    // TODO: revert when todo in class is resolved and it can implement the interface again
//    WaveformReaderTestUtil
//      .testReadTestData(reader, this.getClass().getResourceAsStream(WFE1_FILE),
//        SAMPLES_TO_READ, SAMPLES_TO_SKIP, REF_SAMPLES);
  }
}