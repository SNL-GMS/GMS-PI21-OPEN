package gms.shared.signaldetection.converter.measurementvalues;

import gms.shared.signaldetection.coi.values.ArrivalTimeMeasurementValue;
import gms.shared.signaldetection.coi.values.InstantValue;
import gms.shared.signaldetection.converter.measurementvalue.specs.MeasurementValueSpec;

import java.time.Duration;
import java.util.Optional;

public class ArrivalTimeMeasurementValueConverter implements
  MeasurementValueConverter<ArrivalTimeMeasurementValue> {

  private ArrivalTimeMeasurementValueConverter() {
  }

  public static ArrivalTimeMeasurementValueConverter create() {
    return new ArrivalTimeMeasurementValueConverter();
  }

  @Override
  public Optional<ArrivalTimeMeasurementValue> convert(MeasurementValueSpec<ArrivalTimeMeasurementValue> spec) {
    var arrivalDao = spec.getArrivalDao();

    var arrivalTime = InstantValue.from(arrivalDao.getArrivalKey().getTime(),
      Duration.ofMillis((long) (arrivalDao.getTimeUncertainty() * Math.pow(10, 3))));
    return Optional.of(ArrivalTimeMeasurementValue.fromFeatureMeasurement(arrivalTime));
  }
}
