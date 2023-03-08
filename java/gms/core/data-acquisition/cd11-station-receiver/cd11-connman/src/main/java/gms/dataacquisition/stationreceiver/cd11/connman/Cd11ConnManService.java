package gms.dataacquisition.stationreceiver.cd11.connman;

import com.google.common.annotations.VisibleForTesting;
import gms.dataacquisition.stationreceiver.cd11.connman.configuration.Cd11ConnManConfig;
import gms.shared.frameworks.common.annotations.Component;
import gms.shared.frameworks.control.ControlContext;
import gms.shared.frameworks.control.ControlFactory;
import gms.shared.frameworks.systemconfig.SystemConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.netty.DisposableServer;

import javax.ws.rs.Path;

import static com.google.common.base.Preconditions.checkNotNull;

@Component("connman")
@Path("/da-connman")
public class Cd11ConnManService {

  private static final Logger logger = LoggerFactory.getLogger(Cd11ConnManService.class);

  private final Cd11ConnectionManager cd11ConnectionManager;

  private Cd11ConnManService(Cd11ConnectionManager connMan) {
    this.cd11ConnectionManager = connMan;
  }

  /**
   * Create {@link Cd11ConnManService} from a {@link ControlContext}
   *
   * @param context the control context, not null
   * @return an instance of Cd11ConnManService
   */
  public static Cd11ConnManService create(ControlContext context) {
    checkNotNull(context, "Cannot create Cd11ConnManService from null context");

    logger.info("Retrieving Configuration...");
    SystemConfig systemConfig = context.getSystemConfig();
    Cd11ConnManConfig cd11ConnManConfig = Cd11ConnManConfig
      .create(context.getProcessingConfigurationConsumerUtility(),
        systemConfig.getValueAsInt("cd11-dataconsumer-baseport"));

    logger.info("Initializing Cd11 Connection Manager...");
    var connMan = Cd11ConnectionManager
      .create(context.getSystemConfig(), cd11ConnManConfig);

    logger.info("Connection Manager Successfully Initialized");
    return new Cd11ConnManService(connMan);
  }

  public Cd11ConnectionManager getCd11ConnMan() {
    return cd11ConnectionManager;
  }

  public static void main(String[] args) {
    try {
      Cd11ConnectionManager connMan = ControlFactory.createControl(Cd11ConnManService.class).getCd11ConnMan();
      logger.info("Initializing Connection Manager...");
      connMan.initialize();
      logger.info("Initialization Complete");

      logger.info("Binding Connection Port...");
      var connManServer = connMan.bind()
        .blockOptional()
        .orElseThrow();
      logger.info("Connection Port Binding Complete");

      Runtime.getRuntime().addShutdownHook(new Thread(() -> shutdown(connMan, connManServer)));
      connManServer.onDispose().block();
    } catch (Exception e) {
      logger.error("CD1.1 Connection Manager Service encountered an unrecoverable exception: ", e);
    }
  }

  @VisibleForTesting
  static void shutdown(Cd11ConnectionManager cd11ConnectionManager, DisposableServer connManServer) {
    logger.info("Shutting down Connman service...");
    cd11ConnectionManager.shutdown();
    connManServer.dispose();
    logger.info("Shutdown complete");
  }
}
