package gms.shared.frameworks.injector;

import reactor.core.publisher.SynchronousSink;

import java.util.concurrent.Callable;
import java.util.function.BiFunction;

/**
 * Implement this interface to provide mechanisms for stateful data generation.
 *
 * @param <T> the type of object emitted by the sink
 * @param <S> the type of custom state object
 */
public interface DataGeneratorState<T, S> {

  /**
   * Supply the initial custom state object
   *
   * @return an instance of the custom state
   */
  Callable<S> getStateSupplier();

  /**
   * Accepts the initial state and the {@link SynchronousSink} that emits some type and returns the
   * new state
   *
   * @return the aforementioned {@link BiFunction}
   */
  BiFunction<S, SynchronousSink<T>, S> getGenerator();

  /**
   * Perform some operation on the provided records e.g. store to the OSD, produce to Kafka
   *
   * @param records of type {@link Iterable}{@code <}T{@code >}
   */
  void runRecordConsumer(Iterable<T> records);
}
