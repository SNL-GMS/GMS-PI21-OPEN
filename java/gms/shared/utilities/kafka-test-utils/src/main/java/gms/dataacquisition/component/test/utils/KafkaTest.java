package gms.dataacquisition.component.test.utils;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;
import org.junit.jupiter.api.Tag;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;

import java.util.Collections;
import java.util.Map;

@Testcontainers
@Tag("component")
@Tag("kafka")
public abstract class KafkaTest {
  @Container
  protected static final KafkaContainer container = new KafkaContainer(
    DockerImageName.parse("confluentinc/cp-kafka:6.2.1"));

  protected <V> KafkaSender<String, V> getSender(Serializer<V> valueSerializer) {
    SenderOptions<String, V> senderOptions = SenderOptions.<String, V>create(
        Map.of(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, getBootstrapServersString()))
      .withValueSerializer(valueSerializer);

    return KafkaSender.create(senderOptions);
  }

  protected <V> KafkaReceiver<String, V> getReceiver(String topic, Deserializer<V> valueDeserializer) {
    ReceiverOptions<String, V> receiverOptions = ReceiverOptions.<String, V>create(
        Map.of(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, getBootstrapServersString()))
      .subscription(Collections.singleton(topic))
      .withValueDeserializer(valueDeserializer);

    return KafkaReceiver.create(receiverOptions);
  }

  /**
   * Get the Kafka bootstrap server string e.g. localhost:53412
   *
   * @return the boostrap server connection string
   */
  protected String getBootstrapServersString() {
    return container.getBootstrapServers();
  }

}
