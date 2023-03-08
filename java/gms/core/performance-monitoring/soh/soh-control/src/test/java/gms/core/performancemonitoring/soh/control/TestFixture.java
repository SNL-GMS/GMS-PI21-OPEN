package gms.core.performancemonitoring.soh.control;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import gms.core.performancemonitoring.soh.control.configuration.BestOfRollupOperator;
import gms.core.performancemonitoring.soh.control.configuration.CapabilitySohRollupDefinition;
import gms.core.performancemonitoring.soh.control.configuration.ChannelRollupDefinition;
import gms.core.performancemonitoring.soh.control.configuration.ChannelSohDefinition;
import gms.core.performancemonitoring.soh.control.configuration.DurationSohMonitorStatusThresholdDefinition;
import gms.core.performancemonitoring.soh.control.configuration.PercentSohMonitorStatusThresholdDefinition;
import gms.core.performancemonitoring.soh.control.configuration.SohMonitorStatusThresholdDefinition;
import gms.core.performancemonitoring.soh.control.configuration.StationRollupDefinition;
import gms.core.performancemonitoring.soh.control.configuration.StationSohDefinition;
import gms.core.performancemonitoring.soh.control.configuration.TimeWindowDefinition;
import gms.core.performancemonitoring.soh.control.configuration.WorstOfRollupOperator;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssue;
import gms.shared.frameworks.osd.coi.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.frameworks.osd.coi.soh.AcquiredStationSohExtract;
import gms.shared.frameworks.osd.coi.soh.ChannelSoh;
import gms.shared.frameworks.osd.coi.soh.DurationSohMonitorValueAndStatus;
import gms.shared.frameworks.osd.coi.soh.PercentSohMonitorValueAndStatus;
import gms.shared.frameworks.osd.coi.soh.PercentStationAggregate;
import gms.shared.frameworks.osd.coi.soh.SohMonitorType;
import gms.shared.frameworks.osd.coi.soh.SohStatus;
import gms.shared.frameworks.osd.coi.soh.StationAggregate;
import gms.shared.frameworks.osd.coi.soh.StationAggregateType;

import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Contains static utility methods to be used by other unit tests in the package.
 */
public class TestFixture {

  /**
   * Reverse engineers a set of {@code StationSohDefinition}s from a set of extracts.
   *
   * @param extracts A collection containing {@code AcquiredStationSohExtract}s.
   * @param random For selecting random threshold.
   */
  public static Set<StationSohDefinition> computeStationSohDefinitions(
    final Collection<AcquiredStationSohExtract> extracts,
    SecureRandom random) {

    final Map<String, Set<String>> stationNamesToChannelNames = new HashMap<>();
    final Map<String, String> channelNameToStationName = new HashMap<>();

    collectStationToChannelNameMappings(extracts,
      stationNamesToChannelNames, channelNameToStationName);

    final Map<String, Map<SohMonitorType, Set<String>>> stationNameToChannelsBySohMonitorTypeMap =
      new HashMap<>();

    extracts.stream()
      .map(AcquiredStationSohExtract::getAcquiredChannelEnvironmentIssues)
      .flatMap(List::stream)
      .forEach(issue -> {

        String channelName = issue.getChannelName();
        String stationName = channelNameToStationName.get(channelName);

        if (stationName != null) {

          Map<SohMonitorType, Set<String>> channelsBySohMonitorType =
            stationNameToChannelsBySohMonitorTypeMap.get(stationName);

          if (channelsBySohMonitorType == null) {
            channelsBySohMonitorType = new HashMap<>();
            stationNameToChannelsBySohMonitorTypeMap.put(stationName, channelsBySohMonitorType);
          }

          // Always compute lag and missing. They are not environment issues.
          channelsBySohMonitorType.computeIfAbsent(SohMonitorType.LAG,
            smt -> new HashSet<>()).add(channelName);

          channelsBySohMonitorType.computeIfAbsent(
            SohMonitorType.MISSING, smt -> new HashSet<>()).add(channelName);

          channelsBySohMonitorType.computeIfAbsent(
            SohMonitorType.TIMELINESS, smt -> new HashSet<>()).add(channelName);

          // Don't include all environmental issues in every channel's definition, so we can
          // later check that only those included make it into the rollup.
          channelsBySohMonitorType
            .computeIfAbsent(issue.getType().getMatchingSohMonitorType(),
              smt -> new HashSet<>()).add(channelName);
        }
      });

    final Map<String, ChannelSohDefinition> channelSohDefinitionMap = new HashMap<>();

    channelNameToStationName.keySet().forEach(channelName -> {
      final String stationName = channelNameToStationName.get(channelName);
      if (stationName != null) {

        Map<SohMonitorType, Set<String>> channelsBySohMonitorType =
          stationNameToChannelsBySohMonitorTypeMap.get(stationName);

        Map<SohMonitorType, SohMonitorStatusThresholdDefinition<?>> monitorDefMap =
          new HashMap<>();

        channelsBySohMonitorType.forEach((sohMonitorType, channelNames) -> {
          if (channelNames.contains(channelName)) {
            monitorDefMap.put(
              sohMonitorType,
              randomSohMonitorValueAndStatusDefinition(sohMonitorType, random)
            );
          }
        });

        ChannelSohDefinition channelSohDefinition = ChannelSohDefinition
          .create(channelName, monitorDefMap.keySet(), monitorDefMap, 0.0);

        channelSohDefinitionMap.put(channelName, channelSohDefinition);
      }
    });

    final Set<StationSohDefinition> stationSohDefinitions = new HashSet<>();

    stationNamesToChannelNames.forEach((stationName, channelNames) -> {

      Map<SohMonitorType, Set<String>> channelsBySohMonitorType =
        stationNameToChannelsBySohMonitorTypeMap.get(stationName);

      // At this point, channelsBySohMonitorType only contains SohMonitorTypes mapping to
      // a subset of the environment issues.
      channelsBySohMonitorType.put(SohMonitorType.MISSING, channelNames);
      channelsBySohMonitorType.put(SohMonitorType.LAG, channelNames);
      channelsBySohMonitorType.put(SohMonitorType.TIMELINESS, channelNames);

      Set<ChannelSohDefinition> channelSohDefinitions = new HashSet<>();

      for (String channelName : channelNames) {
        if (channelSohDefinitionMap.containsKey(channelName)) {
          channelSohDefinitions.add(channelSohDefinitionMap.get(channelName));
        }
      }

      stationSohDefinitions.add(StationSohDefinition.create(
        stationName,
        channelsBySohMonitorType.keySet(),
        channelsBySohMonitorType,
        channelSohDefinitions, createTimeWindowDefMap(channelsBySohMonitorType.keySet())));
    });

    return stationSohDefinitions;
  }

  public static Map<SohMonitorType, TimeWindowDefinition> createTimeWindowDefMap(
    Set<SohMonitorType> monitorTypes) {

    Map<SohMonitorType, TimeWindowDefinition> timeWindowDefMap = new HashMap<>();

    monitorTypes.forEach(type -> timeWindowDefMap
      .put(type, TimeWindowDefinition.create(Duration.ofMinutes(5), Duration.ofSeconds(30))));

    return timeWindowDefMap;
  }

  /**
   * Given a collection of {@code AcquiredStationSohExtracts}, generate a set of compatible {@code
   * CapabilitySohRollupDefinition} objects.
   */
  public static Set<CapabilitySohRollupDefinition> computeCapabilitySohRollupDefinitions(
    final Collection<AcquiredStationSohExtract> extracts,
    SecureRandom random) {

    final Map<String, Set<String>> stationNamesToChannelNames = new LinkedHashMap<>();
    final Map<String, String> channelNameToStationName = new LinkedHashMap<>();

    collectStationToChannelNameMappings(
      extracts, stationNamesToChannelNames, channelNameToStationName
    );

    List<String> stationNames = new ArrayList<>(stationNamesToChannelNames.keySet());

    final int stationCount = stationNames.size();

    final Map<String, List<String>> groupNamesToStationNames = new LinkedHashMap<>();
    final int mid = stationCount / 2;

    for (int i = 0; i < stationCount; i++) {
      String groupName = i < mid ? "group1" : "group2";
      String stationName = stationNames.get(i);
      groupNamesToStationNames.computeIfAbsent(groupName, gn -> new ArrayList<>()).add(stationName);
    }

    Set<CapabilitySohRollupDefinition> capabilitySohRollupDefinitions = new LinkedHashSet<>();

    List<SohMonitorType> list1 = List
      .of(SohMonitorType.MISSING, SohMonitorType.LAG, SohMonitorType.ENV_DURATION_OUTAGE);
    List<SohMonitorType> list2 = List.of(SohMonitorType.MISSING, SohMonitorType.LAG);
    groupNamesToStationNames.entrySet().forEach(entry -> {

      final String groupName = entry.getKey();
      final List<String> stations = entry.getValue();

      final Map<String, StationRollupDefinition> stationRollupDefinitions = new HashMap<>();
      stations.forEach(station -> {
        Map<String, ChannelRollupDefinition> channelRollupDefinitions = new HashMap<>();
        Set<String> channels = stationNamesToChannelNames.get(station);
        channels.forEach(channel -> {
          channelRollupDefinitions.put(channel, ChannelRollupDefinition.from(
            WorstOfRollupOperator.from(
              List.of(),
              List.of(),
              random.nextBoolean() ? list1 : list2,
              List.of()
            )
          ));
        });
        stationRollupDefinitions.put(station, StationRollupDefinition.from(
          BestOfRollupOperator.from(
            List.of(),
            new ArrayList<>(channels),
            List.of(),
            List.of()
          ), channelRollupDefinitions));
      });

      capabilitySohRollupDefinitions.add(
        CapabilitySohRollupDefinition.from(
          groupName,
          BestOfRollupOperator.from(
            stations,
            List.of(),
            List.of(),
            List.of()
          ),
          stationRollupDefinitions
        )
      );
    });

    return capabilitySohRollupDefinitions;
  }

  private static void collectStationToChannelNameMappings(
    Collection<AcquiredStationSohExtract> extracts,
    Map<String, Set<String>> stationNamesToChannelNames,
    Map<String, String> channelNameToStationName) {

    extracts.stream()
      .map(AcquiredStationSohExtract::getAcquisitionMetadata)
      .flatMap(List::stream)
      .forEach(metadata -> {
        stationNamesToChannelNames.computeIfAbsent(
          metadata.getStationName(),
          sn -> new LinkedHashSet<>()).addAll(metadata.getChannelNames());
        metadata.getChannelNames().forEach(cn -> channelNameToStationName.put(cn,
          metadata.getStationName()));
      });
  }

  private static SohMonitorStatusThresholdDefinition<?> randomSohMonitorValueAndStatusDefinition(
    SohMonitorType sohMonitorType,
    SecureRandom random
  ) {

    int calcSeconds = 20 + random.nextInt(10);

    int backOffSeconds = 40 + random.nextInt(10);

    if (sohMonitorType == SohMonitorType.LAG || sohMonitorType == SohMonitorType.TIMELINESS) {
      int goodSeconds = 1 + random.nextInt(3);
      int marginalSeconds = goodSeconds + (1 + random.nextInt(3));
      return DurationSohMonitorStatusThresholdDefinition.create(
        Duration.ofSeconds(goodSeconds),
        Duration.ofSeconds(marginalSeconds)
      );
    } else {
      double goodPercentage = 5.0 + random.nextDouble() * 10.0;
      double marginalPercentage = goodPercentage + random.nextDouble() * 15.0;
      return PercentSohMonitorStatusThresholdDefinition.create(
        goodPercentage,
        marginalPercentage
      );
    }
  }

  public static List<AcquiredStationSohExtract> loadExtracts() throws IOException {
    try (InputStream is = TestFixture.class.getResourceAsStream("/sohextracts.json")) {
      ObjectMapper objectMapper = CoiObjectMapperFactory.getJsonObjectMapper();
      JavaType extractListType = objectMapper.getTypeFactory()
        .constructCollectionType(List.class, AcquiredStationSohExtract.class);
      return objectMapper.readValue(is, extractListType);
    }
  }

  /**
   * Returns the maximum end time instant from the acquired channel environment issues in a set of
   * {@code AcquiredStationSohExtract}s.
   */
  public static Optional<Instant> maxEndTime(Collection<AcquiredStationSohExtract> extracts) {
    return extracts.stream()
      .map(AcquiredStationSohExtract::getAcquiredChannelEnvironmentIssues)
      .flatMap(List::stream)
      .map(AcquiredChannelEnvironmentIssue::getEndTime)
      .max(Instant::compareTo);
  }

  public static void main(String[] args) {
    try {
      computeStationSohDefinitions(loadExtracts(), new SecureRandom("hello".getBytes()));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static class StationAggregateSets {

    final static Set<StationAggregate<?>> percent_missing_90 = Set.of(
      PercentStationAggregate.from(90.0, StationAggregateType.MISSING)
    );
  }

  public static class ChannelSohSets {

    final static String CHANNEL_NAME_1 = "FLUPCHANNEL";
    
    final static String CHANNEL_NAME_2 = "FLUPCHANNEL2";
    
    final static String CHANNEL_NAME_3 = "FLUPCHANNEL3";

    final static String STATION_NAME_1 = "FLUP";

    final static Set<ChannelSoh> singleChannel__M_B10__E_B10__L_B1h = Set.of(
      ChannelSoh.from(
        CHANNEL_NAME_1,
        SohStatus.BAD,
        Set.of(
          PercentSohMonitorValueAndStatus.from(
            10.0,
            SohStatus.BAD,
            SohMonitorType.MISSING
          ),
          PercentSohMonitorValueAndStatus.from(
            10.0,
            SohStatus.BAD,
            SohMonitorType.ENV_CLIPPED
          ),
          DurationSohMonitorValueAndStatus.from(
            Duration.ofHours(1),
            SohStatus.BAD,
            SohMonitorType.LAG
          )
        )
      )
    );

    final static Set<ChannelSoh> twoChannel__T_B1D__T_B2D = Set.of(
      ChannelSoh.from(
        CHANNEL_NAME_1,
        SohStatus.BAD,
        Set.of(
          DurationSohMonitorValueAndStatus.from(
            Duration.ofDays(1),
            SohStatus.BAD,
            SohMonitorType.TIMELINESS
          )
        )
      ),
      ChannelSoh.from(
        CHANNEL_NAME_2,
        SohStatus.BAD,
        Set.of(
          DurationSohMonitorValueAndStatus.from(
            Duration.ofDays(2),
            SohStatus.BAD,
            SohMonitorType.TIMELINESS
          )
        )
      )
    );

    final static Set<ChannelSoh> twoChannel__M_B10__E_B10__L_B1h__M_M15__E_B10__L_B1h = Set.of(
      ChannelSoh.from(
        CHANNEL_NAME_1,
        SohStatus.BAD,
        Set.of(
          PercentSohMonitorValueAndStatus.from(
            10.0,
            SohStatus.BAD,
            SohMonitorType.MISSING
          ),
          PercentSohMonitorValueAndStatus.from(
            10.0,
            SohStatus.BAD,
            SohMonitorType.ENV_CLIPPED
          ),
          DurationSohMonitorValueAndStatus.from(
            Duration.ofHours(1),
            SohStatus.BAD,
            SohMonitorType.LAG
          )
        )
      ),

      ChannelSoh.from(
        CHANNEL_NAME_2,
        SohStatus.MARGINAL,
        Set.of(
          PercentSohMonitorValueAndStatus.from(
            15.0,
            SohStatus.MARGINAL,
            SohMonitorType.MISSING
          ),
          PercentSohMonitorValueAndStatus.from(
            10.0,
            SohStatus.BAD,
            SohMonitorType.ENV_CLIPPED
          ),
          DurationSohMonitorValueAndStatus.from(
            Duration.ofHours(1),
            SohStatus.BAD,
            SohMonitorType.LAG
          )
        )
      )
    );

    final static Set<ChannelSoh> twoChannel__M_B10__E_B10__L_B1h__M_Mnull__E_B10__L_B1h = Set.of(
      ChannelSoh.from(
        CHANNEL_NAME_1,
        SohStatus.BAD,
        Set.of(
          PercentSohMonitorValueAndStatus.from(
            10.0,
            SohStatus.BAD,
            SohMonitorType.MISSING
          ),
          PercentSohMonitorValueAndStatus.from(
            10.0,
            SohStatus.BAD,
            SohMonitorType.ENV_CLIPPED
          ),
          DurationSohMonitorValueAndStatus.from(
            Duration.ofHours(1),
            SohStatus.BAD,
            SohMonitorType.LAG
          )
        )
      ),

      ChannelSoh.from(
        CHANNEL_NAME_2,
        SohStatus.MARGINAL,
        Set.of(
          PercentSohMonitorValueAndStatus.from(
            null,
            SohStatus.MARGINAL,
            SohMonitorType.MISSING
          ),
          PercentSohMonitorValueAndStatus.from(
            10.0,
            SohStatus.BAD,
            SohMonitorType.ENV_CLIPPED
          ),
          DurationSohMonitorValueAndStatus.from(
            Duration.ofHours(1),
            SohStatus.BAD,
            SohMonitorType.LAG
          )
        )
      )
    );

    final static Set<ChannelSoh> twoChannel__M_Mnull__E_B10__L_B1h__M_M15__E_B10__L_B1h = Set.of(
      ChannelSoh.from(
        CHANNEL_NAME_1,
        SohStatus.MARGINAL,
        Set.of(
          PercentSohMonitorValueAndStatus.from(
            null,
            SohStatus.MARGINAL,
            SohMonitorType.MISSING
          ),
          PercentSohMonitorValueAndStatus.from(
            10.0,
            SohStatus.GOOD,
            SohMonitorType.ENV_CLIPPED
          ),
          DurationSohMonitorValueAndStatus.from(
            Duration.ofHours(1),
            SohStatus.GOOD,
            SohMonitorType.LAG
          )
        )
      ),

      ChannelSoh.from(
        CHANNEL_NAME_2,
        SohStatus.MARGINAL,
        Set.of(
          PercentSohMonitorValueAndStatus.from(
            15.0,
            SohStatus.MARGINAL,
            SohMonitorType.MISSING
          ),
          PercentSohMonitorValueAndStatus.from(
            10.0,
            SohStatus.GOOD,
            SohMonitorType.ENV_CLIPPED
          ),
          DurationSohMonitorValueAndStatus.from(
            Duration.ofHours(1),
            SohStatus.GOOD,
            SohMonitorType.LAG
          )
        )
      )
    );

    final static Set<ChannelSoh> threeChannel__M_Mnull__E_B10__L_B1h__M_M15__E_B10__L_B1h__M_M25__E_B10__L_B1h = Set
      .of(
        ChannelSoh.from(
          CHANNEL_NAME_1,
          SohStatus.MARGINAL,
          Set.of(
            PercentSohMonitorValueAndStatus.from(
              null,
              SohStatus.MARGINAL,
              SohMonitorType.MISSING
            ),
            PercentSohMonitorValueAndStatus.from(
              10.0,
              SohStatus.GOOD,
              SohMonitorType.ENV_CLIPPED
            ),
            DurationSohMonitorValueAndStatus.from(
              Duration.ofHours(1),
              SohStatus.GOOD,
              SohMonitorType.LAG
            )
          )
        ),

        ChannelSoh.from(
          CHANNEL_NAME_2,
          SohStatus.MARGINAL,
          Set.of(
            PercentSohMonitorValueAndStatus.from(
              15.0,
              SohStatus.MARGINAL,
              SohMonitorType.MISSING
            ),
            PercentSohMonitorValueAndStatus.from(
              10.0,
              SohStatus.GOOD,
              SohMonitorType.ENV_CLIPPED
            ),
            DurationSohMonitorValueAndStatus.from(
              Duration.ofHours(1),
              SohStatus.GOOD,
              SohMonitorType.LAG
            )
          )
        ),

        ChannelSoh.from(
          CHANNEL_NAME_3,
          SohStatus.MARGINAL,
          Set.of(
            PercentSohMonitorValueAndStatus.from(
              25.0,
              SohStatus.MARGINAL,
              SohMonitorType.MISSING
            ),
            PercentSohMonitorValueAndStatus.from(
              10.0,
              SohStatus.GOOD,
              SohMonitorType.ENV_CLIPPED
            ),
            DurationSohMonitorValueAndStatus.from(
              Duration.ofHours(1),
              SohStatus.GOOD,
              SohMonitorType.LAG
            )
          )
        )
      );
  }
}
