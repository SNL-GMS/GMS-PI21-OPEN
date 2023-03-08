package gms.utilities.waveformreader;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class I4FormatWaveformReader implements WaveformReaderInterface {

  private static final Logger logger = LoggerFactory.getLogger(I4FormatWaveformReader.class);

  /**
   * Reads the InputStream as an S4 waveform.
   *
   * @param input the input stream to read from
   * @param numSamples number of bytes to read
   * @param skip number of bytes to skip
   * @return int[] of digitizer counts from the waveform
   */
  public double[] read(InputStream input, int numSamples, int skip) throws IOException {
    Validate.notNull(input);

    double[] data = new double[numSamples];
    if (input.skip(skip * 4L) != skip * 4L) {
      logger.error("The skip method returned an invalid number of bytes skipped.");
      throw new IOException("Skip resulted in error");
    } else {
      DataInputStream dis = new DataInputStream(input);

      int i = 0;
      for (; i < numSamples && dis.available() > 0; i++) {
        data[i] = Integer.reverseBytes(dis.readInt());
      }
    }
    return data;

  }
}
