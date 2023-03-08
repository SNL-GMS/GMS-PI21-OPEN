package gms.dataacquisition.stationreceiver.cd11.dataman.processors;

import gms.dataacquisition.stationreceiver.cd11.common.frames.MalformedFrame;
import gms.dataacquisition.stationreceiver.cd11.common.frames.PartialFrame;
import gms.dataacquisition.stationreceiver.cd11.dataman.DataManagerTestUtils;
import gms.dataacquisition.stationreceiver.cd11.dataman.TestResult;
import gms.shared.frameworks.osd.coi.waveforms.RawStationDataFrame;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;
import reactor.test.StepVerifier;
import reactor.util.function.Tuple2;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MalformedFrameKafkaProcessorTest {

  @Mock
  KafkaSender<String, MalformedFrame> mockSender;
  private MalformedFrameKafkaProcessor.Configuration config;
  private MalformedFrameKafkaProcessor processor;

  @BeforeEach
  void setUp() {
    config = new MalformedFrameKafkaProcessor.Configuration("test", 1);
    processor = new MalformedFrameKafkaProcessor(mockSender, config);
  }

  @Test
  void testProcess() {
    String stationName = "TEST";
    var malformedFrame = mockMalformed(stationName);

    given(mockSender.send(any())).willAnswer(
      context -> context.<Flux<SenderRecord<String, RawStationDataFrame, Tuple2<String, Long>>>>getArgument(0)
        .map(TestResult::success));

    StepVerifier.create(processor.process(Flux.just(malformedFrame)))
      .expectNext(new MalformedFrameKafkaProcessor.Result(stationName, true))
      .verifyComplete();

    verify(mockSender).send(any());
  }

  private static MalformedFrame mockMalformed(String stationName) {

    MalformedFrame.Builder malformedBuilder = MalformedFrame.builder()
      .setPartialFrame(PartialFrame.builder().build())
      .setCause(new IOException("Whoops"))
      .setBytes(new byte[]{})
      .setReadPosition(0);

    if (!stationName.isEmpty()) {
      malformedBuilder.setStation(stationName);
    }

    return malformedBuilder.build();
  }

  @Test
  void testProcessMissingStation() {
    var malformedFrame = mockMalformed("");

    given(mockSender.send(any())).willAnswer(
      context -> context.<Flux<SenderRecord<String, RawStationDataFrame, Tuple2<String, Long>>>>getArgument(0)
        .map(TestResult::success));

    StepVerifier.create(processor.process(Flux.just(malformedFrame)))
      .expectNext(new MalformedFrameKafkaProcessor.Result("EMPTY", true))
      .verifyComplete();

    verify(mockSender).send(any());
  }

  @Test
  void testProcessSendError() {
    String stationName = "TEST";
    var malformedFrame = mockMalformed(stationName);

    given(mockSender.send(any())).willAnswer(
      context -> context.<Flux<SenderRecord<String, RawStationDataFrame, Tuple2<String, Long>>>>getArgument(0)
        .map(record -> TestResult.error(record, new IOException())));

    StepVerifier.create(processor.process(Flux.just(malformedFrame)))
      .expectNext(new MalformedFrameKafkaProcessor.Result(stationName, false))
      .verifyComplete();

    verify(mockSender).send(any());
  }

  /**
   * This test sends enough data through the flux to trigger backpressure. Be aware any changes to the processing Flux
   * that introduce prefetches on the backpressure buffer will fail this test. Update bufferSize to include this
   * prefetch to get the test passing again.
   */
  @Test
  void testProcessBackpressureDropsOldest() {
    var malformedFrame = mockMalformed("");
    int bufferSize = config.getBufferSize();

    given(mockSender.send(any())).willAnswer(
      context -> context.<Flux<SenderRecord<String, RawStationDataFrame, Tuple2<String, Long>>>>getArgument(0)
        .map(TestResult::success));

    StepVerifier.create(processor.process(DataManagerTestUtils.mockUniqueMalformedFrames(malformedFrame, bufferSize + 1)), 0)
      .thenAwait()
      .thenRequest(bufferSize)
      .expectNextCount(bufferSize - 1)
      .expectNext(new MalformedFrameKafkaProcessor.Result(Integer.toString(bufferSize + 1), true))
      .verifyComplete();

    verify(mockSender).send(any());
  }
}