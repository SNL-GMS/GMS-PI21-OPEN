package gms.core.performancemonitoring.soh.control.configuration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import gms.core.performancemonitoring.soh.control.StationSohControlConfiguration;
import gms.shared.frameworks.configuration.RetryConfig;
import gms.shared.frameworks.configuration.repository.FileConfigurationRepository;
import gms.shared.frameworks.osd.api.station.StationGroupRepositoryInterface;
import gms.shared.frameworks.osd.coi.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.frameworks.osd.coi.signaldetection.Station;
import gms.shared.frameworks.osd.coi.signaldetection.StationGroup;
import gms.shared.frameworks.osd.coi.signaldetection.StationGroupDefinition;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Path;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Main class for "configuration checker". This loads config from the provided directory, using
 * the OSD service at the provided host name. It then prints out statistics about the configuration.
 * <p>
 * Hopefully there will be more to come, like digging into configuration to find values or sets
 * of values, etc.
 */
public class ConfigurationChecker {

  private static final Logger logger = LoggerFactory.getLogger(ConfigurationChecker.class);

  public static void main(String... args) {

    getCommandLineArguments(args).ifPresentOrElse(
      commandLineArguments -> {

        if (!commandLineArguments.suppressInfo()) {
          logger.info("Loading configuration...");
        }

        StationSohControlConfiguration stationSohControlConfiguration;

        try {
          var startMs = System.currentTimeMillis();

          final RetryConfig retryConfig = RetryConfig.create(1, 10, ChronoUnit.SECONDS, 1);

          stationSohControlConfiguration = getStationSohControlConfiguration(
            FileConfigurationRepository.create(
              Path.of(commandLineArguments.getConfigurationDirectory())
            ),
            getStationGroupRepositoryInterface(
              commandLineArguments.getOsdHostName(),
              commandLineArguments.getStationGroups(),
              commandLineArguments.getStations(),
              commandLineArguments.printTimingInfo()
            ), retryConfig
          );

          if (commandLineArguments.printTimingInfo()) {
            logger.info("Overall config resolution (including OSD operation) took {} ms",
              System.currentTimeMillis() - startMs);
          }
        } catch (IOException e) {
          logger.error("Configuration resolution failed!", e);
          return;
        }

        var stationSohMonitoringDefinition = stationSohControlConfiguration
          .getInitialConfigurationPair()
          .getStationSohMonitoringDefinition();

        if (!commandLineArguments.suppressInfo()) {

          logger.info("Configuration successfully loaded! \n");
          printStatistics(stationSohMonitoringDefinition);
        }

        if (commandLineArguments.printJson()) {
          try {
            logger.info(
              CoiObjectMapperFactory.getJsonObjectMapper().writeValueAsString(
                stationSohMonitoringDefinition
              )
            );
          } catch (JsonProcessingException e) {
            logger.error("Failed to serialize Station SOH Monitoring definition", e);
          }
        }
      },
      () -> {
        // No arguments, do nothing. The parser will have printed a no-args message.
      }
    );

  }

  /**
   * Retrieve the command line arguments from the command line.
   *
   * @param args list of raw arguments
   * @return CommandLineArguments object with parsed out parameter values.
   */
  private static Optional<CommandLineArguments> getCommandLineArguments(String[] args) {

    CommandLineArguments commandLineArguments = new CommandLineArguments();

    CmdLineParser parser = new CmdLineParser(commandLineArguments);

    try {
      parser.parseArgument(args);
    } catch (CmdLineException e) {
      logger.error("Failed to parse command line arguments", e);
      parser.printUsage(System.err);
      return Optional.empty();
    }

    return Optional.of(commandLineArguments);
  }

  private static StationSohControlConfiguration getStationSohControlConfiguration(
    FileConfigurationRepository fileConfigurationRepository,
    StationGroupRepositoryInterface sohRepositoryInterface, RetryConfig retryConfig
  ) {

    return StationSohControlConfiguration
      .create(
        fileConfigurationRepository, sohRepositoryInterface, retryConfig
      );
  }

  /**
   * Return an OsdRepositoryInterface object with an implementation of retrieveStationGroups.
   * When called, retrieveStationGroups connects to the provided OSD service host to retrieve
   * station groups.
   *
   * @param host OSD service host name
   */
  private static StationGroupRepositoryInterface getStationGroupRepositoryInterface(String host,
    List<String> onlyStationGroups, List<String> onlyStations, boolean printTimingInfo) throws IOException {
    return new StationGroupRepositoryInterface() {

      private final URLConnection connection = new URL(
        "https://" + host + "/osd/station-groups")
        .openConnection();

      @Override
      public List<StationGroup> retrieveStationGroups(Collection<String> stationGroupNames) {

        HttpURLConnection httpURLConnection = (HttpURLConnection) connection;

        try {
          httpURLConnection.setRequestMethod("POST"); // PUT is another valid option
        } catch (ProtocolException e) {
          logger.error("Failed to set request method for HTTP connection", e);
          return List.of();
        }

        httpURLConnection.setDoOutput(true);

        try {
          String requestBody = CoiObjectMapperFactory.getJsonObjectMapper()
            .writeValueAsString(stationGroupNames);
          httpURLConnection.setFixedLengthStreamingMode(requestBody.getBytes().length);
          httpURLConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
          httpURLConnection.connect();
          try (OutputStream os = httpURLConnection.getOutputStream()) {
            os.write(requestBody.getBytes());
          }

          var startMs = System.currentTimeMillis();

          String response;
          try (InputStream is = httpURLConnection.getInputStream()) {
            response = new String(is.readAllBytes());
          }

          if (printTimingInfo) {
            logger.info("OSD Station group operation took {} ms", (System.currentTimeMillis() - startMs));
          }

          return trimStationGroupTree(
            onlyStations,

            CoiObjectMapperFactory.getJsonObjectMapper()
              .readValue(response, new TypeReference<List<StationGroup>>() {
              })
              .stream()
              .filter(
                stationGroup -> onlyStationGroups.isEmpty() || onlyStationGroups
                  .contains(stationGroup.getName())
              )
              .collect(Collectors.toList())
          );

        } catch (IOException e) {
          logger.error("I/O Exception while retrieving station groups", e);
        }

        return List.of();
      }

      @Override
      public void storeStationGroups(Collection<StationGroup> stationGroups) {
        // Only implementing interface for retrieval
      }

      @Override
      public void updateStationGroups(Collection<StationGroupDefinition> stationGroupDefinitions) {
        // Only implementing interface for retrieval
      }
    };
  }

  /**
   * "Trims" the "station group tree" that represents the list of station groups. What this means is
   * that given a set of stations the following occurs:
   *
   * <ul>
   *   <li>
   *     All station groups that contain none of the provided stations are filtered out
   *   </li>
   *   <li>
   *     The station groups that remain each contain ONLY a subset of the passed in stations.
   *   </li>
   * </ul>
   *
   * @param onlyStations Stations used to filter the station groups by
   * @param stationGroups Station groups to be filtered
   * @return A filtered list of station groups that strictly contain the stations intersecting with the desired station list
   */
  private static List<StationGroup> trimStationGroupTree(
    Collection<String> onlyStations,
    List<StationGroup> stationGroups
  ) {

    if (onlyStations.isEmpty()) {
      return stationGroups;
    }

    return stationGroups.stream()
      //
      // Filter out station groups that contain none of the stations
      //
      .filter(stationGroup -> !Collections.disjoint(
        stationGroup.getStations().stream()
          .map(Station::getName)
          .collect(Collectors.toList())
        , onlyStations)
      )
      //
      // All station groups that make it here have a set of stations that intersects
      // onlyStations. For each of those station groups, we now create a new station group that
      // ONLY has the intersection.
      //
      .map(stationGroup -> StationGroup.from(
        stationGroup.getName(),
        stationGroup.getDescription(),
        stationGroup.getStations().stream()
          .filter(station -> onlyStations.contains(station.getName()))
          .collect(Collectors.toList())
      )).collect(Collectors.toList());
  }

  private static void printStatistics(StationSohMonitoringDefinition definition) {

    ConfigurationAnalyzer configurationAnalyzer = new ConfigurationAnalyzer(
      definition
    );

    logger.info(
      "Station SOH definitions: {} \n"
        + "Channel SOH definitions: {} \n"
        + "Channels by monitor type entries: {} \n"
        + "Station monitor types for rollup: {} \n"
        + "Channel monitor types for rollup: {} \n"
        + "Entries of monitor type -> soh status: {} \n"
        + "Capability rollup definitions: {} \n"
        + "Station rollup definitions: {} \n"
        + "Channel rollup definitions: {} \n%n",
      configurationAnalyzer.countStationSohDefinitions(),
      configurationAnalyzer.countChannelSohDefinitions(),
      configurationAnalyzer.countChannelsByMonitorTypeEntries(),
      configurationAnalyzer.countStationMonitorTypesForRollup(),
      configurationAnalyzer.countChannelMonitorTypesForRollup(),
      configurationAnalyzer.countSohMonitorValueAndStatusDefinitionBySohMonitorType(),
      configurationAnalyzer.countCapabilityRollupDefinitions(),
      configurationAnalyzer.countStationCapabilityRollupDefinitions(),
      configurationAnalyzer.countChannelCapabilityRollupDefinitions()
    );

    logger.info("MAX INTERVALS===========================================");
    configurationAnalyzer.stationMaxCalculationInterval().forEach((k, v) -> logger.info("Station {} max calculation interval {}", k, v));

    logger.info("MAX INTERVALS===========================================");

    configurationAnalyzer.stationMaxBackoffDuration().forEach((k, v) -> logger.info("Station {} max backoff duration {}", k, v));
  }
}
