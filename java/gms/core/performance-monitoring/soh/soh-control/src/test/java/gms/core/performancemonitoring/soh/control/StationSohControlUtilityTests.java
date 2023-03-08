package gms.core.performancemonitoring.soh.control;

import gms.core.performancemonitoring.soh.control.configuration.DurationSohMonitorStatusThresholdDefinition;
import gms.core.performancemonitoring.soh.control.configuration.PercentSohMonitorStatusThresholdDefinition;
import gms.core.performancemonitoring.soh.control.configuration.SohMonitorStatusThresholdDefinition;
import gms.core.performancemonitoring.soh.control.configuration.StationSohDefinition;
import gms.core.performancemonitoring.soh.control.configuration.TimeWindowDefinition;
import gms.shared.frameworks.configuration.RetryConfig;
import gms.shared.frameworks.configuration.repository.FileConfigurationRepository;
import gms.shared.frameworks.configuration.repository.client.ConfigurationConsumerUtility;
import gms.shared.frameworks.osd.coi.channel.Channel;
import gms.shared.frameworks.osd.coi.signaldetection.Station;
import gms.shared.frameworks.osd.coi.signaldetection.StationGroup;
import gms.shared.frameworks.osd.coi.soh.SohMonitorType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

class StationSohControlUtilityTests {

  private static List<StationGroup> getMockStationGroups() {

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

    return List.of(mockStationGroupA, mockStationGroupB);
  }

  private static final RetryConfig retryConfig = RetryConfig.create(1, 10, ChronoUnit.SECONDS, 1);

  @ParameterizedTest
  @MethodSource("monitorThresholdTestsSource")
  void testMonitorThresholds(
    Set<StationSohDefinition> definitionSet,
    String stationName,
    String channelName,
    Map<SohMonitorType, SohMonitorStatusThresholdDefinition<?>> sohMonitorStatusThresholdDefinitionMap
  ) {

    var stationDefinition = definitionSet.stream().filter(
      stationSohDefinition -> stationSohDefinition.getStationName().equals(
        stationName
      )
    ).findFirst().get();

    var stationChannelDefinition = stationDefinition.getChannelSohDefinitions().stream().filter(
      channelSohDefinition -> channelSohDefinition.getChannelName().equals(channelName)
    ).findFirst().get();

    sohMonitorStatusThresholdDefinitionMap.forEach(
      (k, v) -> Assertions.assertEquals(
        stationChannelDefinition.getSohMonitorStatusThresholdDefinitionsBySohMonitorType()
          .get(k),
        v
      )
    );

  }

  private static Stream<Arguments> monitorThresholdTestsSource() {

    URL configBasePathUrl = Thread.currentThread().getContextClassLoader().getResource(
      "gms/core/performancemonitoring/soh/configuration-base"
    );
    Objects.requireNonNull(configBasePathUrl, "Null configBasePathUrl");
    Path configurationBasePath = new File(configBasePathUrl.getFile()).toPath();

    FileConfigurationRepository fileConfigurationRepository = FileConfigurationRepository
      .create(configurationBasePath);

    var stationGroups = getMockStationGroups();

    var definitions = PerformanceMonitoringConfigurationUtility.resolveStationSohDefinitions(
      ConfigurationConsumerUtility.builder(
        fileConfigurationRepository
      ).retryConfiguration(retryConfig).build(),
      stationGroups.get(0).getStations()
    );

    var stationAMap = new HashMap<SohMonitorType, SohMonitorStatusThresholdDefinition<?>>();
    var stationBMap = new HashMap<SohMonitorType, SohMonitorStatusThresholdDefinition<?>>();
    var stationCMap = new HashMap<SohMonitorType, SohMonitorStatusThresholdDefinition<?>>();

    SohMonitorType.validTypes().stream().filter(
      SohMonitorType::isEnvironmentIssue
    ).forEach(
      sohMonitorType -> {
        stationAMap.put(
          sohMonitorType,
          PercentSohMonitorStatusThresholdDefinition.create(
            85.222,
            90.111
          )
        );

        stationBMap.put(
          sohMonitorType,
          PercentSohMonitorStatusThresholdDefinition.create(
            85.222,
            90.111
          )
        );

        stationCMap.put(
          sohMonitorType,
          PercentSohMonitorStatusThresholdDefinition.create(
            85.222,
            90.111
          )
        );
      }
    );

    stationAMap.putAll(
      Map.of(
        SohMonitorType.LAG,
        DurationSohMonitorStatusThresholdDefinition.create(
          Duration.parse("PT1S"),
          Duration.parse("PT5S")
        ),
        SohMonitorType.TIMELINESS,
        DurationSohMonitorStatusThresholdDefinition.create(
          Duration.parse("PT1S"),
          Duration.parse("PT2S")
        ),
        SohMonitorType.MISSING,
        PercentSohMonitorStatusThresholdDefinition.create(
          85.222,
          90.111
        )
      )
    );

    stationBMap.putAll(
      Map.of(
        SohMonitorType.LAG,
        DurationSohMonitorStatusThresholdDefinition.create(
          Duration.parse("PT1S"),
          Duration.parse("PT4S")
        ),
        SohMonitorType.TIMELINESS,
        DurationSohMonitorStatusThresholdDefinition.create(
          Duration.parse("PT1S"),
          Duration.parse("PT2S")
        ),
        SohMonitorType.MISSING,
        PercentSohMonitorStatusThresholdDefinition.create(
          85.222,
          90.111
        )
      )
    );

    stationCMap.putAll(
      Map.of(
        SohMonitorType.LAG,
        DurationSohMonitorStatusThresholdDefinition.create(
          Duration.parse("PT1S"),
          Duration.parse("PT2S")
        ),
        SohMonitorType.TIMELINESS,
        DurationSohMonitorStatusThresholdDefinition.create(
          Duration.parse("PT1S"),
          Duration.parse("PT2S")
        ),
        SohMonitorType.MISSING,
        PercentSohMonitorStatusThresholdDefinition.create(
          85.222,
          90.111
        )
      )
    );

    return Stream.of(
      Arguments.arguments(
        definitions,
        "StationA", "ChannelA",
        stationAMap
      ),
      Arguments.arguments(
        definitions,
        "StationB", "ChannelD",
        stationBMap
      ),
      Arguments.arguments(
        definitions,
        "StationB", "ChannelE",
        stationBMap
      ),
      Arguments.arguments(
        definitions,
        "StationC", "ChannelF",
        stationCMap
      ),
      Arguments.arguments(
        definitions,
        "StationC", "ChannelG",
        stationCMap
      )
    );
  }

  @ParameterizedTest
  @MethodSource("timeWindowDefinitionsTestSource")
  void testTimeWindowDefinitions(
    Set<StationSohDefinition> definitionSet,
    String stationName,
    Map<SohMonitorType, TimeWindowDefinition> timeWindowDefinitionMap
  ) {

    var stationDefinition = definitionSet.stream().filter(
      stationSohDefinition -> stationSohDefinition.getStationName().equals(
        stationName
      )
    ).findFirst().get();

    timeWindowDefinitionMap.forEach(
      (k, v) -> Assertions.assertEquals(
        stationDefinition.getTimeWindowBySohMonitorType().get(k),
        v
      )
    );
  }

  private static Stream<Arguments> timeWindowDefinitionsTestSource() {

    URL configBasePathUrl = Thread.currentThread().getContextClassLoader().getResource(
      "gms/core/performancemonitoring/soh/configuration-base"
    );
    Objects.requireNonNull(configBasePathUrl, "Null configBasePathUrl");
    Path configurationBasePath = new File(configBasePathUrl.getFile()).toPath();

    FileConfigurationRepository fileConfigurationRepository = FileConfigurationRepository
      .create(configurationBasePath);

    var stationGroups = getMockStationGroups();

    var definitions = PerformanceMonitoringConfigurationUtility.resolveStationSohDefinitions(
      ConfigurationConsumerUtility.builder(
        fileConfigurationRepository
      ).retryConfiguration(retryConfig).build(),
      stationGroups.get(0).getStations()
    );

    var stationAMap = new HashMap<SohMonitorType, TimeWindowDefinition>();
    var stationBMap = new HashMap<SohMonitorType, TimeWindowDefinition>();

    SohMonitorType.validTypes().forEach(
      sohMonitorType -> {
        stationAMap.put(
          sohMonitorType,
          TimeWindowDefinition.create(
            Duration.ofMinutes(31),
            Duration.ofMinutes(31)
          )
        );

        stationBMap.put(
          sohMonitorType,
          TimeWindowDefinition.create(
            Duration.ofMinutes(30),
            Duration.ofMinutes(30)
          )
        );
      }
    );

    stationAMap.putAll(
      Map.of(
        SohMonitorType.LAG,
        TimeWindowDefinition.create(
          Duration.ofMinutes(15),
          Duration.ofMinutes(15)
        )
      )
    );

    stationBMap.putAll(
      Map.of(
        SohMonitorType.LAG,
        TimeWindowDefinition.create(
          Duration.ofMinutes(5),
          Duration.ofMinutes(5)
        )
      )
    );

    return Stream.of(
      Arguments.arguments(
        definitions,
        "StationA",
        stationAMap
      ),
      Arguments.arguments(
        definitions,
        "StationB",
        stationBMap
      )
    );
  }
}
