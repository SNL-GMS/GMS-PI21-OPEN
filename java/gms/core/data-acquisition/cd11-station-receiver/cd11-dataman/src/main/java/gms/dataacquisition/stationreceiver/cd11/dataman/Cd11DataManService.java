package gms.dataacquisition.stationreceiver.cd11.dataman;

import gms.core.dataacquisition.receiver.DataFrameReceiverConfiguration;
import gms.dataacquisition.stationreceiver.cd11.dataman.configuration.DataManConfig;
import gms.shared.frameworks.common.annotations.Component;
import gms.shared.frameworks.control.ControlContext;
import gms.shared.frameworks.control.ControlFactory;
import gms.shared.frameworks.osd.coi.waveforms.AcquisitionProtocol;
import gms.shared.utilities.kafka.KafkaConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.netty.DisposableChannel;
import reactor.netty.DisposableServer;

import javax.ws.rs.Path;
import java.util.Collections;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;


/**
 * Service class responsible for mapping component information to allow configuration and control
 * frameworks to setup appropriately
 */
@Component("dataman")
@Path("/da-dataman")
public class Cd11DataManService {

  private static final Logger logger = LoggerFactory.getLogger(Cd11DataManService.class);

  private final Cd11DataManager dataMan;

  public Cd11DataManService(Cd11DataManager dataMan) {
    this.dataMan = dataMan;
  }

  public static Cd11DataManService create(ControlContext context) {
    checkNotNull(context, "Cannot create Cd11DataManService from null context");

    logger.info("Initializing Configuration...");
    var systemConfig = context.getSystemConfig();

    var dataManConfig = DataManConfig.create(context.getProcessingConfigurationConsumerUtility(),
      systemConfig);

    var dataFrameReceiverConfiguration = DataFrameReceiverConfiguration.create(AcquisitionProtocol.CD11,
      context.getProcessingConfigurationRepository(), systemConfig);

    var kafkaConfiguration = KafkaConfiguration.create(systemConfig);
    var dataMan = Cd11DataManager.create(dataManConfig, dataFrameReceiverConfiguration, kafkaConfiguration);
    return new Cd11DataManService(dataMan);
  }

  public Cd11DataManager getDataMan() {
    return dataMan;
  }

  public static void main(String[] args) {
    try {
      Cd11DataManager dataMan = ControlFactory.createControl(Cd11DataManService.class).getDataMan();
      logger.info("Initializing Data Manager...");
      dataMan.initialize();
      logger.info("Initialization Complete");

      logger.info("Binding All Station Connection Ports...");
      var disposableServers = dataMan.bindAllPorts()
        .blockOptional()
        .orElseGet(Collections::emptyList);
      logger.info("Station Connection Port Binding Complete");

      logger.info("Configuring Frame Sending...");
      var sendFrames = dataMan.startFrameSending()
        .doOnError(cause -> logger.error("Fatal error encountered in DataMan", cause));
      logger.info("Frame Sending Successfully Configured");

      Runtime.getRuntime().addShutdownHook(new Thread(() -> shutdown(dataMan, disposableServers)));
      sendFrames.block();
    } catch (Exception e) {
      logger.error("CD1.1 Data Manager Service encountered an unrecoverable exception: ", e);
      System.exit(1);
    }
  }

  private static void shutdown(Cd11DataManager dataman, List<? extends DisposableServer> disposableServers) {
    logger.info("Shutting down Dataman service...");
    dataman.shutdownFrameHandling();
    disposableServers.forEach(DisposableChannel::dispose);
    dataman.shutdownKafkaSending();
    logger.info("Shutdown complete");
  }
}

