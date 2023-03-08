package gms.shared.utilities.logging;

import com.google.common.annotations.VisibleForTesting;
import gms.shared.frameworks.utilities.Validation;
import net.logstash.logback.argument.StructuredArgument;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Wrapper for logback-based loggers to transparently insert structured arguments into logging
 * statements. All throwables provided to this wrapper will have their stack traces aggregated into
 * an array field within the structured log in addition to any format insertion.
 * <p>
 * NOTE: If providing a non-logback-based logger, application of structured arguments will fail, but
 * formatted logs should still log successfully.
 */
public class StructuredLoggingWrapper {

  private final Logger logger;
  private final Map<String, CleanStructuredArgument> structuredArgsByName;

  private StructuredLoggingWrapper(Logger logger) {
    this.logger = logger;
    this.structuredArgsByName = new ConcurrentHashMap<>();
  }

  public static StructuredLoggingWrapper create(Logger logger) {
    return new StructuredLoggingWrapper(logger);
  }

  public Logger getWrappedLogger() {
    return logger;
  }

  /**
   * Adds a {@link StructuredArgument} to the wrapper's arguments that, when logged in a format
   * string, will log the entire key=value pair
   *
   * @param key Key to be used for the JSON field
   * @param value Value to be used for the JSON field
   */
  public void addKeyValueArgument(String key, Object value) {
    structuredArgsByName.put(key, CleanStructuredArgument.keyValue(key, value));
  }

  /**
   * Adds a {@link StructuredArgument} to the wrapper's arguments that, when logged in a format
   * string, will only log the value
   *
   * @param key Key to be used for the JSON field
   * @param value Value to be used for the JSON field
   */
  public void addValueArgument(String key, Object value) {
    structuredArgsByName.put(key, CleanStructuredArgument.value(key, value));
  }

  /**
   * Removes the argument with matching key from this wrapper's arguments
   *
   * @param key Key of the structured argument to be removed
   */
  public void removeArgument(String key) {
    structuredArgsByName.remove(key);
  }

  public void info(String message) {
    final var cleansedMessage = Validation.cleanseInputString(message);
    logger.info(cleansedMessage, joinArgs());
  }

  public void info(String formatMessage, Object... arguments) {
    final var cleansedMessage = Validation.cleanseInputString(formatMessage);
    logger.info(cleansedMessage,
      joinArgs(cleanseArguments(arguments).toArray()));
  }

  @VisibleForTesting
  List<Object> cleanseArguments(Object[] arguments) {
    return Arrays.stream(arguments)
      .map(argument -> {
        if (argument instanceof StructuredArgument) {
          logger.warn("Raw StructuredArguments are not supported. Will only appear in formatted string as key=value or value.");
        } else if (argument instanceof CleanStructuredArgument) {
          return ((CleanStructuredArgument)argument).getStructuredArgument();
        } else if (argument instanceof Throwable) {
          return argument;
        }
        return Validation.cleanseInputString(argument.toString());
      })
      .collect(Collectors.toList());
  }

  public void warn(String message) {
    final var cleansedMessage = Validation.cleanseInputString(message);
    logger.warn(cleansedMessage, joinArgs());
  }

  public void warn(String formatMessage, Object... arguments) {
    final var cleansedMessage = Validation.cleanseInputString(formatMessage);
    logger.warn(cleansedMessage,
      joinArgs(cleanseArguments(arguments).toArray()));
  }

  public void error(String message) {
    final var cleansedMessage = Validation.cleanseInputString(message);
    logger.error(cleansedMessage, joinArgs());
  }

  public void error(String formatMessage, Object... arguments) {
    final var cleansedMessage = Validation.cleanseInputString(formatMessage);
    logger.error(cleansedMessage,
      joinArgs(cleanseArguments(arguments).toArray()));
  }

  public void debug(String message) {
    final var cleansedMessage = Validation.cleanseInputString(message);
    logger.debug(cleansedMessage, joinArgs());
  }

  public void debug(String formatMessage, Object... arguments) {
    final var cleansedMessage = Validation.cleanseInputString(formatMessage);
    logger.debug(cleansedMessage,
      joinArgs(cleanseArguments(arguments).toArray()));
  }

  private Object[] joinArgs(Object... arguments) {
    return ArrayUtils.addAll(arguments, structuredArgsByName.values().stream()
      .map(CleanStructuredArgument::getStructuredArgument).toArray());
  }

  Object[] aggregateThrowableStackTraces(Object[] arguments) {
    List<String> traces = Arrays.stream(arguments)
      .filter(Throwable.class::isInstance)
      .map(Throwable.class::cast)
      .map(t -> {
        try {
          return getStackTraceLog(t);
        } catch (IOException e) {
          logger.error("Failed to parse stack trace", e);
          return "";
        }
      })
      .collect(Collectors.toList());
    var stackTraces = CleanStructuredArgument.value("stackTraces", traces);
    if (traces.isEmpty()) {
      return arguments;
    }
    return ArrayUtils.add(Arrays.stream(arguments)
      .filter(obj -> !(obj instanceof Throwable))
      .toArray(), stackTraces);
  }


  static String getStackTraceLog(Throwable throwableToLog) throws IOException {
    final var charset = StandardCharsets.UTF_8;

    var outputStream = new ByteArrayOutputStream();
    var printStream = new PrintStream(outputStream, true, charset.name());
    throwableToLog.printStackTrace(printStream);
    var stackTraceLog = outputStream.toString(charset);

    printStream.close();
    outputStream.close();

    return Validation.cleanseInputString(stackTraceLog);
  }
}
