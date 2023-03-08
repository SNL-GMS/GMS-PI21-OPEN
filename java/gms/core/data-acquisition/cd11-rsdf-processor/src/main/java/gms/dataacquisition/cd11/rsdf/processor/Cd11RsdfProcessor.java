package gms.dataacquisition.cd11.rsdf.processor;

import com.google.common.annotations.VisibleForTesting;
import gms.core.dataacquisition.receiver.DataFrameReceiverConfiguration;
import gms.dataacquisition.stationreceiver.cd11.parser.Cd11StationSohExtractParser;
import gms.shared.frameworks.osd.coi.channel.soh.AcquiredChannelEnvironmentIssue;
import gms.shared.frameworks.osd.coi.datatransferobjects.CoiDeserializer;
import gms.shared.frameworks.osd.coi.datatransferobjects.CoiSerializer;
import gms.shared.frameworks.osd.coi.soh.AcquiredStationSohExtract;
import gms.shared.frameworks.osd.coi.waveforms.RawStationDataFrame;
import gms.shared.utilities.kafka.KafkaConfiguration;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverRecord;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;
import reactor.kafka.sender.SenderResult;

import java.util.Collection;
import java.util.stream.Collectors;
import net.logstash.logback.marker.Markers;

import static java.lang.String.format;
import static net.logstash.logback.marker.Markers.append;

/**
 * Processing class responsible for reading in {@link RawStationDataFrame} messages, parsing them into
 * {@link AcquiredStationSohExtract}, and publishing both this extract and its {@link
 * AcquiredChannelEnvironmentIssue}s to the appropriate topics.
 */
public class Cd11RsdfProcessor {

  private static final String STATION_LOGGING_KEY = "station";

  private static final Logger logger = LoggerFactory.getLogger(Cd11RsdfProcessor.class);

  private final Cd11StationSohExtractParser sohParser;
  private final KafkaReceiver<String, RawStationDataFrame> rsdfReceiver;
  private final KafkaSender<String, AcquiredStationSohExtract> sohExtractSender;
  private final KafkaSender<String, AcquiredChannelEnvironmentIssue<?>> aceiSender;
  private final String sohExtractTopic;
  private final String aceiTopic;

  private Cd11RsdfProcessor(Cd11StationSohExtractParser sohParser,
    KafkaReceiver<String, RawStationDataFrame> rsdfReceiver,
    KafkaSender<String, AcquiredStationSohExtract> sohExtractSender,
    KafkaSender<String, AcquiredChannelEnvironmentIssue<?>> aceiSender, String sohExtractTopic, String aceiTopic) {
    this.sohParser = sohParser;
    this.rsdfReceiver = rsdfReceiver;
    this.sohExtractSender = sohExtractSender;
    this.aceiSender = aceiSender;
    this.sohExtractTopic = sohExtractTopic;
    this.aceiTopic = aceiTopic;
  }

  @VisibleForTesting
  static Cd11RsdfProcessor create(Cd11StationSohExtractParser sohParser,
    KafkaReceiver<String, RawStationDataFrame> rsdfReceiver,
    KafkaSender<String, AcquiredStationSohExtract> sohExtractSender,
    KafkaSender<String, AcquiredChannelEnvironmentIssue<?>> aceiSender, String sohExtractTopic, String aceiTopic) {
    return new Cd11RsdfProcessor(sohParser, rsdfReceiver, sohExtractSender, aceiSender, sohExtractTopic, aceiTopic);
  }

  public static Cd11RsdfProcessor create(KafkaConfiguration kafkaConfig,
    DataFrameReceiverConfiguration frameReceiverConfig) {
    var sohParser = Cd11StationSohExtractParser.create(frameReceiverConfig);

    var rsdfReceiver = KafkaReceiver.create(
      kafkaConfig.getReceiverOptions(new CoiDeserializer<>(RawStationDataFrame.class), KafkaConfiguration.Topic.RSDF));

    var sohExtractSender = KafkaSender.create(
      kafkaConfig.getSenderOptions(new CoiSerializer<AcquiredStationSohExtract>()));

    var aceiSender = KafkaSender.create(
      kafkaConfig.getSenderOptions(new CoiSerializer<AcquiredChannelEnvironmentIssue<?>>()));

    var sohExtractTopic = kafkaConfig.getTopic(KafkaConfiguration.Topic.SOH_EXTRACT).orElseThrow(
      () -> new IllegalStateException("Error Creating RSDF Processor: No topic info found for SOH Extract"));
    var aceiTopic = kafkaConfig.getTopic(KafkaConfiguration.Topic.ACEI).orElseThrow(
      () -> new IllegalStateException("Error Creating RSDF Processor: No topic info found for ACEI"));

    return create(sohParser, rsdfReceiver, sohExtractSender, aceiSender, sohExtractTopic, aceiTopic);
  }

  /**
   * Generates the appropriate processing {@link Mono} that reads {@link RawStationDataFrame}s, parses the frames and
   * sends out {@link AcquiredStationSohExtract}s and {@link AcquiredChannelEnvironmentIssue}s
   *
   * @return Processing Mono encapsulating all read/process/write behavior
   */
  public Mono<Void> process() {
    return rsdfReceiver.receive()
      .publishOn(Schedulers.boundedElastic())
      .concatMap(rsdfRecord -> parseRecord(rsdfRecord)
      .onErrorResume(cause -> logParseError(rsdfRecord, cause))
      .flatMap(extract -> sendAll(rsdfRecord.key(), extract))
      .then(acknowledge(rsdfRecord)))
      .then();
  }

  private Mono<AcquiredStationSohExtract> parseRecord(ReceiverRecord<String, RawStationDataFrame> rsdf) {
    return Mono.fromCallable(() -> sohParser.parseStationSohExtract(rsdf.value()));

  }

  private static Mono<AcquiredStationSohExtract> logParseError(ReceiverRecord<String, RawStationDataFrame> rsdf, Throwable cause) {
    return Mono.fromRunnable(() -> logger.error(Markers.append(STATION_LOGGING_KEY, rsdf.key()),
      "Error parsing rsdf record {}:{}:{}. Dropping record.", rsdf.key(), rsdf.partition(), rsdf.offset(), cause))
      .then(Mono.empty());
  }

  private Mono<Void> sendAll(String station, AcquiredStationSohExtract sohExtract) {
    return sendSohExtract(station, sohExtract).and(sendAcei(sohExtract.getAcquiredChannelEnvironmentIssues()));
  }

  private Mono<Void> acknowledge(ReceiverRecord<String, RawStationDataFrame> receiverRecord) {
    return Mono.fromRunnable(receiverRecord.receiverOffset()::acknowledge);
  }

  private Mono<Void> sendSohExtract(String station, AcquiredStationSohExtract sohExtract) {
    String correlationMetadata = format("soh-extract-%s", buildSohExtractIdentifier(sohExtract));
    var sohExtractRecord = SenderRecord.create(new ProducerRecord<>(sohExtractTopic, station, sohExtract),
      correlationMetadata);

    return sohExtractSender.send(Mono.just(sohExtractRecord)).doOnNext(this::logResult).then();
  }

  private String buildSohExtractIdentifier(AcquiredStationSohExtract sohExtract) {
    return sohExtract.getAcquisitionMetadata().stream()
      .map(metadata -> format("%s@%s", metadata.getStationName(), metadata.getPayloadStartTime()))
      .collect(Collectors.joining(",", "[", "]"));
  }

  private Mono<Void> sendAcei(Collection<AcquiredChannelEnvironmentIssue<?>> aceis) {
    var aceiRecords = aceis.stream()
      .map(acei -> SenderRecord.create(
      new ProducerRecord<String, AcquiredChannelEnvironmentIssue<?>>(aceiTopic, buildAceiKey(acei), acei),
      buildAceiIdentifier(acei)));

    return aceiSender.send(Flux.fromStream(aceiRecords)).doOnNext(this::logResult).then();
  }

  private String buildAceiKey(AcquiredChannelEnvironmentIssue<?> acei) {
    return acei.getChannelName();
  }

  private String buildAceiIdentifier(AcquiredChannelEnvironmentIssue<?> acei) {
    return format("%s[%s]@%s", acei.getChannelName(), acei.getType(), acei.getStartTime());
  }

  private void logResult(SenderResult<String> result) {
    var metadata = result.recordMetadata();
    String corrdata = result.correlationMetadata();
    if (metadata != null && corrdata != null) {
      logger.debug("Successfully published result {} from rsdf {}", metadata, corrdata);
    }
  }
}
