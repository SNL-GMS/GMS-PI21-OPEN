package gms.dataacquisition.stationreceiver.cd11.dataprovider.rsdfsource.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gms.dataacquisition.stationreceiver.cd11.dataprovider.configuration.KafkaRsdfSourceConfig;
import gms.dataacquisition.stationreceiver.cd11.dataprovider.rsdfsource.RsdfSource;
import gms.shared.frameworks.osd.coi.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.frameworks.osd.coi.waveforms.RawStationDataFrame;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of the {@link RsdfSource} interface that provides data via a Kafka topic
 */
public class KafkaRsdfSource implements RsdfSource {

  private static final Logger logger = LoggerFactory.getLogger(KafkaRsdfSource.class);

  private static final ObjectMapper mapper = CoiObjectMapperFactory.getJsonObjectMapper();

  private final KafkaReceiver<String, String> receiver;

  protected KafkaRsdfSource(KafkaReceiver<String, String> kafkaReceiver) {
    this.receiver = kafkaReceiver;
  }

  /**
   * @param config Configuration parameters specific to the consumption of the source topic
   * @param consumerId ID to give this {@link KafkaRsdfSource} as a consumer
   * @param bootstrapServers Comma-separated list of host:port mappings for all bootstrap servers
   * @return a {@link KafkaRsdfSource} to produce {@link RawStationDataFrame}s from a consumed topic
   */
  public static KafkaRsdfSource create(KafkaRsdfSourceConfig config, String consumerId,
    String bootstrapServers) {
    final ReceiverOptions<String, String> receiverOptions = receiverOptions(
      String.format("%s-%s", config.getApplicationId(), consumerId), config.getSourceTopic(),
      bootstrapServers, config.getAutoOffsetResetConfig().orElse("earliest"));
    final KafkaReceiver<String, String> kafkaReceiver = KafkaReceiver.create(receiverOptions);
    return new KafkaRsdfSource(kafkaReceiver);
  }

  @Override
  public Flux<RawStationDataFrame> getRsdfFlux() {
    logger.info("Getting RSDF flux");

    return receiver.receiveAtmostOnce()
      .map(rsdfRecord -> readData(rsdfRecord.value())
        .onErrorContinue(
          (e, val) -> logger
            .error("Error parsing data record {}, dropped from transaction", val, e)))
      .flatMap(Mono::flux)
      .doOnNext(r -> logger.debug("received data from kafka for station {}", r.getMetadata().getStationName()));
  }

  Mono<RawStationDataFrame> readData(String content) {
    try {
      return Mono.just(mapper.readValue(content, RawStationDataFrame.class));
    } catch (JsonProcessingException e) {
      return Mono.error(e);
    }
  }

  static ReceiverOptions<String, String> receiverOptions(String applicationId,
    String bootstrapServers) {
    Map<String, Object> props = new HashMap<>();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    props.put(ConsumerConfig.CLIENT_ID_CONFIG, applicationId);
    props.put(ConsumerConfig.GROUP_ID_CONFIG, applicationId);
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    props.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");
    return ReceiverOptions.create(props);
  }

  static ReceiverOptions<String, String> receiverOptions(String applicationId, String topic,
    String bootstrapServers, String autoOffsetResetConfig) {
    return receiverOptions(applicationId, bootstrapServers)
      .consumerProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetResetConfig)
      .commitBatchSize(10)
      .commitInterval(Duration.ofSeconds(15L))
      .subscription(List.of(topic));
  }
}
