package gms.dataacquisition.stationreceiver.cd11.dataman.processors;

import gms.dataacquisition.stationreceiver.cd11.common.gaps.Cd11GapList;
import gms.shared.frameworks.osd.coi.waveforms.RawStationDataFrame;
import net.logstash.logback.argument.StructuredArguments;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.BufferOverflowStrategy;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.function.TupleUtils;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;
import reactor.kafka.sender.SenderResult;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;
import reactor.util.retry.Retry;

import java.util.Map;
import java.util.Objects;

/**
 * Processor class responsible for consuming a flux of Sequenced RawStationDataFrames, which include their sequence number,
 * converting them to a kafka-acceptable record, sending the kafka record, recording the resulting sequence into
 * the gap list on a successful send, and providing the results of processing.
 */
public class SequencedRsdfKafkaProcessor {

  private static final String STATION_LOGGING_KEY = "station";
  public static final String SEQUENCE_LOGGING_KEY = "sequence";
  private static final Logger logger = LoggerFactory.getLogger(SequencedRsdfKafkaProcessor.class);

  private final KafkaSender<String, RawStationDataFrame> sender;
  private final Map<String, Cd11GapList> gapListByStation;
  private final Configuration config;

  public SequencedRsdfKafkaProcessor(KafkaSender<String, RawStationDataFrame> sender,
    Map<String, Cd11GapList> gapListByStation, Configuration config) {
    this.sender = sender;
    this.gapListByStation = gapListByStation;
    this.config = config;
  }

  /**
   * Processes incoming sequenced raw station data frames by sending them via kafka and updating the gap list
   *
   * @param sequencedRsdfs Flux of sequenced raw station data frames to processes. Can accept a non-terminating flux
   * @return Flux configured to process the input data frames and return the results, will execute on subscription
   */
  public Flux<Result> process(Flux<Tuple2<RawStationDataFrame, Long>> sequencedRsdfs) {
    return sequencedRsdfs
      .onBackpressureBuffer(config.getBufferSize(), TupleUtils.consumer(this::logBufferOverflow),
        BufferOverflowStrategy.DROP_OLDEST)
      .map(TupleUtils.function(this::createSenderRecord))
      .transform(sender::send)
      .map(Result::fromSenderResult)
      .concatMap(this::processSequenceNumber);
  }

  private SenderRecord<String, RawStationDataFrame, Tuple2<String, Long>> createSenderRecord(RawStationDataFrame rsdf,
    long sequenceNumber) {
    // create reactor kafka sender record with the rsdf json string
    String stationName = rsdf.getMetadata().getStationName();
    return SenderRecord.create(new ProducerRecord<>(config.getTopic(), stationName, rsdf),
      Tuples.of(stationName, sequenceNumber));
  }

  private void logBufferOverflow(RawStationDataFrame rsdf, long sequenceNumber) {
    logger.warn("Backpressure buffer overflow occurred, dropping oldest rsdf record {}#{}",
      StructuredArguments.value(STATION_LOGGING_KEY, rsdf.getMetadata().getStationName()),
      StructuredArguments.value(SEQUENCE_LOGGING_KEY, sequenceNumber));
  }

  private Mono<Result> processSequenceNumber(Result incomingResult) {
    String stationName = incomingResult.getStationName();
    long sequenceNumber = incomingResult.getSequenceNumber();

    if (!incomingResult.isRecordSent()) {
      logger.warn("Record {}:{} failed to send to kafka. Skipping processing", stationName, sequenceNumber);
      return Mono.just(incomingResult);
    }

    if (!gapListByStation.containsKey(stationName)) {
      logger.warn("Missing station {} from gaps list config", stationName);
      return Mono.just(incomingResult);
    }

    return gapListByStation.get(stationName)
      .processSequenceNumber(sequenceNumber)
      .retryWhen(config.getGapListRetry())
      .doOnError(e -> logger.warn("Error processing sequence number {}:{}. Skipping processing.",
        StructuredArguments.value(STATION_LOGGING_KEY, stationName), sequenceNumber, e))
      .onErrorReturn(false)
      .map(incomingResult::gapProcessed);
  }

  /**
   * Performs all graceful shutdown routines for the processor, including the closing of the kafka sender.
   */
  public void shutdown() {
    sender.close();
  }

  /**
   * Wrapper class for all configuration needed for the processor to execute
   */
  public static final class Configuration {
    private final String topic;
    private final int bufferSize;
    private final Retry gapListRetry;

    public Configuration(String topic, int bufferSize, Retry gapListRetry) {
      this.topic = topic;
      this.bufferSize = bufferSize;
      this.gapListRetry = gapListRetry;
    }

    public String getTopic() {
      return topic;
    }

    public int getBufferSize() {
      return bufferSize;
    }

    public Retry getGapListRetry() {
      return gapListRetry;
    }
  }

  /**
   * Class representing the result of a processing attempt on a single sequenced raw station data frame
   */
  public static final class Result {
    private final String stationName;
    private final long sequenceNumber;
    private final boolean recordSent;

    private final boolean gapProcessed;

    public Result(String stationName, long sequenceNumber, boolean recordSent, boolean gapProcessed) {
      this.stationName = stationName;
      this.sequenceNumber = sequenceNumber;
      this.recordSent = recordSent;
      this.gapProcessed = gapProcessed;
    }

    public static Result fromSenderResult(SenderResult<Tuple2<String, Long>> senderResult) {
      return new Result(senderResult.correlationMetadata().getT1(), senderResult.correlationMetadata().getT2(),
        senderResult.exception() == null, false);
    }

    public Result gapProcessed(boolean gapProcessed) {
      return new Result(this.stationName, this.sequenceNumber, this.recordSent, gapProcessed);
    }

    public String getStationName() {
      return stationName;
    }

    public long getSequenceNumber() {
      return sequenceNumber;
    }

    public boolean isRecordSent() {
      return recordSent;
    }

    public boolean isGapProcessed() {
      return gapProcessed;
    }

    @Override
    public String toString() {
      return "Result{" +
        "stationName='" + stationName + '\'' +
        ", sequenceNumber=" + sequenceNumber +
        ", recordSent=" + recordSent +
        ", gapProcessed=" + gapProcessed +
        '}';
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      var result = (Result) o;
      return sequenceNumber == result.sequenceNumber && recordSent == result.recordSent && gapProcessed == result.gapProcessed && stationName.equals(
        result.stationName);
    }

    @Override
    public int hashCode() {
      return Objects.hash(stationName, sequenceNumber, recordSent, gapProcessed);
    }

  }

}
