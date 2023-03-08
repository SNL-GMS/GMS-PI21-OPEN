package gms.shared.frameworks.injector;

// import gms.shared.frameworks.systemconfig.SystemConfig;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.util.Properties;

public class KafkaConsumerFactory {

  public static KafkaConsumer<String, String> getConsumer(String clientId,
    String bootstrapServer,
    int retries,
    long retryBackoff) {
    // SystemConfig systemConfig = SystemConfig.create("mockProducer");
    var props = new Properties();
    props.put(ConsumerConfig.CLIENT_ID_CONFIG, clientId);
    props.put(ConsumerConfig.GROUP_ID_CONFIG, clientId);
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
    props.put(ConsumerConfig.RETRY_BACKOFF_MS_CONFIG, retryBackoff);
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
      StringDeserializer.class.getName());
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
      StringDeserializer.class.getName());
    // props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, config.getValue("kafka-consumer-session-timeout"));
    // props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, config.getValue("kafka-consumer-heartbeat-interval"));
    props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

    return new KafkaConsumer<>(props);
  }

}
