package gms.dataacquisition.cssreader.flatfilereaders;

import com.github.ffpojo.FFPojoHelper;
import com.github.ffpojo.exception.FFPojoException;
import gms.dataacquisition.cssreader.data.FapRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FapFileReader {
  private static final String COMMENT_LINE_START = "#";
  private static final Logger logger = LoggerFactory.getLogger(FapFileReader.class);

  /**
   * Default private constructor to hide implicit public constructor
   */
  private FapFileReader() {
  }

  /**
   * Reads FAP (frequency, amplitude, phase) response file into FAP records.
   *
   * @param responsePath Path to response file
   * @return list of FrequencyAmplitudePhase objects from the data in the file represented by
   * responsePath
   */
  public static List<FapRecord> readFapFile(String responsePath) {
    final List<FapRecord> faps = new ArrayList<>();
    int responseCount = 0;

    try {
      final FFPojoHelper ffpojo = FFPojoHelper.getInstance();
      final List<String> responseDataLines = Files.readAllLines(Paths.get(responsePath));
      int linesAfterComments = 0;

      // Can't use lambda here because errors related to "linesAfterComments" needing to be final
      // (which it can't be) are thrown by SonarLint
      for (String line : responseDataLines) {
        if (line.startsWith(COMMENT_LINE_START) || line.isEmpty()) {
          continue;
        }

        // Ignore the first line after the comments section. The second line has the number of
        // responses present in the file.
        linesAfterComments++;
        if (linesAfterComments == 2) {
          responseCount = Integer.valueOf(line.trim());
        }

        if (linesAfterComments > 2) {
          // The FapRecord delimiter is just a space, so replace all whitespace with " "
          faps.add(ffpojo.createFromText(FapRecord.class,
            line.replaceAll("\\s+", " ").trim()));
        }
      }

      if (faps.size() != responseCount) {
        logger.warn("Number of FAP objects ({}) does not equal listed count {} in response file ({})",
          faps.size(), responseCount, responsePath);
      }
    } catch (IOException | FFPojoException e) {
      logger.error("Error reading FAP file {}: {}", responsePath, e.getMessage());
    }
    return faps;
  }
}
