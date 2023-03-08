package gms.dataacquisition.stationreceiver.cd11.dataman;

import gms.core.dataacquisition.receiver.DataFrameReceiverConfiguration;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11Header;
import gms.dataacquisition.stationreceiver.cd11.common.frames.MalformedFrame;
import gms.dataacquisition.stationreceiver.cd11.common.frames.PartialFrame;
import gms.dataacquisition.stationreceiver.cd11.dataman.configuration.DataManConfig;
import gms.dataacquisition.stationreceiver.cd11.dataman.processors.MalformedFrameKafkaProcessor;
import gms.dataacquisition.stationreceiver.cd11.dataman.processors.SequencedRsdfKafkaProcessor;
import gms.shared.frameworks.osd.coi.waveforms.RawStationDataFrame;
import gms.shared.utilities.kafka.KafkaConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;
import reactor.util.retry.Retry;

import java.io.IOException;
import java.time.Duration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class Cd11DataManagerTest {

  private static final Duration MAX_WAIT = Duration.ofSeconds(1);
  private static final long MAX_ATTEMPTS = 10;
  @Mock
  KafkaConfiguration mockKafkaConfiguration;

  @Mock
  DataFrameReceiverConfiguration mockDataFrameReceiverConfiguration;

  @Mock
  DataManConfig mockDataManConfig;

  @Mock
  SequencedRsdfKafkaProcessor mockRsdfProcessor;

  @Mock
  MalformedFrameKafkaProcessor mockMalformedProcessor;

  Cd11DataManager cd11DataManager;

  @BeforeEach
  void setUp() {
    given(mockDataManConfig.getRetryPolicy()).willReturn(Retry.backoff(MAX_ATTEMPTS, MAX_WAIT).maxBackoff(MAX_WAIT));
    cd11DataManager = Cd11DataManager.create(mockDataManConfig, mockDataFrameReceiverConfiguration,
      mockKafkaConfiguration);
  }

  @Test
  void testStartFrameSending() throws IOException {
    long sequenceNumber = 1L;
    var rsdf = DataManagerTestUtils.readRsdf();
    var malformedFrame = mockMalformed();

    Sinks.Many<Tuple2<RawStationDataFrame, Long>> rsdfSink = Sinks.many().multicast().onBackpressureBuffer();
    Sinks.Many<MalformedFrame> malformedSink = Sinks.many().multicast().onBackpressureBuffer();

    cd11DataManager.initialize(rsdfSink, malformedSink, mockRsdfProcessor, mockMalformedProcessor);

    given(mockRsdfProcessor.process(any())).willAnswer(
      context -> context.<Flux<Tuple2<RawStationDataFrame, Long>>>getArgument(0)
        .map(
          srsdf -> new SequencedRsdfKafkaProcessor.Result(srsdf.getT1().getMetadata().getStationName(), srsdf.getT2(),
            true, true)));
    given(mockMalformedProcessor.process(any())).willAnswer(
      context -> context.<Flux<MalformedFrame>>getArgument(0)
        .map(malformed -> new MalformedFrameKafkaProcessor.Result(malformed.getStation().orElse("EMPTY"), true)));

    StepVerifier.create(cd11DataManager.startFrameSending())
      .then(() -> rsdfSink.tryEmitNext(Tuples.of(rsdf, sequenceNumber)))
      .then(() -> malformedSink.tryEmitNext(malformedFrame))
      .then(rsdfSink::tryEmitComplete)
      .expectComplete()
      .verify(Duration.ofSeconds(5));

    verify(mockRsdfProcessor).process(any());
    verify(mockMalformedProcessor).process(any());
  }

  @Test
  void testStartFrameSendingNoSendsOrGaps() throws IOException {
    long sequenceNumber = 1L;
    var rsdf = DataManagerTestUtils.readRsdf();
    var malformedFrame = mockMalformed();

    Sinks.Many<Tuple2<RawStationDataFrame, Long>> rsdfSink = Sinks.many().multicast().onBackpressureBuffer();
    Sinks.Many<MalformedFrame> malformedSink = Sinks.many().multicast().onBackpressureBuffer();

    cd11DataManager.initialize(rsdfSink, malformedSink, mockRsdfProcessor, mockMalformedProcessor);

    given(mockRsdfProcessor.process(any())).willAnswer(
      context -> context.<Flux<Tuple2<RawStationDataFrame, Long>>>getArgument(0)
        .map(
          srsdf -> new SequencedRsdfKafkaProcessor.Result(srsdf.getT1().getMetadata().getStationName(), srsdf.getT2(),
            false, false)));
    given(mockMalformedProcessor.process(any())).willAnswer(
      context -> context.<Flux<MalformedFrame>>getArgument(0)
        .map(malformed -> new MalformedFrameKafkaProcessor.Result(malformed.getStation().orElse("EMPTY"), true)));

    StepVerifier.create(cd11DataManager.startFrameSending())
      .then(() -> rsdfSink.tryEmitNext(Tuples.of(rsdf, sequenceNumber)))
      .then(() -> malformedSink.tryEmitNext(malformedFrame))
      .then(rsdfSink::tryEmitComplete)
      .expectComplete()
      .verify(Duration.ofSeconds(5));

    verify(mockRsdfProcessor).process(any());
    verify(mockMalformedProcessor).process(any());
  }

  @Test
  void testStartFrameSendingRsdfErrorPropagates() {
    var malformedFrame = mockMalformed();

    Sinks.Many<Tuple2<RawStationDataFrame, Long>> rsdfSink = Sinks.many().multicast().onBackpressureBuffer(1, false);
    Sinks.Many<MalformedFrame> malformedSink = Sinks.many().multicast().onBackpressureBuffer(1, false);

    cd11DataManager.initialize(rsdfSink, malformedSink, mockRsdfProcessor, mockMalformedProcessor);

    given(mockRsdfProcessor.process(any())).willAnswer(
      context -> context.<Flux<Tuple2<RawStationDataFrame, Long>>>getArgument(0)
        .map(
          srsdf -> new SequencedRsdfKafkaProcessor.Result(srsdf.getT1().getMetadata().getStationName(), srsdf.getT2(),
            true, true)));
    given(mockMalformedProcessor.process(any())).willAnswer(
      context -> context.<Flux<MalformedFrame>>getArgument(0)
        .map(malformed -> new MalformedFrameKafkaProcessor.Result(malformed.getStation().orElse("EMPTY"), true)));

    StepVerifier.withVirtualTime(cd11DataManager::startFrameSending)
      .then(() -> malformedSink.tryEmitNext(malformedFrame))
      .then(() -> rsdfSink.tryEmitError(new IOException()))
      .thenAwait(MAX_WAIT.multipliedBy(MAX_ATTEMPTS))
      .expectError()
      .verify(Duration.ofSeconds(5));

    verify(mockRsdfProcessor).process(any());
    verify(mockMalformedProcessor).process(any());
  }

  @Test
  void testStartFrameSendingMalformedErrorPropagates() throws IOException {
    long sequenceNumber = 1L;
    var rsdf = DataManagerTestUtils.readRsdf();

    Sinks.Many<Tuple2<RawStationDataFrame, Long>> rsdfSink = Sinks.many().multicast().onBackpressureBuffer(1, false);
    Sinks.Many<MalformedFrame> malformedSink = Sinks.many().multicast().onBackpressureBuffer(1, false);

    cd11DataManager.initialize(rsdfSink, malformedSink, mockRsdfProcessor, mockMalformedProcessor);

    given(mockRsdfProcessor.process(any())).willAnswer(
      context -> context.<Flux<Tuple2<RawStationDataFrame, Long>>>getArgument(0)
        .map(
          srsdf -> new SequencedRsdfKafkaProcessor.Result(srsdf.getT1().getMetadata().getStationName(), srsdf.getT2(),
            true, true)));
    given(mockMalformedProcessor.process(any())).willAnswer(
      context -> context.<Flux<MalformedFrame>>getArgument(0)
        .map(malformed -> new MalformedFrameKafkaProcessor.Result(malformed.getStation().orElse("EMPTY"), true)));

    StepVerifier.withVirtualTime(cd11DataManager::startFrameSending)
      .then(() -> rsdfSink.tryEmitNext(Tuples.of(rsdf, sequenceNumber)))
      .then(() -> malformedSink.tryEmitError(new IOException()))
      .thenAwait(MAX_WAIT.multipliedBy(MAX_ATTEMPTS))
      .expectError()
      .verify(Duration.ofSeconds(5));

    verify(mockRsdfProcessor).process(any());
    verify(mockMalformedProcessor).process(any());
  }

  private MalformedFrame mockMalformed() {
    Cd11Header cd11Header = Mockito.mock(Cd11Header.class);
    Mockito.when(cd11Header.getFrameCreator()).thenReturn("the creator");

    return MalformedFrame.builder()
      .setPartialFrame(PartialFrame.builder().setHeader(cd11Header).build())
      .setStation(cd11Header.getFrameCreator())
      .setCause(new IOException("Whoops"))
      .setBytes(new byte[]{})
      .setReadPosition(0)
      .build();
  }

}