package gms.core.dataacquisition.reactor.util;

import java.time.Duration;
import java.util.function.Function;

/**
 * Base interface for resolving a merge tolerance given a channel name
 */
@FunctionalInterface
public interface ToleranceResolver extends Function<String, Duration> {

  Duration resolveTolerance(String channelName);

  @Override
  default Duration apply(String s) {
    return resolveTolerance(s);
  }
}
