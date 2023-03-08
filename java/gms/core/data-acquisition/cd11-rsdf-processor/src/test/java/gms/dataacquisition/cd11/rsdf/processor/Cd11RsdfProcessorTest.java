package gms.dataacquisition.cd11.rsdf.processor;

import gms.dataacquisition.cd11.rsdf.processor.util.GmsObjectUtility;
import gms.dataacquisition.stationreceiver.cd11.parser.Cd11StationSohExtractParser;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssue;
import gms.shared.frameworks.osd.coi.soh.AcquiredStationSohExtract;
import gms.shared.frameworks.osd.coi.waveforms.RawStationDataFrame;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOffset;
import reactor.kafka.receiver.ReceiverRecord;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;
import reactor.test.StepVerifier;

import java.io.IOException;

import static java.util.function.Predicate.isEqual;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class Cd11RsdfProcessorTest {

  private static final String BASE_PATH = "gms/data/";
  private static final String RSDF_PATH = BASE_PATH + "PLCA-RSDF.json";
  private static final String SOH_EXTRACT_PATH = BASE_PATH + "soh-extract.json";
  private static final String SOH_EXTRACT_TOPIC = "kafka-sohextract-topic";
  private static final String ACEI_TOPIC = "kafka-acei-topic";

  @Mock
  private Cd11StationSohExtractParser sohParser;

  @Mock
  private KafkaReceiver<String, RawStationDataFrame> rsdfReceiver;

  @Mock
  private KafkaSender<String, AcquiredStationSohExtract> sohExtractSender;

  @Captor
  private ArgumentCaptor<Publisher<SenderRecord<String, AcquiredStationSohExtract, String>>> sohExtractSendCaptor;

  @Mock
  private KafkaSender<String, AcquiredChannelEnvironmentIssue<?>> aceiSender;

  @Captor
  private ArgumentCaptor<Publisher<SenderRecord<String, AcquiredChannelEnvironmentIssue<?>, String>>> aceiSendCaptor;

  @Mock
  private ReceiverRecord<String, RawStationDataFrame> mockRecord;

  @Mock
  private ReceiverOffset mockOffset;

  private Cd11RsdfProcessor processor;

  @BeforeEach
  void setUp() {
    processor = Cd11RsdfProcessor.create(sohParser, rsdfReceiver, sohExtractSender, aceiSender, SOH_EXTRACT_TOPIC,
      ACEI_TOPIC);
  }

  @Test
  void testProcessSingleRsdf() throws IOException {
    RawStationDataFrame rsdf = GmsObjectUtility.getGmsObject(RSDF_PATH, RawStationDataFrame.class)
      .orElseThrow();

    AcquiredStationSohExtract extract = GmsObjectUtility.getGmsObject(SOH_EXTRACT_PATH, AcquiredStationSohExtract.class)
      .orElseThrow();

    given(mockRecord.value()).willReturn(rsdf);
    given(mockRecord.receiverOffset()).willReturn(mockOffset);
    given(rsdfReceiver.receive()).willReturn(Flux.just(mockRecord));

    given(sohParser.parseStationSohExtract(rsdf)).willReturn(extract);

    given(sohExtractSender.send(any())).willReturn(Flux.empty());
    given(aceiSender.send(any())).willReturn(Flux.empty());

    StepVerifier.create(processor.process())
      .verifyComplete();

    verify(sohExtractSender).send(sohExtractSendCaptor.capture());

    StepVerifier.create(sohExtractSendCaptor.getValue())
      .expectNextCount(1)
      .verifyComplete();

    verify(aceiSender).send(aceiSendCaptor.capture());

    StepVerifier.create(aceiSendCaptor.getValue())
      .expectNextCount(68)
      .verifyComplete();
  }

  @Test
  void testProcessSingleRsdfParserThrows() throws IOException {
    RawStationDataFrame rsdf = GmsObjectUtility.getGmsObject(RSDF_PATH, RawStationDataFrame.class)
      .orElseThrow();

    given(mockRecord.value()).willReturn(rsdf);
    given(mockRecord.receiverOffset()).willReturn(mockOffset);
    given(rsdfReceiver.receive()).willReturn(Flux.just(mockRecord));

    given(sohParser.parseStationSohExtract(rsdf)).willThrow(new IOException("KABOOM"));

    StepVerifier.create(processor.process())
      .verifyComplete();

    verify(sohExtractSender, never()).send(any());
    verify(aceiSender, never()).send(any());
  }

  @Test
  void testProcessReceiverError() {
    RuntimeException kafkaException = new RuntimeException("Kafka dun goofed");
    given(rsdfReceiver.receive()).willReturn(Flux.error(kafkaException));
    StepVerifier.create(processor.process())
      .expectErrorMatches(isEqual(kafkaException))
      .verify();
  }

}
