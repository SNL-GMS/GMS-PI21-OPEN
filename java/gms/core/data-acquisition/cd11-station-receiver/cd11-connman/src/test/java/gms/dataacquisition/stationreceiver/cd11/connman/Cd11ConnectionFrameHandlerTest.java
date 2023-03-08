package gms.dataacquisition.stationreceiver.cd11.connman;

import com.google.common.net.InetAddresses;
import gms.dataacquisition.stationreceiver.cd11.common.Cd11FrameFactory;
import gms.dataacquisition.stationreceiver.cd11.common.Cd11FrameReader;
import gms.dataacquisition.stationreceiver.cd11.common.FrameUtilities;
import gms.dataacquisition.stationreceiver.cd11.common.configuration.Cd11ConnectionConfig;
import gms.dataacquisition.stationreceiver.cd11.common.enums.FrameType;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11Alert;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11ConnectionExchange;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11Frame;
import gms.dataacquisition.stationreceiver.cd11.common.frames.PartialFrame;
import gms.dataacquisition.stationreceiver.cd11.common.reactor.netty.Cd11Connection;
import gms.dataacquisition.stationreceiver.cd11.connman.configuration.Cd11ConnManConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.netty.ByteBufFlux;
import reactor.netty.NettyInbound;
import reactor.netty.NettyOutbound;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class Cd11ConnectionFrameHandlerTest {

  private static final short version = 1;

  @Mock
  NettyInbound inbound;

  @Mock
  NettyOutbound outbound;

  @Mock
  Cd11ConnManConfig mockConfig;

  @Mock
  Cd11ConnectionConfig mockConnConfig;

  @Mock
  Mono<Void> mockMono;

  @Captor
  ArgumentCaptor<Publisher<? extends byte[]>> byteArrayCaptor;

  Cd11ConnectionFrameHandler handler;

  Map<String, Cd11Station> cd11StationsMap;
  Map<String, Boolean> ignoredStationsMap;
  List<PartialFrame> frameList;
  private TestPublisher<byte[]> testPublisher;

  @BeforeEach
  void setUp() {
    testPublisher = TestPublisher.create();
    frameList = new ArrayList<>();
    ignoredStationsMap = new HashMap<>();
    cd11StationsMap = new HashMap<>();

    given(mockConfig.getFrameCreator()).willReturn("testLoc");
    given(mockConfig.getFrameDestination()).willReturn("1");

    handler = spy(new Cd11ConnectionFrameHandler(mockConfig,
      stationName -> cd11StationsMap.getOrDefault(stationName, null), ignoredStationsMap));
  }

  @Test
  void testInstantShutdown() {
    assertDoesNotThrow(handler::shutdown);
  }

  @Test
  void testHandleConnectionRequest() {
    given(mockConfig.getConnectionConfig()).willReturn(mockConnConfig);

    var providerInetAddress = InetAddresses.forString("192.168.0.1");
    var testFrame = Cd11FrameFactory.createDefault()
      .wrapRequest(Cd11ConnectionExchange.builder()
        .setMajorVersion(version)
        .setMinorVersion(version)
        .setStationOrResponderName("TEST")
        .setStationOrResponderType("")
        .setServiceType("TCP")
        .setIpAddress(InetAddresses.coerceToInteger(InetAddresses.forString("192.168.0.1")))
        .setPort(8080).build());

    cd11StationsMap.put("TEST", new Cd11Station(providerInetAddress, InetAddresses.forString("127.0.0.1"), 8080));

    given(inbound.withConnection(any())).willReturn(inbound);
    given(inbound.receive()).willReturn(ByteBufFlux.fromInbound(testPublisher));

    given(outbound.sendByteArray(byteArrayCaptor.capture())).willReturn(outbound);
    given(outbound.then()).willReturn(mockMono);

    given(mockConfig.getConnectionConfig())
      .willReturn(Cd11ConnectionConfig.builder()
        .setServiceType("TCP")
        .setProtocolMajorVersion(version)
        .setProtocolMinorVersion(version)
        .build());

    StepVerifier.withVirtualTime(() -> handler.apply(inbound, outbound))
      .then(() -> testPublisher.emit(testFrame.toBytes()))
      .then(() -> handler.cd11Connections.forEach(Cd11Connection::close))
      .expectComplete()
      .verify(Duration.ofSeconds(10));

    Publisher<? extends byte[]> publisher = byteArrayCaptor.getValue();
    StepVerifier.create(publisher)
      .assertNext(byteArray -> {
        Cd11Frame responseFrame = Cd11FrameReader.readFrame(ByteBuffer.wrap(byteArray)).cd11();
        assertEquals(FrameType.CONNECTION_RESPONSE, responseFrame.getType());

        Cd11ConnectionExchange response = FrameUtilities.asPayloadType(responseFrame.getPayload(), FrameType.CONNECTION_RESPONSE);
        assertEquals(InetAddresses.coerceToInteger(InetAddresses.forString("127.0.0.1")), response.getIpAddress());
        assertEquals("TEST", response.getStationOrResponderName());
      })
      .verifyComplete();
  }

  @Test
  void testHandleConnectionRequestStationMissing() {
    given(mockConfig.getConnectionConfig()).willReturn(mockConnConfig);

    var testFrame = Cd11FrameFactory.createDefault()
      .wrapRequest(Cd11ConnectionExchange.builder()
        .setMajorVersion(version)
        .setMinorVersion(version)
        .setStationOrResponderName("MISS")
        .setStationOrResponderType("")
        .setServiceType("TCP")
        .setIpAddress(InetAddresses.coerceToInteger(InetAddresses.forString("192.168.0.1")))
        .setPort(8080).build());

    given(inbound.withConnection(any())).willReturn(inbound);
    given(inbound.receive()).willReturn(ByteBufFlux.fromInbound(testPublisher));

    StepVerifier.withVirtualTime(() -> handler.apply(inbound, outbound))
      .then(() -> testPublisher.emit(testFrame.toBytes()))
      .expectComplete()
      .verify(Duration.ofSeconds(10));

    verifyNoInteractions(outbound);
  }

  @Test
  void testHandleConnectionRequestStationIgnored() {
    given(mockConfig.getConnectionConfig()).willReturn(mockConnConfig);

    var testFrame = Cd11FrameFactory.createDefault()
      .wrapRequest(Cd11ConnectionExchange.builder()
        .setMajorVersion(version)
        .setMinorVersion(version)
        .setStationOrResponderName("IGNO")
        .setStationOrResponderType("")
        .setServiceType("TCP")
        .setIpAddress(InetAddresses.coerceToInteger(InetAddresses.forString("192.168.0.1")))
        .setPort(8080).build());

    ignoredStationsMap.put("IGNO", true);

    given(inbound.withConnection(any())).willReturn(inbound);
    given(inbound.receive()).willReturn(ByteBufFlux.fromInbound(testPublisher));

    StepVerifier.withVirtualTime(() -> handler.apply(inbound, outbound))
      .then(() -> testPublisher.emit(testFrame.toBytes()))
      .expectComplete()
      .verify(Duration.ofSeconds(10));

    verifyNoInteractions(outbound);
  }

  @Test
  void testHandleMalformedFrame() {
    ignoredStationsMap.put("IGNO", true);

    given(inbound.withConnection(any())).willReturn(inbound);
    given(inbound.receive()).willReturn(ByteBufFlux.fromInbound(testPublisher));

    StepVerifier.withVirtualTime(() -> handler.apply(inbound, outbound))
      .then(() -> testPublisher.emit("BAD".getBytes(StandardCharsets.UTF_8)))
      .expectComplete()
      .verify(Duration.ofSeconds(10));

    verifyNoInteractions(outbound);
  }

  @Test
  void testHandleWrongFrameType() {
    Cd11FrameFactory frameFactory = Cd11FrameFactory.createDefault();
    Cd11Frame alertFrame = frameFactory.wrap(Cd11Alert.create("TEST ALERT."));

    ignoredStationsMap.put("IGNO", true);

    given(inbound.withConnection(any())).willReturn(inbound);
    given(inbound.receive()).willReturn(ByteBufFlux.fromInbound(testPublisher));

    StepVerifier.withVirtualTime(() -> handler.apply(inbound, outbound))
      .then(() -> testPublisher.emit(alertFrame.toBytes()))
      .expectComplete()
      .verify(Duration.ofSeconds(10));

    verifyNoInteractions(outbound);
  }


}
