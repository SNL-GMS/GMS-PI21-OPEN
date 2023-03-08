package gms.shared.frameworks.messaging;

import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Functional Interface mirroring {@link Consumer}, but expected to produce a Void {@link Mono} that accepts the data
 * when subscribed to
 *
 * @param <T> Input type of consumer
 */
@FunctionalInterface
public interface ReactiveConsumer<T> {

  /**
   * Wraps a Consumer, running its accept method in a Void Mono
   * @param c Consumer to wrap
   * @return ReactiveConsumer wrapping the input
   * @param <T> Type of accepted value
   */
  static <T> ReactiveConsumer<T> wrap(Consumer<T> c) {
    return t -> Mono.fromRunnable(() -> c.accept(t));
  }

  /**
   * Accepts the input value reactively
   * @param t Type of accepted value
   * @return Mono that accepts the input value when subscribed to
   */
  Mono<Void> accept(T t);

  /**
   * Combines this ReactiveConsumer with an input ReactiveConsumer, accepting the same input data, but runs the input
   * acceptance after this acceptance.
   * @param after ReactiveConsumer to run after this ReactiveConsumer
   * @return a ReactiveConsumer representing acceptance of the input data by both consumers
   */
  default ReactiveConsumer<T> andThen(ReactiveConsumer<T> after) {
    Objects.requireNonNull(after);
    return t -> accept(t).then(after.accept(t));
  }

  /**
   * Helper function for providing a ReactiveConsumer that does nothing. Useful when Classes require a ReactiveConsumer
   * as input, but no processing is necessary.
   * @return a ReactiveConsumer that does nothing
   * @param <T> Type of the accepted input value
   */
  static <T> ReactiveConsumer<T> doNothing() {
    return t -> Mono.empty();
  }
}
