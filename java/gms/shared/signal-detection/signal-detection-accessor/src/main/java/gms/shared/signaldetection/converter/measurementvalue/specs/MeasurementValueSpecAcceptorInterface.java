package gms.shared.signaldetection.converter.measurementvalue.specs;

import gms.shared.signaldetection.coi.types.FeatureMeasurementType;
import gms.shared.signaldetection.dao.css.AmplitudeDao;
import gms.shared.signaldetection.dao.css.ArrivalDao;
import gms.shared.signaldetection.dao.css.AssocDao;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Acceptor class that takes in a visitor and applies its corresponding visit method.
 * This creates the Acceptor/Visitor pattern for generalizing similar class methods
 *
 * @param <V> input measurement value type
 */
public interface MeasurementValueSpecAcceptorInterface<V> {

  /**
   * Accept method for {@link ArrivalDao} input to execute vistor pattern
   *
   * @param visitor visitor converter class
   * @param type feature measurement type
   * @param arrivalDao {@link ArrivalDao}
   * @param assocDao optional {@link AssocDao}
   * @param amplitudeDao optional {@link AmplitudeDao}
   * @return stream of {@link MeasurementValueSpec}
   */
  Stream<MeasurementValueSpec<V>> accept(MeasurementValueSpecVisitorInterface<V> visitor,
    FeatureMeasurementType<V> type,
    ArrivalDao arrivalDao,
    Optional<AssocDao> assocDao,
    Optional<AmplitudeDao> amplitudeDao);
}
