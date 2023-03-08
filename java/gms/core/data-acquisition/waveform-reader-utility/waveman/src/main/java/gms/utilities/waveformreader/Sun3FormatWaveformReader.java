package gms.utilities.waveformreader;

import org.apache.commons.lang3.Validate;

import java.io.IOException;
import java.io.InputStream;

/**
 * Code for reading waveform format 's3', SUN integer (3 bytes).
 */
public class Sun3FormatWaveformReader implements WaveformReaderInterface {

  /**
   * Reads the InputStream as an S3 waveform.
   *
   * @param input the input stream to read from
   * @param skip number of samples to skip
   * @param numSamples number of samples to read
   * @return
   * @throws IOException
   */
  public double[] read(InputStream input, int numSamples, int skip) throws IOException {
    Validate.notNull(input);

    long skipBytes = skip * 3 / Byte.SIZE;
    skipBytes = Math.min(input.available(), skipBytes);
    long skippedBytes = input.skip(skipBytes);
    if (skipBytes != skippedBytes) {
      throw new IOException("Bytes to skip was: " + skipBytes + ", but actual bytes skipped was: " + skippedBytes);
    }

    BitInputStream bis = new BitInputStream(input, 1024);

    double[] data = new double[numSamples];
    int i = 0;
    for (; i < numSamples && bis.available() > 0; i++)
      data[i] = bis.read(24, true);

    // Check if no data could be read
    if (i == 0)
      return new double[]{};

    return data;
  }

}
