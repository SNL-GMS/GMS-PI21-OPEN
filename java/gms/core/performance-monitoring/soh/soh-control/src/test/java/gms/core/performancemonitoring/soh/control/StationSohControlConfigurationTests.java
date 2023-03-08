package gms.core.performancemonitoring.soh.control;

import gms.core.performancemonitoring.soh.control.configuration.BestOfRollupOperator;
import gms.core.performancemonitoring.soh.control.configuration.CapabilitySohRollupDefinition;
import gms.core.performancemonitoring.soh.control.configuration.ChannelRollupDefinition;
import gms.core.performancemonitoring.soh.control.configuration.ChannelSohDefinition;
import gms.core.performancemonitoring.soh.control.configuration.MinGoodOfRollupOperator;
import gms.core.performancemonitoring.soh.control.configuration.StationRollupDefinition;
import gms.core.performancemonitoring.soh.control.configuration.StationSohDefinition;
import gms.core.performancemonitoring.soh.control.configuration.StationSohMonitoringDefinition;
import gms.core.performancemonitoring.soh.control.configuration.TimeWindowDefinition;
import gms.shared.frameworks.configuration.RetryConfig;
import gms.shared.frameworks.configuration.repository.FileConfigurationRepository;
import gms.shared.frameworks.osd.api.OsdRepositoryInterface;
import gms.shared.frameworks.osd.coi.channel.Channel;
import gms.shared.frameworks.osd.coi.signaldetection.Station;
import gms.shared.frameworks.osd.coi.signaldetection.StationGroup;
import gms.shared.frameworks.osd.coi.soh.SohMonitorType;
import gms.shared.frameworks.osd.coi.soh.SohMonitorType.SohValueType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

class StationSohControlConfigurationTests {

  StationSohControlConfiguration stationSohControlConfiguration;

  private static final RetryConfig retryConfig = RetryConfig.create(1, 10, ChronoUnit.SECONDS, 1);

  void init(final String basepath) {

    URL configBasePathUrl = Thread.currentThread().getContextClassLoader().getResource(basepath);
    Objects.requireNonNull(configBasePathUrl, "Null configBasePathUrl");
    Path configurationBasePath = new File(configBasePathUrl.getFile()).toPath();

    FileConfigurationRepository fileConfigurationRepository = FileConfigurationRepository
      .create(configurationBasePath);

    Channel mockChannelA = Mockito.mock(Channel.class);
    Mockito.when(mockChannelA.getName()).thenReturn("ChannelA");
    Mockito.when(mockChannelA.getStation()).thenReturn("StationA");
    Channel mockChannelB = Mockito.mock(Channel.class);
    Mockito.when(mockChannelB.getName()).thenReturn("ChannelB");
    Mockito.when(mockChannelB.getStation()).thenReturn("StationA");
    Channel mockChannelC = Mockito.mock(Channel.class);
    Mockito.when(mockChannelC.getName()).thenReturn("ChannelC");
    Mockito.when(mockChannelC.getStation()).thenReturn("StationA");

    Channel mockChannelD = Mockito.mock(Channel.class);
    Mockito.when(mockChannelD.getName()).thenReturn("ChannelD");
    Mockito.when(mockChannelD.getStation()).thenReturn("StationB");
    Channel mockChannelE = Mockito.mock(Channel.class);
    Mockito.when(mockChannelE.getName()).thenReturn("ChannelE");
    Mockito.when(mockChannelE.getStation()).thenReturn("StationB");

    Channel mockChannelF = Mockito.mock(Channel.class);
    Mockito.when(mockChannelF.getName()).thenReturn("ChannelF");
    Mockito.when(mockChannelF.getStation()).thenReturn("StationC");
    Channel mockChannelG = Mockito.mock(Channel.class);
    Mockito.when(mockChannelG.getName()).thenReturn("ChannelG");
    Mockito.when(mockChannelG.getStation()).thenReturn("StationC");

    Station mockStationA = Mockito.mock(Station.class);
    Station mockStationB = Mockito.mock(Station.class);
    Station mockStationC = Mockito.mock(Station.class);

    StationGroup mockStationGroupA = Mockito.mock(StationGroup.class);
    StationGroup mockStationGroupB = Mockito.mock(StationGroup.class);

    OsdRepositoryInterface osdRepositoryInterface = Mockito.mock(OsdRepositoryInterface.class);

    Mockito.when(
      mockStationGroupA.getName()
    ).thenReturn(
      "GroupA"
    );

    Mockito.when(
      mockStationGroupB.getName()
    ).thenReturn(
      "GroupB"
    );

    Mockito.when(mockStationA.getName())
      .thenReturn("StationA");

    TreeSet<Channel> stationAChannels = new TreeSet<>(Comparator.comparing(Channel::getName));
    stationAChannels.add(mockChannelA);
    stationAChannels.add(mockChannelB);
    stationAChannels.add(mockChannelC);
    Mockito.when(mockStationA.getChannels())
      .thenReturn(stationAChannels);

    Mockito.when(mockStationB.getName())
      .thenReturn("StationB");

    TreeSet<Channel> stationBChannels = new TreeSet<>(Comparator.comparing(Channel::getName));
    stationBChannels.add(mockChannelD);
    stationBChannels.add(mockChannelE);
    Mockito.when(mockStationB.getChannels()).thenReturn(stationBChannels);

    Mockito.when(mockStationC.getName())
      .thenReturn("StationC");

    TreeSet<Channel> stationCChannels = new TreeSet<>(Comparator.comparing(Channel::getName));
    stationCChannels.add(mockChannelF);
    stationCChannels.add(mockChannelG);
    Mockito.when(mockStationC.getChannels()).thenReturn(stationCChannels);

    TreeSet<Station> stations = new TreeSet<>(Comparator.comparing(Station::getName));
    stations.add(mockStationA);
    stations.add(mockStationB);

    Mockito.when(
      mockStationGroupA.getStations()
    ).thenReturn(stations);

    TreeSet<Station> stationGroupBStations = new TreeSet<>(Comparator.comparing(Station::getName));
    stations.add(mockStationC);
    Mockito.when(
      mockStationGroupB.getStations()
    ).thenReturn(
      stationGroupBStations
    );

    Mockito.when(
      osdRepositoryInterface.retrieveStationGroups(List.of("GroupA", "GroupB"))
    ).thenReturn(
      List.of(mockStationGroupA, mockStationGroupB)
    );

    stationSohControlConfiguration = StationSohControlConfiguration.create(
      fileConfigurationRepository,
      osdRepositoryInterface, retryConfig);
  }

  @Test
  void testGetStationSohMonitoringDefinition() {

    init("gms/core/performancemonitoring/soh/configuration-base");

    StationSohMonitoringDefinition stationSohMonitoringDefinition = stationSohControlConfiguration
      .getInitialConfigurationPair().getStationSohMonitoringDefinition();

    // Validate correct StationSohDefinitions exist
    Set<StationSohDefinition> stationSohDefinitions = stationSohMonitoringDefinition
      .getStationSohDefinitions();

    Set<String> stationNames = stationSohDefinitions.stream()
      .map(StationSohDefinition::getStationName).collect(
        Collectors.toSet());

    Assertions.assertEquals(Set.of("StationA", "StationB", "StationC"), stationNames);

    Set<SohMonitorType> validMonitorTypes = Arrays.stream(SohMonitorType.values())
      .filter(mt -> !mt.getSohValueType().equals(SohValueType.INVALID))
      .collect(Collectors.toSet());

    // Validate channelsByMonitorType is correct
    stationSohDefinitions.forEach(stationSohDefinition -> {

      Assertions.assertEquals(validMonitorTypes,
        stationSohDefinition.getChannelsBySohMonitorType().keySet());

      stationSohDefinition.getChannelsBySohMonitorType().entrySet()
        .forEach(monitorTypeToChannels -> {

          if ("StationA".equals(stationSohDefinition.getStationName())
            && monitorTypeToChannels.getKey().equals(SohMonitorType.LAG)) {

            Assertions.assertEquals(1, monitorTypeToChannels.getValue().size());
          } else if ("StationA".equals(stationSohDefinition.getStationName())) {

            Assertions.assertEquals(3, monitorTypeToChannels.getValue().size());
          } else {

            Assertions.assertEquals(2, monitorTypeToChannels.getValue().size());
          }
        });
    });

    // Validate monitorTypesInRollup is correct
    stationSohDefinitions.forEach(stationSohDefinition -> {
      if ("StationA".equals(stationSohDefinition.getStationName())) {
        Assertions.assertEquals(Set.of(SohMonitorType.LAG),
          stationSohDefinition.getSohMonitorTypesForRollup());
      } else {
        Assertions.assertEquals(validMonitorTypes,
          stationSohDefinition.getSohMonitorTypesForRollup());
      }
      }
    );

    // Validate correct ChannelSohDefinitions exist
    Set<ChannelSohDefinition> channelSohDefinitions = stationSohDefinitions.stream()
      .flatMap(staDef -> staDef.getChannelSohDefinitions().stream())
      .collect(Collectors.toSet());

    // Validate correct monitorTypesInRollup for ChannelSohDefinitions
    channelSohDefinitions.forEach(channelSohDefinition -> {
      if ("ChannelA".equals(channelSohDefinition.getChannelName())) {
        Assertions.assertEquals(Set.of(SohMonitorType.LAG),
          channelSohDefinition.getSohMonitorTypesForRollup());
      } else {
        Assertions.assertEquals(validMonitorTypes,
          channelSohDefinition.getSohMonitorTypesForRollup());
      }
      }
    );

    Assertions.assertEquals(
      1,
      stationSohMonitoringDefinition.getCapabilitySohRollupDefinitions().size()
    );

    CapabilitySohRollupDefinition theOneDefintion =
      stationSohMonitoringDefinition.getCapabilitySohRollupDefinitions()
        .stream().findFirst().get();

    Assertions.assertEquals(
      MinGoodOfRollupOperator.from(
        List.of("StationA"),
        List.of(),
        List.of(),
        List.of(),
        2,
        1
      ),
      theOneDefintion.getStationsToGroupRollupOperator()
    );

    Map<String, StationRollupDefinition> theMapOfStationRollups = theOneDefintion
      .getStationRollupDefinitionsByStation();

    Assertions.assertEquals(
      1,
      theMapOfStationRollups.keySet().size()
    );

    Assertions.assertNotNull(
      theMapOfStationRollups.get("StationA")
    );

    StationRollupDefinition theStationRollupDefinition = theMapOfStationRollups.get("StationA");

    Assertions.assertEquals(
      BestOfRollupOperator.from(
        List.of(),
        List.of(
          "ChannelA",
          "ChannelB"
        ),
        List.of(),
        List.of()
      ),
      theStationRollupDefinition.getChannelsToStationRollupOperator()
    );

    Assertions.assertEquals(
      Set.of("ChannelA", "ChannelB"),
      theStationRollupDefinition.getChannelRollupDefinitionsByChannel().keySet()
    );

    Map<String, ChannelRollupDefinition> theMapOfChannelRollupDefinitions = theStationRollupDefinition
      .getChannelRollupDefinitionsByChannel();

    theMapOfChannelRollupDefinitions.forEach(
      (k, v) -> {

        if ("ChannelC".equals(k)) {
          Assertions.assertEquals(
            BestOfRollupOperator.from(
              List.of(),
              List.of(),
              List.of(SohMonitorType.MISSING, SohMonitorType.LAG,
                SohMonitorType.ENV_CLIPPED),
              List.of()
            ),
            v.getSohMonitorsToChannelRollupOperator()
          );
        } else {
          Assertions.assertEquals(
            BestOfRollupOperator.from(
              List.of(),
              List.of(),
              List.of(SohMonitorType.MISSING, SohMonitorType.LAG),
              List.of()
            ),
            v.getSohMonitorsToChannelRollupOperator()
          );
        }

      });

    Assertions.assertEquals(
      Duration.ofSeconds(600),
      stationSohMonitoringDefinition.getRollupStationSohTimeTolerance()
    );
  }

  @Test
  void testBackOffDurationOverride() {

    final String defaultStationName = "StationA";
    final Duration defaultValue = Duration.ofSeconds(40);
    final String stationOverrideName = "StationB";
    final Duration stationOverrideValue = Duration.ofSeconds(50);
    final String channelOverrideStationName = "StationC";
    final String channelOverrideChannelName = "ChannelF";
    final Duration channelOverrideValue = Duration.ofSeconds(30);
    final SohMonitorType monitorTypeOverrideType = SohMonitorType.ENV_CALIBRATION_UNDERWAY;
    final Duration monitorTypeOverrideValue = Duration.ofSeconds(20);

    Set<StationSohDefinition> stationSohDefinitions;

    // Validate default value

    init("gms/core/performancemonitoring/soh/configuration-base-backoffduration-1");

    stationSohDefinitions = stationSohControlConfiguration
      .getInitialConfigurationPair().getStationSohMonitoringDefinition()
      .getStationSohDefinitions();

    stationSohDefinitions.stream()
      .filter(station -> station.getStationName().equals(defaultStationName))
      .flatMap(station -> station.getTimeWindowBySohMonitorType().values().stream())
      .forEach(item -> Assertions.assertEquals(defaultValue, item.getBackOffDuration()));

    // Validate Station level override value

    stationSohDefinitions.stream()
      .filter(station -> station.getStationName().equals(stationOverrideName))
      .forEach(station -> {
        TimeWindowDefinition def = station.getTimeWindowBySohMonitorType().get(SohMonitorType.LAG);
        Assertions.assertEquals(stationOverrideValue, def.getBackOffDuration());
      });

    // Validate SohMonitorType override

    init("gms/core/performancemonitoring/soh/configuration-base-backoffduration-2");

    stationSohDefinitions = stationSohControlConfiguration
      .getInitialConfigurationPair().getStationSohMonitoringDefinition()
      .getStationSohDefinitions();

    // TODO: Possibly re-implement this code.
//    stationSohDefinitions.stream()
//        .flatMap(station -> station.getChannelSohDefinitions().stream())
//        .forEach(channel -> channel
//            .getSohMonitorStatusThresholdDefinitionsBySohMonitorType()
//            .forEach((type, smvasd) -> {
//                  if (type.equals(monitorTypeOverrideType)) {
//                    Assertions.assertEquals(monitorTypeOverrideValue, smvasd.getBackOffDuration());
//                  } else {
//                    Assertions.assertEquals(defaultValue, smvasd.getBackOffDuration());
//                  }
//                }
//            )
//        );
  }
}
