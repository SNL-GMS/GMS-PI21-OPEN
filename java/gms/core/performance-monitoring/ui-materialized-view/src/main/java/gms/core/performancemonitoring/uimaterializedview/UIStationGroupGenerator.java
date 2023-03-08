package gms.core.performancemonitoring.uimaterializedview;

import com.google.common.base.Functions;
import com.google.common.collect.Streams;
import gms.shared.frameworks.osd.coi.soh.CapabilitySohRollup;
import gms.shared.frameworks.osd.coi.systemmessages.SystemMessage;
import gms.shared.frameworks.osd.coi.systemmessages.util.StationGroupCapabilityStatusChangedBuilder;
import org.apache.commons.lang3.tuple.Pair;
import reactor.core.publisher.Sinks;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Logic to take Station SOH, Unacknowledged List, Quieted List and Capability Rollup and returns a
 * list of StationGroups to be used by the SOH UI Displays. -
 * StationGroups are a list that encapsulates the order the groups are displayed
 * in the UI and the capability rollup status at the station group level.
 * Class is package private since should only be called by UiStationAndStationGroupGenerator class.
 */
class UIStationGroupGenerator {

  private static final Map<String, CapabilitySohRollup> previousStationGroupsCapabilityRollups =
    new ConcurrentHashMap<>();

  private UIStationGroupGenerator() {
  }

  /**
   * Builds the station group status list. Package private method.
   *
   * @param latestCapabilitySohRollups a collection of current {@link CapabilitySohRollup}s.
   * @param stationGroupsNames a collection of station group names.
   * @param systemMessageFluxSink a Map of system messages.
   * @return List<UiStationGroupSoh>
   */
  static List<UiStationGroupSoh> buildSohStationGroups(
    Set<CapabilitySohRollup> latestCapabilitySohRollups,
    List<String> stationGroupsNames, Sinks.Many<SystemMessage> systemMessageFluxSink) {

    Map<String, CapabilitySohRollup> rollupsByStation = latestCapabilitySohRollups.stream()
      .collect(Collectors.toMap(CapabilitySohRollup::getForStationGroup, Functions.identity()));

    return Streams.zip(IntStream.rangeClosed(1, stationGroupsNames.size()).boxed(),
        stationGroupsNames.stream(),
        Pair::of)
      .sorted(Map.Entry.comparingByKey())
      .map(pair -> {
        if (rollupsByStation.containsKey(pair.getValue())) {
          addStationGroupCapabilityStatusChangedSystemMessage(
            rollupsByStation.get(pair.getValue()),
            systemMessageFluxSink);
        }

        return UiStationGroupSoh.create(pair.getValue(),
          pair.getValue(),
          Instant.now().toEpochMilli(),
          rollupsByStation.containsKey(pair.getValue()) ?
            UiSohStatus.from(rollupsByStation.get(pair.getValue()).getGroupRollupSohStatus())
            : UiSohStatus.NONE, // return NONE if there is no valid rollup for station
          pair.getKey());
      })
      .collect(Collectors.toList());
  }

  /**
   * Add the station group capability status changed system message.
   *
   * @param capabilitySohRollup the current capability SOH Rollup for the station group.
   * @param systemMessageFluxSink the Map of system messages.
   */
  private static void addStationGroupCapabilityStatusChangedSystemMessage(
    CapabilitySohRollup capabilitySohRollup,
    Sinks.Many<SystemMessage> systemMessageFluxSink) {

    Optional.ofNullable(
        previousStationGroupsCapabilityRollups.get(capabilitySohRollup.getForStationGroup()))
      .ifPresent(
        previousCapabilityRollup -> {
          if (!previousCapabilityRollup.getGroupRollupSohStatus()
            .equals(capabilitySohRollup.getGroupRollupSohStatus())) {

            SystemMessage message = new StationGroupCapabilityStatusChangedBuilder(
              capabilitySohRollup.getForStationGroup(),
              previousCapabilityRollup.getGroupRollupSohStatus(),
              capabilitySohRollup.getGroupRollupSohStatus()).build();

            systemMessageFluxSink.tryEmitNext(message);
          }
        }
      );

    previousStationGroupsCapabilityRollups.put(
      capabilitySohRollup.getForStationGroup(),
      capabilitySohRollup
    );
  }

  /**
   * Clear all of the previous values. Used for testing for now, so making package-private.
   */
  static void clearPrevious() {
    previousStationGroupsCapabilityRollups.clear();
  }
}
