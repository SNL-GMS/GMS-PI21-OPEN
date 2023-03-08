package gms.shared.signaldetection.converter.measurementvalue.specs;

import gms.shared.signaldetection.coi.types.FeatureMeasurementType;
import gms.shared.signaldetection.coi.values.ArrivalTimeMeasurementValue;
import gms.shared.signaldetection.dao.css.AmplitudeDao;
import gms.shared.signaldetection.dao.css.ArrivalDao;
import gms.shared.signaldetection.dao.css.AssocDao;

import java.util.Optional;
import java.util.stream.Stream;

public class ArrivalTimeMeasurementValueSpecAcceptor implements MeasurementValueSpecAcceptorInterface<ArrivalTimeMeasurementValue> {
  private ArrivalTimeMeasurementValueSpecAcceptor() {
  }

  public static ArrivalTimeMeasurementValueSpecAcceptor create() {
    return new ArrivalTimeMeasurementValueSpecAcceptor();
  }

  @Override
  public Stream<MeasurementValueSpec<ArrivalTimeMeasurementValue>> accept(
    MeasurementValueSpecVisitorInterface<ArrivalTimeMeasurementValue> visitor,
    FeatureMeasurementType<ArrivalTimeMeasurementValue> type,
    ArrivalDao arrivalDao,
    Optional<AssocDao> assocDao,
    Optional<AmplitudeDao> amplitudeDao) {
    return visitor.visit(this, type, arrivalDao, assocDao);
  }
}