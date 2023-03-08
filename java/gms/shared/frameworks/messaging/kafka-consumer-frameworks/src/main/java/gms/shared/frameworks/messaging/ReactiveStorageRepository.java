package gms.shared.frameworks.messaging;

import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

/**
 * Interface for defining a Repository-type class that stores reactively. All reactive store calls are managed via
 * {@link Mono#fromRunnable(Runnable)}
 *
 * @param <T>
 */
@FunctionalInterface
public interface ReactiveStorageRepository<T> {

  /**
   * Provided method for storing a value reactively with retry
   *
   * @param value Value to store
   * @param retry Retry configuration
   * @return Void Mono that will store the input value when subscribed to, and retry the store call when appropriate
   */
  default Mono<Void> store(T value, Retry retry) {
    return store(value).retryWhen(retry);
  }

  /**
   * Provided method for storing a value reactively
   *
   * @param value Value to store
   * @return Void Mono that will store the input value when subscribed to
   */
  default Mono<Void> store(T value) {
    return Mono.fromRunnable(() -> storeInternal(value));
  }

  /**
   * Non-reactive method requiring implementation
   *
   * @param value Value to store
   */
  void storeInternal(T value);
}
