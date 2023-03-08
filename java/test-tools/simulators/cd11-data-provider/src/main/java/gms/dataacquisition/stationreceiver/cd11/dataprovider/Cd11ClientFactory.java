package gms.dataacquisition.stationreceiver.cd11.dataprovider;

import com.google.common.net.InetAddresses;
import gms.dataacquisition.stationreceiver.cd11.common.Cd11FrameFactory;
import gms.dataacquisition.stationreceiver.cd11.common.Cd11FrameReader;
import gms.dataacquisition.stationreceiver.cd11.common.Cd11OrMalformedFrame;
import gms.dataacquisition.stationreceiver.cd11.common.Cd11OrMalformedFrame.Kind;
import gms.dataacquisition.stationreceiver.cd11.common.FrameUtilities;
import gms.dataacquisition.stationreceiver.cd11.common.configuration.Cd11ConnectionConfig;
import gms.dataacquisition.stationreceiver.cd11.common.enums.FrameType;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11ConnectionExchange;
import gms.shared.frameworks.osd.coi.waveforms.RawStationDataFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;

/**
 * Factory class for creating {@link Cd11Client}s to be used to connect to data consumers
 */
public class Cd11ClientFactory {

  private static final Logger logger = LoggerFactory.getLogger(Cd11ClientFactory.class);

  private static final int CONN_REQUEST_ORIGIN_IP = InetAddresses
    .coerceToInteger(InetAddresses.forString("127.0.0.1"));
  private static final int CONN_REQUEST_ORIGIN_PORT = 32864;
  private static final int SOCKET_READ_TIMEOUT_MS = 500;

  private final Cd11FrameFactory connectionFrameFactory;
  private final Cd11ConnectionConfig connectionConfig;

  private Cd11ClientFactory(Cd11FrameFactory connectionFrameFactory, Cd11ConnectionConfig connectionConfig) {
    this.connectionFrameFactory = connectionFrameFactory;
    this.connectionConfig = connectionConfig;
  }

  /**
   * Creates a {@link Cd11Client} for use with connection to and sending of data to a data consumer
   *
   * @param frameCreator Name of creator of all frames to be sent
   * @param frameDestination Destination of all frames to be sent
   * @return A {@link Cd11Client} for use with connection to and sending of data to a data consumer
   */
  public static Cd11ClientFactory create(String frameCreator, String frameDestination) {
    var connectionFrameFactory = Cd11FrameFactory.createUnauthenticated(frameCreator, frameDestination);
    var connectionConfig = Cd11ConnectionConfig.builder()
      .setProtocolMajorVersion((short) 1)
      .setProtocolMinorVersion((short) 1)
      .setServiceType("TCP")
      .build();

    return new Cd11ClientFactory(connectionFrameFactory, connectionConfig);
  }

  /**
   * Creates a {@link Cd11Client} that will manage the transmission of data to the data manager.
   *
   * @param stationName Name of the station that this client will publish data for
   * @param connectionResponse The connection response frame containing necessary network
   * information to establish a connection to the data manager
   * @param rsdfFlux The Flux of data to transmit to the data manager
   * @return A {@link Cd11Client} that is configured to connect to the data manager
   */
  public Cd11Client createCd11Client(String stationName, Cd11ConnectionExchange connectionResponse,
    Flux<RawStationDataFrame> rsdfFlux) {
    logger.info("Creating Cd11Client for station {}", stationName);

    var host = InetAddresses.fromInteger(connectionResponse.getIpAddress()).toString();
    if (host.charAt(0) == '/') {
      host = host.substring(1);
    }
    return Cd11Client.create(stationName, host, connectionResponse.getPort(), rsdfFlux);
  }

  public Cd11ConnectionExchange tryRequestConnection(String host, int port, String stationName)
    throws IOException {
    logger.info("Requesting connection for station {}", stationName);
    Cd11ConnectionExchange connectionRequest = Cd11ConnectionExchange.withConfig(connectionConfig)
      .setStationOrResponderName(stationName)
      .setStationOrResponderType("IMS")
      .setIpAddress(CONN_REQUEST_ORIGIN_IP)
      .setPort(CONN_REQUEST_ORIGIN_PORT)
      .build();

    try (var clientSocket = getClientSocket(host, port)) {
      clientSocket.setSoTimeout(SOCKET_READ_TIMEOUT_MS);
      clientSocket.getOutputStream().write(connectionFrameFactory.wrapRequest(connectionRequest).toBytes());
      var responseBuffer = new byte[1024];
      var bytesRead = clientSocket.getInputStream().read(responseBuffer);
      if (bytesRead == -1) {
        throw new IOException(
          "Unexpectedly reached end of read stream while waiting for response frame");
      }

      Cd11OrMalformedFrame response = Cd11FrameReader.readFrame(ByteBuffer.wrap(responseBuffer));
      if (Kind.MALFORMED.equals(response.getKind())) {
        throw new IOException("Error reading Cd11 Connection Response Frame", response.malformed()
          .getCause());
      }

      return FrameUtilities
        .asPayloadType(response.cd11().getPayload(), FrameType.CONNECTION_RESPONSE);
    }
  }

  //Default access for testing purposes only
  Socket getClientSocket(String host, int port) throws IOException {
    return new Socket(host, port);
  }
}
