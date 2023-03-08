package gms.dataacquisition.stationreceiver.cd11.dataprovider;

import com.google.common.net.InetAddresses;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11ConnectionExchange;
import gms.dataacquisition.stationreceiver.cd11.dataprovider.rsdfsource.RsdfSource;
import gms.shared.frameworks.configuration.RetryConfig;
import gms.shared.frameworks.osd.coi.waveforms.RawStationDataFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

import java.time.Duration;

/**
 * Publisher in charge of multiplexing a feed of {@link RawStationDataFrame}s to a number of
 * ephemeral {@link Cd11Client}s that will publish data to data consumers
 */
public class Cd11FramePublisher {

  private static final Logger logger = LoggerFactory.getLogger(Cd11FramePublisher.class);

  private final Cd11ClientFactory cd11ClientFactory;
  private final String connManAddress;
  private final int connManPort;
  private final RetryConfig retryConfig;

  private Cd11FramePublisher(String connManAddress, int connManPort,
    Cd11ClientFactory cd11ClientFactory,
    RetryConfig retryConfig) {
    this.cd11ClientFactory = cd11ClientFactory;
    this.connManAddress = connManAddress;
    this.connManPort = connManPort;
    this.retryConfig = retryConfig;
  }

  /**
   * Constructs a {@link Cd11FramePublisher} given configuration, a source for data, and a {@link
   * Cd11ClientFactory}
   *
   * @param connManAddress address to the connection manager
   * @param connManPort port used to connect to the connection manager
   * @param cd11ClientFactory Factory used to construct {@link Cd11Client}s to handle connections to
   * data consumers
   * @return a {@link Cd11FramePublisher}
   */
  public static Cd11FramePublisher create(String connManAddress,
    int connManPort, Cd11ClientFactory cd11ClientFactory,
    RetryConfig retryConfig) {
    return new Cd11FramePublisher(connManAddress, connManPort, cd11ClientFactory, retryConfig);
  }

  /**
   * Initializes the consumption of {@link RawStationDataFrame}s from a source and subsequent
   * publishing to a data provider
   */
  public Mono<Void> publish(RsdfSource rsdfSource) {
    return rsdfSource.getRsdfFlux()
      .groupBy(rsdf -> rsdf.getMetadata().getStationName())
      .flatMap(gf ->
        requestConnection(gf.key())
          .flatMap(response -> connect(response, gf.key(), gf))
          .onErrorResume(e -> drain(gf.key(), gf, e))
          .subscribeOn(Schedulers.boundedElastic()))
      .then();
  }

  private Mono<Void> drain(String stationName, Flux<RawStationDataFrame> frameFlux, Throwable e) {
    logger.error(
      "Data Provider Failed to Connect for Station {}, halting connection requests and draining relevant flux",
      stationName, e);
    return frameFlux.then();
  }

  /**
   * Per CD1.1 protocol, negotiates a connection to the data manager by requesting connection
   * information from the connection manager.
   *
   * @param stationName Station requesting a data connection
   * @return The Mono representing this interaction, that can return the connection response, or
   * signal an error. This is a deferred mono, and will execute the connection request per
   * subscription.
   */
  public Mono<Cd11ConnectionExchange> requestConnection(String stationName) {
    return Mono.fromCallable(() -> cd11ClientFactory.tryRequestConnection(connManAddress, connManPort, stationName))
      .doOnNext(response -> logger
        .info("Connection information successfully requested for station {}. Response for {} says {}:{}",
          stationName, response.getStationOrResponderName(), InetAddresses.fromInteger(response.getIpAddress()), response.getPort()))
      .doOnError(e -> logger
        .warn("Failed to acquire connection information for station {}, retrying", stationName, e))
      .retryWhen(Retry.backoff(retryConfig.getMaxAttempts(),
          Duration.of(retryConfig.getInitialDelay(), retryConfig.getDelayUnits()))
        .scheduler(Schedulers.boundedElastic()));
  }

  public Mono<Void> connect(Cd11ConnectionExchange connectionResponse,
    String stationName, Flux<RawStationDataFrame> rsdfsForStation) {
    return cd11ClientFactory.createCd11Client(stationName, connectionResponse, rsdfsForStation)
      .connect();
  }
}
