package gms.shared.signaldetection.converter.measurementvalues;

import gms.shared.signaldetection.coi.types.PhaseType;
import gms.shared.signaldetection.coi.values.PhaseTypeMeasurementValue;
import gms.shared.signaldetection.converter.measurementvalue.specs.MeasurementValueSpec;
import gms.shared.signaldetection.dao.css.ArrivalDao;
import gms.shared.signaldetection.dao.css.AssocDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class PhaseTypeMeasurementValueConverter implements
  MeasurementValueConverter<PhaseTypeMeasurementValue> {

  private static final Logger logger = LoggerFactory.getLogger(PhaseTypeMeasurementValueConverter.class);

  private PhaseTypeMeasurementValueConverter() {
  }

  public static PhaseTypeMeasurementValueConverter create() {
    return new PhaseTypeMeasurementValueConverter();
  }

  @Override
  public Optional<PhaseTypeMeasurementValue> convert(MeasurementValueSpec<PhaseTypeMeasurementValue> spec) {
    var arrivalDao = spec.getArrivalDao();
    var assocDaoOptional = spec.getAssocDao();

    // if AssocDao exists use this to create measurement value else use ArrivalDao
    return assocDaoOptional.map(this::createPhaseTypeMeasurementValue)
      .orElseGet(() -> createPhaseTypeMeasurementValue(arrivalDao));
  }

  /**
   * Create {@link PhaseTypeMeasurementValue} from the {@link ArrivalDao}
   *
   * @param arrivalDao {@link ArrivalDao} input
   * @return optional of {@link PhaseTypeMeasurementValue}
   */
  private Optional<PhaseTypeMeasurementValue> createPhaseTypeMeasurementValue(ArrivalDao arrivalDao) {
    try {
      return Optional.of(PhaseTypeMeasurementValue.fromFeatureMeasurement(PhaseType.valueOfLabel(arrivalDao.getPhase()),
        Optional.empty(), arrivalDao.getArrivalKey().getTime()));
    } catch (IllegalArgumentException ex) {
      logger.info("Cannot map phase type {}", arrivalDao.getPhase());
      return Optional.of(PhaseTypeMeasurementValue.fromFeatureMeasurement(PhaseType.UNKNOWN,
        Optional.empty(), arrivalDao.getArrivalKey().getTime()));
    }
  }

  /**
   * Create {@link PhaseTypeMeasurementValue} from the {@link AssocDao}
   *
   * @param assocDao {@link AssocDao} input
   * @return optional of {@link PhaseTypeMeasurementValue}
   */
  private Optional<PhaseTypeMeasurementValue> createPhaseTypeMeasurementValue(AssocDao assocDao) {

    Optional<Double> assocBeliefVal = assocDao.getBelief() == -1 ? Optional.empty() : Optional.of(assocDao.getBelief());

    try {
      return Optional.of(PhaseTypeMeasurementValue.fromFeaturePrediction(PhaseType.valueOfLabel(assocDao.getPhase()),
        assocBeliefVal));
    } catch (IllegalArgumentException ex) {
      logger.info("Cannot map phase type {}", assocDao.getPhase());
      return Optional.of(PhaseTypeMeasurementValue.fromFeaturePrediction(PhaseType.UNKNOWN,
        assocBeliefVal));
    }
  }
}
