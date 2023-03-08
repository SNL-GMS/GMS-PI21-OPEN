package gms.dataacquisition.stationreceiver.cd11.connman;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import gms.dataacquisition.stationreceiver.cd11.connman.configuration.Cd11ConnManConfig;
import gms.dataacquisition.stationreceiver.cd11.connman.util.Cd11ConnManUtil;
import gms.shared.frameworks.systemconfig.SystemConfig;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.tcp.TcpServer;
import reactor.util.retry.Retry;

import java.net.InetAddress;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Cd11 ConnMan TCP Server using Reactor Netty
 */
public class Cd11ConnectionManager {

  private static final Logger logger = LoggerFactory.getLogger(Cd11ConnectionManager.class);
  // Connman configuration keys
  private static final String CONNECTION_MANAGER_PORT_KEY = "connection-manager-well-known-port";
  private static final String DATA_MANAGER_ADDRESS_KEY = "data-manager-ip-address";
  private static final String DATA_PROVIDER_KEY = "data-provider-ip-address";

  private static final String BIND_RETRIES_KEY = "bind-retries";

  private static final String BIND_INITIAL_WAIT_DURATION_KEY = "bind-initial-wait";
  // Connman and System config
  private final Cd11ConnManConfig connManConfig;
  private final SystemConfig systemConfig;
  private final int wellKnownPort;

  private final int bindRetries;

  private final Duration bindInitialWait;

  // TCP Server and inbound/outbound handler
  private Cd11ConnectionFrameHandler handler;
  // Cd11Station lookup and inet addresses for ports
  private Map<String, Cd11Station> cd11StationsLookup;
  private Map<String, InetAddress> inetAddressMap;
  private Map<String, Boolean> ignoredStationsMap;

  TcpServer tcpServer;

  private Cd11ConnectionManager(SystemConfig systemConfig,
    Cd11ConnManConfig connManConfig) {

    // Configuration setup
    this.systemConfig = systemConfig;
    this.connManConfig = connManConfig;

    wellKnownPort = this.systemConfig
      .getValueAsInt(CONNECTION_MANAGER_PORT_KEY);

    bindRetries = this.systemConfig
      .getValueAsInt(BIND_RETRIES_KEY);

    bindInitialWait = this.systemConfig
      .getValueAsDuration(BIND_INITIAL_WAIT_DURATION_KEY);

    logger.info(
      "Using well known port of {} to receive queries from stations looking to create a data connection",
      wellKnownPort);
  }

  /**
   * Factory method for creating ConnMan
   *
   * @param systemConfig System configuration
   * @param cd11ConnManConfig ConnMan configuration information and channel information
   * @return The processor
   */
  public static Cd11ConnectionManager create(
    SystemConfig systemConfig,
    Cd11ConnManConfig cd11ConnManConfig) {
    checkNotNull(cd11ConnManConfig, "Cannot create Cd11ConnManServer with null ConnMan config");
    checkNotNull(systemConfig, "Cannot create Cd11ConnManServer with null system config");

    return new Cd11ConnectionManager(systemConfig, cd11ConnManConfig);
  }

  public void initialize() {
    // Initialize Inet Addresses using the Connman System config
    tryInitializeInetAddresses();

    // Query the OSD for all registered CD 1.1 stations.
    this.initializeCd11StationsLookup();

    this.handler = new Cd11ConnectionFrameHandler(connManConfig, this::lookupCd11Station, ignoredStationsMap);

    // Initialize the Netty TCP Server
    this.tcpServer = TcpServer.create()
      .port(wellKnownPort)
      .wiretap(logger.isDebugEnabled())
      .doOnBound(server -> logger.info(
        "Connection Manager is now listening on well-known port {} for incoming data connection requests",
        server.port()))
      .doOnConnection(connection -> {
        logger
          .info("Client successfully connected on well-known port {}", wellKnownPort);

        connection.onDispose(() ->
          logger.debug("Client connection closed on well-known port {}",
            wellKnownPort));
      })
      .handle(handler);
  }

  @VisibleForTesting
  void initForBindTest(TcpServer tcpServer) {
    this.tcpServer = tcpServer;
  }

  /**
   * Binds the TCP server to allow the Connection manager to listen for requests
   */
  public Mono<DisposableServer> bind() {
    return tcpServer.bind()
      .doOnError(e -> logger.error("Error binding well-known connection port:", e))
      .doOnSuccess(s -> logger.info(
        "Connection Manager is now listening on well-known port {} for incoming data connection requests",
        wellKnownPort))
      .retryWhen(Retry.backoff(bindRetries, bindInitialWait))
      .cast(DisposableServer.class);
  }

  //-------------------- CD 1.1 Station Registration Methods --------------------

  /**
   * Register a new CD 1.1 station.
   *
   * @param stationName Name of the station.
   * @param expectedDataProviderIpAddress Expected IP Address of the Data Provider connecting to
   * this Connection Manager.
   * @param dataConsumerIpAddress IP Address of the Data Consumer to redirect this request
   * to.
   * @param dataConsumerPort Port number of the Data Consumer to redirect this request
   * to.
   */
  private void addCd11Station(
    Map<String, Cd11Station> cd11StationMap,
    String stationName,
    InetAddress expectedDataProviderIpAddress,
    InetAddress dataConsumerIpAddress,
    int dataConsumerPort) {

    // Add a new station to the list, or replace an existing station.
    cd11StationMap.put(stationName, new Cd11Station(
      expectedDataProviderIpAddress,
      dataConsumerIpAddress,
      dataConsumerPort));
  }


  // Initialize the cd11 stations lookup map
  private void initializeCd11StationsLookup() {

    // Initialize the data structures.
    Map<String, Boolean> ignoredStationsHashMap = new HashMap<>();
    Map<String, Cd11Station> cd11StationHashMap = new HashMap<>();

    //need to only add stations that are being acquired, ie isAcquired set to true
    // This provider IP address will be used in the future for validation, currently unused logically
    connManConfig.getCd11StationParameters()
      .forEach(cd11Param -> {
        if (cd11Param.isAcquired()) {
          this.addCd11Station(
            cd11StationHashMap,
            cd11Param.getStationName(),
            inetAddressMap.get(DATA_PROVIDER_KEY),
            inetAddressMap.get(DATA_MANAGER_ADDRESS_KEY),
            cd11Param.getPort());
        } else {
          // Check if we have added this station to the ignored map,
          // meaning we don't need to log for the nth time when stations attempt to connect
          if (!ignoredStationsHashMap.containsKey(cd11Param.getStationName())) {
            logger.info(
              "Station {} is configured to not be acquired, ignoring connection requests",
              cd11Param.getStationName());
            ignoredStationsHashMap.put(cd11Param.getStationName(), true);
          }
        }
      });

    ignoredStationsMap = ImmutableMap.copyOf(ignoredStationsHashMap);
    cd11StationsLookup = ImmutableMap.copyOf(cd11StationHashMap);

    logger.info("Ignored stations map size (HashMap/Immutable): {} - {}",
      ignoredStationsHashMap.size(), ignoredStationsMap.size());
    logger.info("Cd11 stations map size (HashMap/Immutable): {} - {}",
      cd11StationHashMap.size(), cd11StationsLookup.size());
  }

  /**
   * Initialize inet addresses using the Connman System Config
   */
  private void initializeInetAddresses() {
    // Acquire InetAddresses for critical components
    inetAddressMap = ImmutableMap.copyOf(Cd11ConnManUtil
      .buildAddressMap(systemConfig, DATA_MANAGER_ADDRESS_KEY,
        DATA_PROVIDER_KEY));
    logger.info("Resolved {} InetAddresses", inetAddressMap.size());
  }

  /**
   * Looks up the CD 1.1 Station, and returns connection information (or null if it does not
   * exist).
   *
   * @param stationName Name of the station.
   * @return Cd11Station info.
   */
  Cd11Station lookupCd11Station(String stationName) {
    return cd11StationsLookup.getOrDefault(stationName, null);
  }

  /**
   * Attempt to resolve configured service addresses from system config, retrying if system config
   * is not yet available.
   */
  private void tryInitializeInetAddresses() {
    final RetryPolicy<Object> retryPolicy = new RetryPolicy<>()
      .withBackoff(1, 60, ChronoUnit.SECONDS)
      .withMaxAttempts(15)
      .handle(List.of(IllegalStateException.class))
      .onFailedAttempt(e -> logger.warn(
        "Invalid state, necessary hosts may be unavailable: {}, checking again",
        e));
    Failsafe.with(retryPolicy).run(this::initializeInetAddresses);
  }

  protected ImmutableMap<String, Cd11Station> getCd11StationsLookup() {
    return ImmutableMap.copyOf(cd11StationsLookup);
  }

  protected ImmutableMap<String, InetAddress> getInetAddressMap() {
    return ImmutableMap.copyOf(inetAddressMap);
  }

  protected ImmutableMap<String, Boolean> getIgnoredStationsMap() {
    return ImmutableMap.copyOf(ignoredStationsMap);
  }

  public void shutdown() {
    handler.shutdown();
  }
}
