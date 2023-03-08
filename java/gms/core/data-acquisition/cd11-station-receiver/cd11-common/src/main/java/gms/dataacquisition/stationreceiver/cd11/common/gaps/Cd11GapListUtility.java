package gms.dataacquisition.stationreceiver.cd11.common.gaps;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Utility to persist and load {@link Cd11GapList}s from disk
 */
public class Cd11GapListUtility {

  private static final Logger logger = LoggerFactory.getLogger(Cd11GapListUtility.class);

  private final String gapStoragePath;
  private final String fileExtension;
  private final ObjectMapper objectMapper;

  @VisibleForTesting
  Cd11GapListUtility(String gapStoragePath, String fileExtension, ObjectMapper objectMapper) {

    Validate.notEmpty(fileExtension, "File extension cannot be null or empty!");
    Validate.notEmpty(gapStoragePath, "GapStoragePath cannot be null or empty!");
    checkNotNull(objectMapper, "Object mapper cannot be null!");

    this.gapStoragePath = gapStoragePath;
    this.fileExtension = fileExtension;
    this.objectMapper = objectMapper;

    try {
      var path = Paths.get(this.gapStoragePath);
      if (Files.notExists(path)) {
        Files.createDirectory(path);
      }
    } catch (InvalidPathException | IOException e) {
      throw new IllegalStateException("Could not create gap storage directory!", e);
    }
  }

  /**
   * Cd11GapListUtility factory method
   *
   * @param gapStoragePath The path to store gaps lists - cannot be null or empty
   * @param fileExtension The file extension to use - cannot be null or empty
   */
  public static Cd11GapListUtility create(String gapStoragePath, String fileExtension) {
    var objectMapper = new ObjectMapper();
    objectMapper.findAndRegisterModules();
    objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    return new Cd11GapListUtility(gapStoragePath, fileExtension, objectMapper);
  }

  /**
   * Attempts to load the gap list file from disk given the station name
   *
   * @param stationName the station name used to build the unique filename
   * @return The deserialzied {@link Cd11GapList} from disk
   */
  public Cd11GapList loadGapState(String stationName) {
    var path = Paths.get(this.gapStoragePath + stationName + this.fileExtension);
    if (Files.exists(path)) {
      try {
        var contents = new String(Files.readAllBytes(path));
        return new Cd11GapList(objectMapper.readValue(contents, GapList.class));
      } catch (IOException e) {
        logger.error("Error deserializing GapList", e);
        return new Cd11GapList();
      }
    } else {
      return new Cd11GapList();
    }
  }

  /**
   * Saves the gap list for a particular station to disk
   *
   * @param stationName the station name used in to create the unique filename
   * @param gapList the {@link GapList} to persist
   */
  public Mono<Void> persistGapState(String stationName, GapList gapList) {
    String path = this.gapStoragePath + stationName + this.fileExtension;

    return Mono.fromRunnable(() -> {
      var filePath = Paths.get(path);
      try {
        objectMapper.writeValue(Files.newOutputStream(filePath), gapList);
      } catch (IOException e) {
        throw new IllegalStateException("Gap persistence I/O failure", e);
      }
    });


  }

  /**
   * clears the gap list file stored on disk
   *
   * @param stationName The stationName associated with the gap List file
   */
  public Mono<Void> clearGapState(String stationName) {
    var path = Paths.get(this.gapStoragePath + stationName + this.fileExtension);

    return Mono.fromRunnable(() -> {
      try {
        Files.deleteIfExists(path);
      } catch (IOException e) {
        throw new IllegalStateException("Gap clear I/O failure", e);
      }
    });
  }

}
