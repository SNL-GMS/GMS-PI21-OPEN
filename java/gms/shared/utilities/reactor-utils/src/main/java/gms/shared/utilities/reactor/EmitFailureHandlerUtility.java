package gms.shared.utilities.reactor;

import org.apache.commons.math3.random.RandomDataGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.SignalType;
import reactor.core.publisher.Sinks;

import java.util.concurrent.locks.LockSupport;

public class EmitFailureHandlerUtility implements Sinks.EmitFailureHandler {

  private static EmitFailureHandlerUtility instance = new EmitFailureHandlerUtility();

  /**
   * Returns an instance of the EmitFailureHandlerUtility
   *
   * @return the EmitFailureHandlerUtility
   */
  public static EmitFailureHandlerUtility getInstance() {
    return instance;
  }

  private EmitFailureHandlerUtility() {

  }

  private static final Logger logger = LoggerFactory.getLogger(EmitFailureHandlerUtility.class);

  private static final RandomDataGenerator sinkFuzzer = new RandomDataGenerator();

  private static final long MIN_PARK_NANOS = 100;
  private static final long MAX_PARK_NANOS = 1000;

  /**
   * A common EmitFailure handler for reactor Sinks emmitNext
   *
   * @param signalType The signalType
   * @param emitResult The emitResult
   * @return boolean if the operation should be retried
   */
  @Override
  public boolean onEmitFailure(SignalType signalType, Sinks.EmitResult emitResult) {
    if (emitResult.equals(Sinks.EmitResult.FAIL_NON_SERIALIZED)) {
      logger.debug("Concurrent emission occurred, parking and retrying...");
      LockSupport.parkNanos(sinkFuzzer.nextLong(MIN_PARK_NANOS, MAX_PARK_NANOS));
      return true;
    } else {
      logger.debug("Error {} encountered while emitting", emitResult);
      return false;
    }
  }
}
