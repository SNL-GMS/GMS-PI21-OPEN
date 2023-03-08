package gms.shared.signaldetection.converter.measurementvalue.specs;

import gms.shared.signaldetection.coi.types.FeatureMeasurementType;
import gms.shared.signaldetection.dao.css.ArrivalDao;

import java.util.stream.Stream;

public interface MeasurementValueConverterSpec<V> {
  Stream<MeasurementValueSpec<V>> accept(MeasurementValueSpecVisitor<V> visitor, FeatureMeasurementType<V> type,
    ArrivalDao arrivalDao);
}
