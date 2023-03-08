package gms.dataacquisition.data.preloader.generator;

import gms.dataacquisition.data.preloader.GenerationSpec;
import gms.shared.frameworks.injector.AceiIdModifier;
import gms.shared.frameworks.osd.api.OsdRepositoryInterface;
import gms.shared.frameworks.osd.coi.channel.Channel;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collection;

import static java.util.stream.Collectors.toList;

/**
 * Base class for generating acquired channel environment issues
 *
 * @param <T>
 */
public abstract class AceiDataGenerator<T extends AcquiredChannelEnvironmentIssue<?>> extends
  CoiDataGenerator<T, AceiIdModifier> {

  protected static final Logger logger = LoggerFactory.getLogger(AceiDataGenerator.class);

  /**
   * Constructor
   *
   * @param generationSpec blueprint for generating data
   * @param sohRepository connection to OSD
   */
  protected AceiDataGenerator(GenerationSpec generationSpec, OsdRepositoryInterface sohRepository) {
    super(generationSpec, sohRepository);
  }

  @Override
  protected Collection<String> getSeedNames() {
    return channels()
      .map(Channel::getName)
      .distinct()
      .collect(toList());
  }

  @Override
  protected AceiIdModifier getModifier(Duration generationFrequency) {
    return new AceiIdModifier(generationFrequency);
  }
}
