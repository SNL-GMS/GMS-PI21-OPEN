package gms.core.dataacquisition.reactor.util;

import gms.shared.frameworks.configuration.Selector;
import gms.shared.frameworks.configuration.repository.client.ConfigurationConsumerUtility;

import java.time.Duration;
import java.util.List;

/**
 * Resolves merge tolerance from Processing Configuration
 */
public class ConfigurationToleranceResolver implements ToleranceResolver {

  private static final String CONFIGURATION = "acei-merge-processor.merge-tolerance";
  private static final String CHANNEL_NAME_SELECTOR_KEY = "ChannelName";
  private static final String MERGE_TOLERANCE = "merge-tolerance";

  private final ConfigurationConsumerUtility config;

  private ConfigurationToleranceResolver(ConfigurationConsumerUtility config) {
    this.config = config;
  }

  public static ConfigurationToleranceResolver create(ConfigurationConsumerUtility config) {
    return new ConfigurationToleranceResolver(config);
  }

  @Override
  public Duration resolveTolerance(String channelName) {
    return Duration.parse(String.valueOf(config.resolve(CONFIGURATION,
      List.of(Selector.from(CHANNEL_NAME_SELECTOR_KEY, channelName))).get(MERGE_TOLERANCE)));
  }
}
