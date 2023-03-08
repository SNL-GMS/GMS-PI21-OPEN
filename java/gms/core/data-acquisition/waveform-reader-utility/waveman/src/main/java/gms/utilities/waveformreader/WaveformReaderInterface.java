package gms.utilities.waveformreader;

import java.io.IOException;
import java.io.InputStream;

/**
 * Functional interface for a WaveformReader; takes an InputStream, number of samples to skip, and
 * number of samples to read, returning a parsed int[] (digitized counts of a waveform).
 */
@FunctionalInterface
public interface WaveformReaderInterface {

  /**
   * Reads a waveform given an InputStream.
   *
   * @param input the input stream to read from
   * @param skip number of samples to skip
   * @param numSamples number of samples to read
   * @return digitizer counts as int[]
   * @throws IOException if I/O problems occur during reading from InputStream
   */
  double[] read(InputStream input, int numSamples, int skip) throws IOException;
}
