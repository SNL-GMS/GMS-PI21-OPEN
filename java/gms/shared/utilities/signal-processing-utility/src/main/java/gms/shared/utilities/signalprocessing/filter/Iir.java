package gms.shared.utilities.signalprocessing.filter;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import gms.shared.frameworks.osd.coi.signaldetection.FilterDefinition;
import gms.shared.frameworks.osd.coi.waveforms.Waveform;

import java.util.Arrays;

/**
 * Provides the ability to filter using an Infinite Impulse Response(Iir).
 */
public class Iir {

  private Iir() {
  }

  public static Waveform filter(Waveform input, FilterDefinition filterDefinition) {
    return Waveform.from(input.getStartTime(), input.getSampleRate(),
      filter(input.getValues(), filterDefinition.getACoefficients(),
        filterDefinition.getBCoefficients()));
  }

  /*
      y[n] = 1/a0 ( sum{i = 0 to P}(bi * x[n - i]) - sum{j = 1 to Q}(aj * y[n - j]) )
      where:
      P = feedforward filter order
      bi = ith coefficient of feedforward filter
      Q = feedback filter order
      aj = jth coefficient of feedback filter
      x[n] = nth value of input signal
      y[n] = nth value of output signal
     */

  /**
   * Filters the input using the Iir A and B coefficients. Note that the coefficients will be
   * normalized relative to the first A coefficient, such that a[0] = 1 before processing begins.
   *
   * @param input The input signal data
   * @param aCoefficients The a coefficients of the IIR filter
   * @param bCoefficients The b coefficients of the IIR filter
   * @return The filtered result, trimmed to the size of the input to remove padding introduced
   * during processing.
   */
  public static double[] filter(double[] input, double[] aCoefficients, double[] bCoefficients) {
    validateArguments(input, aCoefficients, bCoefficients);

    double[] normalizedACoefficients = scale(aCoefficients, 1 / aCoefficients[0]);
    double[] normalizedBCoefficients = scale(bCoefficients, 1 / aCoefficients[0]);

    int aPad = aCoefficients.length - 1;
    int bPad = bCoefficients.length - 1;

    double[] paddedInput = padLeft(input, bPad);
    double[] paddedOutput = new double[input.length + aPad];

    double feedForwardValue;
    double feedBackValue;
    for (int i = 0; i < input.length; i++) {
      feedForwardValue = convolve(paddedInput, i + bPad, normalizedBCoefficients);
      feedBackValue = convolve(paddedOutput, i + aPad, normalizedACoefficients, 1,
        normalizedACoefficients.length);
      paddedOutput[i + aPad] = feedForwardValue - feedBackValue;
    }

    return Arrays.copyOfRange(paddedOutput, aPad, paddedOutput.length);
  }

  private static void validateArguments(double[] input, double[] aCoefficients,
    double[] bCoefficients) {
    checkNotNull(input);
    checkNotNull(aCoefficients);
    checkNotNull(bCoefficients);

    checkArgument(input.length > 0, "Error filtering: cannot filter empty input array");
    checkArgument(aCoefficients.length > 0,
      "Error filtering: cannot filter using empty aCoefficients array");
    checkArgument(bCoefficients.length > 0,
      "Error filtering: cannot filter using empty bCoefficients array");

    checkArgument(aCoefficients[0] != 0, "Error filtering: first a coefficent cannot be 0");
  }

  private static double[] scale(double[] values, double scalar) {
    return Arrays.stream(values)
      .map(value -> scalar * value)
      .toArray();
  }

  private static double convolve(double[] values, int index, double[] coefficients) {
    return convolve(values, index, coefficients, 0, coefficients.length);
  }

  private static double convolve(double[] values, int index,
    double[] coefficients, int from, int to) {
    double convolution = 0;
    for (int i = from; i < to; i++) {
      convolution += coefficients[i] * values[index - i];
    }

    return convolution;
  }

  private static double[] padLeft(double[] values, int padLength) {
    double[] paddedValues = new double[values.length + padLength];
    System.arraycopy(values, 0, paddedValues, padLength, values.length);
    return paddedValues;
  }

}
