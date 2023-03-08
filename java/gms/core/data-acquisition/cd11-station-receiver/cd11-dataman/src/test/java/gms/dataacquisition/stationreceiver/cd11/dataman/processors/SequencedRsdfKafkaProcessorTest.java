package gms.dataacquisition.stationreceiver.cd11.dataman.processors;

import gms.dataacquisition.stationreceiver.cd11.common.gaps.Cd11GapList;
import gms.dataacquisition.stationreceiver.cd11.dataman.DataManagerTestUtils;
import gms.dataacquisition.stationreceiver.cd11.dataman.TestResult;
import gms.shared.frameworks.osd.coi.waveforms.RawStationDataFrame;
import gms.shared.frameworks.osd.coi.waveforms.RawStationDataFrameMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;
import reactor.test.StepVerifier;
import reactor.util.concurrent.Queues;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;
import reactor.util.retry.Retry;

import java.io.IOException;
import java.util.Map;
import java.util.function.Function;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SequencedRsdfKafkaProcessorTest {

  @Mock
  KafkaSender<String, RawStationDataFrame> mockSender;
  @Mock
  Cd11GapList mockGapList;
  private RawStationDataFrame testRsdf;
  private SequencedRsdfKafkaProcessor.Configuration config;
  private SequencedRsdfKafkaProcessor processor;


  @BeforeEach
  void setUp() throws IOException {
    testRsdf = DataManagerTestUtils.readRsdf();

    config = new SequencedRsdfKafkaProcessor.Configuration("test", 1, Retry.max(0));
    processor = new SequencedRsdfKafkaProcessor(mockSender,
      Map.of(testRsdf.getMetadata().getStationName(), mockGapList), config);
  }

  @Test
  void testProcess() {
    long sequenceNumber = 1L;
    String stationName = testRsdf.getMetadata().getStationName();

    given(mockSender.send(any())).willAnswer(
      context -> context.<Flux<SenderRecord<String, RawStationDataFrame, Tuple2<String, Long>>>>getArgument(0)
        .map(TestResult::success));
    given(mockGapList.processSequenceNumber(sequenceNumber)).willReturn(Mono.just(true));

    StepVerifier.create(processor.process(Flux.just(Tuples.of(testRsdf, sequenceNumber))))
      .expectNext(new SequencedRsdfKafkaProcessor.Result(stationName, sequenceNumber, true, true))
      .verifyComplete();

    verify(mockSender).send(any());
    verify(mockGapList).processSequenceNumber(sequenceNumber);
  }

  @Test
  void testProcessMissingStationInGapList() {
    String missingStation = "MISSING";
    long sequenceNumber = 1L;

    RawStationDataFrameMetadata missingRsdfMetadata = testRsdf.getMetadata().toBuilder().setStationName(missingStation)
      .build();
    RawStationDataFrame missingRsdf = testRsdf.toBuilder().setMetadata(missingRsdfMetadata).build();


    given(mockSender.send(any())).willAnswer(
      context -> context.<Flux<SenderRecord<String, RawStationDataFrame, Tuple2<String, Long>>>>getArgument(0)
        .map(TestResult::success));

    StepVerifier.create(processor.process(Flux.just(Tuples.of(missingRsdf, sequenceNumber))))
      .expectNext(new SequencedRsdfKafkaProcessor.Result(missingStation, sequenceNumber, true, false))
      .verifyComplete();

    verify(mockSender).send(any());
    verify(mockGapList, never()).processSequenceNumber(sequenceNumber);
  }

  @Test
  void testProcessNoGap() {
    long sequenceNumber = 1L;
    String stationName = testRsdf.getMetadata().getStationName();

    given(mockSender.send(any())).willAnswer(
      context -> context.<Flux<SenderRecord<String, RawStationDataFrame, Tuple2<String, Long>>>>getArgument(0)
        .map(TestResult::success));
    given(mockGapList.processSequenceNumber(sequenceNumber)).willReturn(Mono.just(false));

    StepVerifier.create(processor.process(Flux.just(Tuples.of(testRsdf, sequenceNumber))))
      .expectNext(new SequencedRsdfKafkaProcessor.Result(stationName, sequenceNumber, true, false))
      .verifyComplete();

    verify(mockSender).send(any());
    verify(mockGapList).processSequenceNumber(sequenceNumber);
  }

  @Test
  void testProcessSendErrorDoesNotProcessGapList() {
    long sequenceNumber = 1L;
    String stationName = testRsdf.getMetadata().getStationName();

    given(mockSender.send(any())).willAnswer(
      context -> context.<Flux<SenderRecord<String, RawStationDataFrame, Tuple2<String, Long>>>>getArgument(0)
        .map(record -> TestResult.error(record, new IOException())));
    StepVerifier.create(processor.process(Flux.just(Tuples.of(testRsdf, sequenceNumber))))
      .expectNext(new SequencedRsdfKafkaProcessor.Result(stationName, sequenceNumber, false, false))
      .verifyComplete();

    verify(mockSender).send(any());
    verify(mockGapList, never()).processSequenceNumber(sequenceNumber);
  }

  @Test
  void testProcessGapListErrorDoesNotFail() throws IOException {
    RawStationDataFrame rsdf = DataManagerTestUtils.readRsdf();
    long sequenceNumber = 1L;
    String stationName = rsdf.getMetadata().getStationName();

    given(mockSender.send(any())).willAnswer(
      context -> context.<Flux<SenderRecord<String, RawStationDataFrame, Tuple2<String, Long>>>>getArgument(0)
        .map(TestResult::success));
    given(mockGapList.processSequenceNumber(sequenceNumber)).willReturn(Mono.error(IOException::new));

    StepVerifier.create(processor.process(Flux.just(Tuples.of(rsdf, sequenceNumber))))
      .expectNext(new SequencedRsdfKafkaProcessor.Result(stationName, sequenceNumber, true, false))
      .verifyComplete();

    verify(mockSender).send(any());
    verify(mockGapList).processSequenceNumber(sequenceNumber);
  }

  /**
   * This test assumes the default usage of {@link Flux#concatMap(Function)} and its default prefetch Queue,
   * and sends enough data through the flux to trigger backpressure
   *
   * @throws IOException If there are errors reading the input RSDF file
   */
  @Test
  void testProcessBackpressureDropsOldest() throws IOException {
    RawStationDataFrame rsdf = DataManagerTestUtils.readRsdf();
    String stationName = rsdf.getMetadata().getStationName();
    int bufferSize = config.getBufferSize();
    int maxBuffer = bufferSize + Queues.XS_BUFFER_SIZE;

    given(mockSender.send(any())).willAnswer(
      context -> context.<Flux<SenderRecord<String, RawStationDataFrame, Tuple2<String, Long>>>>getArgument(0)
        .map(TestResult::success));
    given(mockGapList.processSequenceNumber(anyLong())).willReturn(Mono.just(true));

    StepVerifier.create(processor.process(DataManagerTestUtils.mockSequencedRsdfs(rsdf, maxBuffer + 1)), 0)
      .thenAwait()
      .thenRequest(maxBuffer)
      .expectNextCount(maxBuffer - 1)
      .expectNext(new SequencedRsdfKafkaProcessor.Result(stationName, maxBuffer + 1, true, true))
      .verifyComplete();

    verify(mockSender).send(any());
    verify(mockGapList, times(maxBuffer)).processSequenceNumber(anyLong());
    verify(mockGapList, never()).processSequenceNumber(maxBuffer);
  }

}