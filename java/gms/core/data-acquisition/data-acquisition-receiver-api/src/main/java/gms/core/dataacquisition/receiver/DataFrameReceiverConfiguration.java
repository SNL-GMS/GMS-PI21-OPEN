package gms.core.dataacquisition.receiver;

import gms.shared.frameworks.configuration.ConfigurationRepository;
import gms.shared.frameworks.configuration.RetryConfig;
import gms.shared.frameworks.configuration.Selector;
import gms.shared.frameworks.configuration.repository.client.ConfigurationConsumerUtility;
import gms.shared.frameworks.osd.coi.waveforms.AcquisitionProtocol;
import gms.shared.frameworks.systemconfig.SystemConfig;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public class DataFrameReceiverConfiguration {

  private final AcquisitionProtocol protocol;
  private final ConfigurationConsumerUtility configurationResolver;

  private Map<String, String> channelNamesByPacketName;

  private static final String SEPARATOR = ".";

  private static final String DATAFRAME_RECEIVER_PREFIX = "dataframe-receiver" + SEPARATOR;
  private static final UnaryOperator<String> KEY_BUILDER = s -> DATAFRAME_RECEIVER_PREFIX + s;

  private static final String CHANNEL_LOOKUP_SELECTOR = "channel-lookup";
  private static final String PROTOCOL_SELECTOR = "protocol";

  private DataFrameReceiverConfiguration(ConfigurationConsumerUtility configurationConsumer,
    AcquisitionProtocol protocol) {
    this.configurationResolver = configurationConsumer;
    this.protocol = protocol;
  }

  public static DataFrameReceiverConfiguration create(AcquisitionProtocol protocol,
    ConfigurationRepository configurationRepository,
    SystemConfig systemConfig) {

    final var retryConfig = RetryConfig.create(systemConfig.getValueAsInt("processing-retry-initial-delay"), systemConfig.getValueAsInt("processing-retry-max-delay"),
      ChronoUnit.valueOf(systemConfig.getValue("processing-retry-delay-units")), systemConfig.getValueAsInt("processing-retry-max-attempts"));

    ConfigurationConsumerUtility configurationResolver = ConfigurationConsumerUtility
      .builder(configurationRepository)
      .retryConfiguration(retryConfig)
      .selectorCacheExpiration(systemConfig.getValueAsDuration("config-cache-expiration"))
      .configurationNamePrefixes(List.of(DATAFRAME_RECEIVER_PREFIX))
      .build();

    return new DataFrameReceiverConfiguration(configurationResolver, protocol);
  }

  //TODO: Should cut out the middle-man and pass in a station name + subframe.
  // Will require significant refactor to accommodate tests.
  public Optional<String> getChannelName(String subFrameName) {
    return Optional.ofNullable(getChannelNamesByPacketName().get(subFrameName));
  }

  public Stream<String> channelNames() {
    return getChannelNamesByPacketName().values().stream();
  }

  private Map<String, String> getChannelNamesByPacketName() {
    if (channelNamesByPacketName == null) {
      resolveChannelLookup();
    }

    return channelNamesByPacketName;
  }

  private void resolveChannelLookup() {
    channelNamesByPacketName = configurationResolver.resolve(
        KEY_BUILDER.apply(CHANNEL_LOOKUP_SELECTOR),
        List.of(Selector.from(PROTOCOL_SELECTOR, protocol.toString())),
        ChannelLookupConfigurationFile.class)
      .getChannelIdsByPacketName();
  }
}
