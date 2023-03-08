package gms.shared.stationdefinition.cache;

import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import gms.shared.frameworks.cache.utils.IgniteConnectionManager;
import gms.shared.frameworks.systemconfig.SystemConfig;
import gms.shared.stationdefinition.cache.util.StationDefinitionCacheFactory;
import org.apache.ignite.IgniteCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.NavigableSet;

import static gms.shared.stationdefinition.cache.util.StationDefinitionCacheFactory.VERSION_EFFECTIVE_TIME_CACHE;
import static gms.shared.stationdefinition.cache.util.StationDefinitionCacheFactory.VERSION_ENTITY_TIME_CACHE;

/**
 * VersionCache for caching station definition versions and time ranges
 */
@Component
public class VersionCache {
  private static final Logger logger = LoggerFactory.getLogger(VersionCache.class);
  public static final String CACHE_INITIALIZED = "Cache already initialized: ";
  private final IgniteCache<String, NavigableSet<Instant>> versionEffectiveTimesByEntityIdCache;
  private SystemConfig systemConfig;

  private final IgniteCache<String, RangeMap<Instant, Object>> versionsByEntityIdAndTimeCache;

  @Autowired
  public VersionCache(SystemConfig systemConfig) {
    versionEffectiveTimesByEntityIdCache = IgniteConnectionManager.getOrCreateCache(VERSION_EFFECTIVE_TIME_CACHE);
    versionsByEntityIdAndTimeCache = IgniteConnectionManager.getOrCreateCache(VERSION_ENTITY_TIME_CACHE);
    this.systemConfig = systemConfig;
  }

  @PostConstruct
  public void init() {
    try {
      StationDefinitionCacheFactory.setUpCache(systemConfig);
    } catch (IllegalStateException e) {
      logger.warn(CACHE_INITIALIZED, e);
    }
  }

  public void clear() {
    versionEffectiveTimesByEntityIdCache.clear();
    versionsByEntityIdAndTimeCache.clear();
  }

  /**
   * Cache version effectiveTimes using entityId strings
   *
   * @param key EntityId strings
   * @param value RangeSet of effectiveTimes
   */
  public void cacheVersionEffectiveTimesByEntityId(String key, NavigableSet<Instant> value) {
    versionEffectiveTimesByEntityIdCache.put(key, value);
  }

  /**
   * Check if the key exists in the effectiveTimes cache
   *
   * @param key EntityId strings
   * @return true if cache contains key
   */
  public boolean versionEffectiveTimesByEntityIdHasKey(String key) {
    return versionEffectiveTimesByEntityIdCache.containsKey(key);
  }

  /**
   * Cache version objects using entityId and effectiveTimes
   *
   * @param key EntityId strings
   * @param value RangeMap of effectiveTimes to version objects
   */
  public void cacheVersionsByEntityIdAndTime(String key, RangeMap<Instant, Object> value) {

    if (versionsByEntityIdAndTimeCache.get(key) == null) {
      versionsByEntityIdAndTimeCache.put(key, value);
    } else {
      versionsByEntityIdAndTimeCache.get(key).putAll(value);
    }

  }

  /**
   * Checks if key exists in the object verison cache
   *
   * @param key EntityId strings
   * @return true if cache contains key
   */
  public boolean versionsByEntityIdAndTimeHasKey(String key) {
    return versionsByEntityIdAndTimeCache.containsKey(key);
  }

  /**
   * Retrieve version effective times using entityId strings
   *
   * @param key EntityId string
   * @return RangeSet of effectiveTimes
   */
  public NavigableSet<Instant> retrieveVersionEffectiveTimesByEntityId(String key) {
    return versionEffectiveTimesByEntityIdCache.get(key);
  }

  /**
   * Retrive version range map using entityId string
   *
   * @param key EntityId string
   * @return rangemap of versions by effectiveTime to version object
   */
  public RangeMap<Instant, Object> retrieveVersionsByEntityIdAndTimeRangeMap(String key) {
    return versionsByEntityIdAndTimeCache.get(key);
  }

  /**
   * Retrieve versions using entityId string and effectiveTime
   *
   * @param key EntityId string
   * @param effectiveTime EffectiveTime instant
   * @return version object from RangeMap
   */
  public Object retrieveVersionsByEntityIdAndTime(String key, Instant effectiveTime) {
    RangeMap<Instant, Object> rangeMap = versionsByEntityIdAndTimeCache.get(key);
    if (rangeMap != null) {
      return rangeMap.get(effectiveTime);
    }
    return null;
  }

  /**
   * Retrieve collection of versions using entityId and time range of instants
   *
   * @param key EntityId string
   * @param timeRange Range of Instant
   * @return Collection of version objects
   */
  public Collection<Object> retrieveVersionsByEntityIdAndTimeRange(String key, Range<Instant> timeRange) {

    RangeMap<Instant, Object> rangeMap = versionsByEntityIdAndTimeCache.get(key);
    if (rangeMap != null && timeRange != null) {
      return rangeMap.subRangeMap(timeRange).asDescendingMapOfRanges().values();
    }
    return Collections.emptyList();
  }
}