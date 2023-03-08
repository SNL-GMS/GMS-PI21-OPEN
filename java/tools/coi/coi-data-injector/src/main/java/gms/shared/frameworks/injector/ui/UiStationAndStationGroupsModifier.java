package gms.shared.frameworks.injector.ui;

import com.fasterxml.jackson.core.JsonProcessingException;
import gms.core.performancemonitoring.ssam.control.config.StationSohMonitoringUiClientParameters;
import gms.core.performancemonitoring.ssam.control.datapublisher.SystemEvent;
import gms.core.performancemonitoring.uimaterializedview.QuietedSohStatusChangeUpdate;
import gms.core.performancemonitoring.uimaterializedview.SohQuietAndUnacknowledgedCacheManager;
import gms.core.performancemonitoring.uimaterializedview.UIStationAndStationGroupsChanges;
import gms.core.performancemonitoring.uimaterializedview.UiStationAndStationGroupGenerator;
import gms.core.performancemonitoring.uimaterializedview.UiStationAndStationGroups;
import gms.shared.frameworks.injector.Modifier;
import gms.shared.frameworks.osd.coi.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.frameworks.osd.coi.signaldetection.Station;
import gms.shared.frameworks.osd.coi.signaldetection.StationGroup;
import gms.shared.frameworks.osd.coi.systemmessages.SystemMessage;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import reactor.core.publisher.Sinks;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Simulate the OSD Analysis Manager functionality. Invoked by the Data Injector,
 * calling the apply method with a list (1) of UiStationAndStationGroups message to be populated.
 * The apply method will call the ${@link UiStationAndStationGroupGenerator} with the updated
 * data structures needed to create an up to date UiStationAndStationGroup message
 * to be sent to the UI.
 */
public class UiStationAndStationGroupsModifier implements Modifier<Iterable<SystemEvent<UiStationAndStationGroups>>> {

  /* producer used to send updated UiStationAndStationGroup message
   after receiving an Acknowledge or quiet notification */
  private Producer<String, String> producer;
  /* Listens for Acknowledgement and Quiet notifications and manages QuietedSohStatusChanges list */
  private SohStationConsumer sohStationConsumer;
  /* Capability rollup modifier for capability rollup list */
  private CapabilitySohRollupModifier capabilityRollupModifier;
  /* SohQuietAndUnacknowledgedCacheManager compares previous and current station soh entries to create
   * and track unacknowledged change for a station */
  private SohQuietAndUnacknowledgedCacheManager sohQuietAndUnacknowledgedCacheManager;
  /* Used to create a new list of StationSoh with random changes */
  private StationSohGenerator stationSohGenerator;
  /* Config params mostly used for threshold values in monitor value statuses */
  private StationSohMonitoringUiClientParameters stationSohParameters;
  private List<StationGroup> stationGroups;
  private Sinks.Many<SystemMessage> systemMessageSink = Sinks.many().multicast().onBackpressureBuffer();

  private static class Config {
    public static final String UI_SOH_STATION_TOPIC = "soh.ui-materialized-view";
  }

  /**
   * Constructor
   */
  public UiStationAndStationGroupsModifier() {
    /* After capability rollup modifier is initialized call for loaded station groups */
    var baseFilePath = "gms/shared/frameworks/injector/";
    String stationGroupMapFilePath = baseFilePath + "StationGroupMap.json";
    this.stationGroups =
      UiDataInjectorUtility.loadStationGroupsFromFile(stationGroupMapFilePath);
    this.capabilityRollupModifier = new CapabilitySohRollupModifier(this.stationGroups);
    this.stationSohGenerator = new StationSohGenerator(this.stationGroups);

    Set<Station> stations = UiDataInjectorUtility.getStationSet(this.stationGroups);

    // Get the StationSohMonitoringUiClientParameters configuration
    this.stationSohParameters = UiDataInjectorUtility.buildStationSohParameters(stations);


    this.sohQuietAndUnacknowledgedCacheManager =
      new SohQuietAndUnacknowledgedCacheManager(
        Collections.emptySet(), // empty quieted collection
        new HashSet<>(), // empty unacknowledge Set
        Collections.emptySet(), // empty StationSoh collection
        this.stationSohParameters
      );
    // Unack list manager is called by SOH Station Consumer for acknowledged stations
    this.sohStationConsumer = new SohStationConsumer(
      this.sohQuietAndUnacknowledgedCacheManager, this);
  }

  @Override
  public List<SystemEvent<UiStationAndStationGroups>> apply(
    Iterable<SystemEvent<UiStationAndStationGroups>> uiStationAndStationGroupsSohIterable) {
    var newUpdatedStations = this.stationSohGenerator.getUpdatedStations();

    // Update unack with updated stations
    this.sohQuietAndUnacknowledgedCacheManager.updateUnacknowledgedSet(newUpdatedStations);

    // Call the apply for the CapabiltySohRollupModifier with updated StationSoh
    this.capabilityRollupModifier.applyStationSoh(newUpdatedStations);

    // Call to convert and publish the latest SOH Stations
    List<QuietedSohStatusChangeUpdate> quietedSohStatusChanges =
      this.sohQuietAndUnacknowledgedCacheManager.getQuietedSohStatusChanges();
    var generatedUiStationAndStationGroupsSohList =
      UiStationAndStationGroupGenerator.generateUiStationAndStationGroups(
        newUpdatedStations,
        UIStationAndStationGroupsChanges.builder()
          .setUnacknowledgedStatusChanges(this.sohQuietAndUnacknowledgedCacheManager.getUnacknowledgedList())
          .setQuietedSohStatusChanges(quietedSohStatusChanges)
          .build(),
        this.capabilityRollupModifier.getGroupCapabilityRollupList(),
        this.stationSohParameters,
        this.stationGroups,
        false,
        this.systemMessageSink
      );

    return generatedUiStationAndStationGroupsSohList.stream().map(msg ->
      SystemEvent.from("soh-message", msg)).collect(Collectors.toList());

  }

  /**
   * Generate the UiStationSoh list and publish it on the topic. This is used by
   * SohStationConsumer to send the updated list back to the UI
   * after receiving an acknowledgement or quiet entry from the UI
   *
   * @param stationNames the list of UiStationSoh to send back
   */
  public void generateAndPublishUiStationSoh(List<String> stationNames) {
    List<QuietedSohStatusChangeUpdate> quietedSohStatusChanges =
      this.sohQuietAndUnacknowledgedCacheManager.getQuietedSohStatusChanges();
    var latestStationSohList =
      this.sohQuietAndUnacknowledgedCacheManager.getLastStationSohs();
    var stationsToUpdate = !stationNames.isEmpty() ? latestStationSohList.stream().
      filter(s -> stationNames.contains(s.getStationName())).collect(Collectors.toSet()) : latestStationSohList;
    List<UiStationAndStationGroups> uiStationAndStationGroupsList =
      UiStationAndStationGroupGenerator.generateUiStationAndStationGroups(
        stationsToUpdate,
        UIStationAndStationGroupsChanges.builder()
          .setQuietedSohStatusChanges(quietedSohStatusChanges)
          .setUnacknowledgedStatusChanges(this.sohQuietAndUnacknowledgedCacheManager.getUnacknowledgedList())
          .build(),
        this.capabilityRollupModifier.getGroupCapabilityRollupList(),
        this.stationSohParameters,
        this.stationGroups,
        true,
        systemMessageSink
      );
    this.publishUiStationAndStationGroups(uiStationAndStationGroupsList);
  }

  @Override
  public void setProducer(Producer<String, String> producer) {
    this.producer = producer;
  }

  @Override
  public void setConsumer(Consumer<String, String> consumer) {
    this.sohStationConsumer.setConsumer(consumer);
  }

  /**
   * Sends the {@link {UiStationSoh}} data and attempts to send to
   * the Kafka producer
   *
   * @param uiStationAndstationGroupsSohList list to send on Kafka
   */
  private void publishUiStationAndStationGroups(List<UiStationAndStationGroups> uiStationAndstationGroupsSohList) {
    // JSON string to send
    String uiStationAndStationGroupsSohJson;

    // Loop through the list and send each UiStationSoh as separate messages
    for (UiStationAndStationGroups uiStationAndStationGroup : uiStationAndstationGroupsSohList) {
      try {
        // Convert each UiStationAndStationGroupSoh to JSON string representation
        uiStationAndStationGroupsSohJson = CoiObjectMapperFactory.getJsonObjectMapper().
          writeValueAsString(uiStationAndStationGroup);
      } catch (JsonProcessingException e) {
        throw new IllegalArgumentException("Could not serialize UiStationSoh List to Json", e);
      }

      ProducerRecord<String, String> uiSohStationRecord = new ProducerRecord<>(Config.UI_SOH_STATION_TOPIC,
        uiStationAndStationGroupsSohJson);
      if (this.producer != null) {
        this.producer.send(uiSohStationRecord);
      }
    }
  }
}
