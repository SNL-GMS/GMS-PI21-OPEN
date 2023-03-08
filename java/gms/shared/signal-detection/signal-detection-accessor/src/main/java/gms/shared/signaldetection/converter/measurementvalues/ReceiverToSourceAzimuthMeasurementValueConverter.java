package gms.shared.signaldetection.converter.measurementvalues;

import gms.shared.signaldetection.coi.values.NumericMeasurementValue;
import gms.shared.signaldetection.converter.measurementvalue.specs.MeasurementValueSpec;

import java.util.Optional;

public class ReceiverToSourceAzimuthMeasurementValueConverter implements
  MeasurementValueConverter<NumericMeasurementValue> {

  private ReceiverToSourceAzimuthMeasurementValueConverter() {
  }

  public static ReceiverToSourceAzimuthMeasurementValueConverter create() {
    return new ReceiverToSourceAzimuthMeasurementValueConverter();
  }

  @Override
  public Optional<NumericMeasurementValue> convert(MeasurementValueSpec<NumericMeasurementValue> spec) {
    return NumericMeasurementValueConverter.create().convert(spec);
  }
}
