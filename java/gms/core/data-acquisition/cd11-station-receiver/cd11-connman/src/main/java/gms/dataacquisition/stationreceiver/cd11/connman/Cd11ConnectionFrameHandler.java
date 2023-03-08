package gms.dataacquisition.stationreceiver.cd11.connman;


import com.google.common.annotations.VisibleForTesting;
import com.google.common.net.InetAddresses;
import gms.dataacquisition.stationreceiver.cd11.common.Cd11FrameFactory;
import gms.dataacquisition.stationreceiver.cd11.common.Cd11OrMalformedFrame;
import gms.dataacquisition.stationreceiver.cd11.common.Cd11Validator;
import gms.dataacquisition.stationreceiver.cd11.common.FrameUtilities;
import gms.dataacquisition.stationreceiver.cd11.common.configuration.Cd11ConnectionConfig;
import gms.dataacquisition.stationreceiver.cd11.common.enums.FrameType;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11ConnectionExchange;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11Frame;
import gms.dataacquisition.stationreceiver.cd11.common.reactor.netty.Cd11Connection;
import gms.dataacquisition.stationreceiver.cd11.connman.configuration.Cd11ConnManConfig;
import net.logstash.logback.argument.StructuredArguments;
import net.logstash.logback.marker.Markers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Sinks.Empty;
import reactor.core.scheduler.Schedulers;
import reactor.netty.NettyInbound;
import reactor.netty.NettyOutbound;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;


/**
 * Server listens to incoming messages in NIO fashion. Each message is checked to see if it conforms
 * to the expected payload and if it does, the server responds to the request to continue the
 * handshake
 */
public class Cd11ConnectionFrameHandler implements BiFunction<NettyInbound, NettyOutbound, Mono<Void>> {

  private static final Logger logger = LoggerFactory.getLogger(Cd11ConnectionFrameHandler.class);

  private static final int AUTHENTICATION_KEY_IDENTIFIER = 7;
  private static final String STATION = "station";
  static final Duration HEARTBEAT_DURATION = Duration.ofSeconds(120);

  Cd11ConnManConfig cd11ConnManConfig;
  Function<String, Cd11Station> cd11StationLookup;
  Map<String, Boolean> ignoredStationsMap;
  List<Cd11Connection> cd11Connections;

  Cd11FrameFactory cd11FrameFactory;

  public Cd11ConnectionFrameHandler(
    Cd11ConnManConfig cd11ConnManConfig,
    Function<String, Cd11Station> cd11StationLookup,
    Map<String, Boolean> ignoredStationsMap) {
    this.cd11ConnManConfig = cd11ConnManConfig;
    this.cd11StationLookup = cd11StationLookup;
    this.ignoredStationsMap = ignoredStationsMap;
    this.cd11Connections = new ArrayList<>();

    this.cd11FrameFactory = Cd11FrameFactory.create(AUTHENTICATION_KEY_IDENTIFIER, cd11ConnManConfig.getFrameCreator(),
      cd11ConnManConfig.getFrameDestination());
  }

  @Override
  public Mono<Void> apply(NettyInbound inbound, NettyOutbound outbound) {
    Empty<Void> completionSink = Sinks.empty();
    var cd11Connection = Cd11Connection.create(inbound, outbound, completionSink);
    cd11Connections.add(cd11Connection);

    Mono<Void> handleFrames = cd11Connection.receive()
      .timeout(HEARTBEAT_DURATION, handleTimeout())
      .publishOn(Schedulers.boundedElastic())
      .concatMap(frameOrMalformed -> {
        if (frameOrMalformed.getKind() == Cd11OrMalformedFrame.Kind.MALFORMED) {
          logger.warn("Dropping malformed frame due to read error", frameOrMalformed.malformed().getCause());
          return Mono.empty();
        } else {
          return Mono.just(frameOrMalformed.cd11());
        }
      })
      .flatMap(cd11Frame -> {
        if (!FrameUtilities.isValidCRC(cd11Frame.toBytes(), cd11Frame)) {
          logger.warn("CRC check failed for {} frame.",
            StructuredArguments.value(STATION, cd11Frame.getHeader().getFrameCreator()));
        }
        return makeResponseFrame(cd11Frame);
      })
      .flatMap(frame -> {
        var connectionExchange = (Cd11ConnectionExchange) frame.getPayload();
        logger.info("Sending a {} frame with port {} from {}...",
          connectionExchange.getStationOrResponderName(), connectionExchange.getPort(), System.identityHashCode(cd11Connection));
        return cd11Connection.send(frame);
      })
      .then(Mono.fromRunnable(cd11Connection::close)
        .map(v -> cd11Connections.remove(cd11Connection))
        .then());

    return Mono.firstWithSignal(completionSink.asMono(), handleFrames);
  }

  private Mono<Cd11Frame> makeResponseFrame(Cd11Frame cd11Frame) {
    logger.info("A request to the Connection Manager was received, beginning to process the request.");
    Cd11ConnectionConfig connectionConfig = cd11ConnManConfig.getConnectionConfig();

    if (cd11Frame.getType().equals(FrameType.CONNECTION_REQUEST)) {
      Cd11ConnectionExchange connectionRequest =
        FrameUtilities.asPayloadType(cd11Frame.getPayload(), FrameType.CONNECTION_REQUEST);
      if (ignoredStationsMap.containsKey(connectionRequest.getStationOrResponderName())) {
        logger.debug("Cd11 Request frame for station {} is being ignored.",
          StructuredArguments.value(STATION, connectionRequest.getStationOrResponderName()));
      } else {
        //As this station is NOT set to be ignored, begin processing the request
        return processConnectionRequestFrame(connectionRequest,
          cd11StationLookup,
          connectionConfig,
          cd11FrameFactory);
      }
    } else {
      logger.debug("Expected Cd11Frame of type {}, but received {}",
        FrameType.CONNECTION_REQUEST,
        cd11Frame.getType());
    }
    return Mono.empty();
  }

  private <T> Mono<T> handleTimeout() {
    return warn("Heartbeat Timeout {} reached.", HEARTBEAT_DURATION)
      .then(dispose());
  }

  Mono<Void> warn(String message, Object... arguments) {
    return Mono.fromRunnable(() -> logger.warn(message, arguments));
  }

  <T> Mono<T> dispose() {
    return Mono.fromRunnable(() -> {
      cd11Connections.forEach(Cd11Connection::close);
      cd11Connections.clear();
    });
  }

  @VisibleForTesting
    // Process the connection request frame from client and send response frame
  Mono<Cd11Frame> processConnectionRequestFrame(Cd11ConnectionExchange connectionRequest,
    Function<String, Cd11Station> cd11StationLookup,
    Cd11ConnectionConfig connectionConfig, Cd11FrameFactory cd11FrameFactory) {

    String stationName = connectionRequest.getStationOrResponderName();
    var stationNameMarker = Markers.append(STATION, stationName);
    logger.info("Received connection request from station {} at {}:{}", stationNameMarker,
      InetAddresses.fromInteger(connectionRequest.getIpAddress()), connectionRequest.getPort());

    var cd11StationResult = cd11StationLookup.apply(stationName);

    if (cd11StationResult == null) {
      logger.warn(stationNameMarker,
        "Connection request received from a station that has no active configuration; ignoring connection.");
    }
    return Mono.justOrEmpty(Optional.ofNullable(cd11StationResult)
      .map(cd11Station -> {
        var consumerAddressIp = InetAddresses.toAddrString(cd11Station.dataConsumerIpAddress);
        // Check that the request originates from the expected IP Address.
        // Send out the Connection Response Frame.
        logger.info(stationNameMarker, "Configured data consumer retrieved from cd11Station, resolved IP: {}",
          consumerAddressIp);

        logger.info(stationNameMarker, "Connection Request processed. Redirecting station to {}:{} ",
          cd11Station.dataConsumerIpAddress,
          cd11Station.dataConsumerPort);
        // Create the Cd11ConnectionResponseFrame with the frame factory
        Cd11ConnectionExchange connectionResponse = Cd11ConnectionExchange
          .withConfig(connectionConfig)
          .setStationOrResponderName(connectionRequest.getStationOrResponderName())
          .setStationOrResponderType(connectionRequest.getStationOrResponderType())
          .setIpAddress(Cd11Validator.validIpAddress(cd11Station.dataConsumerIpAddress))
          .setPort(cd11Station.dataConsumerPort)
          .setSecondIpAddress(0)
          .setSecondPort(0)
          .build();
        return cd11FrameFactory.wrapResponse(connectionResponse);
      }));
  }

  public void shutdown() {
    dispose().subscribe();
  }
}

