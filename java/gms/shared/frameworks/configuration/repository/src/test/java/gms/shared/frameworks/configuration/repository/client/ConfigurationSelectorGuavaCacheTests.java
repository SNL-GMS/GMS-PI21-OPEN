package gms.shared.frameworks.configuration.repository.client;

import com.google.common.collect.Lists;
import gms.shared.frameworks.configuration.Configuration;
import gms.shared.frameworks.configuration.ConfigurationResolver;
import gms.shared.frameworks.configuration.Selector;
import mockit.MockUp;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@ExtendWith(MockitoExtension.class)
class ConfigurationSelectorGuavaCacheTests {

  @Mock
  Configuration mockConfiguration;

  @Test
  void testUpdatesCache() {
    ConfigurationSelectorGuavaCache cache = ConfigurationSelectorGuavaCache
      .create(mockConfiguration, Duration.ofDays(1));

    Map<String, Object> testFieldMap = Map.of("TEST", "TEST");

    //resolve should generate new maps everytime it's called
    new MockUp<ConfigurationResolver>() {
      @mockit.Mock
      public Map<String, Object> resolve(Configuration configuration, List<Selector> selectors) {
        return new HashMap<>(testFieldMap);
      }
    };

    var fieldMapCache = cache.getFieldMapCache();

    List<Selector> dummySelectors = Collections.emptyList();
    assertNull(fieldMapCache.getIfPresent(Set.copyOf(dummySelectors)));

    assertEquals(testFieldMap, cache.resolveFieldMap(dummySelectors));
    assertEquals(testFieldMap, fieldMapCache.getIfPresent(Set.copyOf(dummySelectors)));
  }

  @Test
  void testResolveSameSelectors() {
    ConfigurationSelectorGuavaCache cache = ConfigurationSelectorGuavaCache
      .create(mockConfiguration, Duration.ofDays(1));

    Map<String, Object> testFieldMap = Map.of("TEST", "TEST");

    //resolve should generate new maps everytime it's called
    new MockUp<ConfigurationResolver>() {
      @mockit.Mock
      public Map<String, Object> resolve(Configuration configuration, List<Selector> selectors) {
        return new HashMap<>(testFieldMap);
      }
    };

    var fieldMapCache = cache.getFieldMapCache();

    List<Selector> selectors = List.of(
      Selector.from("TEST", 1),
      Selector.from("TEST2", "TEST"));

    assertNull(fieldMapCache.getIfPresent(Set.copyOf(selectors)));

    assertEquals(testFieldMap, cache.resolveFieldMap(selectors));
    assertEquals(testFieldMap, fieldMapCache.getIfPresent(Set.copyOf(selectors)));

    List<Selector> reverse = Lists.reverse(selectors);
    assertEquals(testFieldMap, cache.resolveFieldMap(reverse));
    assertEquals(1, fieldMapCache.size());
  }

  @Test
  void testResolveDifferentSelectorsSameFieldMap() {
    ConfigurationSelectorGuavaCache cache = ConfigurationSelectorGuavaCache
      .create(mockConfiguration, Duration.ofDays(1));

    Map<String, Object> testFieldMap = Map.of("TEST", "TEST");

    //resolve should generate new maps everytime it's called
    new MockUp<ConfigurationResolver>() {
      @mockit.Mock
      public Map<String, Object> resolve(Configuration configuration, List<Selector> selectors) {
        return new HashMap<>(testFieldMap);
      }
    };

    List<Selector> selectors1 = List.of(Selector.from("TEST", 1));
    List<Selector> selectors2 = List.of(Selector.from("TEST", 2));

    Map<String, Object> resolvedMap1 = cache.resolveFieldMap(selectors1);
    Map<String, Object> resolvedMap2 = cache.resolveFieldMap(selectors2);
    assertEquals(testFieldMap, resolvedMap1);
    assertEquals(testFieldMap, resolvedMap2);
    assertSame(resolvedMap1, resolvedMap2);
    assertSame(resolvedMap1, cache.resolveFieldMap(selectors1));
    assertSame(resolvedMap2, cache.resolveFieldMap(selectors2));
  }

  @ParameterizedTest
  @MethodSource("exceptionSource")
  void testResolutionFailureThrowsException(RuntimeException exception) {
    ConfigurationSelectorGuavaCache cache = ConfigurationSelectorGuavaCache
      .create(mockConfiguration, Duration.ofDays(1));

    new MockUp<ConfigurationResolver>() {
      @mockit.Mock
      public Map<String, Object> resolve(Configuration configuration, List<Selector> selectors) {
        throw exception;
      }
    };
    assertThrows(exception.getClass(), () -> cache.resolveFieldMap(List.of()));
  }

  private static Stream<Arguments> exceptionSource() {
    return Stream.of(
      arguments(new NullPointerException()),
      arguments(new IllegalArgumentException()),
      arguments(new IllegalStateException())
    );
  }
}