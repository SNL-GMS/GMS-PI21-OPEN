package gms.shared.reactor;

import reactor.core.publisher.Flux;
import reactor.core.publisher.GroupedFlux;

import java.util.function.Function;

/**
 * Base interface for retrieving fluxes
 *
 * @param <T>
 */
@FunctionalInterface
public interface FluxSupplier<T> {

  Flux<T> getFlux();

  default <K> Flux<GroupedFlux<K, T>> toGroupedFlux(Function<T, K> keyMapper) {
    return getFlux().groupBy(keyMapper);
  }

  default <K, V> Flux<GroupedFlux<K, V>> toGroupedFlux(Function<T, K> keyMapper,
    Function<T, V> valueMapper) {
    return getFlux().groupBy(keyMapper, valueMapper);
  }


}
