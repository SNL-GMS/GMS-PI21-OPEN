package gms.dataacquisition.stationreceiver.cd11.dataprovider;

import com.fasterxml.jackson.databind.ObjectMapper;
import gms.dataacquisition.stationreceiver.cd11.dataprovider.configuration.Cd11DataProviderConfig;
import gms.dataacquisition.stationreceiver.cd11.dataprovider.rsdfsource.RsdfSource;
import gms.dataacquisition.stationreceiver.cd11.dataprovider.rsdfsource.file.FileRsdfSource;
import gms.dataacquisition.stationreceiver.cd11.dataprovider.rsdfsource.kafka.KafkaRsdfSource;
import gms.shared.frameworks.configuration.RetryConfig;
import gms.shared.frameworks.osd.coi.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.frameworks.systemconfig.SystemConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.time.temporal.ChronoUnit;

/**
 * High-level application for providing an infinite {@link RsdfSource} to a
 * {@link Cd11FramePublisher} for the purpose of acting as data provider to a data consumer that
 * adheres to the CD1.1 communication protocol
 */
class Cd11DataProvider {

  public static final String COMPONENT_NAME = "cd11-data-provider";
  private static final Logger logger = LoggerFactory.getLogger(Cd11DataProvider.class);
  private static final ObjectMapper mapper = CoiObjectMapperFactory.getJsonObjectMapper();

  public static void main(String[] args) {

    //Create the system config by reading in all the env vars that are tagged with the component name
    var systemConfig = SystemConfig.create(COMPONENT_NAME);

    Cd11DataProviderConfig providerConfig = null;
    try {
      providerConfig = mapper
        .readValue(Files.readString(systemConfig.getValueAsPath("config-path")),
          Cd11DataProviderConfig.class);
    } catch (IOException | NullPointerException e) {
      logger.error("Failed to read provider configuration", e);
      System.exit(1);
    }

    // Grab from the config the mode that we are using to get data and instantiate the source of that type
    String inputMode = providerConfig.getProviderInputMode();
    RsdfSource source;

    logger.info("Creating data provider using {} as the data source", inputMode);

    if (inputMode.compareToIgnoreCase("kafka") == 0) {
      source = KafkaRsdfSource.create(providerConfig.getKafkaConfig().orElseThrow(),
        systemConfig.getValue("consumer-id"),
        systemConfig.getValue("repeater-servers"));
    } else {
      source = FileRsdfSource.create(providerConfig.getFileConfig().orElseThrow());
    }

    var clientFactory = Cd11ClientFactory
      .create(providerConfig.getConnectionFrameCreator(), providerConfig.getConnectionFrameDestination());

    logger.info("Cd11Client Factory created");

    var connManAddress = systemConfig.getValue("connman-address");
    var connManPort = systemConfig.getValueAsInt("connman-port");

    var framePublisher = Cd11FramePublisher
      .create(connManAddress, connManPort, clientFactory,
        RetryConfig.create(
          systemConfig.getValueAsLong("retry-initial-delay"),
          Long.MAX_VALUE,
          ChronoUnit.valueOf(systemConfig.getValue("retry-delay-units")),
          systemConfig.getValueAsInt("retry-max-attempts")));

    logger.info("Cd11FramePublisher created and now starting");

    var disposable = framePublisher.publish(source).subscribe();
    Runtime.getRuntime().addShutdownHook(new Thread(disposable::dispose));
  }
}