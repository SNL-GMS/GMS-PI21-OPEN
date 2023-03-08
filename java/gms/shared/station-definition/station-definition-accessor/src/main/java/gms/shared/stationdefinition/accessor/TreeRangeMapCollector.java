package gms.shared.stationdefinition.accessor;

import com.google.common.collect.Range;
import com.google.common.collect.TreeRangeMap;

import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;


public class TreeRangeMapCollector<K extends Comparable<? super K>, V> implements Collector<Map.Entry<Range<K>, V>, TreeRangeMap<K, V>, TreeRangeMap<K, V>> {

  @Override
  public Supplier<TreeRangeMap<K, V>> supplier() {
    return TreeRangeMap::create;
  }

  @Override
  public BiConsumer<TreeRangeMap<K, V>, Map.Entry<Range<K>, V>> accumulator() {
    return (rangeMap, entry) -> rangeMap.put(entry.getKey(), entry.getValue());
  }

  @Override
  public BinaryOperator<TreeRangeMap<K, V>> combiner() {
    return (left, right) -> {
      left.putAll(right);
      return left;
    };
  }

  @Override
  public Function<TreeRangeMap<K, V>, TreeRangeMap<K, V>> finisher() {
    return Function.identity();
  }

  @Override
  public Set<Characteristics> characteristics() {
    return Set.of(Characteristics.IDENTITY_FINISH);
  }
}
