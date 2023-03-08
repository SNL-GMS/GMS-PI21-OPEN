package gms.shared.utilities.logging;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import net.logstash.logback.argument.StructuredArgument;
import net.logstash.logback.argument.StructuredArguments;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@ExtendWith(MockitoExtension.class)
class StructuredLoggingWrapperTest {
  Logger logger = (Logger) LoggerFactory.getLogger(StructuredLoggingWrapperTest.class);
  StructuredLoggingWrapper wrapper = StructuredLoggingWrapper.create(logger);
  ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
  private final String INVALID_STRING = "\u0009test";
  private final char INVALID_CHAR = '\u0009';

  @BeforeEach
  private void before() {
    listAppender.start();
    logger.addAppender(listAppender);
  }

  @ParameterizedTest
  @MethodSource("stackTracesTestSource")
  void testStackTraces(Object[] logArguments, Object[] expected) {
    StructuredLoggingWrapper wrapper = StructuredLoggingWrapper.create(LoggerFactory.getLogger(StructuredLoggingWrapperTest.class));
    Object[] result = wrapper.aggregateThrowableStackTraces(logArguments);

    assertArrayEquals(expected, result);
  }

  private static Stream<Arguments> stackTracesTestSource() throws IOException {
    var badStateException = new IllegalStateException("BAD STATE");
    var badArgException = new IllegalArgumentException("BAD ARG");
    return Stream.of(
      Arguments.arguments(
        Stream.of(
          "test",
          badStateException
        ).toArray(),
        Stream.of(
          "test",
          CleanStructuredArgument.value("stackTraces",
            List.of(StructuredLoggingWrapper.getStackTraceLog(badStateException)))
        ).toArray()
      ),
      Arguments.arguments(
        Stream.of(
          "test",
          badStateException,
          badArgException
        ).toArray(),
        Stream.of(
          "test",
          CleanStructuredArgument.value("stackTraces",
            List.of(
              StructuredLoggingWrapper.getStackTraceLog(badStateException),
              StructuredLoggingWrapper.getStackTraceLog(badArgException)))
        ).toArray()
      )
    );
  }


  @Test
  void testInfo() {
    wrapper.info(INVALID_STRING);
    List<ILoggingEvent> logsList = listAppender.list;
    assertEquals(logsList.get(0).getMessage().indexOf(INVALID_CHAR), -1);
  }

  @Test
  void testInfoWithArguments() {
    wrapper.info(INVALID_STRING, INVALID_STRING);
    List<ILoggingEvent> logsList = listAppender.list;
    assertEquals(-1, logsList.get(0).getMessage().indexOf(INVALID_CHAR));
    assertEquals(-1, logsList.get(0).getArgumentArray()[0].toString().indexOf(INVALID_CHAR));
  }

  @Test
  void testWarn() {
    wrapper.warn(INVALID_STRING);
    List<ILoggingEvent> logsList = listAppender.list;
    assertEquals(-1, logsList.get(0).getMessage().indexOf(INVALID_CHAR));
  }

  @Test
  void testWarnWithArguments() {
    wrapper.warn(INVALID_STRING, INVALID_STRING);
    List<ILoggingEvent> logsList = listAppender.list;
    assertEquals(-1, logsList.get(0).getMessage().indexOf(INVALID_CHAR));
    assertEquals(-1, logsList.get(0).getArgumentArray()[0].toString().indexOf(INVALID_CHAR));
  }

  @Test
  void testError() {
    wrapper.error(INVALID_STRING);
    List<ILoggingEvent> logsList = listAppender.list;
    assertEquals(-1, logsList.get(0).getMessage().indexOf(INVALID_CHAR));
  }

  @Test
  void testErrorWithArguments() {
    wrapper.error(INVALID_STRING, INVALID_STRING);
    List<ILoggingEvent> logsList = listAppender.list;
    assertEquals(-1, logsList.get(0).getMessage().indexOf(INVALID_CHAR));
    assertEquals(-1, logsList.get(0).getArgumentArray()[0].toString().indexOf(INVALID_CHAR));
  }

  @Test
  void testDebug() {
    wrapper.debug(INVALID_STRING);
    List<ILoggingEvent> logsList = listAppender.list;
    assertEquals(-1, logsList.get(0).getMessage().indexOf(INVALID_CHAR));
  }

  @Test
  void testDebugWithArguments() {
    wrapper.debug(INVALID_STRING, INVALID_STRING);
    List<ILoggingEvent> logsList = listAppender.list;
    assertEquals(-1, logsList.get(0).getMessage().indexOf(INVALID_CHAR));
    assertEquals(-1, logsList.get(0).getArgumentArray()[0].toString().indexOf(INVALID_CHAR));
  }

  @Test
  void testThrowableArg() {
    RuntimeException boom = new RuntimeException("BOOM");
    wrapper.error("Test throwable", boom);
    List<ILoggingEvent> logsList = listAppender.list;
    var logEvent = logsList.get(0);


    assertNotNull(logEvent.getThrowableProxy());
    assertThat(logEvent.getThrowableProxy().getClassName()).isEqualTo(boom.getClass().getName());
    assertThat(logEvent.getThrowableProxy().getMessage()).isEqualTo(boom.getMessage());
  }


  @ParameterizedTest
  @MethodSource("cleanseParameters")
  void testCleanse(Object[] cleanseArgs, List<Object> expected) {
    var result = wrapper.cleanseArguments(cleanseArgs);
    assertTrue(result.containsAll(expected) && expected.containsAll(result));
  }

  private static Stream<Arguments> cleanseParameters() {
    var strings = List.of("this", "that");
    var saKeyValue = StructuredArguments.kv(strings.get(0), strings.get(1));
    var saValue = StructuredArguments.v(strings.get(0), strings.get(1));
    var csa = CleanStructuredArgument.value(strings.get(0), strings.get(1));
    var withStructuredArg = List.of(strings.get(0), csa);
    var withThrowable = List.of(strings.get(0), new RuntimeException("BOOM"));
    return Stream.of(
      arguments(List.of(strings.get(0), 1024).toArray(), List.of(strings.get(0), "1024")),
      arguments(strings.toArray(), strings),
      arguments(List.of(saKeyValue, saValue).toArray(), List.of(saKeyValue.toString(), saValue.toString())),
      arguments(withStructuredArg.toArray(), List.of(strings.get(0), csa.getStructuredArgument())),
      arguments(withThrowable.toArray(), withThrowable));
  }
}