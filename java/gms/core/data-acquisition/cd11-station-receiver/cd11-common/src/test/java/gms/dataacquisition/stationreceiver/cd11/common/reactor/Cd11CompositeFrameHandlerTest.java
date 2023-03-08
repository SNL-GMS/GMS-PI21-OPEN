package gms.dataacquisition.stationreceiver.cd11.common.reactor;

import gms.dataacquisition.stationreceiver.cd11.common.Cd11OrMalformedFrame;
import gms.dataacquisition.stationreceiver.cd11.common.enums.FrameType;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11Frame;
import gms.dataacquisition.stationreceiver.cd11.common.frames.MalformedFrame;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.BDDMockito.willReturn;

@ExtendWith(MockitoExtension.class)
class Cd11CompositeFrameHandlerTest {

  private Cd11CompositeFrameHandler handler;

  @BeforeEach
  void setUp() {
    handler = new Cd11CompositeFrameHandler();
  }

  @Test
  void testDifferentFrameTypesUseDifferentRegisteredHandlers() {
    Cd11Frame dataFrame = getMockCd11Frame(FrameType.DATA);
    Cd11Frame alertFrame = getMockCd11Frame(FrameType.ALERT);
    MalformedFrame malformedFrame = getMockMalformedFrame();

    Mono<Void> dataMono = Mono.fromRunnable(() -> {
    });
    handler.registerFrameHandler(FrameType.DATA, getMockFrameHandler(dataFrame, dataMono));

    Mono<Void> alertMono = Mono.fromRunnable(() -> {
    });
    handler.registerFrameHandler(FrameType.ALERT, getMockFrameHandler(alertFrame, alertMono));

    Mono<Void> malformedMono = Mono.empty();
    handler.registerMalformedFrameHandler(getMockMalformedHandler(malformedFrame, malformedMono));

    assertThat(handler.handle(Cd11OrMalformedFrame.ofCd11(dataFrame)))
      .isSameAs(dataMono);
    assertThat(handler.handle(Cd11OrMalformedFrame.ofCd11(alertFrame)))
      .isSameAs(alertMono);
    assertThat(handler.handle(Cd11OrMalformedFrame.ofMalformed(malformedFrame)))
      .isSameAs(malformedMono);
  }

  @Test
  void testConsumerRegistration() {
    Cd11Frame dataFrame = getMockCd11Frame(FrameType.DATA);
    MalformedFrame malformedFrame = getMockMalformedFrame();

    handler.registerFrameHandler(FrameType.DATA, input -> {
      assertThat(input).isSameAs(dataFrame);
    });

    handler.registerMalformedFrameHandler(input -> {
      assertThat(input).isSameAs(malformedFrame);
    });

    Mono<Void> dataMono = handler.handle(Cd11OrMalformedFrame.ofCd11(dataFrame));
    assertThat(dataMono).isNotNull();
    StepVerifier.create(dataMono).verifyComplete();

    Mono<Void> malformedMono = handler.handle(Cd11OrMalformedFrame.ofMalformed(malformedFrame));
    assertThat(malformedMono).isNotNull();
    StepVerifier.create(malformedMono).verifyComplete();
  }

  @Test
  void testHandleUnregisteredFrameDoesNothing() {
    Cd11Frame dataFrame = getMockCd11Frame(FrameType.DATA);
    MalformedFrame malformedFrame = getMockMalformedFrame();

    Mono<Void> doNothing;
    doNothing = assertDoesNotThrow(() -> handler.handle(Cd11OrMalformedFrame.ofCd11(dataFrame)));
    assertThat(doNothing).isNotNull();

    doNothing = assertDoesNotThrow(() -> handler.handle(Cd11OrMalformedFrame.ofMalformed(malformedFrame)));
    assertThat(doNothing).isNotNull();
  }

  private static Cd11Frame getMockCd11Frame(FrameType frameType) {
    Cd11Frame dummyDataFrame = Mockito.mock(Cd11Frame.class);
    willReturn(frameType).given(dummyDataFrame).getType();
    return dummyDataFrame;
  }

  private static MalformedFrame getMockMalformedFrame() {
    return Mockito.mock(MalformedFrame.class);
  }

  private static Cd11FrameHandler getMockFrameHandler(Cd11Frame frame, Mono<Void> handleMono) {
    return input -> {
      assertThat(input).isSameAs(frame);
      return handleMono;
    };
  }

  private static MalformedFrameHandler getMockMalformedHandler(MalformedFrame frame, Mono<Void> handleMono) {
    return input -> {
      assertThat(input).isSameAs(frame);
      return handleMono;
    };
  }
}