package gms.shared.frameworks.injector.ui;

import com.fasterxml.jackson.core.JsonProcessingException;
import gms.shared.frameworks.injector.Modifier;
import gms.shared.frameworks.osd.coi.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.frameworks.osd.coi.signaldetection.Station;
import gms.shared.frameworks.osd.coi.signaldetection.StationGroup;
import gms.shared.frameworks.osd.coi.soh.CapabilitySohRollup;
import gms.shared.frameworks.osd.coi.soh.SohStatus;
import gms.shared.frameworks.osd.coi.soh.StationSoh;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Capability Rollup modifier class allows for injecting Capability Rollup statuses via DataInector
 */
public class CapabilitySohRollupModifier implements Modifier<Iterable<CapabilitySohRollup>> {

  private static class Config {
    public static final String CAPABILITY_ROLLUP_TOPIC = "soh.capability-rollup";
  }

  private static final Logger logger = LoggerFactory.getLogger(CapabilitySohRollupModifier.class);

  private Set<CapabilitySohRollup> groupCapabilityRollupList;
  private Producer<String, String> producer;
  // Default constructor creates a random seed
  private SecureRandom random = new SecureRandom();

  private List<StationGroup> stationGroups;

  /**
   * Sets station groups and makes new ArrayList of CapabilitySohRollup
   *
   * @param stationGroups
   */
  public CapabilitySohRollupModifier(List<StationGroup> stationGroups) {
    this.stationGroups = stationGroups;
    this.groupCapabilityRollupList = new HashSet<>();
  }

  /**
   * Sets producer
   *
   * @param producer Producer for working with strings
   */
  @Override
  public void setProducer(Producer<String, String> producer) {
    this.producer = producer;
  }

  /**
   * Need a InjectableType that takes a list of StationSoh and return a list of CapabilitySohRollup
   *
   * @param stationSohIterable
   * @return the generated {@link CapabilitySohRollupModifier#groupCapabilityRollupList}
   */
  public Iterable<CapabilitySohRollup> applyStationSoh(Iterable<StationSoh> stationSohIterable) {
    // Build a map of mock stations to more efficiently build the capabilities
    Map<String, StationSoh> stationSohMap = new HashMap<>();
    for (StationSoh stationSoh : stationSohIterable) {
      stationSohMap.put(stationSoh.getStationName(), stationSoh);
    }

    this.groupCapabilityRollupList = generateGroupCapabilityRollupsWithIDs(stationSohMap);
    return this.groupCapabilityRollupList;
  }

  /**
   * Generate a list of group capability rollup based on latest StationSoh map
   *
   * @param stationSohMap
   * @return updatedList of group capability rollup
   */
  private Set<CapabilitySohRollup> generateGroupCapabilityRollupsWithIDs(
    Map<String, StationSoh> stationSohMap) {
    var updatedList = new HashSet<CapabilitySohRollup>();
    // Iterate through the StationSoh ids adding them Capability Rollup
    for (StationGroup stationGroup : this.stationGroups) {

      //Making a group with NONE capability rollups
      if (stationGroup.getName().equalsIgnoreCase("All_1")) {
        continue;
      }
      // Get the UUIDs from StationSoh
      Set<UUID> stationSohIds = generateStationIDSet(stationGroup, stationSohMap);
      Map<String, SohStatus> stationStatuses = generateStationStatusMap(stationGroup);

      // Add it to new list. Calling create because no other way to update time
      updatedList.add(CapabilitySohRollup.create(
        UUID.randomUUID(),
        Instant.now(),
        getRandomStatus(),
        stationGroup.getName(),
        stationSohIds,
        stationStatuses
      ));
    }
    return updatedList;
  }

  /***
   * Generate a map of station name to SOH status for use in capability rollup soh generation
   * @param stationGroup
   * @return the randomised stationStatuses map
   */
  private Map<String, SohStatus> generateStationStatusMap(StationGroup stationGroup) {
    Map<String, SohStatus> stationStatuses = new HashMap<>();
    for (Station station : stationGroup.getStations()) {
      // Randomize the station status
      stationStatuses.put(station.getName(), getRandomStatus());
    }
    return stationStatuses;
  }

  private Set<UUID> generateStationIDSet(StationGroup stationGroup,
    Map<String, StationSoh> stationSohMap) {
    Set<UUID> stationSohIds = new HashSet<>();
    for (Station station : stationGroup.getStations()) {
      var stationSoh = stationSohMap.get(station.getName());
      stationSohIds.add(stationSoh.getId());
    }
    return stationSohIds;
  }

  /**
   * Using random number and modulo to return a random status
   *
   * @return SohStatus
   */
  private SohStatus getRandomStatus() {
    return SohStatus.values()[this.random.nextInt(3)];
  }

  /**
   * Sends the Group {@link CapabilitySohRollup} data and attempts to send to the KafkaProducer
   */
  public void publishRollups() {
    // JSON string to send
    String groupCapabilityRollup;

    // Loop through the list and send each group capability as separate messages
    for (CapabilitySohRollup capability : this.groupCapabilityRollupList) {
      try {

        // Convert each capability rollup to JSON string representation
        groupCapabilityRollup = CoiObjectMapperFactory.getJsonObjectMapper().writeValueAsString(capability);
      } catch (JsonProcessingException e) {
        throw new IllegalArgumentException("Could not serialize Group Capability Rollup List to Json", e);
      }

      ProducerRecord<String, String> capabilityRollupProducerRecord = new ProducerRecord<>(
        Config.CAPABILITY_ROLLUP_TOPIC, groupCapabilityRollup
      );
      if (logger.isDebugEnabled())
        logger.debug("Created capability record: {} ", capabilityRollupProducerRecord.value());
      if (this.producer != null) {
        this.producer.send(capabilityRollupProducerRecord);
      }
    }
  }

  /**
   * Accessor to the latest CapabilitySohRollup list
   *
   * @return the groupCapabilityRollupList
   */
  public Set<CapabilitySohRollup> getGroupCapabilityRollupList() {
    return this.groupCapabilityRollupList;
  }
}
