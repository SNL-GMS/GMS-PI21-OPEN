package gms.utilities.waveformreader;

import org.apache.commons.lang3.Validate;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Code for reading waveform format 'f4', VAX single precision real (4 bytes).
 */
public class Float4FormatWaveformReader implements WaveformReaderInterface {

  private static final int VAX_SINGLE_BIAS = 0x81;

  private static final int IEEE_SINGLE_BIAS = 0x7f;

  /**
   * Reads the Input Stream as an F4 waveform.
   *
   * @param input the input stream to read from
   * @param numSamples number of samples to read
   * @param skip number of samples to skip
   * @return
   * @throws IOException
   */
  public double[] read(InputStream input, int numSamples, int skip) throws IOException {
    Validate.notNull(input);

    int skipBytes = skip * Float.SIZE / Byte.SIZE;
    skipBytes = Math.min(input.available(), skipBytes);
    long skippedBytes = input.skip(skipBytes);

    if (skipBytes != skippedBytes) {
      throw new IOException("Bytes to skip was: " + skipBytes + ", but actual bytes skipped was: " + skippedBytes);
    }

    DataInputStream dis = new DataInputStream(input);

    double[] data = new double[numSamples];
    int i = 0;
    for (; i < numSamples && dis.available() > 0; i++) {
      data[i] = vax2float(dis.readFloat());
    }

    return data;

  }

  /**
   * Convert a float from VAX F to ieee float
   * <p>
   * VAX F: Mantissa 2 |S Exp | M1 MMMMMMMM|MMMMMMMM|SEEEEEEE|EMMMMMMM
   * <p>
   * IEEE Float: S Exp | M1 | Mantissa 2
   * SEEEEEEE|EMMMMMMM|MMMMMMMM|MMMMMMMM
   * <p>
   * E: Exponent M: Mantissa S: Sign
   *
   * @param f vax float
   * @return ieee float
   */
  public static float vax2float(float f) {
    int i = Float.floatToRawIntBits(f);
    i = Integer.reverseBytes(i);

    // Extract the sign (0=positive, 1=negative)
    int sign = ((i >> 15) & 0x1);

    // Extract the exponent
    int exp = ((i >> 7) & 0xFF) - VAX_SINGLE_BIAS + IEEE_SINGLE_BIAS;

    // Extract the mantissa
    int mantissa1 = (i & 0x7F);
    int mantissa2 = ((i >> 16) & 0xFFFF);

    // Re-assemble the float
    i = (sign << 31) | (exp << 23) | (mantissa1 << 16) | mantissa2;

    return Float.intBitsToFloat(i);
  }

}
