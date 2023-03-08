package gms.utilities.waveformreader;

import java.io.InputStream;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class WaveformReaderTestUtil {

  public static void testReadTestData(WaveformReaderInterface reader, InputStream is, int samplesToRead,
    int samplesToSkip, double[] expected) throws Exception {

    double[] actual = reader.read(is, samplesToRead, samplesToSkip);
    IntStream.range(0, actual.length)
      .forEach(i -> assertEquals(expected[i], actual[i], 1e-7));
  }

}

