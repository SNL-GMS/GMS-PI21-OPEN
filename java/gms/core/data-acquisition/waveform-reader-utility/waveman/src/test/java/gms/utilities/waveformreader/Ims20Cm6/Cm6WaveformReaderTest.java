package gms.utilities.waveformreader.Ims20Cm6;


import gms.utilities.waveformreader.Cm6WaveformReader;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class Cm6WaveformReaderTest {

  /**
   * Test that the CM6 decompression code is working properly by comparing the decompressed values
   * to "truth" int values.
   */
  @Test
  void testCM6Decompression() throws Exception {
    // Read in contents of CM6 and INT files
    try (InputStream cm6Is = this.getClass().getResourceAsStream(TestFixtures.KURK_CM6_FILE);
         InputStream intIs = this.getClass().getResourceAsStream(TestFixtures.KURK_INT_FILE)) {
      assertNotNull(cm6Is);
      assertNotNull(intIs);

      byte[] cm6Bytes = cm6Is.readAllBytes();
      byte[] intBytes = intIs.readAllBytes();

      List<String> cm6WaveformStrings = getWaveformData(cm6Bytes);
      List<String> intWaveformStrings = getWaveformData(intBytes);

      Cm6WaveformReader waveformReader = new Cm6WaveformReader();
      // For each byte[] containing waveform data:
      //   - Call the decompression function to convert the data into doubles
      //   - Convert those doubles to ints
      //   - Compare those ints to "truth" ints
      for (int i = 0; i < cm6WaveformStrings.size(); i++) {
        try (InputStream waveform = new ByteArrayInputStream(cm6WaveformStrings.get(i).getBytes());
             InputStream truthWaveform = new ByteArrayInputStream(intWaveformStrings.get(i).getBytes())) {
          double[] parsedWaveformDoubles = waveformReader.read(waveform, waveform.available(), 0);
          int[] parsedWaveformInts =
            Arrays.stream(parsedWaveformDoubles)
              .mapToInt((x) -> Double.valueOf(x).intValue())
              .toArray();

          String[] truthWaveformStringArray = new String(truthWaveform.readAllBytes()).split(" ");
          int[] truthWaveformInts =
            Arrays.stream(truthWaveformStringArray)
              .mapToInt(Integer::parseInt)
              .toArray();

          assertArrayEquals(truthWaveformInts, parsedWaveformInts);
        }
      }
    }
  }

  /**
   * Extract just the waveform data from a byte[] payload. For IMS 2.0 data, this is binary data
   * that is compressed into a string with a CM6 compression algorithm.
   *
   * @param rawFramePayload RawStationDataFrame byte[] payload with IMS2.0 data for a single station
   * @return list of waveform byte arrays for each waveform in RawStationDataFrame
   */
  private static List<String> getWaveformData(byte[] rawFramePayload) {
    String payloadString = new String(rawFramePayload);
    // getting rid of newlines makes regex-ing much easier
    payloadString = payloadString.replaceAll("\n", " ");

    String patternString = "DAT2 (.*?)CHK2 \\d+";
    Pattern pattern = Pattern.compile(patternString);
    Matcher matcher = pattern.matcher(payloadString);
    List<String> waveformStrings = new ArrayList<>();

    while (matcher.find()) {
      // The () in the above regex puts the waveform data in group(1)
      String waveform = matcher.group(1).trim();
      waveformStrings.add(waveform);
    }
    return waveformStrings;
  }
}

