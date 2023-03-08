package gms.dataacquisition.stationreceiver.cd11.dataprovider;


import gms.dataacquisition.stationreceiver.cd11.common.Cd11FrameFactory;
import gms.dataacquisition.stationreceiver.cd11.common.Cd11OrMalformedFrame;
import gms.dataacquisition.stationreceiver.cd11.common.FrameUtilities;
import gms.dataacquisition.stationreceiver.cd11.common.enums.FrameType;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11Alert;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11Data;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11Frame;
import gms.dataacquisition.stationreceiver.cd11.common.frames.CustomReset;
import gms.dataacquisition.stationreceiver.cd11.common.reactor.netty.Cd11Connection;
import gms.dataacquisition.stationreceiver.cd11.dataprovider.rsdfsource.ProviderUtils;
import gms.shared.frameworks.osd.coi.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.frameworks.osd.coi.waveforms.RawStationDataFrame;
import io.netty.buffer.ByteBuf;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.netty.ByteBufFlux;
import reactor.netty.NettyInbound;
import reactor.netty.NettyOutbound;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;


@ExtendWith(MockitoExtension.class)
class Cd11ClientTest {

  private static final String DUMMY_RSDF_LOCATION = "src/test/resources/simple-rsdfs/rsdf-BOSA.json";
  static final String STATION = "TEST";
  static final String HOST = "127.0.0.1";
  static final int PORT = 5643;

  static Cd11FrameFactory frameFactory;

  Sinks.Many<RawStationDataFrame> sendSink;
  Cd11Client cd11Client;
  Cd11Client.Handler handler;

  @Captor
  ArgumentCaptor<Flux<ByteBuf>> byteBufFluxCaptor;

  @Mock
  NettyInbound inbound;

  @Mock
  NettyOutbound outbound;


  @BeforeAll
  static void beforeAll() {
    frameFactory = Cd11FrameFactory.createDefault();
  }

  @BeforeEach
  void setUp() {
    sendSink = Sinks.many().unicast().onBackpressureError();
    cd11Client = Cd11Client.create(STATION, HOST, PORT, sendSink.asFlux());
    handler = cd11Client.getHandler();
  }

  @Test
  void testHandleAlert() {
    TestPublisher<byte[]> bytePublisher = TestPublisher.create();
    Cd11Frame alertFrame = frameFactory.wrap(Cd11Alert.create(("TEST")));
    byte[] alertFrameBytes = alertFrame.toBytes();

    given(inbound.withConnection(any())).willReturn(inbound);
    given(inbound.receive()).willReturn(ByteBufFlux.fromInbound(bytePublisher));

    StepVerifier.create(handler.handle(inbound, outbound, Flux.empty()))
      .then(() -> handler.setConnection(spy(handler.getConnection().orElseThrow())))
      .then(() -> bytePublisher.emit(alertFrameBytes))
      .verifyComplete();

    Cd11Connection spiedConnection = handler.getConnection().orElseThrow();
    verify(spiedConnection, never()).send(argThat(f -> FrameType.ALERT.equals(f.getType())));
    verify(spiedConnection).close();
    verifyNoMoreInteractions(spiedConnection);
  }

  @Test
  void testHandleUnsupportedFrame() {
    Cd11Frame frame = frameFactory.wrap(CustomReset.create(new byte[0]));
    Mono<Void> result = handler.handleFrame(Cd11OrMalformedFrame.ofCd11(frame));
    StepVerifier.create(result)
      .verifyComplete();
  }

  @Test
  void testSendAcknackPeriodically() {
    int expectedAcknacks = 10;
    Duration testAwait = Duration.ofSeconds(expectedAcknacks * Cd11Client.Handler.ACKNACK_TIME_SECONDS);

    StepVerifier.withVirtualTime(() -> handler.periodicAcknackFlux().take(10))
      .thenAwait(testAwait)
      .expectNextCount(10)
      .then(handler::shutdown)
      .verifyComplete();
  }

  @Test
  void testDataStreamSending() throws IOException {
    URL rsdfUrl = Paths.get(DUMMY_RSDF_LOCATION).toUri().toURL();
    RawStationDataFrame dummyRsdf = CoiObjectMapperFactory.getJsonObjectMapper()
      .readValue(rsdfUrl, RawStationDataFrame.class);

    Cd11Frame dummyFrame = ProviderUtils.readCd11Rsdf(dummyRsdf);
    int frameCount = 10;
    List<Cd11Frame> dummyFrames = Stream
      .iterate(dummyFrame, this::incrementSequenceNumber)
      .limit(frameCount)
      .collect(Collectors.toList());

    TestPublisher<RawStationDataFrame> dataFramePublisher = TestPublisher.create();
    RawStationDataFrame[] inputRsdfs = dummyFrames.stream()
      .map(Cd11Frame::toBytes)
      .map(frameBytes -> dummyRsdf.toBuilder().generatedId().setRawPayload(frameBytes)
        .build())
      .toArray(RawStationDataFrame[]::new);

    given(inbound.withConnection(any())).willReturn(inbound);
    given(inbound.receive()).willReturn(ByteBufFlux.fromInbound(Flux.never()));

    given(outbound.sendByteArray(any())).willReturn(outbound);
    given(outbound.then()).willReturn(Mono.empty());

    StepVerifier.create(handler.handle(inbound, outbound, dataFramePublisher.flux()))
      .then(() -> handler.setConnection(spy(handler.getConnection().orElseThrow())))
      .then(() -> dataFramePublisher.emit(inputRsdfs))
      .then(handler::shutdown)
      .verifyComplete();

    Cd11Connection spiedConnection = handler.getConnection().orElseThrow();
    for (Cd11Frame cd11Frame : dummyFrames) {
      verify(spiedConnection).send(cd11Frame);
    }

    assertEquals(2, handler.getGapList().getGapList().getTotalGaps());
    assertEquals(dummyFrames.get(0).getHeader().getSequenceNumber(), handler.getGapList().getLowestSequenceNumber());
    assertEquals(dummyFrames.get(dummyFrames.size() - 1).getHeader().getSequenceNumber(), handler.getGapList().getHighestSequenceNumber());
  }

  private Cd11Frame incrementSequenceNumber(Cd11Frame cd11Frame) {
    long sequenceNumber = cd11Frame.getHeader().getSequenceNumber();
    Cd11Data payload = FrameUtilities.asPayloadType(cd11Frame.getPayload(), FrameType.DATA);
    return frameFactory.wrap(payload, sequenceNumber + 1);
  }
}