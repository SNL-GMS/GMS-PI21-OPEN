package gms.dataacquisition.data.preloader.generator;

import gms.dataacquisition.data.preloader.GenerationSpec;
import gms.shared.frameworks.injector.DataGeneratorState;
import gms.shared.frameworks.osd.api.OsdRepositoryInterface;
import gms.shared.frameworks.osd.api.rawstationdataframe.AceiUpdates;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssue.AcquiredChannelEnvironmentIssueType;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssueBoolean;

import java.util.Optional;

/**
 * Generate boolean acquired channel environment issues
 */
public class AceiBooleanDataGenerator extends
  AceiDataGenerator<AcquiredChannelEnvironmentIssueBoolean> {

  private int aceiBooleanDataGeneratorStateStatusesSize;

  /**
   * Constructor
   *
   * @param generationSpec blueprint for generating data
   * @param sohRepository connection to OSD
   */
  public AceiBooleanDataGenerator(GenerationSpec generationSpec,
    OsdRepositoryInterface sohRepository) {
    super(generationSpec, sohRepository);
  }

  protected int getAceiBooleanDataGeneratorStateStatusesSize() {
    return this.aceiBooleanDataGeneratorStateStatusesSize;
  }

  @Override
  protected void consumeRecords(Iterable<AcquiredChannelEnvironmentIssueBoolean> records) {
    logger.debug("ACEI consuming records - STARTING...");

    sohRepository.syncAceiUpdates(AceiUpdates.builder().setBooleanInserts(convertToSet(records)).build());

    logger.debug("ACEI consuming records - COMPLETE");
  }

  @Override
  protected AcquiredChannelEnvironmentIssueBoolean generateSeed(String channelName) {
    logger.debug("ACEI seed generation - STARTING...");

    final var type = AcquiredChannelEnvironmentIssueType.VAULT_DOOR_OPENED;
    final var status = false;

    final var acei = AcquiredChannelEnvironmentIssueBoolean
      .from(channelName,
        type,
        seedTime,
        seedTime.plus(generationFrequency),
        status);

    logger.debug("ACEI seed generation - COMPLETE");

    return acei;
  }

  @Override
  protected Optional<DataGeneratorState<AcquiredChannelEnvironmentIssueBoolean, AceiBooleanDataGeneratorState>> getDataGeneratorState(
    GenerationSpec generationSpec, String seedName) {
    var aceiBooleanDataGeneratorState = new AceiBooleanDataGeneratorState(
      generationSpec, seedName,
      sohRepository);
    this.aceiBooleanDataGeneratorStateStatusesSize = aceiBooleanDataGeneratorState
      .getSampleStatusesSize();
    return Optional.of(aceiBooleanDataGeneratorState);
  }

}
