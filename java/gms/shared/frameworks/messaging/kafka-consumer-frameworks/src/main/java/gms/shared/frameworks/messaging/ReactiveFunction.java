package gms.shared.frameworks.messaging;

import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.function.Function;

/**
 * Functional Interface mirroring {@link Function}, but expected to produce a {@link Mono} of the output value
 * @param <T> Input type of function
 * @param <R> Output type of function
 */
@FunctionalInterface
public interface ReactiveFunction<T, R> {

  /**
   * Wraps a {@link Function} to run in a {@link Mono}
   * @param f Input Function
   * @return ReactiveFunction calling input function in a Mono
   * @param <T> Input type
   * @param <R> Output type
   */
  static <T, R> ReactiveFunction<T, R> wrap(Function<T, R> f) {
    return (T t) -> Mono.fromCallable(() -> f.apply(t));
  }

  /**
   * Apply the Reactive function to the input value
   * @param t Input value
   * @return Mono that will return the output value when subscribed to
   */
  Mono<R> apply(T t);

  /**
   * Compose this ReactiveFunction with another ReactiveFunction
   * @param before ReactiveFunction to run prior to this ReactiveFunction
   * @return ReactiveFunction composing both functions
   * @param <V> Input value type of composed function
   */
  default <V> ReactiveFunction<V, R> compose(ReactiveFunction<? super V, ? extends T> before) {
    Objects.requireNonNull(before);
    return (V v) -> before.apply(v).flatMap(this::apply);
  }

  /**
   * Apply input ReactiveFunction with the output of this ReactiveFunction
   * @param after ReactiveFunction to run after this ReactiveFunction
   * @return ReactiveFunction combining application of both functions
   * @param <V> Input value type of applied function
   */
  default <V> ReactiveFunction<T, V> andThen(ReactiveFunction<? super R, ? extends V> after) {
    Objects.requireNonNull(after);
    return (T t) -> apply(t).flatMap(after::apply);
  }

  /**
   * Returns a function that always returns a Mono of its input argument.
   *
   * @param <T> the type of the input and output objects to the function
   * @return a function that always returns a Mono of its input argument
   */
  static <T> ReactiveFunction<T, T> identity() {
    return Mono::just;
  }
}
