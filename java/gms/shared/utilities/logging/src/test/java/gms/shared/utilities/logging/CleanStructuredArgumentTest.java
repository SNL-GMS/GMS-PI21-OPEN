package gms.shared.utilities.logging;

import ch.qos.logback.classic.Logger;
import net.logstash.logback.argument.StructuredArgument;
import net.logstash.logback.argument.StructuredArguments;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class CleanStructuredArgumentTest {
  static String key;
  static Object value;
  static Object value2;

  StructuredArgument structuredArgument;
  CleanStructuredArgument csa;

  @BeforeAll
  static void setup() {
    key = "key";
    value = "Hello";
    value2 = "Hello too";
  }

  @Test
  void testKeyValue() {
    structuredArgument = StructuredArguments.keyValue(key, value);
    csa = CleanStructuredArgument.keyValue(key, value);
    assertEquals(structuredArgument.toString(), csa.getStructuredArgument().toString());
  }

  @Test
  void testKV() {
    structuredArgument = StructuredArguments.kv(key, value);
    csa = CleanStructuredArgument.kv(key, value);
    assertEquals(structuredArgument.toString(), csa.getStructuredArgument().toString());
  }

  @Test
  void testEquals() {
    CleanStructuredArgument csa2 = CleanStructuredArgument.value(key, value);
    csa = CleanStructuredArgument.keyValue(key, value);
    assertEquals(csa, csa2);
  }

  @Test
  void testNotEquals() {
    CleanStructuredArgument csa2 = CleanStructuredArgument.value(key, value);
    csa = CleanStructuredArgument.kv(key, value2);
    assertNotEquals(csa, csa2);
  }

  @ParameterizedTest
  @MethodSource("scrubbedStringTest")
  void testStringScrubbing(String value, String expected) {
    //String with dirty chars
    CleanStructuredArgument csa2 = CleanStructuredArgument.value(key, value);
    //cleaned string input
    csa = CleanStructuredArgument.kv(key, expected);
    //If scrubbing works, these should be the same now...
    assertEquals(csa, csa2);
  }

  private static Stream<Arguments> scrubbedStringTest() {
    return Stream.of(
      arguments("INFO:\t2022-01-01T00:00:00Z\tLog entry successful.", "INFO:\t2022-01-01T00:00:00Z\tLog entry successful."),
      arguments("Bad#@!", "Bad!"),
      arguments("Bad123@456%", "Bad123456"),
      arguments("Hello\n\n%$ too", "Hello too"),
      arguments("Hi%myAttributes%[]", "HimyAttributes[]"),
      arguments("^-- 2022-01-01 INFO: {This is an entry[This too]}", "^-- 2022-01-01 INFO: {This is an entry[This too]}"),
      arguments("LogLevel: INFO \t\t####My comment here!\n[And another one.]", "LogLevel: INFO \t\tMy comment here![And another one.]"));
  }


}
