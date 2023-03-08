package gms.dataacquisition.cssreader.flatfilereaders;

import com.github.ffpojo.FFPojoHelper;
import com.github.ffpojo.exception.FFPojoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GenericFlatFileReader {

  private GenericFlatFileReader() {
    // Hiding the implicit public constructor
  }

  private static final Logger logger = LoggerFactory.getLogger(GenericFlatFileReader.class);

  public static <S, T extends S> List<S> read(
    String filePath, Class<T> type) throws IOException {

    FFPojoHelper ffpojo;
    try {
      ffpojo = FFPojoHelper.getInstance();
    } catch (FFPojoException e) {
      throw new IOException(e);
    }

    final List<String> lines = Files.readAllLines(Paths.get(filePath));
    List<S> results = new ArrayList<>();
    for (int i = 0; i < lines.size(); i++) {
      final String line = lines.get(i);
      if (line == null || line.isEmpty()) {
        continue;
      }
      try {
        results.add(ffpojo.createFromText(type, line));
      } catch (FFPojoException ex) {
        logger.error("Encountered error (" + ex.getMessage() + ") at line "
          + i + " of file: " + filePath);
      }
    }
    return results;
  }

  public static <T> List<T> read(
    String filePath, Map<Integer, Class<? extends T>> lineLengthToType) throws IOException {

    final List<String> lines = Files.readAllLines(Paths.get(filePath));
    List<T> results = new ArrayList<>();
    FFPojoHelper ffpojo;
    try {
      ffpojo = FFPojoHelper.getInstance();
    } catch (FFPojoException e) {
      throw new IOException(e);
    }

    for (int i = 0; i < lines.size(); i++) {
      final String line = lines.get(i);
      if (!lineLengthToType.containsKey(line.length())) {
        throw new IllegalArgumentException("Line " + i + " has length " + line.length()
          + " does not have a known type; known types: " + lineLengthToType);
      }
      try {
        results.add(ffpojo.createFromText(lineLengthToType.get(line.length()), line));
      } catch (FFPojoException ex) {
        logger.error("Encountered error (" + ex.getMessage() + ") at line "
          + i + " of file: " + filePath);
      }
    }
    return results;
  }

}
