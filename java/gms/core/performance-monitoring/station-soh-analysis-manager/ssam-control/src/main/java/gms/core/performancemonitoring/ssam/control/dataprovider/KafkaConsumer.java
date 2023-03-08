package gms.core.performancemonitoring.ssam.control.dataprovider;

import gms.shared.frameworks.osd.coi.datatransferobjects.CoiDeserializer;
import gms.shared.frameworks.systemconfig.SystemConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;
import gms.shared.utilities.reactor.EmitFailureHandlerUtility;

import java.util.Map;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.kafka.receiver.ReceiverRecord;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Collections.singleton;

/**
 * FluxSupplier that is able to create a Flux of the desired type from a Kafka
 * topic
 *
 * @param <T> The type of object that will be emitted from the flux
 */
public class KafkaConsumer<T> implements ReactiveConsumer<T> {

  private static final String APPLICATION_ID = "application-id";
  private static final String KAFKA_BOOTSTRAP_SERVERS = "kafka-bootstrap-servers";

  private final KafkaReceiver<String, T> receiver;
  private Sinks.Many<T> consumerSink;

  private KafkaConsumer(
    Class<T> clazz,
    String topicName,
    SystemConfig systemConfig) {
    checkArgument(!topicName.isBlank());

    this.receiver = KafkaReceiver
      .create(createReceiverOptions(checkNotNull(systemConfig), checkNotNull(topicName)).withValueDeserializer(new CoiDeserializer<>(clazz)));

    this.consumerSink = Sinks.many().multicast().onBackpressureBuffer(Integer.MAX_VALUE, false);
  }

  // Package private constructor for testing
  KafkaConsumer(
    KafkaReceiver<String, T> receiver
  ) {
    this.receiver = receiver;
    this.consumerSink = Sinks.many().multicast().onBackpressureBuffer(Integer.MAX_VALUE, false);
  }

  /**
   * Get a builder that builds a KafkaConsumer
   *
   * @param systemConfig system configuration
   *
   * @return A builder that builds a Kafka consumer
   */
  public static ReactiveConsumerBuilder getBuilder(
    SystemConfig systemConfig
  ) {

    return new ReactiveConsumerBuilder() {
      String topic;

      @Override
      public <T> ReactiveConsumer<T> build(Class<T> clazz) {
        return new KafkaConsumer<>(
          clazz,
          topic,
          systemConfig
        );
      }

      @Override
      public ReactiveConsumerBuilder withTopic(String topic) {
        this.topic = topic;
        return this;
      }
    };
  }

  private ReceiverOptions<String, T> createReceiverOptions(SystemConfig systemConfig,
    String topic) {
    return createReceiverOptions(systemConfig.getValue(KAFKA_BOOTSTRAP_SERVERS),
      systemConfig.getValue(APPLICATION_ID) + "-" + topic)
      .subscription(singleton(topic));
  }

  private ReceiverOptions<String, T> createReceiverOptions(String bootstrapServers,
    String applicationId) {
    Map<String, Object> props = Map.of(
      ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
      ConsumerConfig.GROUP_ID_CONFIG, applicationId,
      ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
      ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
      ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");

    return ReceiverOptions.<String, T>create(props);
  }

  /**
   * Get the flux that will contain items consumed from the topic
   *
   * @return Flux of data to consume
   */
  @Override
  public Flux<T> getFlux() {
    return this.consumerSink.asFlux();
  }

  @Override
  public Mono<Void> receive() {
    return this.receiver.receive()
      .publishOn(Schedulers.boundedElastic())
      .flatMap(this::parseRecord).then();
  }
   
  private Mono<Void> parseRecord(ReceiverRecord<String, T> r) {
    return Mono.fromRunnable(() -> 
      this.consumerSink.emitNext(r.value(), EmitFailureHandlerUtility.getInstance())
    ).doOnSuccess(ignored -> r.receiverOffset().acknowledge()).then();
  }

}
