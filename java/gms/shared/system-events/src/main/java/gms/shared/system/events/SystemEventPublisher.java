package gms.shared.system.events;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.annotations.VisibleForTesting;
import gms.shared.frameworks.systemconfig.SystemConfig;
import gms.shared.utilities.javautilities.objectmapper.ObjectMapperFactory;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Properties;

/**
 * Responsible for publishing {@link SystemEvent}s to Kafka
 */
@Component
public class SystemEventPublisher {

  static final String KAFKA_BOOTSTRAP_SERVERS = "kafka-bootstrap-servers";
  public static final String SYSTEM_EVENT_TOPIC = "system-event";


  private static final Logger logger = LoggerFactory.getLogger(SystemEventPublisher.class);

  private final KafkaProducer<String, String> kafkaSender;

  @Autowired
  private SystemEventPublisher(SystemConfig systemConfig) {
    this.kafkaSender = new KafkaProducer<>(senderProperties(systemConfig));
  }

  @VisibleForTesting
  public SystemEventPublisher(KafkaProducer<String, String> kafkaSender) {
    this.kafkaSender = kafkaSender;
  }

  /**
   * Will publish a single {@link SystemEvent}
   *
   * @param systemEvent the event to publish
   */
  public void sendSystemEvent(SystemEvent systemEvent) {
    try {
      var message = ObjectMapperFactory.getJsonObjectMapper().writeValueAsString(systemEvent);
      var producerRecord = new ProducerRecord<String, String>(SYSTEM_EVENT_TOPIC, message);
      kafkaSender.send(producerRecord);
    } catch (JsonProcessingException e) {
      logger.error("Could not send SystemEvents", e);
    }

  }

  /**
   * Will publish a list of events one at a time
   *
   * @param systemEvents the system events to publish
   */
  public void sendSystemEvent(List<SystemEvent> systemEvents) {
    systemEvents.forEach(this::sendSystemEvent);
  }

  private static Properties senderProperties(SystemConfig systemConfig) {
    var properties = new Properties();
    properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
      systemConfig.getValue(KAFKA_BOOTSTRAP_SERVERS));
    properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    properties
      .put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    // By default, a producer doesn't wait for an acknowledgement from kafka when it sends
    // a message to a topic. Setting it to "1" means that it will wait for at least one kafka
    // node to acknowledge. The safest is "all", but that makes sending a little slower.
    properties.put(ProducerConfig.ACKS_CONFIG, "1");
    return properties;
  }
}
