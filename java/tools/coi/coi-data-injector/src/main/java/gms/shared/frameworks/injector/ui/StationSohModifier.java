package gms.shared.frameworks.injector.ui;

import gms.shared.frameworks.injector.Modifier;
import gms.shared.frameworks.osd.coi.signaldetection.StationGroup;
import gms.shared.frameworks.osd.coi.soh.StationSoh;
import org.apache.kafka.clients.producer.Producer;

import java.util.List;
import java.util.Set;

/**
 * Calls the StationSohGenerator which makes a list of stations and
 */
public class StationSohModifier implements Modifier<Iterable<StationSoh>> {
  private final CapabilitySohRollupModifier capabilityRollupModifier;
  private final StationSohGenerator stationSohGenerator;

  /**
   * Constructor for StationSohModifier
   */
  public StationSohModifier() {
    /* Load StationGroups used by CapabilityRollupModifier and StationSohGenerator */
    var baseFilePath = "gms/shared/frameworks/injector/";
    var stationGroupMapFilePath = baseFilePath + "StationGroupMap.json";
    List<StationGroup> stationGroups =
      UiDataInjectorUtility.loadStationGroupsFromFile(stationGroupMapFilePath);

    this.capabilityRollupModifier = new CapabilitySohRollupModifier(stationGroups);
    this.stationSohGenerator = new StationSohGenerator(stationGroups);
  }

  @Override
  public Set<StationSoh> apply(Iterable<StationSoh> stationSohIterable) {
    var newUpdatedStations = this.stationSohGenerator.getUpdatedStations();

    // Call the apply for the CapabiltySohRollupModifier with updated StationSoh
    this.capabilityRollupModifier.applyStationSoh(newUpdatedStations);

    // Call publish (which for now just prints the CapabilitySOHRollup list)
    this.capabilityRollupModifier.publishRollups();
    return newUpdatedStations;
  }

  /**
   * Sets this capabilityRollupModifier's producer
   *
   * @param producer kafka Producer for sending String/String key-value records
   */
  @Override
  public void setProducer(Producer<String, String> producer) {
    this.capabilityRollupModifier.setProducer(producer);
  }
}
