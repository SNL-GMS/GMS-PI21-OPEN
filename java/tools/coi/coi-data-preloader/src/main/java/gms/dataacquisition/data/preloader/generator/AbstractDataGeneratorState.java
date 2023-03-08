package gms.dataacquisition.data.preloader.generator;

import gms.shared.frameworks.injector.DataGeneratorState;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;
import org.postgresql.util.PSQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * Common {@link DataGeneratorState} functionality
 *
 * @param <T>
 * @param <S>
 */
public abstract class AbstractDataGeneratorState<T, S> implements DataGeneratorState<T, S> {

  private static final Logger logger = LoggerFactory.getLogger(AbstractDataGeneratorState.class);

  private static final int MAX_ATTEMPTS = 10;

  /**
   * Perform some operation on the provided records e.g. store to the OSD, produce to Kafka
   *
   * @param records of type {@link Iterable}{@code <}T{@code >}
   */
  public void runRecordConsumer(Iterable<T> records) {
    try {
      logger.debug("run record consumer");
      this.tryConsume(records);
      logger.debug("records consumed");
    } catch (Exception e) {
      final var error = new GmsPreloaderException("Failed to consume records", e);
      logger.error(error.getMessage(), error);
      throw error;
    }
  }

  private void tryConsume(Iterable<T> records) {
    final RetryPolicy<T> retryPolicy = new RetryPolicy<T>()
      .withBackoff(100, 3000, ChronoUnit.MILLIS)
      .withMaxAttempts(MAX_ATTEMPTS)
      .handle(List.of(ExecutionException.class, IllegalStateException.class,
        InterruptedException.class, PSQLException.class))
      .onFailedAttempt(e -> logger.warn("Unable to consume records, retrying: {}", e));
    Failsafe.with(retryPolicy).run(() -> this.consumeRecords(records));
  }

  protected <D> Set<D> convertToSet(Iterable<D> records) {
    final var data = new HashSet<D>();
    records.iterator().forEachRemaining(data::add);
    return data;
  }

  protected abstract void consumeRecords(Iterable<T> records);
}
