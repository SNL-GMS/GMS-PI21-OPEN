package gms.shared.frameworks.injector.ui;

import gms.core.performancemonitoring.soh.control.configuration.ChannelSohDefinition;
import gms.core.performancemonitoring.soh.control.configuration.DurationSohMonitorStatusThresholdDefinition;
import gms.core.performancemonitoring.soh.control.configuration.PercentSohMonitorStatusThresholdDefinition;
import gms.core.performancemonitoring.soh.control.configuration.SohMonitorStatusThresholdDefinition;
import gms.core.performancemonitoring.soh.control.configuration.StationSohDefinition;
import gms.core.performancemonitoring.soh.control.configuration.TimeWindowDefinition;
import gms.core.performancemonitoring.ssam.control.config.StationSohMonitoringUiClientParameters;
import gms.shared.frameworks.osd.coi.channel.Channel;
import gms.shared.frameworks.osd.coi.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.frameworks.osd.coi.signaldetection.Station;
import gms.shared.frameworks.osd.coi.signaldetection.StationGroup;
import gms.shared.frameworks.osd.coi.soh.SohMonitorType;
import org.apache.commons.lang3.Validate;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class UiDataInjectorUtility {

  /* Hiding default public constructor */
  private UiDataInjectorUtility() {
  }

  /**
   * Load the station group mapping via file in resource directory
   *
   * @param fullFilePath the file path.
   * @return List of station groups
   */
  public static List<StationGroup> loadStationGroupsFromFile(String fullFilePath) {
    try (final InputStream stationGroupMappingStream = StationGroup.class.getClassLoader()
      .getResourceAsStream(fullFilePath)) {
      Validate.notNull(stationGroupMappingStream, String.format("File at [%s] does not exist.", fullFilePath));
      StationGroup[] stationGroups = CoiObjectMapperFactory.getJsonObjectMapper()
        .readValue(stationGroupMappingStream, StationGroup[].class);
      return Arrays.asList(stationGroups);
    } catch (IOException | NullPointerException e) {
      throw new IllegalArgumentException("Unable to load Station Groups from Json file", e);
    }
  }

  /**
   * Walk the station group's station list to return a unique set of stations
   *
   * @param stationGroups the station groups.
   * @return Set of Stations
   */
  public static Set<Station> getStationSet(List<StationGroup> stationGroups) {
    // Process through the station groups to build a unique list of stations list
    Map<String, Station> stationMap = new HashMap<>();
    for (StationGroup stationGroup : stationGroups) {
      stationGroup.getStations().forEach(station -> stationMap.put(station.getName(), station));
    }
    return new HashSet<>(stationMap.values());
  }

  /**
   * Build a StationSohMonitoringUiClientParameters because the config object was not designed to
   * deserialize from JSON. Working with a set of stations walk through each station constructing
   * the StationSohDefinition and the ChannelSohDefinition objects
   *
   * @param stations a Set of stations.
   * @return the client parameters.
   */
  public static StationSohMonitoringUiClientParameters buildStationSohParameters(
    Set<Station> stations) {

    // Walk through the Stations build the StationSohDefinitions
    Set<StationSohDefinition> stationDefinitions = new HashSet<>();
    for (Station station : stations) {

      // Get a Map of Channel names to Channel
      var channelNameToChannelMap =
        station.getChannels().stream()
          .collect(Collectors.toMap(Channel::getName, Function.identity()));

      // Get the channel names for the station
      Map<SohMonitorType, Set<String>> channelsBySohMonitorType = new EnumMap<>(SohMonitorType.class);

      // Walk thru each monitor type adding them to the map
      for (SohMonitorType monitorType : SohMonitorType.validTypes()) {
        channelsBySohMonitorType.put(monitorType, channelNameToChannelMap.keySet());
      }

      // Figure out a more efficient way of build ChannelSohDefs
      Set<ChannelSohDefinition> channelSohDefinitions = new HashSet<>();
      EnumMap<SohMonitorType, TimeWindowDefinition> timeWindowDefinitionMap = new EnumMap<>(SohMonitorType.class);

      for (String channelName : channelNameToChannelMap.keySet()) {
        // Walk thru each monitor type adding them to the map
        Map<SohMonitorType, SohMonitorStatusThresholdDefinition<?>>
          sohMonitorThresholdDefinitionsBySohMonitorType = new EnumMap<>(SohMonitorType.class);

        for (SohMonitorType monitorType : SohMonitorType.validTypes()) {
          timeWindowDefinitionMap.put(monitorType,
            TimeWindowDefinition.create(Duration.ofMinutes(30), Duration.ofSeconds(40)));

          SohMonitorStatusThresholdDefinition<?> monitorStatusThresholdDefinition =
            monitorType.equals(SohMonitorType.LAG) ?
              DurationSohMonitorStatusThresholdDefinition.create(
                Duration.ofSeconds(5),
                Duration.ofSeconds(12)
              ) :
              PercentSohMonitorStatusThresholdDefinition.create(5.0, 11.0);
          sohMonitorThresholdDefinitionsBySohMonitorType.put(monitorType, monitorStatusThresholdDefinition);
        }

        ChannelSohDefinition channelSohDefinition =
          ChannelSohDefinition.create(channelName, SohMonitorType.validTypes(),
            sohMonitorThresholdDefinitionsBySohMonitorType,
            channelNameToChannelMap.get(channelName).getNominalSampleRateHz());
        channelSohDefinitions.add(channelSohDefinition);
      }


      stationDefinitions.add(StationSohDefinition.create(station.getName(), SohMonitorType.validTypes(),
        channelsBySohMonitorType, channelSohDefinitions, timeWindowDefinitionMap));
    }

    StationSohMonitoringUiClientParameters stationSohParams = loadStationSohParametersFromFile();
    stationSohParams.getStationSohControlConfiguration().getStationSohDefinitions().addAll(stationDefinitions);
    return stationSohParams;
  }

  /**
   * Loads the base StationSohMonitoringUiClientParameters without the StationSohDefinitions
   * which cannot be deserialized into a java object. To be used by buildStationSohParameters
   * function above which builds a fake set of StationSohDefinitions.
   *
   * @return StationSohMonitoringUiClientParameters from file
   */
  private static StationSohMonitoringUiClientParameters loadStationSohParametersFromFile() {
    var baseFilePath = "gms/shared/frameworks/injector/";
    var sohParametersFilename = baseFilePath + "SohParamsWithoutStationDefinitions.json";

    try (final InputStream paramsMappingStream = StationSohMonitoringUiClientParameters.class.getClassLoader()
      .getResourceAsStream(sohParametersFilename)) {
      return CoiObjectMapperFactory.getJsonObjectMapper()
        .readValue(paramsMappingStream, StationSohMonitoringUiClientParameters.class);
    } catch (IOException | NullPointerException e) {
      throw new IllegalArgumentException("Unable to load StationSohParameters from Json file", e);
    }
  }
}
