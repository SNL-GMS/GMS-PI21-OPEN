package gms.shared.frameworks.cache.utils;

import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.cache.eviction.lru.LruEvictionPolicyFactory;
import org.apache.ignite.configuration.CacheConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static java.util.Collections.singleton;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IgniteConnectionUtilityTest {

  @Test
  void testBuildCacheConfigurations() {
    CacheInfo inputCacheInfo = new CacheInfo("test-cache",
      CacheMode.LOCAL, CacheAtomicityMode.TRANSACTIONAL, true, Optional.of(new LruEvictionPolicyFactory<String, String>()));

    var actualConfigurations = IgniteConnectionUtility.buildCacheConfigurations(inputCacheInfo);
    assertEquals(1, actualConfigurations.length);
    CacheConfiguration<?, ?> firstConfiguration = actualConfigurations[0];
    assertEquals(inputCacheInfo.getCacheName(), firstConfiguration.getName());
    assertEquals(inputCacheInfo.getCacheMode(), firstConfiguration.getCacheMode());
    assertEquals(inputCacheInfo.isOnHeap(), firstConfiguration.isOnheapCacheEnabled());
    inputCacheInfo.getEvictionPolicy().ifPresent(factory -> assertEquals(factory, firstConfiguration.getEvictionPolicyFactory()));

    actualConfigurations = IgniteConnectionUtility.buildCacheConfigurations(singleton(inputCacheInfo));
    assertEquals(1, actualConfigurations.length);
    CacheConfiguration<?, ?> secondConfiguration = actualConfigurations[0];
    assertEquals(inputCacheInfo.getCacheName(), secondConfiguration.getName());
    assertEquals(inputCacheInfo.getCacheMode(), secondConfiguration.getCacheMode());
    assertEquals(inputCacheInfo.isOnHeap(), secondConfiguration.isOnheapCacheEnabled());
    inputCacheInfo.getEvictionPolicy().ifPresent(factory -> assertEquals(factory, secondConfiguration.getEvictionPolicyFactory()));
  }

  @Test
  void testBuildNodeAttributes() {
    CacheInfo inputCacheInfo = new CacheInfo("test-cache",
      CacheMode.LOCAL, CacheAtomicityMode.TRANSACTIONAL, true, Optional.of(new LruEvictionPolicyFactory<String, String>()));

    var actualNodeAttributes = IgniteConnectionUtility.buildNodeAttributes(inputCacheInfo);
    assertEquals(1, actualNodeAttributes.size());
    assertTrue(actualNodeAttributes.containsKey(inputCacheInfo.getNodeAttr()));
    assertEquals(true, actualNodeAttributes.get(inputCacheInfo.getNodeAttr()));

    actualNodeAttributes = IgniteConnectionUtility.buildNodeAttributes(singleton(inputCacheInfo));
    assertEquals(1, actualNodeAttributes.size());
    assertTrue(actualNodeAttributes.containsKey(inputCacheInfo.getNodeAttr()));
    assertEquals(true, actualNodeAttributes.get(inputCacheInfo.getNodeAttr()));
  }
}