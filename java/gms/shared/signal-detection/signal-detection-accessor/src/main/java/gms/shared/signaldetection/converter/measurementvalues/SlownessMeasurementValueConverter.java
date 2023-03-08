package gms.shared.signaldetection.converter.measurementvalues;

import gms.shared.signaldetection.coi.values.NumericMeasurementValue;
import gms.shared.signaldetection.converter.measurementvalue.specs.MeasurementValueSpec;

import java.util.Optional;

public class SlownessMeasurementValueConverter implements
  MeasurementValueConverter<NumericMeasurementValue> {

  private SlownessMeasurementValueConverter() {
  }

  public static SlownessMeasurementValueConverter create() {
    return new SlownessMeasurementValueConverter();
  }

  @Override
  public Optional<NumericMeasurementValue> convert(MeasurementValueSpec<NumericMeasurementValue> spec) {
    return NumericMeasurementValueConverter.create().convert(spec);
  }
}
