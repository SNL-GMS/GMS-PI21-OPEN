package gms.shared.utilities.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static java.util.Collections.singleton;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KafkaConfigurationTest {

  @Test
  void testGetSenderOptions() {
    var fixture = getDefaultFixture();
    StringSerializer valueSerializer = new StringSerializer();
    var senderOptions = fixture.getSenderOptions(valueSerializer);

    assertEquals(fixture.getBootstrapServers(), senderOptions.producerProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG));
    assertEquals(fixture.getApplicationId(), senderOptions.producerProperty(ProducerConfig.CLIENT_ID_CONFIG));
    assertEquals(fixture.getAcks(), senderOptions.producerProperty(ProducerConfig.ACKS_CONFIG));
    assertEquals(fixture.getRequestTimeout(), senderOptions.producerProperty(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG));
    assertEquals(fixture.getDeliveryTimeout(), senderOptions.producerProperty(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG));
    assertEquals(StringSerializer.class, senderOptions.producerProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG));
    assertEquals(valueSerializer, senderOptions.valueSerializer());
  }

  @Test
  void testGetReceiverOptions() {
    var fixture = getDefaultFixture();
    Optional<String> topic = fixture.getTopic(KafkaConfiguration.Topic.RSDF);
    assertTrue(topic.isPresent());

    StringDeserializer valueDeserializer = new StringDeserializer();
    var receiverOptions = fixture.getReceiverOptions(valueDeserializer, KafkaConfiguration.Topic.RSDF);

    assertEquals(fixture.getBootstrapServers(), receiverOptions.consumerProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG));
    assertEquals(fixture.getApplicationId(), receiverOptions.consumerProperty(ConsumerConfig.CLIENT_ID_CONFIG));
    assertEquals(fixture.getApplicationId(), receiverOptions.consumerProperty(ConsumerConfig.GROUP_ID_CONFIG));
    assertEquals(StringDeserializer.class, receiverOptions.consumerProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG));
    assertEquals(fixture.getRequestTimeout(), receiverOptions.consumerProperty(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG));
    assertEquals(fixture.getSessionTimeout(), receiverOptions.consumerProperty(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG));
    assertEquals(fixture.getMaxPollInterval(), receiverOptions.consumerProperty(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG));
    assertEquals(fixture.getMaxPollRecords(), receiverOptions.consumerProperty(ConsumerConfig.MAX_POLL_RECORDS_CONFIG));

    assertEquals(Boolean.toString(fixture.getAutoCommit()), receiverOptions.consumerProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG));

    assertEquals(fixture.getHeartbeatInterval(), receiverOptions.consumerProperty(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG));
    assertEquals("earliest", receiverOptions.consumerProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG));

    assertEquals(valueDeserializer, receiverOptions.valueDeserializer());
    assertEquals(singleton(topic.get()), receiverOptions.subscriptionTopics());
  }

  KafkaConfiguration getDefaultFixture() {
    return KafkaConfiguration.builder()
      .setApplicationId("application-id")
      .setBootstrapServers("kafka-bootstrap-servers")
      .putTopic(KafkaConfiguration.Topic.RSDF, "kafka-rsdf-topic")
      .putTopic(KafkaConfiguration.Topic.MALFORMED, "kafka-malformed-topic")
      .putTopic(KafkaConfiguration.Topic.ACEI, "kafka-acei-topic")
      .putTopic(KafkaConfiguration.Topic.SOH_EXTRACT, "kafka-sohextract-topic")
      .setKeySerializer("reactor-kafka-key-serializer")
      .setValueSerializer("reactor-kafka-value-serializer")
      .setNumberOfVerificationAttempts(1)
      .setConnectionRetryCount(2)
      .setRetryBackoffMs(3)
      .setAcks("reactor-kafka-sender-acks")
      .setRequestTimeout(5)
      .setDeliveryTimeout(6)
      .setSessionTimeout(7)
      .setMaxPollInterval(8)
      .setMaxPollRecords(9)
      .setAutoCommit(false)
      .setAutoCommitInterval(10)
      .setHeartbeatInterval(11)
      .build();
  }
}
