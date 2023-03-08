package gms.dataacquisition.cssreader.waveformreaders;

import gms.dataacquisition.cssreader.data.WfdiscRecord;
import gms.utilities.waveformreader.WaveformReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Class for reading waveform files (.w).
 */
public class FlatFileWaveformReader {

  private static final Logger logger = LoggerFactory.getLogger(FlatFileWaveformReader.class);

  // improving performance by saving last read file; assumes wfdiscs are sorted prior to reading
  private String lastReadFile = "";
  private ByteArrayInputStream lastReadWaveform;

  /**
   * Performance is improved dramatically if multiple waveforms are sorted by path to .w files (dir
   * + dfile) prior to reading.  However, this method still works even if not sorted.
   *
   * @param wfd the wfdisc record to read
   * @return int[] of the waveform samples
   */
  public double[] readWaveform(WfdiscRecord wfd, String wfdiscFilePath) throws IOException {
    checkNotNull(wfd);

    // prepend path with directory of wfdisc file if path is not absolute.
    String prefix = wfd.getDir().startsWith(File.separator) ?
      "" : new File(wfdiscFilePath).getParent() + File.separator;

    return readWaveform(prefix + wfd.getDir() + File.separator + wfd.getDfile(),
      wfd.getFoff(), wfd.getNsamp(), wfd.getDatatype());
  }

  public double[] readWaveform(String wfFilePath, int foff,
    int samplesToRead, String format) throws IOException {

    checkNotNull(wfFilePath);

    logger.debug("Reading waveform: {}", wfFilePath);
    if (lastReadFile.equals(wfFilePath)) {
      logger.debug("Re-using waveform from memory");
      lastReadWaveform.reset();
      lastReadWaveform.skip(foff);
    } else {
      byte[] data = Files.readAllBytes(Paths.get(wfFilePath));
      if (data.length == 0) {
        logger.error("File at path {} has no data", wfFilePath);
      }

      lastReadWaveform = new ByteArrayInputStream(data);
      lastReadWaveform.skip(foff);
      lastReadFile = wfFilePath;
    }

    return WaveformReader.readSamples(
      lastReadWaveform, format, samplesToRead, 0);
  }
}
