package gms.shared.signaldetection.converter.measurementvalue.specs;

import gms.shared.signaldetection.coi.types.FeatureMeasurementType;
import gms.shared.signaldetection.coi.values.PhaseTypeMeasurementValue;
import gms.shared.signaldetection.dao.css.AmplitudeDao;
import gms.shared.signaldetection.dao.css.ArrivalDao;
import gms.shared.signaldetection.dao.css.AssocDao;

import java.util.Optional;
import java.util.stream.Stream;

public class PhaseTypeMeasurementValueSpecAcceptor implements MeasurementValueSpecAcceptorInterface<PhaseTypeMeasurementValue> {
  private PhaseTypeMeasurementValueSpecAcceptor() {
  }

  public static PhaseTypeMeasurementValueSpecAcceptor create() {
    return new PhaseTypeMeasurementValueSpecAcceptor();
  }

  @Override
  public Stream<MeasurementValueSpec<PhaseTypeMeasurementValue>> accept(
    MeasurementValueSpecVisitorInterface<PhaseTypeMeasurementValue> visitor,
    FeatureMeasurementType<PhaseTypeMeasurementValue> type,
    ArrivalDao arrivalDao,
    Optional<AssocDao> assocDao,
    Optional<AmplitudeDao> amplitudeDao) {
    return visitor.visit(this, type, arrivalDao, assocDao);
  }
}
