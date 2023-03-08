package gms.dataacquisition.cssreader.flatfilereaders;

import com.github.ffpojo.FFPojoHelper;
import com.github.ffpojo.exception.FFPojoException;
import gms.dataacquisition.cssreader.data.WfdiscRecord;
import gms.dataacquisition.cssreader.data.WfdiscRecord32;
import gms.dataacquisition.cssreader.data.WfdiscRecord64;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * WfdiscReader that operates on a flat file, provided a location to it (String).
 */
public class FlatFileWfdiscReader implements WfdiscReaderInterface {

  private static final Logger logger = LoggerFactory.getLogger(FlatFileWfdiscReader.class);

  private static final int WFDISC64_LINE_LENGTH = 289;

  private List<String> stations;
  private List<String> channels;
  private Instant time;
  private Instant endtime;

  /**
   * Create a flatfilereaders reader without any filtering of the flatfilereaders file entires.
   */
  public FlatFileWfdiscReader() {
  }

  /**
   * The arguments provided will be used to filter the flatfilereaders file entries, and may be null
   * so they are not used.
   */
  public FlatFileWfdiscReader(List<String> stations,
    List<String> channels,
    Instant time,
    Instant endtime) {
    logger.info(
      "Creating flatfilereaders reader with parameters: stations = {}, channels = {}, time = {}, endtime = {}",
      stations, channels, time, endtime);
    this.stations = stations;
    this.channels = channels;
    this.time = time;
    this.endtime = endtime;
  }

  /**
   * Reads the file at the specified path into WfdiscRecord's.
   *
   * @param wfdiscPath the path to the file to read
   * @return the read Wfdisc records
   */
  @Override
  public List<WfdiscRecord> read(String wfdiscPath) throws IOException {
    // Validate file path argument.
    Validate.notEmpty(wfdiscPath);
    var path = Paths.get(wfdiscPath);
    if (!Files.exists(path)) {
      throw new IllegalArgumentException("Path " + wfdiscPath + " doesn't exist in file system.");
    }

    // read the lines out of the file
    List<String> fileLines = Files.readAllLines(path);
    int numLines = fileLines.size();
    logger.debug("Wfdisc has {} entries in file: {}", numLines, path);
    if (numLines <= 0) {
      logger.error("Provided flatfilereaders file {} is empty", wfdiscPath);
      return new ArrayList<>();
    }

    // convert the file into WfdiscRecord's
    FFPojoHelper ffpojo;
    try {
      ffpojo = FFPojoHelper.getInstance();
    } catch (FFPojoException e) {
      throw new IOException(e);
    }

    List<WfdiscRecord> wfDiscRecords = new ArrayList<>();
    for (String line : fileLines) {
      try {
        var wfdiscRecord = readAsWfdisc(ffpojo, line);
        wfDiscRecords.add(wfdiscRecord);
      } catch (Exception ex) {
        logger.error(
          "Encountered error at line {} of file: {}", line, ex.getMessage());
      }
    }

    // Apply filters on station, channel, start time, endtime.
    Stream<WfdiscRecord> wfdiscStream = wfDiscRecords.stream()
      .filter(wfdisc -> stations == null || stations.contains(wfdisc.getSta()))
      .filter(wfdisc -> channels == null || channels.contains(wfdisc.getChan()))
      .filter(wfdisc -> time == null || (time.compareTo(wfdisc.getTime()) <= 0))
      .filter(wfdisc -> endtime == null || (endtime.compareTo(wfdisc.getEndtime()) >= 0));

    // Generate the list of Wfdisc Records.
    // Sort the Wfdisc stream by directory, filename, and file-offset.
    Comparator<WfdiscRecord> wfdiscRecordComparator = Comparator.comparing(WfdiscRecord::getDir)
      .thenComparing(WfdiscRecord::getDfile)
      .thenComparing(WfdiscRecord::getFoff);
    wfDiscRecords = wfdiscStream.sorted(wfdiscRecordComparator)
      .collect(Collectors.toList());

    logger.debug("{} wfdisc records retrieved.", wfDiscRecords.size());
    return wfDiscRecords;
  }

  /**
   * Helper method to read a String into a WfdiscRecord using a FFPojoHelper.
   *
   * @param ffpojo used to parse the input string into a WfdiscRecord
   * @param l the line to parse
   * @return the parsed WfdiscRecord, or null if an error occurs.
   */
  private static WfdiscRecord readAsWfdisc(FFPojoHelper ffpojo, String l) throws FFPojoException {
    WfdiscRecord wfdisc;
    if (l.length() == WFDISC64_LINE_LENGTH) {
      wfdisc = ffpojo.createFromText(WfdiscRecord64.class, l);
    } else {
      wfdisc = ffpojo.createFromText(WfdiscRecord32.class, l);
    }

    logger.debug("Read WFDISC line: {}", wfdisc);

    wfdisc.validate();
    return wfdisc;
  }
}
