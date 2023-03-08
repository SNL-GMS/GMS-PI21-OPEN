package gms.shared.signaldetection.converter.measurementvalues;

import gms.shared.signaldetection.coi.types.FirstMotionType;
import gms.shared.signaldetection.coi.values.FirstMotionMeasurementValue;
import gms.shared.signaldetection.converter.measurementvalue.specs.MeasurementValueSpec;

import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.Optional;

public class FirstMotionMeasurementValueConverter implements
  MeasurementValueConverter<FirstMotionMeasurementValue> {

  private FirstMotionMeasurementValueConverter() {
  }

  public static FirstMotionMeasurementValueConverter create() {
    return new FirstMotionMeasurementValueConverter();
  }

  @Override
  public Optional<FirstMotionMeasurementValue> convert(MeasurementValueSpec<FirstMotionMeasurementValue> spec) {
    var arrivalDao = spec.getArrivalDao();
    Optional<Instant> optionalTime = Optional.ofNullable(arrivalDao.getArrivalKey().getTime());

    String featureMeasurementTypeCode = spec.getFeatureMeasurementTypeCode().orElseThrow(() ->
      new NoSuchElementException("Feature measurement type index does not exist"));
    return
      Optional.of(FirstMotionMeasurementValue.from(
        FirstMotionType.fromCode(featureMeasurementTypeCode),
        Optional.empty(), optionalTime));
  }
}
