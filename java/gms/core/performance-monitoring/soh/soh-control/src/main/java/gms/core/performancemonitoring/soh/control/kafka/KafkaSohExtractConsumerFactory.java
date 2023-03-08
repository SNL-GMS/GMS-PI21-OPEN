package gms.core.performancemonitoring.soh.control.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gms.core.performancemonitoring.soh.control.api.StationSohMonitoringResultsFluxPair;
import gms.shared.frameworks.osd.coi.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.frameworks.osd.coi.soh.AcquiredStationSohExtract;
import gms.shared.frameworks.osd.coi.soh.CapabilitySohRollup;
import gms.shared.frameworks.osd.coi.soh.StationSoh;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * "Factory" for creating a consumer that consumes a pair of Set of AcquiredStationSohExtract
 * and collection of receiver offsets. The consumer is responsible for commiting the offsets,
 * once it has determined that its task has completed.
 */
public class KafkaSohExtractConsumerFactory {

  private static final Logger logger = LoggerFactory.getLogger(KafkaSohExtractConsumerFactory.class);

  /**
   * and interface to extend Consumer, just so we arent carrying the complex type around.
   */
  public interface SohExtractKafkaConsumer extends
    Consumer<Set<AcquiredStationSohExtract>> {
  }

  private final KafkaSender<String, String> kafkaSender;
  private final String stationSohOutputTopic;
  private final String capabilitySohRollupOutputTopic;

  private final Function<Set<AcquiredStationSohExtract>, StationSohMonitoringResultsFluxPair>
    resultsPublisher;

  private final ObjectMapper objectMapper;

  /**
   * Constructor which takes senders. This constructor is meant for testing with mock senders.
   *
   * @param kafkaSender The KafkaSender for StationSoh
   * @param stationSohOutputTopic the StationSoh Kafka output topic
   * @param capabilitySohRollupOutputTopic the CapabilitySohRollup kafka output topic
   * @param resultsPublisher The publisher that publishes computed state-of-health. Needs to be a Mono
   */
  public KafkaSohExtractConsumerFactory(
    KafkaSender<String, String> kafkaSender,
    String stationSohOutputTopic,
    String capabilitySohRollupOutputTopic,
    Function<Set<AcquiredStationSohExtract>, StationSohMonitoringResultsFluxPair> resultsPublisher) {

    this.objectMapper = CoiObjectMapperFactory.getJsonObjectMapper();

    this.kafkaSender = kafkaSender;
    this.resultsPublisher = resultsPublisher;
    this.stationSohOutputTopic = stationSohOutputTopic;
    this.capabilitySohRollupOutputTopic = capabilitySohRollupOutputTopic;

  }

  /**
   * Get a consumer which takes the tuple of SOH set and offsets, applies resultsPublisher to them,
   * sends them, and then commits the Soh extract offsets.
   *
   * @return SohExtractKafkaConsumer
   */
  public SohExtractKafkaConsumer getConsumer() {

    return asseSetAndOffsetsTuple -> {

      if (logger.isDebugEnabled()) {
        logger.debug("Handling {} extracts", asseSetAndOffsetsTuple.size());
      }

      var monitoringResults = resultsPublisher.apply(asseSetAndOffsetsTuple);

      sendMessages(
        StationSoh.class,
        monitoringResults.getStationSohPublisher(),
        kafkaSender,
        stationSohOutputTopic,
        "StationSoh",
        StationSoh::getStationName
      );


      sendMessages(
        CapabilitySohRollup.class,
        monitoringResults.getCapabilitySohRollupPublisher(),
        kafkaSender,
        capabilitySohRollupOutputTopic,
        "CapabilitySohRollup",
        capabilitySohRollup -> null
      );

    };
  }

  /**
   * Send dataFlux to a Kafka topic
   *
   * @param dataType Datatype of what is being sent
   * @param dataFlux Flux with data to send
   * @param sender KafkaSender to use
   * @param topic Topic to post to
   * @param messageType Message type, used for logging
   * @param partitionKeyResolver Function that resolves the kafka partition key
   * @param <T> type of dataFlux to send
   */
  private <T> void sendMessages(
    Class<T> dataType,
    Flux<T> dataFlux,
    KafkaSender<String, String> sender,
    String topic,
    String messageType,
    Function<T, String> partitionKeyResolver) {

    Flux<SenderRecord<String, String, Class<T>>> senderRecordFlux = dataFlux
      .map(item -> {

        try {
          return Optional.of(Pair.of(objectMapper.writeValueAsString(item), partitionKeyResolver.apply(item)));
        } catch (JsonProcessingException e) {
          Exceptions.propagate(e);
        }
        // Needs to be here for the sake of compilation, but should be unreachable unless
        // serialization to JSON throws something other than a JsonProcessingException.
        return Optional.<Pair<String, String>>empty();


      }).filter(Optional::isPresent)
      .map(Optional::get)
      .map(pair -> new ProducerRecord<>(topic, pair.getRight(), pair.getLeft()))
      .map(producerRecord -> SenderRecord.create(producerRecord, dataType));

    sender.send(senderRecordFlux)
      .subscribe(
        senderResult -> {
          // noop intended
        },
        throwable -> {
          if (logger.isErrorEnabled()) {
            logger.error("Sending messages of type {} messages failed",
              messageType, throwable);
          }
        },
        () -> {
          if (logger.isDebugEnabled()) {
            logger.debug("Sending of messages of type {} complete",
              messageType);
          }
        }
      );
  }
}
