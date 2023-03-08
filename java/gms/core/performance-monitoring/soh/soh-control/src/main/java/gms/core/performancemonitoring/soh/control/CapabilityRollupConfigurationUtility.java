package gms.core.performancemonitoring.soh.control;

import com.google.common.base.Functions;
import gms.core.performancemonitoring.soh.control.RollupOperatorConfigurationUtility.TerminalRollupOperatorResolver;
import gms.core.performancemonitoring.soh.control.configuration.CapabilitySohRollupConfigurationOption;
import gms.core.performancemonitoring.soh.control.configuration.CapabilitySohRollupDefinition;
import gms.core.performancemonitoring.soh.control.configuration.CapabilitySohRollupOption;
import gms.core.performancemonitoring.soh.control.configuration.ChannelRollupConfigurationOption;
import gms.core.performancemonitoring.soh.control.configuration.ChannelRollupDefinition;
import gms.core.performancemonitoring.soh.control.configuration.RollupOperator;
import gms.core.performancemonitoring.soh.control.configuration.StationRollupConfigurationOption;
import gms.core.performancemonitoring.soh.control.configuration.StationRollupDefinition;
import gms.shared.frameworks.configuration.Selector;
import gms.shared.frameworks.configuration.repository.client.ConfigurationConsumerUtility;
import gms.shared.frameworks.osd.coi.channel.Channel;
import gms.shared.frameworks.osd.coi.signaldetection.Station;
import gms.shared.frameworks.osd.coi.signaldetection.StationGroup;
import gms.shared.frameworks.osd.coi.soh.SohMonitorType;
import org.apache.commons.lang3.Validate;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Instantiable utility for resolving capability-rollup-specific configuration.
 */
class CapabilityRollupConfigurationUtility {

  /**
   * Splits out a set of station groups into three maps: a map of station group name to station
   * group for all of the stationgroups in the collection, a map of station name to station for all
   * of the stations in all of the station groups, and a map of channel name to channel for all of
   * the channels in all of the stations in all of the station groups.
   */
  private static class PartitionedStationGroups {

    private final Map<String, StationGroup> stationGroupMap;

    private final Map<String, Station> stationMap;

    private final Map<String, Map<String, Station>> stationGroupToStationMap =
      new ConcurrentHashMap<>();

    private PartitionedStationGroups(
      Collection<StationGroup> stationGroups
    ) {

      stationGroupMap = stationGroups.stream()
        .collect(Collectors.toMap(StationGroup::getName, stationGroup -> stationGroup));

      stationMap = stationGroups.stream()
        .flatMap(stationGroup -> stationGroup.getStations().stream())
        .distinct()
        .collect(
          Collectors.toMap(Station::getName, Functions.identity()));
    }

    public Map<String, StationGroup> getStationGroupMap() {
      return stationGroupMap;
    }

    public Map<String, Station> getStationMap(String stationGroup) {
      if (!stationGroupToStationMap.containsKey(stationGroup)) {
        stationGroupToStationMap.put(
          stationGroup,
          stationGroupMap.get(stationGroup)
            .getStations().stream()
            .collect(Collectors.toMap(Station::getName, station -> station))
        );
      }

      return stationGroupToStationMap.get(stationGroup);
    }

  }

  private final String stationGroupNameSelectorKey;
  private final String stationNameSelectorKey;
  private final String channelNameSelectorKey;
  private final String stationSohPrefix;
  private final ConfigurationConsumerUtility configurationConsumerUtility;
  private final PartitionedStationGroups partitionedStationGroups;

  CapabilityRollupConfigurationUtility(String stationGroupNameSelectorKey,
    String stationNameSelectorKey, String channelNameSelectorKey, String stationSohPrefix,
    ConfigurationConsumerUtility configurationConsumerUtility,
    Collection<StationGroup> stationGroups) {
    this.stationGroupNameSelectorKey = stationGroupNameSelectorKey;
    this.stationNameSelectorKey = stationNameSelectorKey;
    this.channelNameSelectorKey = channelNameSelectorKey;
    this.stationSohPrefix = stationSohPrefix;
    this.configurationConsumerUtility = configurationConsumerUtility;
    this.partitionedStationGroups = new PartitionedStationGroups(stationGroups);
  }

  /**
   * Resolve the rollup time tolerance.
   */
  Duration resolveRollupStationSohTimeTolerance() {

    return Duration.parse(String.valueOf(
      configurationConsumerUtility.resolve(
        stationSohPrefix + ".rollup-stationsoh-time-tolerance",
        List.of()
      ).get("rollupStationSohTimeTolerance")));

  }

  /**
   * Using a list of station groups, construct the CapabilitySohRollupDefinition for each station
   * group
   *
   * @return Set of CapabilitySohRollupDefinition objects, one for each station group.
   */
  Set<CapabilitySohRollupDefinition> resolveCapabilitySohRollupDefinitions() {

    Map<String, CapabilitySohRollupOption>
      capabilitySohRollupConfigurationOptionByName =
      resolveCapabilitySohRollupConfigurationOptionByName();

    return resolveCapabilityRollupDefinitions(
      capabilitySohRollupConfigurationOptionByName
    );

  }

  /**
   * Transform a map of stationgroup -> CapabilitySohRollupConfigurationOption to a set of
   * CapabilitySohRollupDefinition objects.
   *
   * @param capabilitySohRollupConfigurationOptionMap map of stationgroup ->
   * CapabilitySohRollupConfigurationOption
   * @return Set of CapabilitySohRollupDefinition objects, one for each entry in the map
   */
  private Set<CapabilitySohRollupDefinition> resolveCapabilityRollupDefinitions(
    Map<String, CapabilitySohRollupOption> capabilitySohRollupConfigurationOptionMap
  ) {

    return capabilitySohRollupConfigurationOptionMap.entrySet().parallelStream().map(
      entry -> CapabilitySohRollupDefinition.from(
        entry.getKey(),
        entry.getValue().rollupOperator(),
        resolveStationRollupDefinitions(
          entry.getKey(),
          resolveStationRollupConfigurationOptionSetForCapability(entry.getKey())
        )
      )
    ).collect(Collectors.toSet());
  }

  /**
   * Transform a map of station name -> stationRollupConfigurationOption to a map of station name ->
   * StationRollupDefinition
   *
   * @param stationRollupConfigurationOptionsByName map of station name ->
   * tationRollupConfigurationOption
   * @return map of station name -> StationRollupDefinition
   */
  private Map<String, StationRollupDefinition> resolveStationRollupDefinitions(
    String stationGroupName,
    Map<String, CapabilitySohRollupOption> stationRollupConfigurationOptionsByName
  ) {

    return stationRollupConfigurationOptionsByName.entrySet().parallelStream()
      .map(entry -> {

        var channelConfigurationOptionByName =
          resolveChannelRollupConfigurationOptionSet(
            stationGroupName,
            entry.getKey(),
            new HashSet<>(findOperatorLeaves(entry.getValue().rollupOperator(),
              RollupOperator::getChannelOperands))
          );

        return Map.entry(
          entry.getKey(),
          StationRollupDefinition.from(
            entry.getValue().rollupOperator(),
            channelConfigurationOptionByName.entrySet().parallelStream().map(
              entry1 -> Map.entry(entry1.getKey(),
                ChannelRollupDefinition.from(
                  entry1.getValue().rollupOperator()
                )
              )).collect(Collectors.toMap(Entry::getKey, Entry::getValue))
          )
        );

      }).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
  }

  /**
   * Resolve the set of capabilitySohRollupConfigurationOptions represented in configrations as a
   * map of stationgroup -> CapabilitySohRollupConfigurationOption, using the provided list of
   * station groups.
   *
   * @return map of stationgroup -> CapabilitySohRollupConfigurationOption
   */
  private Map<String, CapabilitySohRollupOption> resolveCapabilitySohRollupConfigurationOptionByName() {

    return resolveConfigurationOption(
      CapabilitySohRollupConfigurationOption.class,
      partitionedStationGroups.getStationGroupMap().keySet(),
      "station-group-capability-rollup",
      stationGroupNameSelectorKey,
      List.of(),
      stationSohPrefix
    );

  }

  /**
   * Resolve the set of StationRollupConfigurationOptions represented in configuration as a map of
   * station name -> StationRollupConfigurationOption. The station names to use are in the provided
   * CapabilitySohRollupConfigurationOption object.
   *
   * @return map of station name -> StationRollupConfigurationOption
   */
  private Map<String, CapabilitySohRollupOption> resolveStationRollupConfigurationOptionSetForCapability(
    String stationGroupName
  ) {

    return resolveConfigurationOption(
      StationRollupConfigurationOption.class,
      partitionedStationGroups.getStationMap(stationGroupName).keySet(),
      "station-capability-rollup",
      stationNameSelectorKey,
      List.of(
        Selector.from(
          stationGroupNameSelectorKey,
          stationGroupName
        )
      ),
      stationSohPrefix
    );

  }

  /**
   * Resolve the set of ChannelRollupConfigurationOptions represented in configuration as a map of
   * channel name -> ChannelRollupConfigurationOption. The channel names to use are inside the
   * provided StationRollupConfigurationOption object.
   *
   * @return map of channel name -> ChannelRollupConfigurationOption
   */
  private Map<String, CapabilitySohRollupOption> resolveChannelRollupConfigurationOptionSet(
    String stationGroupName,
    String stationName,
    Collection<String> channelNames
  ) {

    return resolveConfigurationOption(
      ChannelRollupConfigurationOption.class,
      channelNames,
      "channel-capability-rollup",
      channelNameSelectorKey,
      List.of(
        Selector.from(
          stationGroupNameSelectorKey,
          stationGroupName
        ),
        Selector.from(
          stationNameSelectorKey,
          stationName
        )
      ),
      stationSohPrefix
    );
  }

  /**
   * Resolve a set of one of the capability configuration options.
   *
   * @param optionClass *Option class to resolve
   * @param configKey configation key - the thing that appears after the configuration prefix
   * @param selectorKey Key to use for selecting configuration
   * @param notFoundLoggerMessage Logger message to use if configuration not found. Must contain a
   * place holder for the key and a place holder for whatever exception was thrown.
   * @param <T> Type of thing to resolve
   * @return A map of key -> Option object. The key identifies the Option object.
   */
  private Map<String, CapabilitySohRollupOption> resolveConfigurationOption(
    Class<? extends CapabilitySohRollupOption> optionClass,
    Collection<String> configurationOptionIdentifiers,
    String configKey,
    String selectorKey,
    List<Selector> additionalSelectors,
    String stationSohPrefix
  ) {

    return configurationOptionIdentifiers.parallelStream()
      .map(
        id -> resolveConfigurationOption(
          optionClass,
          id,
          configKey,
          selectorKey,
          additionalSelectors,
          stationSohPrefix
        ).map(value -> Map.entry(id, value))
      ).filter(Optional::isPresent)
      .map(Optional::get)
      .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
  }

  /**
   * Resolve one of the capability configuration options.
   */
  private Optional<CapabilitySohRollupOption> resolveConfigurationOption(
    Class<? extends CapabilitySohRollupOption> optionClass,
    String identifier,
    String configKey,
    String selectorKey,
    List<Selector> additionalSelectors,
    String stationSohPrefix
  ) {
    var selectors = new ArrayList<>(additionalSelectors);

    selectors.add(
      Selector.from(
        selectorKey,
        identifier
      )
    );

    Map<String, ?> optionClassObjectMap;
    try {
      optionClassObjectMap = configurationConsumerUtility.resolve(
        stationSohPrefix + "." + configKey,
        selectors
      );
    } catch (IllegalStateException e) {
      //
      // ConfigurationConsumerUtility throws a IllegalStateException if configuration
      // was not found that matches the selectors. This is not exceptional.
      //
      return Optional.empty();
    }

    //
    // Just a closure so we are only casting once. It also has the effect of allowing
    // us to bring this annotation closer to the cast.
    //
    @SuppressWarnings("unchecked")
    Function<String, Map<String, ?>> rollupOperatorFieldResolver = fieldName -> {

      Object rollupOperatorObjectMap = optionClassObjectMap.get(fieldName);

      Validate.isTrue(
        Map.class.isAssignableFrom(rollupOperatorObjectMap.getClass()),
        fieldName + ": Expected something that looks like a rollup operator; got something else"
      );

      //
      // "unchecked" suppression:
      // We are checking that this is a map above. Also, if it is a map, it must be
      // a map of String to something, because that is what the
      // ConfigurationConsumerUtility gives us.
      //
      return (Map<String, ?>) rollupOperatorObjectMap;
    };

    if (optionClass == CapabilitySohRollupConfigurationOption.class) {

      return Optional.of(CapabilitySohRollupConfigurationOption.create(
        RollupOperatorConfigurationUtility.resolveOperator(
          rollupOperatorFieldResolver.apply("stationsToGroupRollupOperator"),
          partitionedStationGroups.stationGroupMap.get(identifier).getStations()
            .stream()
            .map(Station::getName)
            .collect(Collectors.toList()),
          TerminalRollupOperatorResolver.STATION_OPERATOR_RESOLVER
        )

      ));
    } else if (optionClass == StationRollupConfigurationOption.class) {

      return Optional.of(StationRollupConfigurationOption.create(
        RollupOperatorConfigurationUtility.resolveOperator(
          rollupOperatorFieldResolver.apply("channelsToStationRollupOperator"),
          partitionedStationGroups.stationMap.get(identifier).getChannels()
            .stream()
            .map(Channel::getName)
            .collect(Collectors.toList()),
          TerminalRollupOperatorResolver.CHANNEL_OPERATOR_RESOLVER
        )
      ));
    } else if (optionClass == ChannelRollupConfigurationOption.class) {

      return Optional.of(ChannelRollupConfigurationOption.create(
        RollupOperatorConfigurationUtility.resolveOperator(
          rollupOperatorFieldResolver.apply("sohMonitorsToChannelRollupOperator"),
          new ArrayList<>(SohMonitorType.validTypes()),
          TerminalRollupOperatorResolver.SOH_MONITOR_TYPE_OPERATOR_RESOLVER
        )
      ));
    }

    throw new IllegalArgumentException("Unknown configuration option type");
  }

  private static List<String> findOperatorLeaves(
    RollupOperator operator,
    Function<RollupOperator, List<String>> operandFunction
  ) {

    if (operator.getRollupOperatorOperands().isEmpty()) {
      return operandFunction.apply(operator);
    } else {

      List<String> operands = new ArrayList<>();
      operator.getRollupOperatorOperands().forEach(innerOperator ->
        operands.addAll(
          findOperatorLeaves(innerOperator, operandFunction)
        )
      );

      return operands;
    }
  }
}
