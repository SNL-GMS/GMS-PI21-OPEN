package gms.dataacquisition.stationreceiver.cd11.dataman.processors;

import com.google.common.base.Preconditions;
import gms.dataacquisition.stationreceiver.cd11.common.frames.MalformedFrame;
import net.logstash.logback.argument.StructuredArguments;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.BufferOverflowStrategy;
import reactor.core.publisher.Flux;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;
import reactor.kafka.sender.SenderResult;

import java.util.Objects;

/**
 * Processor class responsible for consuming a flux of malformed frames, converting them to a kafka-acceptable record,
 * sending the kafka record, and providing the results of processing
 */
public class MalformedFrameKafkaProcessor {

  private static final String STATION_LOGGING_KEY = "station";
  private static final Logger logger = LoggerFactory.getLogger(MalformedFrameKafkaProcessor.class);
  private final KafkaSender<String, MalformedFrame> sender;
  private final Configuration config;

  public MalformedFrameKafkaProcessor(KafkaSender<String, MalformedFrame> sender, Configuration config) {
    this.sender = sender;
    this.config = config;
  }

  /**
   * Processes incoming malformed frames by sending them via kafka
   *
   * @param malformedFrames Flux of malformed frames to processes. Can accept a non-terminating flux
   * @return Flux configured to process the input frames and return the results, will execute on subscription
   */
  public Flux<Result> process(Flux<MalformedFrame> malformedFrames) {
    return malformedFrames
      .onBackpressureBuffer(config.getBufferSize(), this::logBufferOverflow, BufferOverflowStrategy.DROP_OLDEST)
      .map(this::createSenderRecord)
      .transform(sender::send)
      .map(Result::fromSenderResult);
  }

  private SenderRecord<String, MalformedFrame, String> createSenderRecord(MalformedFrame malformedFrame) {
    String stationName = malformedFrame.getStation().orElse("EMPTY");
    return SenderRecord.create(new ProducerRecord<>(config.getTopic(), stationName, malformedFrame), stationName);
  }

  private void logBufferOverflow(MalformedFrame malformedFrame) {
    String stationName = malformedFrame.getStation().orElse("EMPTY");
    logger.warn("Backpressure buffer overflow occurred, dropping oldest malformed frame record from station {}",
      StructuredArguments.value(STATION_LOGGING_KEY, stationName));
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

    public Configuration(String topic, int bufferSize) {
      this.topic = topic;
      this.bufferSize = bufferSize;
    }

    public String getTopic() {
      return topic;
    }

    public int getBufferSize() {
      return bufferSize;
    }

  }

  /**
   * Class representing the result of a processing attempt on a single malformed frame
   */
  public static final class Result {
    private final String stationName;
    private final boolean recordSent;

    public Result(String stationName, boolean recordSent) {
      this.stationName = Preconditions.checkNotNull(stationName);
      this.recordSent = recordSent;
    }

    public static Result fromSenderResult(SenderResult<String> senderResult) {
      return new Result(senderResult.correlationMetadata(), senderResult.exception() == null);
    }

    public String getStationName() {
      return stationName;
    }

    public boolean isRecordSent() {
      return recordSent;
    }

    @Override
    public String toString() {
      return "Result{" +
        "stationName='" + stationName + '\'' +
        ", recordSent=" + recordSent +
        '}';
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      var result = (Result) o;
      return recordSent == result.recordSent && stationName.equals(result.stationName);
    }

    @Override
    public int hashCode() {
      return Objects.hash(stationName, recordSent);
    }
  }

}
