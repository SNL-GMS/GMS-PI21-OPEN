package gms.shared.frameworks.osd.coi.waveforms;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertTrue;

class Immutable2dArrayTests {

  @Test
  void testCopyOf() {
    double[][] expected = new double[][]{{1, 2, 3}, {4, 5, 6}, {7, 8, 9}};
    assertTrue(Arrays.deepEquals(expected, Immutable2dDoubleArray.from(expected).copyOf()));
  }
}
