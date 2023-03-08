package gms.dataacquisition.data.preloader.generator;

import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AceiAnalogStatusGeneratorTest {

  @Test
  void testWithNoNoise() {
    final double MIN = -Double.MAX_VALUE;
    final double MAX = Double.MAX_VALUE;
    final double BETA_0 = 5.0;
    final double BETA_1 = -0.1;
    final double STDERR = 0.0;
    final double INIT = 1.0;

    AceiAnalogStatusGenerator generator =
      new AceiAnalogStatusGenerator(MIN, MAX, BETA_0, BETA_1, STDERR, INIT);

    double expected;
    expected = BETA_0 + BETA_1 * INIT;
    assertEquals(expected, generator.next());
    expected = BETA_0 + BETA_1 * expected;
    assertEquals(expected, generator.next());
    expected = BETA_0 + BETA_1 * expected;
    assertEquals(expected, generator.next());
    expected = BETA_0 + BETA_1 * expected;
    assertEquals(expected, generator.next());
    expected = BETA_0 + BETA_1 * expected;
    assertEquals(expected, generator.next());
  }

  @Test
  void testClipping() {
    final double MIN = 4.9;
    final double MAX = 5.1;
    final double BETA_0 = 5.0;
    final double BETA_1 = -0.1;
    final double STDERR = 10.0;
    final double INIT = 1.0;

    AceiAnalogStatusGenerator generator =
      new AceiAnalogStatusGenerator(MIN, MAX, BETA_0, BETA_1, STDERR, INIT);

    IntStream.range(0, 50)
      .forEach(i -> {
          double value = generator.next();
          assertTrue(MIN <= value);
          assertTrue(value <= MAX);
        }
      );
  }

  @Test
  void testInvalidMinMax() {
    final double MIN = 1.0;
    final double MAX = -1.0;
    final double BETA_0 = 5.0;
    final double BETA_1 = -0.1;
    final double STDERR = 10.0;
    final double INIT = 1.0;

    assertThrows(IllegalArgumentException.class,
      () -> new AceiAnalogStatusGenerator(MIN, MAX, BETA_0, BETA_1, STDERR, INIT)
    );
  }

  @Test
  void testInvalidStderr() {
    final double MIN = -Double.MAX_VALUE;
    final double MAX = Double.MAX_VALUE;
    final double BETA_0 = 5.0;
    final double BETA_1 = -0.1;
    final double STDERR = -1.0;
    final double INIT = 1.0;

    assertThrows(IllegalArgumentException.class,
      () -> new AceiAnalogStatusGenerator(MIN, MAX, BETA_0, BETA_1, STDERR, INIT)
    );
  }

}
