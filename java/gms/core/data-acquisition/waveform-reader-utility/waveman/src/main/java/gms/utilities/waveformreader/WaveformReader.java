package gms.utilities.waveformreader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class WaveformReader {

  private WaveformReader() {
  }

  private static final Logger logger = LoggerFactory.getLogger(WaveformReader.class);

  // Mapping from Format to WaveformReader.  This gives a WaveformReader that can be used to read
  // a particular Format of waveform.
  private static final Map<FormatCode, WaveformReaderInterface> formatReaders = Map.of(
    FormatCode.F4, new Float4FormatWaveformReader(),
    FormatCode.S3, new Sun3FormatWaveformReader(),
    FormatCode.S4, new Sun4FormatWaveformReader(),
    FormatCode.I4, new I4FormatWaveformReader(),
    FormatCode.CD, new CanadianCompressedWaveformReader(),
    FormatCode.CC, new CanadianCompressedWaveformReader(),
    FormatCode.E1, new E1FormatWaveformReader(),
    FormatCode.CM6, new Cm6WaveformReader(),
    FormatCode.T4, new SunSinglePrecisionReal(),
    FormatCode.S2, new Sun2FormatWaveformReader());


  /**
   * Calls the proper waveform reader and reads the data bytes
   *
   * @param input data bytes
   * @param format the format code, e.g. 's4' or 'b#'.
   * @param samplesToRead number of samples to read
   * @param skip number of samples to skip
   * @return WaveformReader for the given format code, or null if the format code is unknown or
   * there is no WaveformReader for it.
   */
  public static double[] readSamples(InputStream input, String format, int samplesToRead, int skip)
    throws IOException {
    WaveformReaderInterface reader = readerFor(format);
    return reader.read(input, samplesToRead, skip);
  }

  public static double[] readSamples(InputStream input, String format) throws IOException {
    WaveformReaderInterface reader = readerFor(format);
    return reader.read(input, input.available(), 0);
  }

  public static double[] readSamples(InputStream input, String format, int samplesToRead, long fOff, int skip)
    throws IOException {

    WaveformReaderInterface reader = readerFor(format);

    if (fOff > input.available()) {
      throw new IOException("Number of bytes from input stream of file less than foff.");
    }
    long skipped = input.skip(fOff);

    if (skipped != fOff) {
      throw new IOException("Skipped bytes of file not equal to foff.");
    }
    return reader.read(input, samplesToRead, skip);
  }

  /**
   * Looks up a WaveformReader corresponding to the given format code (CSS 3.0).
   *
   * @param fc the format code, e.g. 's4' or 'b#'.
   * @return WaveformReader for the given format code, or null if the format code is unknown or
   * there is no WaveformReader for it.
   */
  public static WaveformReaderInterface readerFor(String fc) {
    FormatCode format = FormatCode.fcFromString(fc);
    if (format == null || !formatReaders.containsKey(format)) {
      String error = "Unsupported format: " + fc;
      logger.error(error);
      throw new IllegalArgumentException(error);
    }
    return formatReaders.get(format);
  }
}
