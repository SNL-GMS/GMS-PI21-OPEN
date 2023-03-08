package gms.dataacquisition.data.preloader.generator;

import gms.dataacquisition.data.preloader.GenerationSpec;
import gms.shared.frameworks.osd.api.OsdRepositoryInterface;
import gms.shared.frameworks.osd.api.rawstationdataframe.AceiUpdates;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssue.AcquiredChannelEnvironmentIssueType;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssueAnalog;

/**
 * Generate analog acquired channel environment issues
 */
public class AceiAnalogDataGenerator extends
  AceiDataGenerator<AcquiredChannelEnvironmentIssueAnalog> {


  /**
   * Constructor
   *
   * @param generationSpec blueprint for generating data
   * @param sohRepository connection to OSD
   */
  public AceiAnalogDataGenerator(GenerationSpec generationSpec,
    OsdRepositoryInterface sohRepository) {
    super(generationSpec, sohRepository);
  }

  @Override
  protected void consumeRecords(Iterable<AcquiredChannelEnvironmentIssueAnalog> records) {
    logger.debug("ACEI consuming records - STARTING...");

    sohRepository.syncAceiUpdates(AceiUpdates.builder().setAnalogInserts(convertToSet(records)).build());

    logger.debug("ACEI consuming records - COMPLETE");
  }

  @Override
  protected AcquiredChannelEnvironmentIssueAnalog generateSeed(String channelName) {
    logger.debug("ACEI seed generation - STARTING...");

    final var type = AcquiredChannelEnvironmentIssueType.CLOCK_DIFFERENTIAL_IN_MICROSECONDS;
    final var status = 0.0;

    final var acei = AcquiredChannelEnvironmentIssueAnalog
      .from(channelName,
        type,
        seedTime,
        seedTime.plus(generationFrequency),
        status);

    logger.debug("ACEI seed generation - COMPLETE");

    return acei;
  }

}
