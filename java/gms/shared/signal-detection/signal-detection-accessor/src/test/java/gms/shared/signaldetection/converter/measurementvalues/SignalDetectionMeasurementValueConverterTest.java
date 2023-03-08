package gms.shared.signaldetection.converter.measurementvalues;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Generic signal detection measurement converter that tests null
 * inputs to the measurement value converters
 *
 * @param <T> the type of measurement value converter
 */
abstract class SignalDetectionMeasurementValueConverterTest<T
  extends MeasurementValueConverter<?>> {
  T converter;

  void testConvertNull(T converter) {
    this.converter = converter;
    assertThrows(NullPointerException.class,
      () -> converter.convert(null));
  }
}
