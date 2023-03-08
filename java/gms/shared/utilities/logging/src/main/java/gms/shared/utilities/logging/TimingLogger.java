package gms.shared.utilities.logging;

import net.logstash.logback.argument.StructuredArguments;
import net.logstash.logback.marker.Markers;
import org.slf4j.Logger;

import java.util.function.BiFunction;
import java.util.function.Supplier;

public class TimingLogger<T> implements BiFunction<String, Supplier<T>, T> {
  private final Logger logger;

  private TimingLogger(Logger logger) {
    this.logger = logger;
  }

  public static <T> TimingLogger<T> create(Logger logger) {
    return new TimingLogger<>(logger);
  }

  @Override
  public T apply(String timedMethod, Supplier<T> supplier) {
    long start = System.currentTimeMillis();
    T t = supplier.get();
    long end = System.currentTimeMillis();
    long elapsedTime = end - start;
    logger.info(Markers.aggregate(
          Markers.append("startTime", start),
          Markers.append("endTime", end)),
      "{} ran in {} milliseconds",
      StructuredArguments.v("methodName", timedMethod),
      StructuredArguments.v("elapsedTime", elapsedTime));
    return t;
  }
}
