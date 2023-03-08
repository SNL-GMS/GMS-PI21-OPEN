package gms.shared.featureprediction.utilities.view;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Immutable2dArrayTest {

  static Double[][] values;
  static Immutable2dArray<Double> immutableDoubleArray;

  @BeforeAll
  static void initialize() {
    values = new Double[][]{{1.0, 2.0, 3.0}, {4.0, 5.0, 6.0}, {7.0, 8.0, 9.0}};
    immutableDoubleArray = Immutable2dArray.from(Double.class, values);
  }

  @Test
  void testGetRowCount() {
    assertEquals(3, immutableDoubleArray.rowCount());
  }

  @Test
  void testGetColumnCount() {
    assertEquals(3, immutableDoubleArray.columnCount());
  }

  @Test
  void testGetValue() {

    int rowCount = immutableDoubleArray.rowCount();
    int columnCount = immutableDoubleArray.columnCount();

    assertEquals(9.0, immutableDoubleArray.getValue(rowCount - 1, columnCount - 1));

    Assertions.assertThrows(
      IndexOutOfBoundsException.class,
      () -> immutableDoubleArray.getValue(rowCount, 1)
    );

    Assertions.assertThrows(
      IndexOutOfBoundsException.class,
      () -> immutableDoubleArray.getValue(1, columnCount)
    );
  }

  @Test
  void testEmptyArray() {
    Double[][] values2 = new Double[][]{};
    Immutable2dArray<Double> immutableDoubleArray2 = Immutable2dArray.from(Double.class, values2);

    Assertions.assertEquals(0, immutableDoubleArray2.rowCount());
    Assertions.assertEquals(0, immutableDoubleArray2.columnCount());
    Assertions.assertThrows(
      IndexOutOfBoundsException.class,
      () -> immutableDoubleArray2.getValue(0, 0)
    );
  }

  @Test
  void testEmptyFirstRow() {
    Double[][] values2 = new Double[][]{{}, {4.0, 5.0, 6.0}, {7.0, 8.0, 9.0}};
    Assertions.assertThrows(
      IllegalArgumentException.class,
      () -> Immutable2dArray.from(Double.class, values2)
    );
  }


  @Test
  void testRaggedArray() {
    Double[][] values2 = new Double[][]{{1.0, 2.0, 3.0}, {4.0, 5.0}, {7.0, 8.0, 9.0}};
    Assertions.assertThrows(
      IllegalArgumentException.class,
      () -> Immutable2dArray.from(Double.class, values2)
    );
  }

  @Test
  void testCopyOf() {
    assertTrue(Arrays.deepEquals(values, immutableDoubleArray.copyOf()));
  }
}