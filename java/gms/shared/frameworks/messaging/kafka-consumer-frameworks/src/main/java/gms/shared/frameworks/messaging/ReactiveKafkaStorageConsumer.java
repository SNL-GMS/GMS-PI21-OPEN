package gms.shared.frameworks.messaging;

import gms.shared.frameworks.coi.exceptions.StorageUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.ReceiverRecord;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Collection;

/**
 * Class responsible for consuming batches of Kafka {@link ReceiverRecord}s, processing them into a store-able data
 * classes, storing them, and providing a post-store action on the records (e.g. offset acknowledge/commit). The three
 * stages of processing are all configurable and provided on construction of the consumer, allowing for simple construction
 * when the type of data and method for storing are well-known.
 *
 * @param <R> Value type of the Kafka record
 * @param <T> Value type of the data to store
 */
public class ReactiveKafkaStorageConsumer<R, T> {

  protected static final Logger logger = LoggerFactory.getLogger(ReactiveKafkaStorageConsumer.class);

  private final ReactiveFunction<Collection<ReceiverRecord<String, R>>, T> recordPreprocessor;
  private final ReactiveStorageRepository<T> valueRepository;
  private final ReactiveConsumer<Collection<ReceiverRecord<String, R>>> recordPostprocessor;

  public ReactiveKafkaStorageConsumer(ReactiveFunction<Collection<ReceiverRecord<String, R>>, T> recordPreprocessor,
    ReactiveStorageRepository<T> valueRepository,
    ReactiveConsumer<Collection<ReceiverRecord<String, R>>> recordPostprocessor) {
    this.recordPreprocessor = recordPreprocessor;
    this.valueRepository = valueRepository;
    this.recordPostprocessor = recordPostprocessor;
  }

  /**
   * Reactive method representing the storage of all data in the batchRecordFlux. Storage will occur when the returned
   * Mono is subscribed to.
   *
   * @param batchRecordFlux Flux of record batches to store. Infinite Fluxes are allowed.
   * @return Void Mono that will store all data when subscribed to.
   */
  public Mono<Void> store(Flux<? extends Collection<ReceiverRecord<String, R>>> batchRecordFlux) {
    return batchRecordFlux
      .concatMap(
        receiverRecords -> recordPreprocessor.apply(receiverRecords)
          .flatMap(value -> valueRepository.store(value, retryWhenUnavailable()))
          .then(recordPostprocessor.accept(receiverRecords)))
      .then();
  }

  private Retry retryWhenUnavailable() {
    return Retry.backoff(Long.MAX_VALUE, Duration.ofMillis(100))
      .maxBackoff(Duration.ofMillis(2000))
      .filter(StorageUnavailableException.class::isInstance)
      .transientErrors(true)
      .doAfterRetry(retry -> logger.warn("Store operation failed. Retrying...", retry.failure()));
  }

}
