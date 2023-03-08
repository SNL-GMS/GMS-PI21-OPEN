package gms.core.performancemonitoring.ssam.control.dataprovider;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Provides a generalized interface for the different SSAM Control consumers
 *
 * @param <T>
 */
public interface ReactiveConsumer<T> {

  /**
   * Gets the data Flux of consumed records. Flux is only populated if receive()
   * is called a subscribed to
   *
   * @return the Flux<T> of records
   */
  Flux<T> getFlux();

  /**
   * Returns the Mono to then be subscribed to in order to start the
   * Reactive Consumer
   *
   * @return the Mono<Void> to subscribe to
   */
  Mono<Void> receive();

}
