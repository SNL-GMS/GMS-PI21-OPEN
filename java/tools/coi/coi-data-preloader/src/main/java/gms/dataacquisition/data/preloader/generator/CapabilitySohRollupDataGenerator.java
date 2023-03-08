package gms.dataacquisition.data.preloader.generator;

import gms.dataacquisition.data.preloader.GenerationSpec;
import gms.shared.frameworks.injector.CapabilitySohRollupIdModifier;
import gms.shared.frameworks.osd.api.OsdRepositoryInterface;
import gms.shared.frameworks.osd.coi.signaldetection.Station;
import gms.shared.frameworks.osd.coi.signaldetection.StationGroup;
import gms.shared.frameworks.osd.coi.soh.CapabilitySohRollup;
import gms.shared.frameworks.osd.coi.soh.SohStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 * Generate capability roll-ups
 */
public class CapabilitySohRollupDataGenerator extends
  CoiDataGenerator<CapabilitySohRollup, CapabilitySohRollupIdModifier> {

  private static final Logger logger = LoggerFactory
    .getLogger(CapabilitySohRollupDataGenerator.class);

  /**
   * Constructor
   *
   * @param generationSpec blueprint for generating data
   * @param sohRepository connection to OSD
   */
  public CapabilitySohRollupDataGenerator(GenerationSpec generationSpec,
    OsdRepositoryInterface sohRepository) {
    super(generationSpec, sohRepository);
  }

  @Override
  protected Collection<String> getSeedNames() {
    return stationGroups()
      .map(StationGroup::getName)
      .distinct()
      .collect(toList());
  }

  @Override
  protected CapabilitySohRollup generateSeed(String stationGroupName) {
    logger.debug("CapabilitySohRollup seed generation - STARTING...");

    final var basedOnStationSohs = new HashSet<UUID>();
    final var rollupSohStatusByStation = stationGroups()
      .filter(sg -> stationGroupName.equals(sg.getName()))
      .flatMap(StationGroup::stations)
      .collect(toMap(Station::getName, s -> SohStatus.GOOD));
    final var rollupSohStatus = SohStatus.GOOD;

    final var capabilitySohRollup = CapabilitySohRollup
      .create(UUID.randomUUID(),
        seedTime,
        rollupSohStatus,
        stationGroupName,
        basedOnStationSohs,
        rollupSohStatusByStation);

    logger.debug("CapabilitySohRollup seed generation - COMPLETE");

    return capabilitySohRollup;
  }

  @Override
  protected CapabilitySohRollupIdModifier getModifier(Duration generationFrequency) {
    return new CapabilitySohRollupIdModifier(generationFrequency);
  }

  @Override
  protected void consumeRecords(Iterable<CapabilitySohRollup> records) {
    logger.debug("CapabilitySohRollup consuming records - STARTING...");

    sohRepository.storeCapabilitySohRollup(convertToSet(records));

    logger.debug("CapabilitySohRollup consuming records - COMPLETE");
  }


}
