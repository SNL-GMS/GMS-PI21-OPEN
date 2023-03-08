package gms.core.performancemonitoring.soh.control;

import gms.core.performancemonitoring.soh.control.configuration.BestOfRollupOperator;
import gms.core.performancemonitoring.soh.control.configuration.CapabilitySohRollupDefinition;
import gms.core.performancemonitoring.soh.control.configuration.ChannelRollupDefinition;
import gms.core.performancemonitoring.soh.control.configuration.MinGoodOfRollupOperator;
import gms.core.performancemonitoring.soh.control.configuration.StationRollupDefinition;
import gms.core.performancemonitoring.soh.control.configuration.WorstOfRollupOperator;
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
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

class CapabilityRollupConfigurationUtilityTests {

  @ParameterizedTest
  @MethodSource("testProvider")
  void testUtilityReturnsCorrectDefinitions(
    String base,
    Consumer<CapabilityRollupConfigurationUtility> asserter,
    Function<String, CapabilityRollupConfigurationUtility> utilitySupplier
  ) {

    var utility = utilitySupplier.apply("gms/core/performancemonitoring/soh/" + base);

    asserter.accept(utility);
  }

  private static Stream<Arguments> testProvider() {

    return Stream.of(
      Arguments.arguments(
        "configuration-base-capatests-per-channel-only",
        (Consumer<CapabilityRollupConfigurationUtility>)
          CapabilityRollupConfigurationUtilityTests::assertUtilityTestNoNesting,
        (Function<String, CapabilityRollupConfigurationUtility>)
          CapabilityRollupConfigurationUtilityTests::getUtilityToTest__GroupA_StationA_ABC
      ),
      Arguments.arguments(
        "configuration-base-capatests-channel-default-only",
        (Consumer<CapabilityRollupConfigurationUtility>)
          CapabilityRollupConfigurationUtilityTests::assertUtilityTestNoNesting,
        (Function<String, CapabilityRollupConfigurationUtility>)
          CapabilityRollupConfigurationUtilityTests::getUtilityToTest__GroupA_StationA_ABC
      ),
      Arguments.arguments(
        "configuration-base-capatests-channel-default-only-implicit-channels",
        (Consumer<CapabilityRollupConfigurationUtility>)
          CapabilityRollupConfigurationUtilityTests::assertUtilityTestNoNesting,
        (Function<String, CapabilityRollupConfigurationUtility>)
          CapabilityRollupConfigurationUtilityTests::getUtilityToTest__GroupA_StationA_AB
      ),
      Arguments.arguments(
        "configuration-base-capatests-by-station-group",
        (Consumer<CapabilityRollupConfigurationUtility>)
          CapabilityRollupConfigurationUtilityTests::assertUtilityTestNoNesting,
        (Function<String, CapabilityRollupConfigurationUtility>)
          CapabilityRollupConfigurationUtilityTests::getUtilityToTest__GroupA_StationA_AB
      ),
      Arguments.arguments(
        "configuration-base-capatests-by-station",
        (Consumer<CapabilityRollupConfigurationUtility>)
          CapabilityRollupConfigurationUtilityTests::assertUtilityTestNoNesting,
        (Function<String, CapabilityRollupConfigurationUtility>)
          CapabilityRollupConfigurationUtilityTests::getUtilityToTest__GroupA_StationA_AB
      ),
      Arguments.arguments(
        "configuration-base-capatests-by-station-station-group",
        (Consumer<CapabilityRollupConfigurationUtility>)
          CapabilityRollupConfigurationUtilityTests::assertUtilityTestNoNesting,
        (Function<String, CapabilityRollupConfigurationUtility>)
          CapabilityRollupConfigurationUtilityTests::getUtilityToTest__GroupA_StationA_AB
      ),
      Arguments.arguments(
        "configuration-base-capatests-by-stationgroup-channel",
        (Consumer<CapabilityRollupConfigurationUtility>)
          CapabilityRollupConfigurationUtilityTests::assertUtilityTestNoNesting,
        (Function<String, CapabilityRollupConfigurationUtility>)
          CapabilityRollupConfigurationUtilityTests::getUtilityToTest__GroupA_StationA_AB
      ),
      Arguments.arguments(
        "configuration-base-capatests-by-station-channel",
        (Consumer<CapabilityRollupConfigurationUtility>)
          CapabilityRollupConfigurationUtilityTests::assertUtilityTestNoNesting,
        (Function<String, CapabilityRollupConfigurationUtility>)
          CapabilityRollupConfigurationUtilityTests::getUtilityToTest__GroupA_StationA_AB
      ),
      Arguments.arguments(
        "configuration-base-capatests-by-station-stationgroup-channel",
        (Consumer<CapabilityRollupConfigurationUtility>)
          CapabilityRollupConfigurationUtilityTests::assertUtilityTestNoNesting,
        (Function<String, CapabilityRollupConfigurationUtility>)
          CapabilityRollupConfigurationUtilityTests::getUtilityToTest__GroupA_StationA_AB
      ),

      Arguments.arguments(
        "configuration-base-capatests-nested-operators-channel",
        (Consumer<CapabilityRollupConfigurationUtility>)
          CapabilityRollupConfigurationUtilityTests::assertUtilityTestForNestedChannelOperator,
        (Function<String, CapabilityRollupConfigurationUtility>)
          CapabilityRollupConfigurationUtilityTests::getUtilityToTest__GroupA_StationA_AB
      )
    );
  }

  private static Channel getMockChannel(
    String channelName,
    String stationName
  ) {

    Channel mockChannel = Mockito.mock(Channel.class);

    Mockito.when(mockChannel.getName()).thenReturn(channelName);
    Mockito.when(mockChannel.getStation()).thenReturn(stationName);

    return mockChannel;
  }

  private static Station getMockStation(
    NavigableSet<Channel> channels,
    String stationName
  ) {

    Station mockStation = Mockito.mock(Station.class);

    Mockito.when(mockStation.getChannels()).thenReturn(channels);

    Mockito.when(mockStation.getName()).thenReturn(stationName);

    return mockStation;
  }

  private static StationGroup getMockStationGroup(
    Set<Station> stations,
    String stationGroupName
  ) {

    StationGroup mockStationGroup = Mockito.mock(StationGroup.class);

    var sortedStations = new TreeSet<>(Comparator.comparing(Station::getName));

    sortedStations.addAll(stations);

    Mockito.when(mockStationGroup.getStations()).thenReturn(sortedStations);

    Mockito.when(mockStationGroup.getName()).thenReturn(stationGroupName);

    return mockStationGroup;
  }

  private static ConfigurationConsumerUtility getFileConsumerUtility(String basepath) {

    URL configBasePathUrl = Thread.currentThread().getContextClassLoader().getResource(basepath);

    Path configurationBasePath = new File(configBasePathUrl.getFile()).toPath();

    final RetryConfig retryConfig = RetryConfig.create(1, 10, ChronoUnit.SECONDS, 1);

    return ConfigurationConsumerUtility.builder(
      FileConfigurationRepository
        .create(configurationBasePath)
    ).retryConfiguration(retryConfig).configurationNamePrefixes(Set.of("soh-control")).build();

  }

  private static void assertUtilityTestNoNesting(CapabilityRollupConfigurationUtility utility) {

    var definitions = utility.resolveCapabilitySohRollupDefinitions();

    Assertions.assertEquals(
      CapabilitySohRollupDefinition.from(
        "GroupA",
        MinGoodOfRollupOperator.from(
          List.of("StationA"),
          List.of(),
          List.of(),
          List.of(),
          2, 1
        ),
        Map.of(
          "StationA",
          StationRollupDefinition.from(
            BestOfRollupOperator.from(
              List.of(),
              List.of("ChannelA", "ChannelB"),
              List.of(),
              List.of()
            ),
            Map.of(
              "ChannelA",
              ChannelRollupDefinition.from(
                BestOfRollupOperator.from(
                  List.of(),
                  List.of(),
                  List.of(SohMonitorType.MISSING, SohMonitorType.LAG),
                  List.of()
                )
              ),
              "ChannelB",
              ChannelRollupDefinition.from(
                BestOfRollupOperator.from(
                  List.of(),
                  List.of(),
                  List.of(SohMonitorType.MISSING, SohMonitorType.LAG),
                  List.of()
                )
              )
            )
          )
        )
      ),
      definitions.toArray()[0]
    );

  }

  private static void assertUtilityTestForNestedChannelOperator(CapabilityRollupConfigurationUtility utility) {

    var definitions = utility.resolveCapabilitySohRollupDefinitions();

    Assertions.assertEquals(
      CapabilitySohRollupDefinition.from(
        "GroupA",
        MinGoodOfRollupOperator.from(
          List.of("StationA"),
          List.of(),
          List.of(),
          List.of(),
          2, 1
        ),
        Map.of(
          "StationA",
          StationRollupDefinition.from(
            BestOfRollupOperator.from(
              List.of(),
              List.of(),
              List.of(),
              List.of(
                WorstOfRollupOperator.from(
                  List.of(),
                  List.of("ChannelA", "ChannelB"),
                  List.of(),
                  List.of()
                ),
                BestOfRollupOperator.from(
                  List.of(),
                  List.of("ChannelA", "ChannelB"),
                  List.of(),
                  List.of()
                )
              )
            ),
            Map.of(
              "ChannelA",
              ChannelRollupDefinition.from(
                WorstOfRollupOperator.from(
                  List.of(),
                  List.of(),
                  List.of(SohMonitorType.MISSING, SohMonitorType.LAG),
                  List.of()
                )
              ),
              "ChannelB",
              ChannelRollupDefinition.from(
                WorstOfRollupOperator.from(
                  List.of(),
                  List.of(),
                  List.of(SohMonitorType.MISSING, SohMonitorType.LAG),
                  List.of()
                )
              )
            )
          )
        )
      ),
      definitions.toArray()[0]
    );
  }

  private static CapabilityRollupConfigurationUtility getUtilityToTest__GroupA_StationA_ABC(String basepath) {
    var channels = new TreeSet<>(Comparator.comparing(Channel::getName));

    channels.add(
      getMockChannel("ChannelA", "StationA")
    );

    channels.add(
      getMockChannel("ChannelB", "StationA")
    );

    channels.add(
      getMockChannel("ChannelC", "StationA")
    );

    var station = getMockStation(
      channels,
      "StationA"
    );

    var stationGroup = getMockStationGroup(
      Set.of(station),
      "GroupA"
    );

    var consumerUtility = getFileConsumerUtility(basepath);

    return new CapabilityRollupConfigurationUtility(
      "StationGroupName",
      "StationName",
      "ChannelName",
      "soh-control",
      consumerUtility,
      Set.of(stationGroup)
    );
  }

  private static CapabilityRollupConfigurationUtility getUtilityToTest__GroupA_StationA_AB(String basepath) {
    var channels = new TreeSet<>(Comparator.comparing(Channel::getName));

    channels.add(
      getMockChannel("ChannelA", "StationA")
    );

    channels.add(
      getMockChannel("ChannelB", "StationA")
    );

    var station = getMockStation(
      channels,
      "StationA"
    );

    var stationGroup = getMockStationGroup(
      Set.of(station),
      "GroupA"
    );

    var consumerUtility = getFileConsumerUtility(basepath);

    return new CapabilityRollupConfigurationUtility(
      "StationGroupName",
      "StationName",
      "ChannelName",
      "soh-control",
      consumerUtility,
      Set.of(stationGroup)
    );
  }

}
