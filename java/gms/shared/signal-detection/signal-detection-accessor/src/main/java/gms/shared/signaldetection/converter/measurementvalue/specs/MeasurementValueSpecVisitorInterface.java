package gms.shared.signaldetection.converter.measurementvalue.specs;

import gms.shared.signaldetection.coi.types.FeatureMeasurementType;
import gms.shared.signaldetection.dao.css.AmplitudeDao;
import gms.shared.signaldetection.dao.css.ArrivalDao;
import gms.shared.signaldetection.dao.css.AssocDao;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Visitor interface to create {@link MeasurementValueSpec} objects for featurement measurement types
 *
 * @param <V> measurement value class for given FM type
 */
public interface MeasurementValueSpecVisitorInterface<V> {

  /**
   * Visitor methods for each feature measurement type, includes {@link ArrivalDao}
   * and optional {@link AssocDao} inputs for creating {@link MeasurementValueSpec}
   *
   * @param specAcceptor measurement value spec acceptor
   * @param type {@link FeatureMeasurementType}
   * @param arrivalDao {@link ArrivalDao}
   * @param assocDao {@link AssocDao}
   * @return {@link MeasurementValueSpec}
   */
  Stream<MeasurementValueSpec<V>> visit(ArrivalTimeMeasurementValueSpecAcceptor specAcceptor,
    FeatureMeasurementType<V> type, ArrivalDao arrivalDao, Optional<AssocDao> assocDao);

  /**
   * Visitor methods for each feature measurement type, includes {@link ArrivalDao}
   * and optional {@link AssocDao} inputs for creating {@link MeasurementValueSpec}
   *
   * @param specAcceptor measurement value spec acceptor
   * @param type {@link FeatureMeasurementType}
   * @param arrivalDao {@link ArrivalDao}
   * @param assocDao {@link AssocDao}
   * @return {@link MeasurementValueSpec}
   */
  Stream<MeasurementValueSpec<V>> visit(EmergenceAngleMeasurementValueSpecAcceptor specAcceptor,
    FeatureMeasurementType<V> type, ArrivalDao arrivalDao, Optional<AssocDao> assocDao);

  /**
   * Visitor methods for each feature measurement type, includes {@link ArrivalDao}
   * and optional {@link AssocDao} inputs for creating {@link MeasurementValueSpec}
   *
   * @param specAcceptor measurement value spec acceptor
   * @param type {@link FeatureMeasurementType}
   * @param arrivalDao {@link ArrivalDao}
   * @param assocDao {@link AssocDao}
   * @return {@link MeasurementValueSpec}
   */
  Stream<MeasurementValueSpec<V>> visit(FirstMotionMeasurementValueSpecAcceptor specAcceptor,
    FeatureMeasurementType<V> type, ArrivalDao arrivalDao, Optional<AssocDao> assocDao);

  /**
   * Visitor methods for each feature measurement type, includes {@link ArrivalDao}
   * and optional {@link AssocDao} inputs for creating {@link MeasurementValueSpec}
   *
   * @param specAcceptor measurement value spec acceptor
   * @param type {@link FeatureMeasurementType}
   * @param arrivalDao {@link ArrivalDao}
   * @param assocDao {@link AssocDao}
   * @return {@link MeasurementValueSpec}
   */
  Stream<MeasurementValueSpec<V>> visit(PhaseTypeMeasurementValueSpecAcceptor specAcceptor,
    FeatureMeasurementType<V> type, ArrivalDao arrivalDao, Optional<AssocDao> assocDao);

  /**
   * Visitor methods for each feature measurement type, includes {@link ArrivalDao}
   * and optional {@link AssocDao} inputs for creating {@link MeasurementValueSpec}
   *
   * @param specAcceptor measurement value spec acceptor
   * @param type {@link FeatureMeasurementType}
   * @param arrivalDao {@link ArrivalDao}
   * @param assocDao {@link AssocDao}
   * @return {@link MeasurementValueSpec}
   */
  Stream<MeasurementValueSpec<V>> visit(ReceiverToSourceAzimuthMeasurementValueSpecAcceptor specAcceptor,
    FeatureMeasurementType<V> type, ArrivalDao arrivalDao, Optional<AssocDao> assocDao);

  /**
   * Visitor methods for each feature measurement type, includes {@link ArrivalDao}
   * and optional {@link AssocDao} inputs for creating {@link MeasurementValueSpec}
   *
   * @param specAcceptor measurement value spec acceptor
   * @param type {@link FeatureMeasurementType}
   * @param arrivalDao {@link ArrivalDao}
   * @param assocDao {@link AssocDao}
   * @return {@link MeasurementValueSpec}
   */
  Stream<MeasurementValueSpec<V>> visit(RectilinearityMeasurementValueSpecAcceptor specAcceptor,
    FeatureMeasurementType<V> type, ArrivalDao arrivalDao, Optional<AssocDao> assocDao);

  /**
   * Visitor methods for each feature measurement type, includes {@link ArrivalDao}
   * and optional {@link AssocDao} inputs for creating {@link MeasurementValueSpec}
   *
   * @param specAcceptor measurement value spec acceptor
   * @param type {@link FeatureMeasurementType}
   * @param arrivalDao {@link ArrivalDao}
   * @param assocDao {@link AssocDao}
   * @return {@link MeasurementValueSpec}
   */
  Stream<MeasurementValueSpec<V>> visit(SlownessMeasurementValueSpecAcceptor specAcceptor,
    FeatureMeasurementType<V> type, ArrivalDao arrivalDao, Optional<AssocDao> assocDao);

  /**
   * Visitor methods for each feature measurement type, includes {@link ArrivalDao}
   * and optional {@link AmplitudeDao} inputs for creating {@link MeasurementValueSpec}
   *
   * @param specAcceptor measurement value spec acceptor
   * @param type {@link FeatureMeasurementType}
   * @param arrivalDao {@link ArrivalDao}
   * @param amplitudeDao {@link AmplitudeDao}
   * @return {@link MeasurementValueSpec}
   */
  Stream<MeasurementValueSpec<V>> visit(AmplitudeMeasurementValueSpecAcceptor specAcceptor,
    FeatureMeasurementType<V> type, ArrivalDao arrivalDao, Optional<AmplitudeDao> amplitudeDao);
}
