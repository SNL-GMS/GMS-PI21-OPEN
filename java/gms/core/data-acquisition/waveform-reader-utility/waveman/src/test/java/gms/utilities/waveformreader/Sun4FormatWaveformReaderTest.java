package gms.utilities.waveformreader;


import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;


/**
 * Test the functionality of the Sun4FormatWaveformReader WaveformReader.
 */
class Sun4FormatWaveformReaderTest {

  private final WaveformReaderInterface reader = new Sun4FormatWaveformReader();
  private final String TEST_DATA_DIR = "/css/WFS4/";
  private final String DAVOX0_FILE = TEST_DATA_DIR + "DAVOX0.w";
  private final String MLR0_FILE = TEST_DATA_DIR + "MLR0.w";
  private final String SIV0_FILE = TEST_DATA_DIR + "SIV0.w";
  private final String WRA0_FILE = TEST_DATA_DIR + "WRA0.w";
  private final String KMBO_FILE = TEST_DATA_DIR + "I22FR.s4.w";
  private final List<String> TEST_FILES = Arrays.asList(
    DAVOX0_FILE, MLR0_FILE, SIV0_FILE, WRA0_FILE, KMBO_FILE);

  // known samples from the files, taken out by reading them.
  // This is used for testing against regressions.
  private final Map<String, Integer[]> firstSamples = Map.of(
    DAVOX0_FILE, new Integer[]{281, 282, 280, 277, 277, 277, 278, 277, 278, 277},
    MLR0_FILE, new Integer[]{44, 41, 39, 39, 39, 38, 36, 35, 33, 32},
    SIV0_FILE, new Integer[]{271, 19, -405, -974, -1493, -1719, -1516, -865, 138, 1231},
    WRA0_FILE, new Integer[]{175, 129, 38, 73, -42, -112, -146, -240, -253, -333},
    KMBO_FILE, new Integer[]{-11129, -10996, -10919, -10813, -10713, -10681, -10617, -10674, -10598, -10356}
  );

  @Test
  void testReadTestData() throws Exception {
    final int SAMPLES_TO_READ = 10;

    for (String testFile : TEST_FILES) {
      // Get an InputStream for the test file.
      try (InputStream is = this.getClass().getResourceAsStream(testFile)) {
        assertNotNull(is);

        // Read the first SAMPLES_TO_READ samples out of the input file using the reader.
        double[] samples = reader.read(is, 10, 0);
        assertNotNull(samples);
        assertEquals(samples.length, SAMPLES_TO_READ);

        // Get the expected result, check some properties of it.
        Integer[] expected = firstSamples.get(testFile);
        assertNotNull(expected);
        assertEquals(expected.length, SAMPLES_TO_READ);
        assertEquals(samples.length, expected.length);
        // Compare the actual and expected samples read out.  This seems to be the cleanest way to do it...
        // issue is that the 'expected' is an Integer[] so that it can be the value of a Map.
        IntStream.range(0, samples.length)
          .forEach(i -> assertEquals(samples[i], expected[i].doubleValue(), 1e-7));
      }
    }
  }

  @Test
  void testReadNullInputThrowsNullPointerException() {
    assertThrows(NullPointerException.class, () -> reader.read(null, 0, 0));
  }

  @Test
  void testReadTestData_skipError() throws Exception {
    assertThrows(IOException.class, () -> WaveformReaderTestUtil
      .testReadTestData(reader, this.getClass().getResourceAsStream(DAVOX0_FILE),
        10, -2, new double[]{}));
  }

}
