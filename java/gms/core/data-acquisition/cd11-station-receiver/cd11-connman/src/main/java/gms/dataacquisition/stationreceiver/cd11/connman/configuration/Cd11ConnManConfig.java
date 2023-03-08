package gms.dataacquisition.stationreceiver.cd11.connman.configuration;

import gms.dataacquisition.stationreceiver.cd11.common.configuration.Cd11ConnectionConfig;
import gms.dataacquisition.stationreceiver.cd11.common.configuration.Cd11DataConsumerParameters;
import gms.dataacquisition.stationreceiver.cd11.common.configuration.Cd11DataConsumerParametersTemplatesFile;
import gms.shared.frameworks.configuration.ConfigurationRepository;
import gms.shared.frameworks.configuration.RetryConfig;
import gms.shared.frameworks.configuration.repository.client.ConfigurationConsumerUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public class Cd11ConnManConfig {

  Logger logger = LoggerFactory.getLogger(Cd11ConnManConfig.class);

  public static final short DEFAULT_PROTOCOL_MAJOR_VERSION = 1;
  public static final short DEFAULT_PROTOCOL_MINOR_VERSION = 1;
  public static final String DEFAULT_SERVICE_TYPE = "TCP";
  public static final String DEFAULT_FRAME_CREATOR = "CONNMAN";
  public static final String DEFAULT_FRAME_DESTINATION = "0";

  ConfigurationConsumerUtility configurationConsumerUtility;
  private static final String SEPARATOR = ".";
  private static final String CONFIGURATION_NAME_PREFIX = "connman" + SEPARATOR;
  private static final UnaryOperator<String> KEY_BUILDER = s -> CONFIGURATION_NAME_PREFIX + s;
  private static final String CONFIGURATION_NAME = KEY_BUILDER.apply("station-parameters");


  private final short protocolMajorVersion;
  private final short protocolMinorVersion;
  private final String serviceType;
  private final String frameCreator;
  private final String frameDestination;
  private final int cd11DataConsumerBasePort;

  private Cd11ConnManConfig(ConfigurationConsumerUtility configurationConsumerUtility, int datConsumerBasePort) {
    this.configurationConsumerUtility = configurationConsumerUtility;
    this.protocolMajorVersion = DEFAULT_PROTOCOL_MAJOR_VERSION;
    this.protocolMinorVersion = DEFAULT_PROTOCOL_MINOR_VERSION;
    this.serviceType = DEFAULT_SERVICE_TYPE;
    this.frameCreator = DEFAULT_FRAME_CREATOR;
    this.frameDestination = DEFAULT_FRAME_DESTINATION;

    this.cd11DataConsumerBasePort = datConsumerBasePort;
  }

  /**
   * Obtain a new {@link Cd11ConnManConfig} using the provided {@link ConfigurationConsumerUtility}
   * to provide QC configuration.
   *
   * @param configurationConsumerUtility {@link ConfigurationConsumerUtility}, not null
   * @return {@link Cd11ConnManConfig}, not null
   * @throws NullPointerException if configurationConsumerUtility is null
   */
  public static Cd11ConnManConfig create(
    ConfigurationConsumerUtility configurationConsumerUtility, int dataConsumerBasePort) {
    return new Cd11ConnManConfig(configurationConsumerUtility, dataConsumerBasePort);
  }

  public static Cd11ConnManConfig create(ConfigurationRepository configurationRepository,
    int dataConsumerBasePort, RetryConfig retryConfig) {

    Objects.requireNonNull(configurationRepository,
      "Cd11ConnManConfig cannot be created with null "
        + "ConfigurationRepository");

    // Construct a ConfigurationConsumerUtility with the provided configurationRepository and
    // the necessary ConfigurationTransforms
    final var configurationConsumerUtility = ConfigurationConsumerUtility
      .builder(configurationRepository)
      .configurationNamePrefixes(List.of(CONFIGURATION_NAME_PREFIX))
      .retryConfiguration(retryConfig)
      .build();

    return new Cd11ConnManConfig(configurationConsumerUtility, dataConsumerBasePort);
  }

  public Cd11ConnectionConfig getConnectionConfig() {
    return Cd11ConnectionConfig.builder()
      .setProtocolMajorVersion(getProtocolMajorVersion())
      .setProtocolMinorVersion(getProtocolMinorVersion())
      .setServiceType(getServiceType())
      .build();
  }

  public List<Cd11DataConsumerParameters> getCd11StationParameters() {

    Cd11DataConsumerParametersTemplatesFile templatesFile = configurationConsumerUtility
      .resolve(CONFIGURATION_NAME, Collections.emptyList(),
        Cd11DataConsumerParametersTemplatesFile.class);

    return templatesFile.getCd11DataConsumerParametersTemplates().stream()
      .map(template -> {
        logger.info("Constructing Consumer parameters for station {} and port {} (basePort {} + offset {})",
          template.getStationName(), cd11DataConsumerBasePort + template.getPortOffset(),
          cd11DataConsumerBasePort, template.getPortOffset());
        return Cd11DataConsumerParameters.create(template, cd11DataConsumerBasePort);
      })
      .collect(Collectors.toList());
  }

  public short getProtocolMajorVersion() {
    return protocolMajorVersion;
  }

  public short getProtocolMinorVersion() {
    return protocolMinorVersion;
  }

  public String getServiceType() {
    return serviceType;
  }

  public String getFrameCreator() {
    return frameCreator;
  }

  public String getFrameDestination() {
    return frameDestination;
  }

}


