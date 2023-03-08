package gms.shared.frameworks.configuration.repository.client;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.UncheckedExecutionException;
import gms.shared.frameworks.configuration.Configuration;
import gms.shared.frameworks.configuration.ConfigurationResolver;
import gms.shared.frameworks.configuration.Selector;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;

public class ConfigurationSelectorGuavaCache implements ConfigurationSelectorCache {

  private final Configuration configuration;

  private final Cache<Set<Selector>, Map<String, Object>> fieldMapCache;

  private final Collection<Map<String, Object>> cacheValues;

  private ConfigurationSelectorGuavaCache(Configuration configuration, Duration expiration) {
    this.configuration = configuration;
    this.fieldMapCache = CacheBuilder.newBuilder()
      .expireAfterAccess(expiration)
      .build();
    this.cacheValues = fieldMapCache.asMap().values();
  }

  public static ConfigurationSelectorGuavaCache create(Configuration configuration,
    Duration expiration) {
    return new ConfigurationSelectorGuavaCache(configuration, expiration);
  }

  @Override
  public Map<String, Object> resolveFieldMap(List<Selector> selectors) {
    Set<Selector> selectorSet = Set.copyOf(selectors);
    try {
      return fieldMapCache
        .get(selectorSet, () -> resolveAndCheckExistingFieldMap(selectors));
    } catch (ExecutionException | UncheckedExecutionException e) {
      //Need to unwrap the exception if it's unchecked.
      Throwable cause = e.getCause();
      if (cause instanceof NullPointerException) {
        throw (NullPointerException) cause;
      }
      if (cause instanceof IllegalArgumentException) {
        throw (IllegalArgumentException) cause;
      }
      if (cause instanceof IllegalStateException) {
        throw (IllegalStateException) cause;
      }
      throw new IllegalStateException(
        "Exception encountered when resolving configuration", e);
    }
  }

  private Map<String, Object> resolveAndCheckExistingFieldMap(List<Selector> selectors) {
    var fieldMap = ConfigurationResolver.resolve(configuration, selectors);
    var existingFieldMap = cacheValues.stream()
      .filter(Predicate.isEqual(fieldMap))
      .findAny();
    return existingFieldMap.orElse(fieldMap);
  }

  protected Cache<Set<Selector>, Map<String, Object>> getFieldMapCache() {
    return fieldMapCache;
  }
}
