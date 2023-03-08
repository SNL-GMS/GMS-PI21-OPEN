package gms.core.performancemonitoring.uimaterializedview;

import com.google.common.base.Preconditions;
import gms.core.performancemonitoring.ssam.control.config.StationSohMonitoringUiClientParameters;
import gms.shared.frameworks.osd.coi.signaldetection.StationGroup;
import gms.shared.frameworks.osd.coi.soh.CapabilitySohRollup;
import gms.shared.frameworks.osd.coi.soh.StationSoh;
import gms.shared.frameworks.osd.coi.systemmessages.SystemMessage;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Sinks;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Logic to take Station SOH, Unacknowledged List, Quieted List and Capability Rollup and returns a
 * list of StationGroups and a list of UiStationSohs to be used by the SOH UI Displays. -
 * UiStationSoh is created in the UiStationGenerator. - StationGroups are created in the
 * UiStationGroupGenerator
 */
public class UiStationAndStationGroupGenerator {

  private static final Logger logger = LoggerFactory
    .getLogger(UiStationAndStationGroupGenerator.class);

  private UiStationAndStationGroupGenerator() {
  }

  static final int KAFKA_MSG_SIZE_LIMIT = 1000000; // 1MB msg size limit

  /**
   * Generates the UiStationAndStationGroups
   *
   * @param stationSohs a collection of {@link StationSoh}s.
   * @param uiStationAndStationGroupsChanges - Current unacknowledged and quieted changes wrapper
   * @param latestCapabilitySohRollups a collection of {@link CapabilitySohRollup}s.
   * @param stationSohConfig - The soh configuration parameters used for thresholds, station groups,
   * etc.
   * @param stationGroups a collection of station groups.
   * @param isUpdate Is this message an update from a Ack or Quiet notification.
   * @return uiStationAndStationGroupsList the collection sent to the UI.
   */
  public static synchronized List<UiStationAndStationGroups> generateUiStationAndStationGroups(
    Set<StationSoh> stationSohs,
    UIStationAndStationGroupsChanges uiStationAndStationGroupsChanges,
    Set<CapabilitySohRollup> latestCapabilitySohRollups,
    StationSohMonitoringUiClientParameters stationSohConfig,
    List<StationGroup> stationGroups,
    boolean isUpdate,
    Sinks.Many<SystemMessage> systemMessageFluxSink) {

    Objects.requireNonNull(stationSohs);
    Preconditions.checkState(!stationSohs.isEmpty());
    Objects.requireNonNull(uiStationAndStationGroupsChanges.getUnacknowledgedStatusChanges());
    Objects.requireNonNull(uiStationAndStationGroupsChanges.getQuietedSohStatusChanges());
    Objects.requireNonNull(latestCapabilitySohRollups);
    Objects.requireNonNull(stationSohConfig);
    Objects.requireNonNull(systemMessageFluxSink);

    // Build the UiStationSoh list
    List<UiStationSoh> uiStationSohs = UiStationGenerator.buildUiStationSohList(
      stationSohs,
      uiStationAndStationGroupsChanges.getUnacknowledgedStatusChanges(),
      uiStationAndStationGroupsChanges.getQuietedSohStatusChanges(),
      latestCapabilitySohRollups,
      stationSohConfig,
      stationGroups,
      systemMessageFluxSink
    );

    List<String> stationGroupNames = stationSohConfig.getStationSohControlConfiguration()
      .getDisplayedStationGroups();

    UiStationAndStationGroups uiStationGroups = UiStationAndStationGroupGenerator
      .getUiStationGroupMessage(
        uiStationSohs,
        latestCapabilitySohRollups,
        stationGroupNames,
        isUpdate,
        systemMessageFluxSink
      );

    // Chunk it up until figure out how to send messages larger than 1MB
    return makeGroupsMessage(uiStationGroups);
  }

  /**
   * Create and return the UiStationAndStationGroups after building the StationGroup status list
   *
   * @param uiStationSohList a collection of {@link UiStationSoh}s.
   * @param capabilitySohRollups a collection of {@link CapabilitySohRollup}s.
   * @param stationGroupNames a collection of station group names.
   * @param systemMessageFluxSink a Map of system messages.
   * @return prioritized UiStationAndStationGroups
   */
  public static UiStationAndStationGroups getUiStationGroupMessage(
    List<UiStationSoh> uiStationSohList,
    Set<CapabilitySohRollup> capabilitySohRollups,
    List<String> stationGroupNames,
    boolean isUpdate,
    Sinks.Many<SystemMessage> systemMessageFluxSink) {

    // Get the StationGroup with Priority
    List<UiStationGroupSoh> stationGroupsWithPriority =
      UIStationGroupGenerator.buildSohStationGroups(capabilitySohRollups, stationGroupNames, systemMessageFluxSink);

    return UiStationAndStationGroups.create(stationGroupsWithPriority, uiStationSohList, isUpdate);
  }

  /**
   * Clear all of the previous values. Used for testing for now, so making package-private.
   */
  static void clearPrevious() {
    UIStationGroupGenerator.clearPrevious();
    UiStationGenerator.clearPrevious();
  }

  /**
   * Chops up the UiStationAndStationGroups UiStationSoh into smaller messages ({@literal <} 1mb) due to limit on
   * message size (1mb Kafka limit). Hopefully this function will be removed when able to compress
   * the message.
   *
   * @param uiStationAndStationGroups the {@link UiStationAndStationGroups}.
   * @return uiStationAndStationGroupsList - list of messages fromm one larger message
   */
  public static List<UiStationAndStationGroups> makeGroupsMessage(
    UiStationAndStationGroups uiStationAndStationGroups) {

    // byte array and serializer to figure out how many bytes various java objects
    try (var serializer = new StringSerializer()) {

      // How big is the whole message
      byte[] b = serializer.serialize(null, String.valueOf(uiStationAndStationGroups));

      // Less than 1 MB send normally, else break the messages into multiple
      if (b.length < KAFKA_MSG_SIZE_LIMIT) {
        return List.of(uiStationAndStationGroups);
      }

      // Create the list to add the entries to.
      List<UiStationAndStationGroups> uiStationAndStationGroupsList = new ArrayList<>();
      UiStationAndStationGroups newEntry = null;

      // Loop thru the UiStationSoh list checking if we can add it to the latest entry or do we
      // need to make a new entry to add to the uiStationAndStationGroupsList being returned
      for (UiStationSoh uiStationSoh : uiStationAndStationGroups.getStationSoh()) {
        // How big is the station to be added
        int sizeOfStationSoh = serializer.serialize(null, String.valueOf(uiStationSoh)).length;
        int sizeOfNewEntry = serializer.serialize(null, String.valueOf(newEntry)).length;

        // Check to see if we need to create a new entry to the uiStationAndStationGroupsList list
        if (newEntry == null || sizeOfNewEntry + sizeOfStationSoh > KAFKA_MSG_SIZE_LIMIT) {

          newEntry = UiStationAndStationGroups.create(
            uiStationAndStationGroups.getStationGroups(),
            new ArrayList<>(),
            uiStationAndStationGroups.getIsUpdateResponse()
          );

          uiStationAndStationGroupsList.add(newEntry);
        }

        // Add the uiStationSoh to the UiStationSoh list
        newEntry.getStationSoh().add(uiStationSoh);
      }

      if (logger.isDebugEnabled()) {
        logger.debug(
          "Returning UiStationAndStationGroup list size: {} for UiStationSoh {} entries.",
          uiStationAndStationGroupsList.size(),
          uiStationAndStationGroups.getStationSoh().size());
      }

      return uiStationAndStationGroupsList;
    } catch (Exception e) {
      logger.warn("Found an issue splitting up the UiStationAndStationGroup message: {}", e.getMessage());
      return List.of(uiStationAndStationGroups);
    }
  }
}