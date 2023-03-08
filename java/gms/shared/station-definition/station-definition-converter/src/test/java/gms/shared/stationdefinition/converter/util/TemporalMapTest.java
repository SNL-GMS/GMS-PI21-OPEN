package gms.shared.stationdefinition.converter.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class TemporalMapTest {

  TemporalMap<String, Integer> temporalMap;

  @BeforeEach
  void setUp() {
    temporalMap = TemporalMap.create();
  }

  @Test
  void testContainsKey() {
    Instant activeTime = Instant.EPOCH;
    String testKey = "TEST";
    String otherKey = "OTHER";

    temporalMap.put(testKey, activeTime, 1);
    assertTrue(temporalMap.containsKey(testKey));
    assertFalse(temporalMap.containsKey(otherKey));
  }

  @Test
  void testPutAndGet() {
    Instant activeTime = Instant.EPOCH;
    String testKey = "TEST";
    String otherKey = "OTHER";

    temporalMap.put(testKey, activeTime, 1);
    temporalMap.put(otherKey, activeTime.plusSeconds(1), 2);
    temporalMap.put(testKey, activeTime.plusSeconds(2), 3);

    assertEquals(List.of(1, 3), temporalMap.getVersions(testKey));
    assertEquals(List.of(2), temporalMap.getVersions(otherKey));
    temporalMap.getVersionFloor(testKey, activeTime).ifPresentOrElse(
      v -> assertEquals(1, v),
      Assertions::fail
    );
    temporalMap.getVersionFloor(testKey, activeTime.plusMillis(500)).ifPresentOrElse(
      v -> assertEquals(1, v),
      Assertions::fail
    );
    temporalMap.getVersionFloor(testKey, activeTime.plusSeconds(1)).ifPresentOrElse(
      v -> assertEquals(1, v),
      Assertions::fail
    );
    temporalMap.getVersionFloor(otherKey, activeTime.plusSeconds(1)).ifPresentOrElse(
      v -> assertEquals(2, v),
      Assertions::fail
    );
    temporalMap.getVersionFloor(testKey, activeTime.plusSeconds(2)).ifPresentOrElse(
      v -> assertEquals(3, v),
      Assertions::fail
    );
    assertTrue(temporalMap.getVersionFloor(testKey, activeTime.minusMillis(1)).isEmpty());

    temporalMap.put(testKey, activeTime, 4);
    assertEquals(List.of(4, 3), temporalMap.getVersions(testKey));
    assertEquals(List.of(2), temporalMap.getVersions(otherKey));
    temporalMap.getVersionFloor(testKey, activeTime).ifPresentOrElse(
      v -> assertEquals(4, v),
      Assertions::fail
    );
    temporalMap.getVersionFloor(otherKey, activeTime.plusSeconds(1)).ifPresentOrElse(
      v -> assertEquals(2, v),
      Assertions::fail
    );

  }

  @Test
  void testPutVersions() {
    Instant activeTime = Instant.EPOCH;
    String testKey = "TEST";
    temporalMap.put(testKey, activeTime.plusSeconds(1), 2);
    temporalMap.putVersions(testKey, Map.of(activeTime, 1, activeTime.plusSeconds(2), 3));

    assertEquals(List.of(1, 2, 3), temporalMap.getVersions(testKey));
    temporalMap.getVersionFloor(testKey, activeTime).ifPresentOrElse(
      v -> assertEquals(1, v),
      Assertions::fail
    );
    temporalMap.getVersionFloor(testKey, activeTime.plusSeconds(1)).ifPresentOrElse(
      v -> assertEquals(2, v),
      Assertions::fail
    );
    temporalMap.getVersionFloor(testKey, activeTime.plusSeconds(2)).ifPresentOrElse(
      v -> assertEquals(3, v),
      Assertions::fail
    );
  }

  @Test
  void testPutAll() {
    Instant activeTime = Instant.EPOCH;
    String testKey = "TEST";
    String otherKey = "OTHER";
    TemporalMap<String, Integer> otherMap = TemporalMap.create();

    temporalMap.put(testKey, activeTime, 1);
    temporalMap.put(testKey, activeTime.plusSeconds(1), 2);
    temporalMap.put(otherKey, activeTime.plusSeconds(2), 3);
    otherMap.put(otherKey, activeTime, 4);
    otherMap.put(testKey, activeTime.plusSeconds(1), 5);
    otherMap.put(testKey, activeTime.plusSeconds(2), 6);

    temporalMap.putAll(otherMap);
    assertEquals(List.of(1, 5, 6), temporalMap.getVersions(testKey));
    assertEquals(List.of(4, 3), temporalMap.getVersions(otherKey));
  }

  @Test
  void testCollector() {
    Instant activeTime = Instant.EPOCH;

    TemporalMap<String, Integer> evenOddMap = IntStream.iterate(1, i -> i + 1)
      .limit(10)
      .boxed()
      .collect(TemporalMap.collector(
        i -> i % 2 == 0 ? "EVEN" : "ODD",
        (Function<Integer, Instant>) activeTime::plusSeconds));

    assertEquals(List.of(1, 3, 5, 7, 9), evenOddMap.getVersions("ODD"));
    assertEquals(List.of(2, 4, 6, 8, 10), evenOddMap.getVersions("EVEN"));

    assertEquals(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10), evenOddMap.values());
  }

  @ParameterizedTest
  @MethodSource("getFirstKeyArguments")
  void testFirstKey(Optional<Instant> expected, TemporalMap<String, Integer> temporalMap, String key) {
    Optional<Instant> actual = temporalMap.getFirstVersion(key);
    assertEquals(expected.isPresent(), actual.isPresent());
    expected.ifPresent(instant -> assertEquals(instant, actual.get()));
  }

  static Stream<Arguments> getFirstKeyArguments() {
    TemporalMap<String, Integer> temporalMap = TemporalMap.create();
    temporalMap.putVersions("Test1", Map.of(Instant.EPOCH, 1, Instant.EPOCH.plusSeconds(100), 2));
    temporalMap.putVersions("Test2", Map.of());
    return Stream.of(arguments(Optional.empty(), temporalMap, null),
      arguments(Optional.of(Instant.EPOCH), temporalMap, "Test1"),
      arguments(Optional.empty(), temporalMap, "Test2"));
  }

}
