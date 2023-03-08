package gms.shared.frameworks.utilities;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.junit.jupiter.api.Test;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.EnumMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SumStatsAccumulatorTests {

  enum TestKey {
    KEY1,
    KEY2,
    KEY3
  }

  ;

  @Test
  void testSumStatsAccumulatorWithIntegerKeys() {
    Integer[] iKeys = new Integer[]{
      0, 1, 2, 3, 4
    };
    performTests(iKeys, null, null);
  }

  @Test
  void testSumStatsAccumulatorWithEnumKeys() {
    performTests(TestKey.values(), new EnumMap<>(TestKey.class), new EnumMap<>(TestKey.class));
  }

  private static <K> void performTests(K[] keys,
    Map<K, SummaryStatistics> map1,
    Map<K, SummaryStatistics> map2) {

    final int numKeys = keys.length;

    double[] mins = new double[numKeys];
    for (int i = 0; i < numKeys; i++) {
      mins[i] = i * 2.0;
    }

    double[] maxs = new double[numKeys];
    for (int i = 0; i < maxs.length; i++) {
      maxs[i] = mins[i] + (1 + 1) * 10.0;
    }

    SecureRandom random = new SecureRandom("my test seed".getBytes());

    int[] numSamples = new int[numKeys];
    for (int i = 0; i < numSamples.length; i++) {
      numSamples[i] = 1 + random.nextInt(100);
    }

    SumStatsAccumulator<K> accumulator1 = map1 != null ? new SumStatsAccumulator<>(map1) :
      new SumStatsAccumulator<>();
    SumStatsAccumulator<K> accumulator2 = map2 != null ? new SumStatsAccumulator<>(map2) :
      new SumStatsAccumulator<>();

    Instant now = Instant.now();
    accumulator1.reset(now);
    accumulator2.reset(now);

    assertEquals(accumulator1.hashCode(), accumulator2.hashCode());
    assertEquals(accumulator1, accumulator2);

    for (int i = 0; i < numKeys; i++) {

      K key = keys[i];
      double min = mins[i];
      double max = maxs[i];
      int n = numSamples[i];

      assertEquals(0, accumulator1.getN(key));
      assertEquals(Double.doubleToLongBits(Double.NaN),
        Double.doubleToLongBits(accumulator1.getMean(key)));

      for (int j = 0; j < n; j++) {
        double value = min + random.nextDouble() * (max - min);
        accumulator1.addValue(key, value);
        accumulator2.addValue(key, value);
      }

      assertEquals(n, accumulator1.getN(key));
      assertTrue(accumulator1.getMin(key) >= min);
      assertTrue(accumulator1.getMax(key) <= max);
      assertTrue(accumulator1.getMean(key) >= min && accumulator1.getMean(key) <= max);
    }

    assertEquals(accumulator1.hashCode(), accumulator2.hashCode());
    assertEquals(accumulator1, accumulator2);

    accumulator1.reset();

    assertNotEquals(accumulator1, accumulator2);

    for (int i = 0; i < numKeys; i++) {
      assertEquals(0, accumulator1.getN(keys[i]));
    }

  }
}
