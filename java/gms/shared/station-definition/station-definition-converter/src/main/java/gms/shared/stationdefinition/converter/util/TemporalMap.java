package gms.shared.stationdefinition.converter.util;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;

/**
 * Map-like structure that also captures what time (as an {@link Instant} a value was active. A
 * value is considered active for a provided key and time if there are no other values set whose
 * active time are greater without going over the provided time (floor behavior)
 *
 * @param <K> Type of the key
 * @param <V> Type of the value
 */
public class TemporalMap<K, V> {

  private final Map<K, NavigableMap<Instant, V>> temporalMapsByKey;

  private TemporalMap(Map<K, NavigableMap<Instant, V>> temporalMapsByKey) {
    this.temporalMapsByKey = temporalMapsByKey;
  }

  /**
   * Basic factory method
   *
   * @param <K> Type of the key
   * @param <V> Type of the value
   * @return an empty initialized {@link TemporalMap}
   */
  public static <K, V> TemporalMap<K, V> create() {
    return new TemporalMap<>(
      new HashMap<>()
    );
  }

  public Map<K, NavigableMap<Instant, V>> asMap(){
    return temporalMapsByKey;
  }

  /**
   * Returns whether or not this map contains a value for the provided key
   *
   * @param key whose presence in Map is to be tested
   * @return true if the map contains a value for the key, false otherwise
   */
  public boolean containsKey(K key) {
    return temporalMapsByKey.containsKey(key);
  }

  /**
   * Returns a set of the keys contained in the map
   *
   * @return a set of the keys contained in the map
   */
  public Set<K> keySet() {
    return temporalMapsByKey.keySet();
  }

  /**
   * Associates the specified value with the specified key at the given time. This designates that
   * the value for this key is this value at this time going forward unless the value is changed at
   * a later time. If the map previously contained a value for the key and active time, it is
   * replaced.
   *
   * @param key the key with which the specified value is to be associated with
   * @param time the time the value was made active
   * @param value the value associated to the key
   * @return the previous value associated with the key, or null if was no mapping for the key
   */
  public V put(K key, Instant time, V value) {
    return getVersionMap(key).put(time, value);
  }

  /**
   * Associates the specified values with the specified key at the times they were active. New
   * versions of the value will be inserted alongside previous versions. If the map previously
   * contained a value for the key and active time, it is replaced.
   *
   * @param key the key with which the specified values are to be associated with
   * @param temporalValues Map of values keyed to the times they were active
   */
  public void putVersions(K key, Map<Instant, V> temporalValues) {
    getVersionMap(key).putAll(temporalValues);
  }

  /**
   * Copies all mappings from the specified map into this map. If this map previously contained a
   * value for a key and active time in the specified map, it is replaced.
   *
   * @param map mappings to be stored in this map
   */
  public void putAll(TemporalMap<K, V> map) {
    map.keySet().forEach(
      key -> getVersionMap(key).putAll(map.getVersionMap(key)));
  }

  /**
   * Returns the active value for the specified key at the specified time. This follows floor-like
   * behavior, in that it will find the value with the latest active time less than or equal to the
   * specified time.
   *
   * @param key the key used to retrieve a value
   * @param time the time used to retrieve the right version of the value
   * @return The value active during the specified time, or Optional.empty if no such value is
   * present
   */
  public Optional<V> getVersionFloor(K key, Instant time) {
    return Optional.ofNullable(getVersionMap(key).floorEntry(time))
      .map(Entry::getValue);
  }

  /**
   * Returns the first active value for the specified key at or after the specified time. This
   * follows ceiling-like behavior, in that it will find the value with the earliest active time
   * greater than or equal to the specified time.
   *
   * @param key the key used to retrieve a value
   * @param time the time used to retrieve the right version of the value
   * @return The first value active during or after the specified time, or Optional.empty if no such
   * value is present
   */
  public Optional<V> getVersionCeiling(K key, Instant time) {
    return Optional.ofNullable(getVersionMap(key).ceilingEntry(time))
      .map(Entry::getValue);
  }

  public Optional<Instant> getFirstVersion(K key) {
    if (getVersionMap(key) == null) {
      return Optional.empty();
    } else {
      NavigableMap<Instant, V> versionMap = getVersionMap(key);
      if (versionMap.isEmpty()) {
        return Optional.empty();
      } else {
        return Optional.of(versionMap.firstKey());
      }
    }
  }

  /**
   * Returns all values with instant keys in the given range, endpoint non-inclusive
   */
  public List<V> getVersionsInRange(K key, Range<Instant> range) {
    return getVersionMap(key).subMap(range.lowerEndpoint(), true, range.upperEndpoint(), false).values().stream()
      .collect(Collectors.toList());
  }

  /**
   * Returns all values with instant keys in the given range, endpoint non-inclusive
   */
  public NavigableMap<Instant, V> getVersionMapInRange(K key, Range<Instant> range) {
    return getVersionMap(key).subMap(range.lowerEndpoint(), true, range.upperEndpoint(), false);
  }

  /**
   * Returns all versions of a value for the specified key, ordered by time they were active.
   *
   * @param key the key used to retrieve the versions
   * @return the versions of the value for the specified key, ordered by time they were active
   */
  public List<V> getVersions(K key) {
    return List.copyOf(getVersionMap(key).values());
  }

  /**
   * Returns all versions of all values for all keys in the map, ordered by time. If values for
   * different keys have the same active time, the secondary ordering is undefined.
   *
   * @return All versions of all values in the map, sorted by active time.
   */
  public List<V> values() {
    return temporalMapsByKey.values().stream()
      .flatMap(map -> map.entrySet().stream())
      .sorted(Entry.comparingByKey())
      .map(Entry::getValue)
      .collect(toList());
  }

  public NavigableMap<Instant, V> getVersionMap(K key) {
    return temporalMapsByKey.computeIfAbsent(key, k -> new TreeMap<>());
  }

  /**
   * Factory method for generating a Stream Collector to build a TemporalMap from a Stream of
   * values
   *
   * @param keyExtractor Function for extracting a key from the value stream
   * @param timeExtractor Function for extracting an active time from the value stream
   * @param <K> Type of TemporalMap keys
   * @param <V> Type of the TemporalMap values
   * @return A Collector to build a TemporalMap
   */
  public static <K, V> TemporalMapCollector<K, V> collector(
    Function<V, K> keyExtractor, Function<V, Instant> timeExtractor) {

    return new TemporalMapCollector<>(keyExtractor, timeExtractor);

  }

  /**
   * Collector for TemporalMap given a value Stream
   *
   * @param <K> Type of the TemporalMap key
   * @param <V> Type of the TemporalMap value
   */
  public static class TemporalMapCollector<K, V> implements
    Collector<V, TemporalMap<K, V>, TemporalMap<K, V>> {

    private Function<V, K> keyExtractor;

    private Function<V, Instant> timeExtractor;

    public TemporalMapCollector(Function<V, K> keyExtractor, Function<V, Instant> timeExtractor) {

      this.keyExtractor = keyExtractor;
      this.timeExtractor = timeExtractor;
    }

    @Override
    public Supplier<TemporalMap<K, V>> supplier() {
      return TemporalMap::create;
    }

    @Override
    public BiConsumer<TemporalMap<K, V>, V> accumulator() {
      return (map, v) -> map.put(keyExtractor.apply(v), timeExtractor.apply(v), v);
    }

    @Override
    public BinaryOperator<TemporalMap<K, V>> combiner() {
      return (left, right) -> {
        left.putAll(right);
        return left;
      };
    }

    @Override
    public Function<TemporalMap<K, V>, TemporalMap<K, V>> finisher() {
      return identity();
    }

    @Override
    public Set<Characteristics> characteristics() {
      return Set.of(Characteristics.UNORDERED, Characteristics.IDENTITY_FINISH);
    }

  }
}