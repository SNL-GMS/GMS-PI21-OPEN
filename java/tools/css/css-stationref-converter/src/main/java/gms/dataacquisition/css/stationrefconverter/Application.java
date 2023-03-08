package gms.dataacquisition.css.stationrefconverter;

import com.fasterxml.jackson.databind.ObjectMapper;
import gms.dataacquisition.css.stationrefconverter.commandline.StationRefConverterCommandLineArgs;
import gms.shared.frameworks.osd.coi.datatransferobjects.CoiObjectMapperFactory;
import org.kohsuke.args4j.CmdLineParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Command-line application to load data from CSS flat files.
 */
public class Application {

  private static final Logger logger = LoggerFactory.getLogger(Application.class);

  private static final ObjectMapper objMapper = CoiObjectMapperFactory.getJsonObjectMapper();

  private static final String REFERENCE_NETWORK_FILE_NAME = "reference-network.json";
  private static final String REFERENCE_STATION_FILE_NAME = "reference-station.json";
  private static final String REFERENCE_SITE_FILE_NAME = "reference-site.json";
  private static final String REFERENCE_CHANNEL_FILE_NAME = "reference-channel.json";
  private static final String RESPONSE_OUTPUT_DIR = "responses/";
  private static final String RESPONSE_FILE_NAME_PREFIX = "response-";
  private static final String RESPONSE_FILE_NAME_SUFFIX = ".json";
  private static final String REFERENCE_SENSOR_FILE_NAME = "reference-sensor.json";
  private static final String REFERENCE_NETWORK_MEMBERSHIPS_FILE_NAME = "reference-network-memberships.json";
  private static final String REFERENCE_STATION_MEMBERSHIPS_FILE_NAME = "reference-station-memberships.json";
  private static final String REFERENCE_SITE_MEMBERSHIPS_FILE_NAME = "reference-site-memberships.json";
  private static final String PROCESSING_STATION_GROUP_FILE_NAME = "processing-station-group.json";
  private static final String PROCESSING_RESPONSE_FILE_NAME = "processing-response.json";


  private static final StationRefConverterCommandLineArgs cmdLineArgs
    = new StationRefConverterCommandLineArgs();

  public static void main(String[] args) {
    try {
      // Read command line args
      new CmdLineParser(cmdLineArgs).parseArgument(args);
      if (!runningInsideDockerContainer()) {
        logger.info("Running outside a container");
        var targetDir = new File(cmdLineArgs.getOutputDir());
        if (targetDir.exists()) {
          throw new IllegalArgumentException(String.format("Target directory %s already exists.",
            cmdLineArgs.getOutputDir()));
        } else {
          createOutputDirectory(targetDir);
        }
      }
      execute(cmdLineArgs);
      System.exit(0);
    } catch (Exception ex) {
      logger.error("Error in Application.execute", ex);
      System.exit(1);
    }
  }

  // Check if we're running inside a container
  // https://stackoverflow.com/questions/52580008/how-does-java-application-know-it-is-running-within-a-docker-container
  public static Boolean runningInsideDockerContainer() {
    var cgroupPath = File.separator + "proc" + File.separator + "1" + File.separator + "cgroup";

    try (Stream<String> stream =
           Files.lines(Paths.get(cgroupPath))) {
      return stream.anyMatch(line -> line.contains("/docker"));
    } catch (IOException e) {
      logger.error("Unable to parse {}: {}", cgroupPath, e.getMessage());
      return false;
    }
  }

  private static void createOutputDirectory(File targetDir) {
    try {
      targetDir.mkdirs();
    } catch (SecurityException ex) {
      logger.error("Error creating directory", ex);
      System.exit(1);
    }
  }

  /**
   * Performs the load.
   *
   * @param args command-line args
   */
  private static void execute(StationRefConverterCommandLineArgs args) {
    Objects.requireNonNull(args, "Cannot take null arguments");
    //Check if the directory already exists, refuse to write new files if it does, for security
    var outputDirString = args.getOutputDir();
    if (!outputDirString.endsWith(File.separator)) {
      outputDirString += File.separator;
    }
    if (outputFilesExist(outputDirString)) {
      throw new IllegalArgumentException("Cannot create new files in " + outputDirString +
        ", some of the files to be created already exist. " +
        "Please specify a new directory or remove the files.");
    } else {
      try {
        // read the files and convert the objects
        CssReferenceReader.process(args.getDataDir(), args.getNetworkFile());

        writeToJson(CssReferenceReader.getReferenceNetworksByName().values(), outputDirString,
          REFERENCE_NETWORK_FILE_NAME);
        writeToJson(CssReferenceReader.getReferenceStationsByName().values(), outputDirString,
          REFERENCE_STATION_FILE_NAME);
        writeToJson(CssReferenceReader.getReferenceSitesByName().values(), outputDirString,
          REFERENCE_SITE_FILE_NAME);
        writeToJson(CssReferenceReader.getReferenceChannelsByName().values(), outputDirString,
          REFERENCE_CHANNEL_FILE_NAME);
        writeResponseFiles(CssReferenceReader.getReferenceResponses(),
          outputDirString + RESPONSE_OUTPUT_DIR,
          RESPONSE_FILE_NAME_PREFIX, RESPONSE_FILE_NAME_SUFFIX);
        writeToJson(CssReferenceReader.getReferenceSensors(), outputDirString,
          REFERENCE_SENSOR_FILE_NAME);
        writeToJson(CssReferenceReader.getReferenceNetworkMemberships(), outputDirString,
          REFERENCE_NETWORK_MEMBERSHIPS_FILE_NAME);
        writeToJson(CssReferenceReader.getReferenceStationMemberships(), outputDirString,
          REFERENCE_STATION_MEMBERSHIPS_FILE_NAME);
        writeToJson(CssReferenceReader.getReferenceSiteMemberships(), outputDirString,
          REFERENCE_SITE_MEMBERSHIPS_FILE_NAME);
        writeToJson(CssReferenceReader.getStationGroups(), outputDirString,
          PROCESSING_STATION_GROUP_FILE_NAME);
        writeToJson(CssReferenceReader.getResponseSet(), outputDirString,
          PROCESSING_RESPONSE_FILE_NAME);
      } catch (Exception e) {
        logger.error("Error Application.execute", e);

      }
    }
  }

  private static <T> void writeToJson(Collection<T> data, String outputDir, String dataName)
    throws IOException {
    final String outputFileName = outputDir + dataName;

    logger.info("Creating {} with {} objects that will be saved to {}.", dataName, data.size(),
      outputFileName);
    objMapper.writeValue(new File(outputFileName), data);
  }

  /**
   * ReferenceResponse data is written to individual files. Otherwise the single file is too big.
   * This is not a generic method since it's only used for response file writing.
   *
   * @param data response data to be written
   * @param outputDir directory to write to
   * @param filenamePrefix prefix of filename response data will be written to
   * @param filenameSuffix suffix of filename response data will be written to
   * @param <T> T is always ReferenceResponse for now
   * @throws IOException if the write operation fails
   */
  private static <T> void writeResponseFiles(Collection<T> data, String outputDir,
    String filenamePrefix, String filenameSuffix) throws IOException {
    var targetDirectory = new File(outputDir);
    if (!targetDirectory.exists()) {
      targetDirectory.mkdir();
    }

    var i = 0;
    for (T d : data) {
      final String outputFileName = outputDir + filenamePrefix + i++ + filenameSuffix;
      objMapper.writeValue(new File(outputFileName), d);
    }
    logger.info("Writing {} response files to {}", data.size(), outputDir);
  }

  /**
   * Check if any of the output files writeToJson executes exist
   *
   * @param outputDirString absolute path to destination directory
   * @return true if any of the produced output files exists
   */
  private static boolean outputFilesExist(String outputDirString) {
    return new File(outputDirString + REFERENCE_NETWORK_FILE_NAME).exists() ||
      new File(outputDirString + REFERENCE_STATION_FILE_NAME).exists() ||
      new File(outputDirString + REFERENCE_SITE_FILE_NAME).exists() ||
      new File(outputDirString + REFERENCE_CHANNEL_FILE_NAME).exists() ||
      new File(outputDirString + RESPONSE_OUTPUT_DIR).exists() ||
      new File(outputDirString + REFERENCE_SENSOR_FILE_NAME).exists() ||
      new File(outputDirString + REFERENCE_NETWORK_MEMBERSHIPS_FILE_NAME).exists() ||
      new File(outputDirString + REFERENCE_STATION_MEMBERSHIPS_FILE_NAME).exists() ||
      new File(outputDirString + REFERENCE_SITE_MEMBERSHIPS_FILE_NAME).exists() ||
      new File(outputDirString + PROCESSING_STATION_GROUP_FILE_NAME).exists() ||
      new File(outputDirString + PROCESSING_RESPONSE_FILE_NAME).exists();
  }
}
