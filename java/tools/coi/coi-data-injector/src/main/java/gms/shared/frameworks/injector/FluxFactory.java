package gms.shared.frameworks.injector;

import reactor.core.publisher.Flux;
import reactor.core.publisher.SynchronousSink;
import reactor.core.scheduler.Scheduler;

import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Utility class containing convenience methods for creating commonly used {@link Flux}es
 */
public class FluxFactory {

  private FluxFactory() {
  }

  // In the meantime, re-purpose or augment DataInjectorArguments as necessary.
  // We will probably use InjectableType instead of GenerationType.
  public static <T> Flux<T> createBoundedFlux(int batchCount,
    long initialDelayMs,
    Duration interval,
    int batchSize,
    Supplier<T> supplier,
    Modifier modifier,
    Consumer<Iterable<T>> consumer,
    Consumer<? super Throwable> onError) {
    return createBoundedFluxFromIterable(batchCount, initialDelayMs, interval, batchSize,
      () -> Collections.singletonList(supplier.get()), (Modifier<Iterable<T>>) modifier, consumer,
      onError);
  }

  public static <T> Flux<T> createBoundedFluxFromIterable(int batchCount,
    long initialDelayMs,
    Duration interval,
    int batchSize,
    Supplier<Iterable<T>> supplier,
    Modifier<Iterable<T>> modifier,
    Consumer<Iterable<T>> consumer,
    Consumer<? super Throwable> onError) {

    int totalCount;

    totalCount = batchCount * batchSize;
    // Flux.range emits a range of integers from 1 to totalCount
    // provided interval. We use the provided interval divided by the batch size so that we can
    // later collect them up, which takes numToEmit * interval/numToEmit time, thus producing the
    // desired interval. The sequence completes immediately after the last value
    // {@code (start + count - 1)} has been reached.
    return Flux.range(1, totalCount)
      // We then add an initial delay so nothing is emitted until after than time has
      // finished, on the
      .delaySequence(Duration.ofMillis(initialDelayMs))
      // Then delay each element by the total interval / batch size (so that when they get
      // reassembled into a batch later
      // Convert the integers int longs so that it can match up with the other flux
      .map(Integer::longValue)
      // The supplier: convert the interval value into an object of the type being emitted,
      // using the json file to provide the initial object
      //flux.map(val -> readValue(mapper, arguments.getBase(), arguments.getType().getBaseClass()))
      .map(l -> supplier.get())
      // Collect into a batch of numToEmit items (possibly 1) that will be put on the topic
      // Since this batches
      // by number of emits, it will force the data to be batched on the desired interval
      // The modifier: take the basic object and tweaks values so not all objects being
      // submitted to the topic are identical
      .map(modifier)
      // the buffer has effectively removed the delay between the individual items in the
      // list, so we can flatmap without changing the interval, so that we can then submit
      // them to Kafka individually
      .flatMap(Flux::fromIterable)
      // re-batch
      .buffer(batchSize)
      .delayElements(interval.dividedBy(batchSize))
      // Move the work being done to the current thread, causing it to block and prevent
      // program termination.  Flux.interval is on the compute scheduler, so it won't
      // block and the program will finish before anything happens if we don't move the
      // subscription.
      // Put each item on the kafka topic, or log an error if one has occurred somewhere in
      // the flux.
      .doOnNext(consumer)
      // Handle any errors.
      // Note that the likely errors are either from 1) the supplier being a
      // format other than json, or 2) the modifier expecting a data type other than that
      // produced by the supplier
      .doOnError(onError)
      .flatMap(Flux::fromIterable);
  }

  public static <T> Flux<T> createOrderedInfiniteFlux(
    Duration delay,
    Duration period,
    Supplier<Iterable<T>> supplier,
    Modifier<Iterable<T>> modifier, Scheduler scheduler) {

    return Flux.interval(delay, period, scheduler)
      .map(l -> supplier.get())
      .map(modifier)
      .flatMapSequential(Flux::fromIterable);
  }

  public static <T, S> Flux<T> createOrderedFiniteFlux(
    Callable<S> stateSupplier,
    BiFunction<S, SynchronousSink<T>, S> generator,
    Consumer<Iterable<T>> consumer,
    int batchSize) {
    return Flux.generate(stateSupplier, generator)
      .buffer(batchSize)
      .doOnNext(consumer)
      .flatMap(Flux::fromIterable);
  }
}
