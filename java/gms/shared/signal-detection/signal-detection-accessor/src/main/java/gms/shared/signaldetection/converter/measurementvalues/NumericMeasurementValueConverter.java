package gms.shared.signaldetection.converter.measurementvalues;

import gms.shared.signaldetection.coi.values.NumericMeasurementValue;
import gms.shared.signaldetection.converter.measurementvalue.specs.MeasurementValueSpec;
import gms.shared.stationdefinition.coi.utils.DoubleValue;

import java.util.NoSuchElementException;
import java.util.Optional;

public class NumericMeasurementValueConverter implements
  MeasurementValueConverter<NumericMeasurementValue> {

  private NumericMeasurementValueConverter() {
  }

  public static NumericMeasurementValueConverter create() {
    return new NumericMeasurementValueConverter();
  }

  @Override
  public Optional<NumericMeasurementValue> convert(MeasurementValueSpec<NumericMeasurementValue> measurementValueSpec) {
    var arrivalDao = measurementValueSpec.getArrivalDao();

    var measuredValueExtractor = measurementValueSpec
      .getMeasuredValueExtractor().orElseThrow(() ->
        new NoSuchElementException("Measured value extractor does not exist"));
    var units = measurementValueSpec.getUnits().orElseThrow(() ->
      new NoSuchElementException("Units does not exist"));

    Optional<Double> uncertaintyOptional = measurementValueSpec.getUncertaintyValueExtractor().map(
      arrivalDaoToDoubleFunction -> arrivalDaoToDoubleFunction.applyAsDouble(arrivalDao));
    return Optional.of(NumericMeasurementValue.from(Optional.empty(),
      DoubleValue.from(measuredValueExtractor.applyAsDouble(arrivalDao), uncertaintyOptional, units)));
  }
}
