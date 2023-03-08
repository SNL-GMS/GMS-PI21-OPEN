package gms.core.performancemonitoring.ssam.control.datapublisher;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;
import reactor.kafka.sender.SenderResult;

import java.util.function.Function;

import static java.util.Objects.requireNonNull;

/**
 * Create a kafka publisher that publishes items from an arbitrary Flux
 *
 * @param <T> Type of items in the Flux
 */
public class KafkaProducer<T> {

  private static final Logger logger = LoggerFactory.getLogger(KafkaProducer.class);

  private final Flux<T> dataFlux;
  private final String topic;
  private final Function<T, String> keyMapper;
  private final KafkaSender<String, T> kafkaSender;

  /**
   * Main constructor.
   *
   * @param dataFlux Flux that will publish the items
   * @param topic Kafka topic to publish to
   * @param keyMapper Mapping function from incoming data record to its kafka
   * message key
   * @param kafkaSender KafkaSender that publish the items to Kafka
   */
  public KafkaProducer(Flux<T> dataFlux, String topic, Function<T, String> keyMapper,
    KafkaSender<String, T> kafkaSender) {
    this.dataFlux = requireNonNull(dataFlux);
    this.topic = requireNonNull(topic);
    this.keyMapper = requireNonNull(keyMapper);
    this.kafkaSender = requireNonNull(kafkaSender);
  }

  private static <T> String getCorrelationMetadata(ProducerRecord<String, T> producerRecord) {
    return String.format("%s:%s:%s", producerRecord.topic(), producerRecord.key(),
      producerRecord.value().hashCode());
  }

  /**
   * Get a builder that will build an instance of KafkaProducer
   *
   * @return A builder for an instance of KafkaProducer
   */
  public static <T> Builder<T> builder() {
    return new Builder<>();
  }

  /**
   * Transforms the provided data flux to create kafka records and publish them
   * to the kafka broker.
   *
   * @return Flux of send results from the KafkaSender
   */
  public Flux<SenderResult<String>> sendMessages() {
    return dataFlux
      .map(data -> new ProducerRecord<>(topic, keyMapper.apply(data), data))
      .onErrorContinue((e, val) -> logger.error("Error generating kafka producer record, data dropped from flux", e))
      .map(producerRecord -> SenderRecord.create(producerRecord, getCorrelationMetadata(producerRecord)))
      .transform(kafkaSender::send)
      .doOnNext(senderResult -> logger.debug("Message {} sent", senderResult.correlationMetadata()))
      .doOnError(throwable -> logger.error("Failed to send message to topic {}", topic, throwable))
      .doOnComplete(() -> logger.debug("Sending messages to topic {} completed", topic));
  }

  public static class Builder<T> {

    private Flux<T> dataFlux;
    private String topic;
    private Function<T, String> keyMapper;
    private KafkaSender<String, T> kafkaSender;

    public Builder<T> setDataFlux(Flux<T> dataFlux) {
      this.dataFlux = dataFlux;
      return this;
    }

    public Builder<T> setTopic(String topic) {
      this.topic = topic;
      return this;
    }

    /**
     * Helper method for specifying that no key will be supplied with the kafka
     * message
     *
     * @return This Builder
     */
    public Builder<T> keyless() {
      return setKeyMapper(val -> null);
    }

    public Builder<T> setKeyMapper(Function<T, String> keyMapper) {
      this.keyMapper = keyMapper;
      return this;
    }

    public Builder<T> setKafkaSender(KafkaSender<String, T> kafkaSender) {
      this.kafkaSender = kafkaSender;
      return this;
    }

    public KafkaProducer<T> build() {
      return new KafkaProducer<>(dataFlux, topic, keyMapper, kafkaSender);
    }
  }
}
