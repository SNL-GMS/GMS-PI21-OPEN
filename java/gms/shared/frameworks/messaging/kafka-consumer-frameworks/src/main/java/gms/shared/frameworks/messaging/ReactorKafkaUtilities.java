package gms.shared.frameworks.messaging;

import gms.shared.frameworks.systemconfig.SystemConfig;
import gms.shared.utilities.kafka.KafkaConfiguration;
import org.apache.kafka.common.serialization.Deserializer;
import org.slf4j.Logger;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.receiver.ReceiverRecord;
import reactor.util.retry.Retry;
import reactor.util.retry.RetryBackoffSpec;

import java.time.Duration;
import java.util.Collection;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Utilities class aiding in working with Reactive Kafka record processing.
 */
public class ReactorKafkaUtilities {

  private ReactorKafkaUtilities() {
  }

  /**
   * Uses Reactive Kafka to generate a flux of incoming {@link ReceiverRecord}s, batching them via a buffer configured
   * using the kafka polling configuration. The goal is for every inner batch collection to represent a single poll
   * to the kafka broker.
   *
   * @param systemConfig System configuration defining how to receive and batch kafka records
   * @param deserializer Kafka value deserializer to build a flux of the expected record value type
   * @param <T> Type of value in the record
   * @return Flux that batches incoming records
   */
  public static <T> Flux<List<ReceiverRecord<String, T>>> createBatchRecordFlux(SystemConfig systemConfig,
    Deserializer<T> deserializer) {
    String inputTopic = systemConfig.getValue("input-topic");
    var kafkaConfiguration = KafkaConfiguration.create(systemConfig);
    ReceiverOptions<String, T> receiverOptions = kafkaConfiguration.getReceiverOptions(deserializer, inputTopic)
      .schedulerSupplier(Schedulers::boundedElastic);

    return KafkaReceiver.create(receiverOptions).receive()
      .bufferTimeout(kafkaConfiguration.getMaxPollRecords(),
        Duration.ofMillis(kafkaConfiguration.getMaxPollInterval()).dividedBy(10));
  }

  /**
   * Generates a {@link Retry} spec that retries forever with a backoff.
   *
   * @param logger Logger to use when logging the retry
   * @return Retry spec that retries forever
   */
  public static RetryBackoffSpec retryForever(Logger logger) {
    return Retry.backoff(Long.MAX_VALUE, Duration.ofMillis(100))
      .maxBackoff(Duration.ofMillis(2000))
      .transientErrors(true)
      .doAfterRetry(
        retry -> logger.error("Error encountered while running kafka consumer, attempting re-subscription...",
          retry.failure()));
  }

  /**
   * Generates a {@link ReactiveFunction} that extracts and collects all values in a batch of {@link ReceiverRecord}s
   *
   * @param <T> Value type of the receiver record and returned collection of values
   * @return ReactiveFunction that extracts and collects all values in a batch of {@link ReceiverRecord}s
   */
  public static <T> ReactiveFunction<Collection<ReceiverRecord<String, T>>, Collection<T>> getValues() {
    return ReactiveFunction.wrap(ReactorKafkaUtilities::getValues);
  }

  /**
   * Extracts and collects all values in a batch of {@link ReceiverRecord}s
   *
   * @param receiverRecords Batch of records
   * @param <T> Type of value in the record
   * @return All values from the records
   */
  public static <T> List<T> getValues(Collection<ReceiverRecord<String, T>> receiverRecords) {
    return receiverRecords.stream().map(ReceiverRecord::value).collect(toList());
  }

  /**
   * Generates a {@link ReactiveConsumer} that acknowledges all input {@link ReceiverRecord}s from a batch
   *
   * @param <T> Type of value in the record
   * @return ReactiveConsumer that acknowledges all records
   */
  public static <T> ReactiveConsumer<Collection<ReceiverRecord<String, T>>> acknowledgeAll() {
    return ReactiveConsumer.wrap(ReactorKafkaUtilities::acknowledgeAll);
  }

  /**
   * Acknowledges all input {@link ReceiverRecord}s from a batch
   *
   * @param receiverRecords records to acknowledge
   * @param <T> Type of value in the record
   */
  public static <T> void acknowledgeAll(Collection<ReceiverRecord<String, T>> receiverRecords) {
    receiverRecords.forEach(r -> r.receiverOffset().acknowledge());
  }

}
