package gms.core.performancemonitoring.soh.control;

import gms.core.performancemonitoring.soh.control.configuration.ChannelSohDefinition;
import gms.core.performancemonitoring.soh.control.configuration.ChannelsByMonitorType;
import gms.core.performancemonitoring.soh.control.configuration.ChannelsByMonitorTypeConfigurationOption;
import gms.core.performancemonitoring.soh.control.configuration.ChannelsForMonitorTypeConfigurationOption;
import gms.core.performancemonitoring.soh.control.configuration.ChannelsForMonitorTypeConfigurationOption.ChannelsMode;
import gms.core.performancemonitoring.soh.control.configuration.DurationSohMonitorStatusThresholdDefinition;
import gms.core.performancemonitoring.soh.control.configuration.PercentSohMonitorStatusThresholdDefinition;
import gms.core.performancemonitoring.soh.control.configuration.SohMonitorStatusThresholdDefinition;
import gms.core.performancemonitoring.soh.control.configuration.SohMonitorTypesForRollupConfigurationOption;
import gms.core.performancemonitoring.soh.control.configuration.StationSohDefinition;
import gms.core.performancemonitoring.soh.control.configuration.TimeWindowDefinition;
import gms.shared.frameworks.configuration.ConfigurationRepository;
import gms.shared.frameworks.configuration.Selector;
import gms.shared.frameworks.configuration.repository.client.ConfigurationConsumerUtility;
import gms.shared.frameworks.osd.coi.FieldMapUtilities;
import gms.shared.frameworks.osd.coi.channel.Channel;
import gms.shared.frameworks.osd.coi.signaldetection.Station;
import gms.shared.frameworks.osd.coi.soh.SohMonitorType;
import gms.shared.frameworks.osd.coi.soh.SohMonitorType.SohValueType;
import org.apache.commons.lang3.Validate;

import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;


public class PerformanceMonitoringConfigurationUtility {

  private static final String STATION_SOH_PREFIX = "soh-control";

  private static final String MONITOR_TYPE_SELECTOR_KEY = "MonitorType";

  private static final String STATION_NAME_SELECTOR_KEY = "StationName";

  private static final String CHANNEL_NAME_SELECTOR_KEY = "ChannelName";

  private static final String NULL_CONFIGURATION_CONSUMER_UTILITY = "Null configurationConsumerUtility";

  private static final String NULL_STATION_NAME = "Null stationName";

  /* Hiding default public constructor */
  private PerformanceMonitoringConfigurationUtility() {
  }

  /**
   * Resolves {@link StationSohDefinition}s for the provided {@link Set} of {@link Station}s against
   * the provided {@link ConfigurationConsumerUtility}.
   *
   * @param configurationConsumerUtility Used to resolve processing configuration.  Not null.
   * @param stations Channels to resolve StationSohDefinitions for.  Not null.
   * @return Set of StationSohDefinitions for the provided Set of Stations loaded from processing
   * configuration.
   */
  public static Set<StationSohDefinition> resolveStationSohDefinitions(
    ConfigurationConsumerUtility configurationConsumerUtility,
    Collection<Station> stations) {

    Objects.requireNonNull(configurationConsumerUtility, NULL_CONFIGURATION_CONSUMER_UTILITY);

    return stations.stream().map(station -> {

      var stationName = station.getName();
      Objects.requireNonNull(stationName, NULL_STATION_NAME);

      var allChannelNameSet = station.getChannels()
        .stream()
        .map(Channel::getName)
        .collect(Collectors.toSet());

      var channelSohDefinitionSet = resolveChannelSohDefinitions(
        configurationConsumerUtility,
        station.getChannels()
      );

      var monitorTypeSetInStationLevelRollup =
        resolveSohMonitorTypesForRollupConfigurationOptionForStationRollup(
          configurationConsumerUtility,
          stationName
        ).getSohMonitorTypesForRollup();

      var channelsBySohMonitorTypeMap =
        resolveChannelsByMonitorTypeForStation(
          configurationConsumerUtility,
          stationName,
          allChannelNameSet
        ).getChannelsByMonitorType();

      var timeWindowDefinitionMap = resolveTimeWindowDefinitionsForStation(
        configurationConsumerUtility,
        stationName
      );

      return StationSohDefinition.create(
        stationName,
        monitorTypeSetInStationLevelRollup,
        channelsBySohMonitorTypeMap,
        channelSohDefinitionSet,
        timeWindowDefinitionMap
      );
    }).collect(Collectors.toSet());
  }

  /**
   * Converts the provided {@link ChannelsForMonitorTypeConfigurationOption} to a {@link Set} of
   * {@link String}s representing Channel Names.  If the ChannelsForMonitorTypeConfigurationOption's
   * {@link ChannelsForMonitorTypeConfigurationOption#getChannelsMode()} is USE_LISTED, only those
   * channels contained in the configuration option will be returned.  Otherwise, the provided set
   * of channel names will be returned.
   *
   * @param channelsForMonitorTypeConfigurationOption Configuration option to be converted to a Set
   * of Channel names.
   * @param allChannelNames Set of valid Channel names to return if the configuration option's
   * ChannelMode is USE_ALL.
   * @return Set of Channel names.
   * @throws IllegalArgumentException If the provided configuration option's ChannelMode is not
   * USE_ALL or USE_LISTED.
   */
  private static Set<String> convertChannelsForMonitorTypeToChannelNames(
    ChannelsForMonitorTypeConfigurationOption channelsForMonitorTypeConfigurationOption,
    Set<String> allChannelNames) {

    if (channelsForMonitorTypeConfigurationOption.getChannelsMode()
      .equals(ChannelsMode.USE_LISTED)) {

      return channelsForMonitorTypeConfigurationOption.getChannels();
    } else if (channelsForMonitorTypeConfigurationOption.getChannelsMode()
      .equals(ChannelsMode.USE_ALL)) {

      return allChannelNames;
    } else {

      throw new IllegalArgumentException(String
        .format("%s must be one of %s or %s, but was %s", ChannelsMode.class.getSimpleName(),
          ChannelsMode.USE_LISTED, ChannelsMode.USE_ALL,
          channelsForMonitorTypeConfigurationOption.getChannelsMode()));
    }
  }

  /**
   * Converts a {@link ChannelsByMonitorTypeConfigurationOption} into a Map from {@link
   * SohMonitorType}s to {@link Set}s of {@link Channel} names.  Adds the converted key/value pairs
   * to the provided map from SohMonitorType to Channel names.
   *
   * @param channelsByMonitorTypeToPopulate Configuration values contained in
   * channelsByMonitorTypeConfigurationOption are added to this map based on the configuration
   * option's ChannelsMode value.
   * @param channelsByMonitorTypeConfigurationOption Raw configuration to be converted into a Map
   * from SohMonitorType to Set of Channel names.
   */
  private static void populateChannelsByMonitorTypeFromConfigurationOption(
    Map<SohMonitorType, Set<String>> channelsByMonitorTypeToPopulate,
    ChannelsByMonitorTypeConfigurationOption channelsByMonitorTypeConfigurationOption,
    Set<String> allChannels) {

    channelsByMonitorTypeConfigurationOption.getChannelsByMonitorType()
      .forEach((monitorType, channelsForMonitorTypeConfigurationOption) ->
        channelsByMonitorTypeToPopulate.put(
          SohMonitorType.valueOf(monitorType.toString()),
          convertChannelsForMonitorTypeToChannelNames(
            channelsForMonitorTypeConfigurationOption, allChannels)
        )
      );
  }

  /**
   * Resolves channelsByMonitorType configuration values into a {@link Map} from {@link
   * SohMonitorType} to {@link Set} of {@link Channel} names.  Uses the provided {@link Selector}s
   * to resolve the correct configuration.
   *
   * @param configurationConsumerUtility {@link ConfigurationConsumerUtility} used to resolve
   * configuration against a {@link ConfigurationRepository}. Not null.
   * @param selectors {@link Selector}s to apply when resolving a MonitorTypesInRollupConfigurationOption.
   * @param channelNames Contains set of all Channel names that will be used for any SohMonitorType
   * who's value's ChannelsMode is USE_ALL
   * @return Map from SohMonitorType to set of Channel names.
   */
  private static Map<SohMonitorType, Set<String>> resolveChannelsByMonitorTypeConfiguration(
    ConfigurationConsumerUtility configurationConsumerUtility,
    List<Selector> selectors,
    Set<String> channelNames) {

    Map<SohMonitorType, Set<String>> channelsByMonitorType = new EnumMap<>(SohMonitorType.class);

    var channelsByMonitorTypeConfigurationName = STATION_SOH_PREFIX + ".channels-by-monitor-type";

    var channelsByMonitorTypeConfigFieldMap = configurationConsumerUtility
      .resolve(
        channelsByMonitorTypeConfigurationName,
        selectors
      );

    Map<SohMonitorType, ChannelsForMonitorTypeConfigurationOption> channelsByMonitorTypeConfigMap = channelsByMonitorTypeConfigFieldMap
      .entrySet().stream()
      .map(entry -> {
          var sohMonitortype = SohMonitorType.valueOf(entry.getKey());

          var channelsForMonitorTypeFieldMap = FieldMapUtilities.toFieldMap(entry.getValue());

          var channelsForMonitorTypeConfigurationOption = FieldMapUtilities.fromFieldMap(
            channelsForMonitorTypeFieldMap,
            ChannelsForMonitorTypeConfigurationOption.class
          );

          return Map.entry(sohMonitortype, channelsForMonitorTypeConfigurationOption);
        }
      ).collect(Collectors.toMap(Entry::getKey, Entry::getValue));

    Objects.requireNonNull(channelsByMonitorTypeConfigMap,
      "Null channelsByMonitorTypeConfigurationMap");

    populateChannelsByMonitorTypeFromConfigurationOption(
      channelsByMonitorType,
      ChannelsByMonitorTypeConfigurationOption.create(channelsByMonitorTypeConfigMap),
      channelNames);

    return channelsByMonitorType;
  }

  /**
   * Resolves a {@link ChannelsByMonitorType} using the provided {@link Selector}s.
   *
   * @param configurationConsumerUtility {@link ConfigurationConsumerUtility} used to resolve
   * configuration against a {@link ConfigurationRepository}. Not null.
   * @param stationName Name of the Station to use a selector when resolving the
   * ChannelsByMonitorTypeConfigurationOption.
   * @param channelNames Set of all Channel names to be used as values for those SohMonitorTypes
   * whose {@link ChannelsForMonitorTypeConfigurationOption}'s ChannelsMode is USE_ALL.
   * @return ChannelsByMonitorType matching the provided selectors.
   */
  private static ChannelsByMonitorType resolveChannelsByMonitorTypeForStation(
    ConfigurationConsumerUtility configurationConsumerUtility,
    String stationName,
    Set<String> channelNames) {

    return ChannelsByMonitorType.create(
      resolveChannelsByMonitorTypeConfiguration(
        configurationConsumerUtility,
        List.of(
          Selector.from(STATION_NAME_SELECTOR_KEY, stationName)
        ),
        channelNames
      )
    );
  }

  /**
   * Resolves a {@link SohMonitorTypesForRollupConfigurationOption} using the provided {@link
   * Selector}s.  If no monitor types in rollup configuration exists, or if no
   * MonitorTypesInRollupConfigurationOptions exist, returns a MonitorTypesInRollupConfigurationOption
   * containing all {@link SohMonitorType}s.
   *
   * @param configurationConsumerUtility {@link ConfigurationConsumerUtility} used to resolve
   * configuration against a {@link ConfigurationRepository}. Not null.
   * @param selectors Selectors to apply when resolving a MonitorTypesInRollupConfigurationOption.
   * @return MonitorTypesInRollupConfigurationOption matching the provided Selectors.  If no monitor
   * types in rollup configuration exists, or if no MonitorTypesInRollupConfigurationOptions exist,
   * returns a MonitorTypesInRollupConfigurationOption containing all SohMonitorTypes. Not null.
   */
  private static SohMonitorTypesForRollupConfigurationOption
  resolveMonitorTypesInRollupConfigurationOptionForSelectors(
    ConfigurationConsumerUtility configurationConsumerUtility,
    List<Selector> selectors,
    String monitorTypesInRollupConfigurationName) {

    SohMonitorTypesForRollupConfigurationOption sohMonitorTypesForRollupConfigurationOption;

    try {

      sohMonitorTypesForRollupConfigurationOption = configurationConsumerUtility.resolve(
        monitorTypesInRollupConfigurationName,
        selectors,
        SohMonitorTypesForRollupConfigurationOption.class
      );
    } catch (IllegalStateException | IllegalArgumentException e) {

      // IllegalStateException is caught if a configuration option for the specified Selectors
      // does not exist.
      // IllegalArgumentException is caught if no configuration with the specified name exists.
      // This is expected and is not an error.
      sohMonitorTypesForRollupConfigurationOption = SohMonitorTypesForRollupConfigurationOption
        .create(SohMonitorType.validTypes());
    }

    return sohMonitorTypesForRollupConfigurationOption;
  }

  /**
   * Resolves a {@link SohMonitorTypesForRollupConfigurationOption} by providing the necessary
   * selectors. MonitorTypesInRollupConfigurationOption can be overriden by {@link
   * gms.shared.frameworks.osd.coi.signaldetection.Station} or {@link
   * gms.shared.frameworks.osd.coi.channel.Channel}.  This implementation of
   * resolveMonitorTypesInRollupConfigurationOption(...) is only for retrieving configuration
   * overridden by Channel.
   *
   * @param configurationConsumerUtility {@link ConfigurationConsumerUtility} used to resolve
   * configuration against a {@link ConfigurationRepository}. Not null.
   * @param channelName Channel name selector allowing MonitorTypesInRollupConfigurationOption to be
   * overridden by Channel name.  Not null.
   * @return MonitorTypesInRollupConfigurationOption matching the provided selector.  Not null.
   */
  private static SohMonitorTypesForRollupConfigurationOption
  resolveSohMonitorTypesForRollupConfigurationOptionChannelRollup(
    ConfigurationConsumerUtility configurationConsumerUtility,
    String stationName,
    String channelName) {

    return resolveMonitorTypesInRollupConfigurationOptionForSelectors(
      configurationConsumerUtility,
      List.of(
        Selector.from(CHANNEL_NAME_SELECTOR_KEY, channelName),
        Selector.from(STATION_NAME_SELECTOR_KEY, stationName)
      ),
      STATION_SOH_PREFIX + ".soh-monitor-types-for-rollup-channel"
    );
  }


  /**
   * Resolves a {@link SohMonitorStatusThresholdDefinition} by providing the necessary selectors.
   * The {@link SohMonitorStatusThresholdDefinition} can be overridden by {@link SohMonitorType},
   * {@link gms.shared.frameworks.osd.coi.signaldetection.Station} name, and {@link
   * gms.shared.frameworks.osd.coi.channel.Channel} name.
   *
   * @param configurationConsumerUtility {@link ConfigurationConsumerUtility} used to resolve
   * configuration against a {@link ConfigurationRepository}. Not null.
   * @param monitorType SohMonitorType selector allowing SohMonitorValueAndStatusDefinition to be
   * overridden by SohMonitorType.  Not null.
   * @param stationName Station name selector allowing SohMonitorValueAndStatusDefinition to be
   * overridden by Station name.  Not null.
   * @param channelName Channel name selector allowing SohMonitorValueAndStatusDefinition to be
   * overridden by Channel name.  Not null.
   * @return SohMonitorStatusThresholdDefinition matching the provided selectors.  Either a {@link
   * DurationSohMonitorStatusThresholdDefinition} or {@link PercentSohMonitorStatusThresholdDefinition}.
   * Not null.
   */
  private static SohMonitorStatusThresholdDefinition<?> resolveSohMonitorStatusThresholdDefinition(
    ConfigurationConsumerUtility configurationConsumerUtility,
    SohMonitorType monitorType,
    String stationName,
    String channelName
  ) {

    var sohMonitorThresholdConfigurationName =
      STATION_SOH_PREFIX + ".soh-monitor-thresholds";

    //
    // Force this List be of the non-generic Selector, because
    // that is what resolve takes.
    //
    var selectors = List.<Selector>of(
      Selector.from(MONITOR_TYPE_SELECTOR_KEY, monitorType.toString()),
      Selector.from(STATION_NAME_SELECTOR_KEY, stationName),
      Selector.from(CHANNEL_NAME_SELECTOR_KEY, channelName)
    );

    try {
      if (monitorType.getSohValueType() == SohValueType.DURATION) {
        return configurationConsumerUtility.resolve(
          sohMonitorThresholdConfigurationName,
          selectors,
          DurationSohMonitorStatusThresholdDefinition.class
        );
      } else if (monitorType.getSohValueType() == SohValueType.PERCENT) {
        return configurationConsumerUtility.resolve(
          sohMonitorThresholdConfigurationName,
          selectors,
          PercentSohMonitorStatusThresholdDefinition.class
        );
      } else {
        throw new IllegalArgumentException(
          "Unrecognized SohMonitorType"
        );
      }
    } catch (IllegalStateException e) {
      throw new IllegalStateException(
        "Configuration error for monitor type " + monitorType
          + ", station " + stationName
          + ", channel " + channelName,
        e
      );
    }

  }


  /**
   * Resolves a {@link SohMonitorTypesForRollupConfigurationOption} by providing the necessary
   * selectors. MonitorTypesInRollupConfigurationOption can be overriden by {@link
   * gms.shared.frameworks.osd.coi.signaldetection.Station} or Station and {@link
   * gms.shared.frameworks.osd.coi.channel.Channel}.  This implementation of
   * resolveMonitorTypesInRollupConfigurationOption(...) is only for retrieving configuration
   * overridden by Station.
   *
   * @param configurationConsumerUtility {@link ConfigurationConsumerUtility} used to resolve
   * configuration against a {@link ConfigurationRepository}. Not null.
   * @param stationName Station name selector allowing MonitorTypesInRollupConfigurationOption to be
   * overridden by Station name.  Not null.
   * @return MonitorTypesInRollupConfigurationOption matching the provided selector.  Not null.
   */
  private static SohMonitorTypesForRollupConfigurationOption
  resolveSohMonitorTypesForRollupConfigurationOptionForStationRollup(
    ConfigurationConsumerUtility configurationConsumerUtility,
    String stationName) {

    return resolveMonitorTypesInRollupConfigurationOptionForSelectors(
      configurationConsumerUtility,
      List.of(Selector.from(STATION_NAME_SELECTOR_KEY, stationName)),
      STATION_SOH_PREFIX + ".soh-monitor-types-for-rollup-station"
    );
  }

  /**
   * Resolves {@link ChannelSohDefinition}s for the provided {@link Set} of {@link Channel}s against
   * the provided {@link ConfigurationConsumerUtility}.
   *
   * @param configurationConsumerUtility Used to resolve processing configuration.  Not null.
   * @param channels Channels to resolve ChannelSohDefinitions for.  Not null.
   * @return Set of ChannelSohDefinitions for the provided Set of Channels loaded from processing
   * configuration.
   */
  private static Set<ChannelSohDefinition> resolveChannelSohDefinitions(
    ConfigurationConsumerUtility configurationConsumerUtility,
    Collection<Channel> channels) {

    return channels.stream().map(channel -> {

      var stationName = channel.getStation();
      Validate.isTrue(!stationName.isEmpty(), "Empty stationName");

      var channelName = channel.getName();

      var definitionsByMonitorType =
        new EnumMap<SohMonitorType, SohMonitorStatusThresholdDefinition<?>>(SohMonitorType.class);

      SohMonitorType.validTypes().forEach(
        sohMonitorType ->
          definitionsByMonitorType.put(
            sohMonitorType,
            resolveSohMonitorStatusThresholdDefinition(
              configurationConsumerUtility,
              sohMonitorType,
              stationName,
              channelName
            )
          )
      );

      var sohMonitorTypesForChannelLevelRollup =
        resolveSohMonitorTypesForRollupConfigurationOptionChannelRollup(
          configurationConsumerUtility,
          stationName,
          channelName
        ).getSohMonitorTypesForRollup();

      return ChannelSohDefinition.create(
        channelName,
        sohMonitorTypesForChannelLevelRollup,
        definitionsByMonitorType,
        channel.getNominalSampleRateHz()
      );
    }).collect(Collectors.toSet());
  }

  /**
   * Resolve a map of SohMonitorType to TimeWindowDefinition, for a particular station.
   *
   * @param configurationConsumerUtility configuration consumer utility to utilize
   * @param stationName station name to resolve time windows for
   * @return Map of SohMonitorType to TimeWindowDefinition
   */
  private static Map<SohMonitorType, TimeWindowDefinition> resolveTimeWindowDefinitionsForStation(
    ConfigurationConsumerUtility configurationConsumerUtility,
    String stationName
  ) {

    return SohMonitorType.validTypes().stream().map(
      sohMonitorType -> Map.entry(
        sohMonitorType,
        configurationConsumerUtility.resolve(
          STATION_SOH_PREFIX + ".soh-monitor-timewindows",
          List.of(
            Selector.from(STATION_NAME_SELECTOR_KEY, stationName),
            Selector.from(MONITOR_TYPE_SELECTOR_KEY, sohMonitorType.toString())
          ),
          TimeWindowDefinition.class
        )
      )
    ).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
  }

}
