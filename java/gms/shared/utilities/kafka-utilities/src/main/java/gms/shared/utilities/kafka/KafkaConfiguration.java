package gms.shared.utilities.kafka;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableMap;
import gms.shared.frameworks.systemconfig.SystemConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.sender.SenderOptions;

import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.String.format;
import static java.util.Collections.singleton;
import static java.util.Map.entry;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@AutoValue
@JsonSerialize(as = KafkaConfiguration.class)
@JsonDeserialize(builder = AutoValue_KafkaConfiguration.Builder.class)
public abstract class KafkaConfiguration {

  public enum Topic {RSDF, MALFORMED, ACEI, SOH_EXTRACT}

  public abstract String getApplicationId();

  public abstract String getBootstrapServers();

  abstract ImmutableMap<Topic, String> getTopics();

  public Optional<String> getTopic(Topic type) {
    return Optional.ofNullable(getTopics().get(type));
  }

  public abstract String getKeySerializer();

  public abstract String getValueSerializer();

  public abstract int getConnectionRetryCount();

  public abstract int getNumberOfVerificationAttempts();

  public abstract long getRetryBackoffMs();

  public abstract String getAcks();

  public abstract int getRequestTimeout();

  public abstract int getDeliveryTimeout();

  public abstract int getSessionTimeout();

  public abstract int getMaxPollInterval();

  public abstract int getMaxPollRecords();

  public abstract boolean getAutoCommit();

  public abstract int getAutoCommitInterval();

  public abstract int getHeartbeatInterval();

  /**
   * Build the Kafka Reactor SenderOptions
   *
   * @param valueSerializer serializer for the data type T
   * @param <T> Type of the value
   * @return Options to create a KafkaSender
   */
  public <T> SenderOptions<String, T> getSenderOptions(Serializer<T> valueSerializer) {
    Map<String, Object> props = Map.of(
      ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, getBootstrapServers(),
      ProducerConfig.CLIENT_ID_CONFIG, getApplicationId(),
      ProducerConfig.ACKS_CONFIG, getAcks(),
      ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, getRequestTimeout(),
      ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, getDeliveryTimeout(),
      ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

    return SenderOptions.<String, T>create(props).withValueSerializer(valueSerializer);
  }

  public <T> ReceiverOptions<String, T> getReceiverOptions(Deserializer<T> valueDeserializer, Topic topic) {
    String topicValue = getTopic(topic).orElseThrow(
      () -> new IllegalArgumentException(
        format("Error creating Kafka Receiver Options: no topic found for %s", topic)));

    return getReceiverOptions(valueDeserializer, topicValue);
  }

  public <T> ReceiverOptions<String, T> getReceiverOptions(Deserializer<T> valueDeserializer, String topic) {
    return getReceiverOptions(valueDeserializer).subscription(singleton(topic));
  }

  <T> ReceiverOptions<String, T> getReceiverOptions(Deserializer<T> valueDeserializer) {
    Map<String, Object> props = Map.ofEntries(
      entry(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, getBootstrapServers()),
      entry(ConsumerConfig.CLIENT_ID_CONFIG, getApplicationId()),
      entry(ConsumerConfig.GROUP_ID_CONFIG, getApplicationId()),
      entry(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class),
      entry(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, getRequestTimeout()),
      entry(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, getSessionTimeout()),
      entry(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, getMaxPollInterval()),
      entry(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, getMaxPollRecords()),
      entry(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, getAutoCommit()),
      entry(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, getAutoCommitInterval()),
      entry(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, getHeartbeatInterval()),
      entry(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"));

    return ReceiverOptions.<String, T>create(props).withValueDeserializer(valueDeserializer);
  }


  public static Builder builder() {
    return new AutoValue_KafkaConfiguration.Builder();
  }

  public static KafkaConfiguration create(SystemConfig systemConfig) {
    return KafkaConfiguration.builder()
      .setApplicationId(systemConfig.getValue("application-id"))
      .setBootstrapServers(systemConfig.getValue("kafka-bootstrap-servers"))
      .putTopic(Topic.RSDF, systemConfig.getValue("kafka-rsdf-topic"))
      .putTopic(Topic.MALFORMED, systemConfig.getValue("kafka-malformed-topic"))
      .putTopic(Topic.ACEI, systemConfig.getValue("kafka-acei-topic"))
      .putTopic(Topic.SOH_EXTRACT, systemConfig.getValue("kafka-sohextract-topic"))
      .setKeySerializer(systemConfig.getValue("reactor-kafka-key-serializer"))
      .setValueSerializer(systemConfig.getValue("reactor-kafka-value-serializer"))
      .setNumberOfVerificationAttempts(systemConfig.getValueAsInt("verification-attempts"))
      .setConnectionRetryCount(systemConfig.getValueAsInt("connection-retry-count"))
      .setRetryBackoffMs(systemConfig.getValueAsLong("retry-backoff-ms"))
      .setAcks(systemConfig.getValue("reactor-kafka-sender-acks"))
      .setRequestTimeout(systemConfig.getValueAsInt("reactor-kafka-request-timeout"))
      .setDeliveryTimeout(systemConfig.getValueAsInt("reactor-kafka-sender-delivery-timeout"))
      .setSessionTimeout(systemConfig.getValueAsInt("reactor-kafka-consumer-session-timeout"))
      .setMaxPollInterval(systemConfig.getValueAsInt("reactor-kafka-consumer-max-poll-interval"))
      .setMaxPollRecords(systemConfig.getValueAsInt("reactor-kafka-consumer-max-poll-records"))
      .setAutoCommit(systemConfig.getValueAsBoolean("reactor-kafka-auto-commit"))
      .setAutoCommitInterval(systemConfig.getValueAsInt("reactor-kafka-auto-commit-interval"))
      .setHeartbeatInterval(systemConfig.getValueAsInt("reactor-kafka-consumer-heartbeat-interval"))
      .build();
  }

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public interface Builder {

    Builder setApplicationId(String applicationId);

    Builder setBootstrapServers(String bootstrapServers);

    ImmutableMap.Builder<Topic, String> topicsBuilder();

    default Builder putTopic(Topic topic, String value) {
      topicsBuilder().put(topic, value);
      return this;
    }

    Builder setKeySerializer(String keySerializer);

    Builder setValueSerializer(String valueSerializer);

    Builder setConnectionRetryCount(int retryCount);

    Builder setNumberOfVerificationAttempts(int numberOfVerificationAttempts);

    Builder setRetryBackoffMs(long retryBackoffMs);

    Builder setAcks(String acks);

    Builder setRequestTimeout(int requestTimeout);

    Builder setDeliveryTimeout(int deliveryTimeout);

    Builder setSessionTimeout(int sessionTimeout);

    Builder setMaxPollInterval(int maxPollInterval);

    Builder setMaxPollRecords(int maxPollRecords);

    Builder setAutoCommit(boolean autoCommit);

    Builder setAutoCommitInterval(int autoCommitInterval);

    Builder setHeartbeatInterval(int heartbeatInterval);

    KafkaConfiguration autoBuild();

    default KafkaConfiguration build() {
      KafkaConfiguration kafkaConfig = autoBuild();
      checkArgument(isNotEmpty(kafkaConfig.getApplicationId()),
        "ReactorKafkaConfiguration requires non-null, non-empty applicationId");
      checkArgument(isNotEmpty(kafkaConfig.getBootstrapServers()),
        "ReactorKafkaConfiguration requires non-null, non-empty bootstrapServers");

      checkArgument(isNotEmpty(kafkaConfig.getKeySerializer()),
        "ReactorKafkaConfiguration requires non-null, non-empty keySerializer");
      checkArgument(isNotEmpty(kafkaConfig.getValueSerializer()),
        "ReactorKafkaConfiguration requires non-null, non-empty valueSerializer");

      checkArgument(kafkaConfig.getNumberOfVerificationAttempts() >= 0,
        "ReactorKafkaConfiguration requires non-null, non-empty verificationAttempts");
      checkArgument(kafkaConfig.getConnectionRetryCount() >= 0,
        "ReactorKafkaConfiguration requires non-null, non-empty connectionRetryCount");
      checkArgument(kafkaConfig.getRetryBackoffMs() >= 0,
        "ReactorKafkaConfiguration requires non-null, non-empty retryBackoffMs");
      checkArgument(isNotEmpty(kafkaConfig.getAcks()),
        "ReactorKafkaConfiguration requires non-null, non-empty acks");

      checkArgument(kafkaConfig.getRequestTimeout() >= 0, "Kafka request timeout must be non-negative");
      checkArgument(kafkaConfig.getDeliveryTimeout() >= 0, "Kafka delivery timeout must be non-negative");
      checkArgument(kafkaConfig.getSessionTimeout() >= 0, "Kafka session timeout must be non-negative");
      checkArgument(kafkaConfig.getMaxPollInterval() >= 0,
        "ReactorKafkaConfiguration requires non-null, non-empty maxPollInterval");
      checkArgument(kafkaConfig.getMaxPollRecords() >= 0,
        "ReactorKafkaConfiguration requires non-null, non-empty maxPollRecords");
      checkArgument(kafkaConfig.getHeartbeatInterval() >= 0,
        "ReactorKafkaConfiguration requires non-null, non-empty heartbeatInterval");

      return kafkaConfig;
    }
  }

}
