package gms.core.performancemonitoring.uimaterializedview;

import com.google.common.base.Functions;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import gms.core.performancemonitoring.soh.control.configuration.ChannelSohDefinition;
import gms.core.performancemonitoring.soh.control.configuration.DurationSohMonitorStatusThresholdDefinition;
import gms.core.performancemonitoring.soh.control.configuration.SohMonitorStatusThresholdDefinition;
import gms.core.performancemonitoring.soh.control.configuration.StationSohDefinition;
import gms.core.performancemonitoring.ssam.control.config.StationSohMonitoringUiClientParameters;
import gms.shared.frameworks.osd.coi.signaldetection.Station;
import gms.shared.frameworks.osd.coi.signaldetection.StationGroup;
import gms.shared.frameworks.osd.coi.soh.CapabilitySohRollup;
import gms.shared.frameworks.osd.coi.soh.ChannelSoh;
import gms.shared.frameworks.osd.coi.soh.SohMonitorType;
import gms.shared.frameworks.osd.coi.soh.SohMonitorType.SohValueType;
import gms.shared.frameworks.osd.coi.soh.SohStatus;
import gms.shared.frameworks.osd.coi.soh.StationSoh;
import gms.shared.frameworks.osd.coi.soh.quieting.SohStatusChange;
import gms.shared.frameworks.osd.coi.soh.quieting.UnacknowledgedSohStatusChange;
import gms.shared.frameworks.osd.coi.systemmessages.SystemMessage;
import gms.shared.frameworks.osd.coi.systemmessages.util.StationCapabilityStatusChangedBuilder;
import gms.shared.frameworks.osd.coi.systemmessages.util.StationNeedsAttentionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Logic to take Station SOH, Unacknowledged List, Quieted List and Capability Rollup and returns a
 * list UiStationSohs to be used by the SOH UI Displays. -
 * UiStationSoh has acknowledged, quiet status information included in structure to simplify
 * displaying in UI.
 * Class is package private since should only be called by UiStationAndStationGroupGenerator class.
 */
public class UiStationGenerator {

  private static final Map<String, SohStatus> previousStationCapabilityRollups = new ConcurrentHashMap<>();
  private static final Set<String> needsAttentionStationNames = new HashSet<>();
  private static final Logger logger = LoggerFactory.getLogger(UiStationGenerator.class);

  private UiStationGenerator() {
  }

  /**
   * Converts StationSohs to UiStationSohs (the Ui compatible format). Package private method.
   *
   * @param stationSohs a collection of {@link StationSoh}s.
   * @param unacknowledgedStatusChanges a collection of unacknowledged status changes.
   * @param quietedSohStatusChanges a collection of quieted status changes.
   * @param latestCapabilitySohRollups a collection of {@link CapabilitySohRollup}s.
   * @param stationSohConfig the StationSohMonitoringUiClientParameters.
   * @param stationGroups a collection of station groups.
   * @param systemMessageFluxSink a systemMessageFluxSink of system messages.
   * @return uiStationSohList
   */
  public static List<UiStationSoh> buildUiStationSohList(
    Set<StationSoh> stationSohs,
    List<UnacknowledgedSohStatusChange> unacknowledgedStatusChanges,
    List<QuietedSohStatusChangeUpdate> quietedSohStatusChanges,
    Set<CapabilitySohRollup> latestCapabilitySohRollups,
    StationSohMonitoringUiClientParameters stationSohConfig,
    List<StationGroup> stationGroups,
    Sinks.Many<SystemMessage> systemMessageFluxSink) {

    // Build a StationSohDefinition map to find the StationSohDefinition
    Map<String, StationSohDefinition> definitionsByStationName = stationSohConfig
      .getStationSohControlConfiguration()
      .getStationSohDefinitions()
      .stream()
      .collect(Collectors.toMap(StationSohDefinition::getStationName, Functions.identity()));

    // Walk thru the Station SOH creating the equivalent UI Station SOH
    var uiStationSoh = stationSohs.stream()
      .map(stationSoh -> {

        // Find the StationSohDefinition
        var stationSohDefinition =
          definitionsByStationName.get(stationSoh.getStationName());

        // Log warning missing StationSohDefinition
        if (stationSohDefinition == null) {
          logger.warn(
            "Missing configuration entry Station SOH Definition for station {}",
            stationSoh.getStationName());
          return Optional.<UiStationSoh>empty();
        }

        // Build UiChannelSoh set
        Set<UiChannelSoh> channelSohs = convertChannelSohs(
          stationSoh.getStationName(),
          stationSoh.getChannelSohs(),
          unacknowledgedStatusChanges,
          quietedSohStatusChanges,
          stationSohDefinition.getChannelSohDefinitions()
        );

        // Return the UiStationSoh created
        return Optional.of(
          UiStationSoh.create(
            stationSoh,
            needsAcknowledgement(stationSoh.getStationName(), unacknowledgedStatusChanges),
            needsAttention(
              stationSoh.getStationName(),
              unacknowledgedStatusChanges,
              quietedSohStatusChanges, systemMessageFluxSink
            ),
            statusContributors(stationSoh, stationSohDefinition),
            getCapabilityRollupForStation(
              stationSoh.getStationName(),
              latestCapabilitySohRollups,
              stationGroups,
              systemMessageFluxSink),
            channelSohs
          )
        );

      })
      .flatMap(Optional::stream)
      .collect(Collectors.toList());

    // add quieted Soh Status Changes that exist for any unknown values
    quietedSohStatusChanges.forEach(quieted -> {
      var channelOptional = uiStationSoh.stream()
        .filter(station -> station.getStationName().equals(quieted.getStationName())).findFirst()
        .flatMap(station -> station.getChannelSohs().stream()
          .filter(channel -> channel.getChannelName().equals(quieted.getChannelName())).findFirst());

      // determine if quieted status already exists, if not found add an empty entry
      // so that the quieted information is sent to the UI
      channelOptional.ifPresent(
        channel ->
          channel.getAllSohMonitorValueAndStatuses().stream()
            .filter(s -> s.getMonitorType().equals(quieted.getSohMonitorType())).findFirst()
            .ifPresentOrElse(s -> {
              }, () ->
                channel.getAllSohMonitorValueAndStatuses().add(UiSohMonitorValueAndStatus.create(
                  0.0,
                  false,
                  SohStatus.MARGINAL,
                  quieted.getSohMonitorType(),
                  false,
                  -1,
                  -1,
                  quieted.getQuietUntil().toEpochMilli(),
                  quieted.getQuietDuration().toMillis(),
                  false))
            )

      );
    });

    return uiStationSoh;
  }

  /**
   * Create the UI Capability Rollup Status structure used by the UI
   *
   * @param stationName the station name.
   * @param latestCapabilitySohRollups a collection of current {@link CapabilitySohRollup}s.
   * @param stationGroups a collection of station groups.
   * @param systemMessageFluxSink a FluxSink of system messages.
   * @return List<UiStationSohCapabilityStatus>
   */
  private static List<UiStationSohCapabilityStatus> getCapabilityRollupForStation(
    String stationName,
    Set<CapabilitySohRollup> latestCapabilitySohRollups,
    List<StationGroup> stationGroups,
    Sinks.Many<SystemMessage> systemMessageFluxSink) {

    addStationCapabilityStatusChangedSystemMessage(stationName, latestCapabilitySohRollups,
      systemMessageFluxSink);

    Map<String, CapabilitySohRollup> rollupsByGroup = latestCapabilitySohRollups.stream()
      .collect(Collectors.toMap(CapabilitySohRollup::getForStationGroup, Functions.identity()));

    return stationGroups.stream()
      .filter(stationGroup -> stationGroup.getStations().stream().map(Station::getName)
        .collect(Collectors.toList()).contains(stationName))
      .map(stationGroup ->
        UiStationSohCapabilityStatus.create(stationGroup.getName(),
          stationName,
          rollupsByGroup.containsKey(stationGroup.getName()) ?
            UiSohStatus.from(rollupsByGroup.get(stationGroup.getName())
              .getRollupSohStatusByStation().get(stationName)) :
            UiSohStatus.NONE))
      .collect(Collectors.toList());

  }

  /**
   * Add the station capability status changed system message.
   *
   * @param stationName the station name.
   * @param latestCapabilitySohRollups the current capability rollups.
   * @param systemMessageFluxSink the Map of system messages.
   */
  private static void addStationCapabilityStatusChangedSystemMessage(
    String stationName,
    Set<CapabilitySohRollup> latestCapabilitySohRollups,
    Sinks.Many<SystemMessage> systemMessageFluxSink
  ) {

    latestCapabilitySohRollups.forEach(
      capabilitySohRollup ->
        Optional.ofNullable(
          //
          // Find the station in the current CapabilitySohRollup and its status
          //
          capabilitySohRollup.getRollupSohStatusByStation().get(stationName)
        ).ifPresent(
          sohStatus -> {

            //
            // previousStationCapabilityRollups maps the combination of station name and
            // station group name to a status.
            //
            var stationGroupStationKey =
              capabilitySohRollup.getForStationGroup() + "/" + stationName;
            var previousSohStatus = Optional.ofNullable(previousStationCapabilityRollups
              .get(stationGroupStationKey)).orElse(sohStatus);

            previousStationCapabilityRollups.put(
              stationGroupStationKey,
              sohStatus
            );

            if (sohStatus != previousSohStatus) {

              SystemMessage message = new StationCapabilityStatusChangedBuilder(
                stationName,
                capabilitySohRollup.getForStationGroup(),
                previousSohStatus,
                sohStatus
              ).build();

              systemMessageFluxSink.tryEmitNext(message);
            }
          }
        )
    );

  }

  /**
   * Determine if station needs to be acknowledgement (dirty dots on UI)
   *
   * @param stationName the station name.
   * @param unacknowledgedStatusChanges a collection of unacknowledged status changes.
   * @return boolean (needs acknowledgement for this station)
   */
  private static boolean needsAcknowledgement(
    String stationName,
    List<UnacknowledgedSohStatusChange> unacknowledgedStatusChanges) {

    return unacknowledgedStatusChanges
      .stream()
      .filter(unack -> unack.getStation().equals(stationName))
      .findFirst()
      .map(unack -> !unack.getSohStatusChanges().isEmpty())
      .orElse(false);
  }

  /**
   * Determine if station needs attention in UI
   *
   * @param stationName the station name.
   * @param quietedSohStatusChanges a collection of quieted status changes.
   * @param unacknowledgedStatusChanges a collection unacknowledged status changes.
   * @param systemMessageFluxSink a Map of system messages.
   * @return boolean (needs attention for this station)
   */
  private static boolean needsAttention(
    String stationName,
    List<UnacknowledgedSohStatusChange> unacknowledgedStatusChanges,
    List<QuietedSohStatusChangeUpdate> quietedSohStatusChanges,
    Sinks.Many<SystemMessage> systemMessageFluxSink
  ) {

    boolean attention = unacknowledgedStatusChanges.stream()
      .filter(unack -> unack.getStation().equals(stationName))
      .anyMatch(unack -> {
        Table<String, SohMonitorType, QuietedSohStatusChangeUpdate> quietedByChannel =
          quietedSohStatusChanges.stream()
            .filter(quieted -> quieted.getStationName().equals(stationName))
            .collect(Collector.of(HashBasedTable::create,
              (table, change) -> table.put(change.getChannelName(),
                change.getSohMonitorType(), change),
              (table1, table2) -> {
                table1.putAll(table2);
                return table1;
              },
              Functions.identity()));
        return unack.getSohStatusChanges().stream()
          .anyMatch(change -> !quietedByChannel.contains(change.getChangedChannel(),
            change.getSohMonitorType()));
      });

    // If the station needs attention test if it moved to needs attention bin and send a SystemMessage.
    if (attention) {
      if (!needsAttentionStationNames.contains(stationName)) {
        addStationNeedsAttentionSystemMessage(stationName, systemMessageFluxSink);
        needsAttentionStationNames.add(stationName);
      }
    } else {
      needsAttentionStationNames.remove(stationName);
    }

    return attention;
  }

  /**
   * Add the needs attention system message.
   *
   * @param stationName the station name.
   * @param systemMessageFluxSink the Map of system messages.
   */
  private static void addStationNeedsAttentionSystemMessage(String stationName,
    Sinks.Many<SystemMessage> systemMessageFluxSink) {

    SystemMessage message = new StationNeedsAttentionBuilder(stationName).build();

    systemMessageFluxSink.tryEmitNext(message);
  }

  /**
   * Looks through SohMonitorValueAndStatuses to find SOH Contributors
   *
   * @param stationSoh the {@link StationSoh}.
   * @return statusContributors
   */
  private static List<UiSohContributor> statusContributors(
    StationSoh stationSoh, StationSohDefinition stationSohDefinition) {

    /*
     * Walk through all SohMonitorValueAndStatuses to figure out the SOH
     * Contributors
     */

    return stationSoh.getSohMonitorValueAndStatuses().stream()
      .map(sohMonitorValue -> {
        SohMonitorType type = sohMonitorValue.getMonitorType();
        Optional<?> monitorValueOp = sohMonitorValue.getValue();

        //
        // Convert the value to a Double. For now the only types defined are Double and Duration
        // If a value is set and is not an instanceof a know type log a warning.
        //
        Double value = -1.0;

        if (monitorValueOp.isPresent()) {
          if (type.getSohValueType() == SohValueType.PERCENT) {
            value = UiMaterializedViewUtility.setDecimalPrecisionAsNumber(
              (Double) sohMonitorValue.getValue().get(), 2);
          } else if (type.getSohValueType() == SohValueType.DURATION) {
            var doubleValue =
              ((Duration) sohMonitorValue.getValue().get()).toMillis() / 1000.0;
            value = UiMaterializedViewUtility.setDecimalPrecisionAsNumber(doubleValue, 2);
          } else {
            logger.warn(
              "Failed to extract value from SohMonitorValueAndStatus entry, {} unknown value type",
              sohMonitorValue.getValue().get().getClass().getName()
            );
          }
        }

        // Look up in the StationSohDefinition if the monitor is part of the rollup
        boolean isContributing = stationSohDefinition.getSohMonitorTypesForRollup().contains(type);
        return UiSohContributor.from(
          value,
          sohMonitorValue.getValue().isPresent(),
          sohMonitorValue.getStatus(),
          isContributing,
          type
        );
      })
      .collect(Collectors.toList());
  }

  /**
   * Makes UiChannelSohs (Ui compatible) from old ChannelSohs
   *
   * @param stationName the station name.
   * @param channelSohs a collection of {@link ChannelSoh}s.
   * @param unacknowledgedStatusChanges a collection of unacknowledged status changes.
   * @param quietedSohStatusChanges a collection of quieted status changes.
   * @return Set<UiChannelSoh>
   */
  private static Set<UiChannelSoh> convertChannelSohs(
    String stationName,
    Set<ChannelSoh> channelSohs,
    List<UnacknowledgedSohStatusChange> unacknowledgedStatusChanges,
    List<QuietedSohStatusChangeUpdate> quietedSohStatusChanges,
    Set<ChannelSohDefinition> setChanSohDef) {

    List<UnacknowledgedSohStatusChange> stationUnacknowledgedStatusChanges =
      unacknowledgedStatusChanges.stream()
        .filter(unack -> unack.getStation().equals(stationName))
        .collect(Collectors.toList());

    Map<String, ChannelSohDefinition> channelSohDefinitionsByName = setChanSohDef.stream()
      .collect(Collectors.toMap(ChannelSohDefinition::getChannelName, Functions.identity()));

    return channelSohs.stream()
      .filter(channelSoh -> channelSohDefinitionsByName.containsKey(channelSoh.getChannelName()))
      .map(channelSoh -> createUiChannelSoh(
        channelSoh,
        stationUnacknowledgedStatusChanges,
        quietedSohStatusChanges,
        channelSohDefinitionsByName.get(channelSoh.getChannelName()),
        stationName))
      .collect(Collectors.toSet());
  }


  /**
   * Creates a new Channel SOH Object
   *
   * @param channelSoh
   * @param unacknowledgedStatusChanges
   * @param quietedSohStatusChanges
   * @return AutoValue_UiChannelSoh
   */
  private static UiChannelSoh createUiChannelSoh(
    ChannelSoh channelSoh,
    List<UnacknowledgedSohStatusChange> unacknowledgedStatusChanges,
    List<QuietedSohStatusChangeUpdate> quietedSohStatusChanges,
    ChannelSohDefinition chanSohDef,
    String stationName) {

    // Walk thru the SohMonitor Value and Statuses converting to the UiSohMonitorValueAndStatus
    Set<UiSohMonitorValueAndStatus> uiSmvs = channelSoh.getAllSohMonitorValueAndStatuses().stream()
      .filter(smvs -> chanSohDef.getSohMonitorStatusThresholdDefinitionsBySohMonitorType()
        .containsKey(smvs.getMonitorType()))
      .map(smvs -> {
        SohMonitorStatusThresholdDefinition<?> definition = chanSohDef
          .getSohMonitorStatusThresholdDefinitionsBySohMonitorType().get(smvs.getMonitorType());
        double marginalThreshold;
        double badThreshold;
        if (definition instanceof DurationSohMonitorStatusThresholdDefinition) {
          marginalThreshold = UiMaterializedViewUtility.getDurationInSeconds(
            (Duration) definition.getGoodThreshold());
          badThreshold = UiMaterializedViewUtility.getDurationInSeconds(
            (Duration) definition.getMarginalThreshold());
        } else {
          marginalThreshold = (Double) definition.getGoodThreshold();
          badThreshold = (Double) definition.getMarginalThreshold();
        }

        return UiSohMonitorValueAndStatus.from(
          smvs,
          findQuietEntry(
            channelSoh.getChannelName(),
            smvs.getMonitorType(),
            quietedSohStatusChanges
          ),
          hasUnacknowledgedChanges(
            channelSoh.getChannelName(),
            smvs.getMonitorType(),
            unacknowledgedStatusChanges
          ),
          marginalThreshold,
          badThreshold,
          StationSohContributingUtility.getInstance().isChannelMonitorContributing(
            stationName,
            channelSoh.getChannelName(),
            smvs.getMonitorType()
          )
        );
      })
      .collect(Collectors.toSet());

    return UiChannelSoh.from(
      channelSoh.getChannelName(),
      channelSoh.getSohStatusRollup(),
      uiSmvs);
  }


  /**
   * Determine if channel needs to be acknowledgement (dirty dots on UI)
   *
   * @param channelName
   * @param type monitor type
   * @param unacknowledgedStatusChanges
   * @return boolean (needs acknowledgement for this channel)
   */
  private static boolean hasUnacknowledgedChanges(
    String channelName,
    SohMonitorType type,
    List<UnacknowledgedSohStatusChange> unacknowledgedStatusChanges) {

    // Look through the unacknowledged list to see if any apply to station
    for (UnacknowledgedSohStatusChange unack : unacknowledgedStatusChanges) {
      for (SohStatusChange sohStatusChange : unack.getSohStatusChanges()) {
        if (sohStatusChange != null &&
          sohStatusChange.getChangedChannel() != null &&
          sohStatusChange.getChangedChannel().equals(channelName) &&
          sohStatusChange.getSohMonitorType().equals(type)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Returns quiet entry for channel or null
   *
   * @param channelName
   * @param type monitor type
   * @param quietedSohStatusChanges
   * @return QuietedSohStatusChange or null
   */
  private static Optional<QuietedSohStatusChangeUpdate> findQuietEntry(
    String channelName,
    SohMonitorType type,
    List<QuietedSohStatusChangeUpdate> quietedSohStatusChanges) {

    // Look through the quieted list to see if any apply to station
    for (QuietedSohStatusChangeUpdate quietEntry : quietedSohStatusChanges) {
      if (quietEntry.getChannelName().equals(channelName) &&
        quietEntry.getSohMonitorType().equals(type)) {
        return Optional.of(quietEntry);
      }
    }
    return Optional.empty();
  }

  /**
   * Clear all of the previous values. Used for testing for now, so making package-private.
   */
  static void clearPrevious() {
    previousStationCapabilityRollups.clear();
    needsAttentionStationNames.clear();
  }
}
