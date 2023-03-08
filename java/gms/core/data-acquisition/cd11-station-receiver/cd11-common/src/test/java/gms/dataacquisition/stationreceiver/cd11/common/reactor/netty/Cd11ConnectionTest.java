package gms.dataacquisition.stationreceiver.cd11.common.reactor.netty;

import gms.dataacquisition.stationreceiver.cd11.common.Cd11FrameFactory;
import gms.dataacquisition.stationreceiver.cd11.common.Cd11OrMalformedFrame;
import gms.dataacquisition.stationreceiver.cd11.common.FrameParsingDecoder;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11Alert;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11Frame;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.netty.ByteBufFlux;
import reactor.netty.Connection;
import reactor.netty.NettyInbound;
import reactor.netty.NettyOutbound;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class Cd11ConnectionTest {
  @Mock
  NettyInbound inbound;

  @Mock
  NettyOutbound outbound;

  @Mock
  Sinks.Empty<Void> completionSink;

  @Captor
  ArgumentCaptor<Consumer<Connection>> connectionConsumerCaptor;
  @Captor
  ArgumentCaptor<Mono<byte[]>> frameSerializerCaptor;

  Cd11Connection cd11Connection;

  @BeforeEach
  void setUp() {
    given(inbound.withConnection(connectionConsumerCaptor.capture())).willReturn(inbound);

    cd11Connection = Cd11Connection.create(inbound, outbound, completionSink);

    validateHandlers();
  }

  private void validateHandlers() {
    var connection = Mockito.mock(Connection.class);
    given(connection.addHandlerFirst(any(FrameParsingDecoder.class))).willReturn(connection);
    connectionConsumerCaptor.getValue().accept(connection);
  }

  @Test
  void testSend() {
    given(outbound.sendByteArray(frameSerializerCaptor.capture())).willReturn(outbound);
    given(outbound.then()).willReturn(Mono.empty());

    var frame = Mockito.mock(Cd11Frame.class);

    StepVerifier.create(cd11Connection.send(frame))
      .verifyComplete();

    byte[] dummy = {};
    given(frame.toBytes()).willReturn(dummy);

    StepVerifier.create(frameSerializerCaptor.getValue())
      .expectNext(dummy)
      .verifyComplete();
  }

  @Test
  void testReceive() {
    var alert = Cd11Alert.create("testing alert");
    Cd11Frame validFrame = Cd11FrameFactory.createDefault().wrap(alert);
    TestPublisher<byte[]> frameSource = TestPublisher.create();

    given(inbound.receive()).willReturn(ByteBufFlux.fromInbound(frameSource));

    StepVerifier.create(cd11Connection.receive())
      .then(() -> frameSource.emit(validFrame.toBytes()))
      .expectNextMatches(cd11OrMalformedFrame ->
        cd11OrMalformedFrame.getKind().equals(Cd11OrMalformedFrame.Kind.CD11)
          && cd11OrMalformedFrame.cd11().equals(validFrame))
      .verifyComplete();
  }

  @Test
  void testClose() {
    when(completionSink.tryEmitEmpty())
      .thenReturn(Sinks.EmitResult.FAIL_NON_SERIALIZED)
      .thenReturn(Sinks.EmitResult.OK);
    assertDoesNotThrow(() -> {
      cd11Connection.close();
      cd11Connection.close();
      cd11Connection.close();
    });

    verify(completionSink, times(2)).tryEmitEmpty();
  }
}