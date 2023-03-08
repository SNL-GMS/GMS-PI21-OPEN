package gms.shared.utilities.signalprocessing.normalization;


import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class DeMeanerTests {

  private static double[] dataSet;
  private static double[] validationSet;

  @BeforeAll
  public static void setupClass() {
    dataSet = new double[100];
    validationSet = new double[100];

    double mean = 7.0;

    int offset = 1;
    boolean increment = true;
    for (int i = 0; i < dataSet.length / 2; i++) {
      dataSet[i] = offset + mean;
      dataSet[dataSet.length - 1 - i] = mean - offset;

      validationSet[i] = Math.abs(dataSet[i]) - mean;
      validationSet[validationSet.length - 1 - i] =
        Math.abs(dataSet[dataSet.length - 1 - i]) - mean;
      if (offset == 5) {
        offset--;
        increment = false;
      } else if (offset == 1) {
        offset++;
        increment = true;
      } else if (increment) {
        offset++;
      } else {
        offset--;
      }
    }
  }

  @Test
  void testDemeanNullDataExpectNullPointerException() {

    NullPointerException actual = assertThrows(NullPointerException.class,
      () -> DeMeaner.demean(null));
    assertEquals("Cannot demean data from a null dataset", actual.getMessage());
  }

  @Test
  void testDemeanEmptyDataExpectIllegalArgumentException() {
    IllegalArgumentException actual = assertThrows(IllegalArgumentException.class,
      () -> DeMeaner.demean(new double[]{}));
    assertEquals("Cannot demean data for an empty dataset", actual.getMessage());
  }

  @Test
  void testDemean() {
    double[] demeanedData = DeMeaner.demean(dataSet);
    assertArrayEquals(validationSet, demeanedData, 0.0000001);
  }

}
