package gms.shared.event.repository.converter;

import gms.shared.event.coi.Ellipse;
import gms.shared.event.coi.LocationUncertainty;
import gms.shared.event.coi.ScalingFactorType;
import gms.shared.event.dao.EventControlDao;
import gms.shared.event.dao.OrigerrDao;
import gms.shared.event.repository.BridgedEhInformation;
import gms.shared.signaldetection.dao.css.converter.DurationToDoubleConverter;

import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Utility class the help create {@link LocationUncertainty} objects
 */
public class LocationUncertaintyConverterUtility {

  private static final DurationToDoubleConverter durationToDoubleConverter = new DurationToDoubleConverter();

  private LocationUncertaintyConverterUtility() {
  }

  /**
   * Creates a {@link LocationUncertainty} from {@link OrigerrDao} and {@link EventControlDao}
   *
   * @param ehInfo relevant bridged event hypothesis info
   * @return {@link LocationUncertainty}
   */
  public static LocationUncertainty fromLegacyToLocationUncertainty(BridgedEhInformation ehInfo) {
    checkNotNull(ehInfo, "Bridged EventHypothesis Information must not be null");
    var origErrDao = ehInfo.getOrigerrDao();

    var locationUncertaintyBuilder = LocationUncertainty.builder()
      .setXx(origErrDao.getCovarianceMatrixSxx())
      .setXy(origErrDao.getCovarianceMatrixSxy())
      .setXz(origErrDao.getCovarianceMatrixSxz())
      .setYy(origErrDao.getCovarianceMatrixSyy())
      .setYz(origErrDao.getCovarianceMatrixSyz())
      .setXt(origErrDao.getCovarianceMatrixStx())
      .setYt(origErrDao.getCovarianceMatrixSty())
      .setZt(origErrDao.getCovarianceMatrixStz())
      .setTt(origErrDao.getCovarianceMatrixStt())
      .setZz(origErrDao.getCovarianceMatrixSzz())
      .setStDevOneObservation(origErrDao.getStandardErrorOfObservations())
      .setEllipsoids(Set.of());

    var confidenceEllipse = buildConfidenceEllipse(origErrDao);
    var coverageEllipse = ehInfo.getEventControlDao()
      .map(eventControlDao -> buildCoverageEllipse(origErrDao, eventControlDao));

    locationUncertaintyBuilder.addEllipse(confidenceEllipse);
    coverageEllipse.ifPresent(locationUncertaintyBuilder::addEllipse);

    return locationUncertaintyBuilder.build();
  }

  private static Ellipse buildConfidenceEllipse(OrigerrDao origErrDao) {
    return Ellipse.builder()
      .setScalingFactorType(ScalingFactorType.CONFIDENCE)
      .setkWeight(0.0)
      .setConfidenceLevel(origErrDao.getConfidence())
      .setSemiMajorAxisLengthKm(origErrDao.getSemiMajorAxisOfError())
      .setSemiMajorAxisTrendDeg(origErrDao.getStrikeOfSemiMajorAxis())
      .setSemiMinorAxisLengthKm(origErrDao.getSemiMinorAxisOfError())
      .setDepthUncertaintyKm(origErrDao.getDepthError())
      .setTimeUncertainty(durationToDoubleConverter.convertToEntityAttribute(origErrDao.getOriginTimeError()))
      .build();
  }

  private static Ellipse buildCoverageEllipse(OrigerrDao origErrDao, EventControlDao eventControlDao) {
    return Ellipse.builder()
      .setScalingFactorType(ScalingFactorType.COVERAGE)
      .setkWeight(Double.POSITIVE_INFINITY)
      .setConfidenceLevel(origErrDao.getConfidence())
      .setSemiMajorAxisLengthKm(origErrDao.getSemiMajorAxisOfError() * eventControlDao.getEllipseSemiaxisConversionFactor())
      .setSemiMajorAxisTrendDeg(origErrDao.getStrikeOfSemiMajorAxis())
      .setSemiMinorAxisLengthKm(origErrDao.getSemiMinorAxisOfError() * eventControlDao.getEllipseSemiaxisConversionFactor())
      .setDepthUncertaintyKm(origErrDao.getDepthError() * eventControlDao.getEllipseDepthTimeConversionFactor())
      .setTimeUncertainty(durationToDoubleConverter.convertToEntityAttribute(origErrDao.getOriginTimeError() * eventControlDao.getEllipseDepthTimeConversionFactor()))
      .build();
  }
}
