package gms.shared.frameworks.cache.utils;

import org.apache.ignite.cache.CacheRebalanceMode;
import org.apache.ignite.configuration.CacheConfiguration;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

public class IgniteConnectionUtility {

  private IgniteConnectionUtility() {
  }

  static CacheConfiguration[] buildCacheConfigurations(CacheInfo... cacheInfos) {
    return Arrays.stream(cacheInfos)
      .map(IgniteConnectionUtility::createCacheFromCacheInfo)
      .toArray(CacheConfiguration[]::new);
  }

  static CacheConfiguration[] buildCacheConfigurations(Collection<CacheInfo> cacheInfos) {
    return cacheInfos.stream()
      .map(IgniteConnectionUtility::createCacheFromCacheInfo)
      .toArray(CacheConfiguration[]::new);
  }

  static <T, U> CacheConfiguration<T, U> createCacheFromCacheInfo(CacheInfo cache) {
    CacheConfiguration<T, U> cacheCfg = new CacheConfiguration<T, U>()
      .setName(cache.getCacheName())
      .setCacheMode(cache.getCacheMode())
      .setAtomicityMode(cache.getCacheAtomicityMode())
      .setOnheapCacheEnabled(cache.isOnHeap())
      .setRebalanceMode(CacheRebalanceMode.NONE)
      .setNodeFilter(cache.getNodeFilter());

    cache.getEvictionPolicy().ifPresent(cacheCfg::setEvictionPolicyFactory);
    return cacheCfg;
  }

  static Map<String, Object> buildNodeAttributes(CacheInfo... cacheInfos) {
    return Arrays.stream(cacheInfos)
      .collect(toMap(CacheInfo::getNodeAttr, cacheInfo -> true));
  }

  static Map<String, Object> buildNodeAttributes(Collection<CacheInfo> cacheInfos) {
    return cacheInfos.stream()
      .collect(toMap(CacheInfo::getNodeAttr, cacheInfo -> true));
  }
}
