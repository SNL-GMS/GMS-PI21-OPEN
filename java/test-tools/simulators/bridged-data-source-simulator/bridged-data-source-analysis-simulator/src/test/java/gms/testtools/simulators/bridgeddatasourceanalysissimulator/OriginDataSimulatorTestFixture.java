package gms.testtools.simulators.bridgeddatasourceanalysissimulator;

import gms.shared.event.coi.type.DepthMethod;
import gms.shared.event.dao.ArInfoDao;
import gms.shared.event.dao.EventControlDao;
import gms.shared.event.dao.EventDao;
import gms.shared.event.dao.EventIdOriginIdKey;
import gms.shared.event.dao.LatLonDepthTimeKey;
import gms.shared.event.dao.MagnitudeIdAmplitudeIdStationNameKey;
import gms.shared.event.dao.NetMagDao;
import gms.shared.event.dao.OrigerrDao;
import gms.shared.event.dao.OriginDao;
import gms.shared.event.dao.OriginIdArrivalIdKey;
import gms.shared.event.dao.StaMagDao;
import gms.shared.signaldetection.dao.css.AridOridKey;
import gms.shared.signaldetection.dao.css.AssocDao;
import gms.shared.signaldetection.dao.css.enums.DefiningFlag;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class OriginDataSimulatorTestFixture {

  public static OriginDao.Builder happyOriginDaoBuilder =
    new OriginDao.Builder()
      .withLatLonDepthTimeKey(
        new LatLonDepthTimeKey.Builder()
          .withLatitude(10)
          .withLongitude(10)
          .withDepth(10)
          .withTime(10000000L)
          .build()
      )
      .withOriginId(1)
      .withEventId(1)
      .withJulianDate(julianDate(10000000L))
      .withNumAssociatedArrivals(2)
      .withNumTimeDefiningPhases(2)
      .withNumDepthPhases(2)
      .withGeographicRegionNumber(2)
      .withEventType("e")
      .withEstimatedDepth(1)
      .withDepthMethod(DepthMethod.UNKNOWN)

      .withBodyWaveMag(1)
      .withBodyWaveMagId(1)

      .withSurfaceWaveMag(1)
      .withSurfaceWaveMagId(1)

      .withSeismicRegionNumber(1)

      .withLocalMag(1)
      .withLocalMagId(1)

      .withLocationAlgorithm("algo")
      .withAuthor("me")
      .withCommentId(12)
      .withLoadDate(Instant.ofEpochSecond(1));


  public static OriginDao.Builder happyModifiedOriginDaoBuilder =
    new OriginDao.Builder()
      .withLatLonDepthTimeKey(
        new LatLonDepthTimeKey.Builder()
          .withLatitude(10)
          .withLongitude(10)
          .withDepth(10)
          .withTime(10000000L + 1)
          .build()
      )
      .withOriginId(1)
      .withEventId(1)
      .withJulianDate(julianDate(10000000L))
      .withNumAssociatedArrivals(2)
      .withNumTimeDefiningPhases(2)
      .withNumDepthPhases(2)
      .withGeographicRegionNumber(2)
      .withEventType("e")
      .withEstimatedDepth(1)
      .withDepthMethod(DepthMethod.UNKNOWN)

      .withBodyWaveMag(1)
      .withBodyWaveMagId(1)

      .withSurfaceWaveMag(1)
      .withSurfaceWaveMagId(1)

      .withSeismicRegionNumber(1)

      .withLocalMag(1)
      .withLocalMagId(1)

      .withLocationAlgorithm("algo")
      .withAuthor("me")
      .withCommentId(12)
      .withLoadDate(Instant.ofEpochSecond(1));

  public static OrigerrDao.Builder happyOrigerrDaoBuilder =
    new OrigerrDao.Builder()
      .withOriginId(1L)
      .withCovarianceMatrixSxx(1)
      .withCovarianceMatrixSyy(2)
      .withCovarianceMatrixSzz(3)
      .withCovarianceMatrixStt(4)
      .withCovarianceMatrixSxy(5)
      .withCovarianceMatrixSxz(6)
      .withCovarianceMatrixSyz(7)
      .withCovarianceMatrixStx(8)
      .withCovarianceMatrixSty(9)
      .withCovarianceMatrixStz(10)
      .withStandardErrorOfObservations(3)
      .withSemiMajorAxisOfError(2)
      .withSemiMinorAxisOfError(4)
      .withStrikeOfSemiMajorAxis(3)
      .withDepthError(23)
      .withOriginTimeError(1)
      .withConfidence(1)
      .withCommentId(1231235948)
      .withLoadDate(Instant.ofEpochMilli(1619185740000L));


  public static EventDao.Builder happyEventDaoBuilder =
    new EventDao.Builder()
      .withEventId(1)
      .withEventName("You can call me Al")
      .withPreferredOrigin(1)
      .withAuthor("Aldous Huxley")
      .withCommentId(12)
      .withLoadDate(Instant.ofEpochSecond(325345740));

  public static EventControlDao.Builder happyEventControlDao = new EventControlDao.Builder()
    .withEventIdOriginIdKey(
      new EventIdOriginIdKey.Builder()
        .withOriginId(1)
        .withEventId(1)
        .build()
    )
    .withPreferredLocation("L")
    .withConstrainOriginTime(true)
    .withConstrainLatLon(true)
    .withConstrainDepth(true)
    .withSourceDependentCorrectionCode(23)
    .withSourceDependentLocationCorrectionRegion("GGGHHHFFFGGG")
    .withIgnoreLargeResidualsInLocation(true)
    .withLocationLargeResidualMultiplier(10)
    .withUseStationSubsetInLocation(true)
    .withUseAllStationsInLocation(true)
    .withUseDistanceVarianceWeighting(true)
    .withUserDefinedDistanceVarianceWeighting(10)
    .withSourceDependentMagnitudeCorrectionRegion("FFFDDDFFFFF")
    .withIgnoreLargeResidualsInMagnitude(true)
    .withMagnitudeLargeResidualMultiplier(10)
    .withUseStationSubsetInMagnitude(true)
    .withUseAllStationsInMagnitude(true)
    .withMbMinimumDistance(10)
    .withMbMaximumDistance(10)
    .withMagnitudeModel("FDFDFDFDFDFDF")
    .withEllipseSemiaxisConversionFactor(10)
    .withEllipseDepthTimeConversionFactor(10)
    .withLoadDate(Instant.ofEpochSecond(325345740));

  public static AssocDao.Builder happyAssocDaoBuilder = new AssocDao.Builder()
    .withId(
      new AridOridKey.Builder()
        .withOriginId(1)
        .withArrivalId(1)
        .build()
    )
    .withStationCode("AA")
    .withPhase("P")
    .withBelief(1.0)
    .withDelta(1.0)
    .withStationToEventAzimuth(1.0)
    .withEventToStationAzimuth(1.0)
    .withTimeResidual(1.0)
    .withTimeDefining(DefiningFlag.DEFAULT_DEFINING)
    .withAzimuthResidual(1.0)
    .withAzimuthDefining(DefiningFlag.DEFAULT_DEFINING)
    .withSlownessResidual(1.0)
    .withSlownessDefining(DefiningFlag.DEFAULT_DEFINING)
    .withEmergenceAngleResidual(1.0)
    .withLocationWeight(1.0)
    .withVelocityModel("model")
    .withCommentId(5)
    .withLoadDate(Instant.ofEpochSecond(325345740));

  public static ArInfoDao.Builder happyArInfoDaoBuilder = new ArInfoDao.Builder()
    .withOriginIdArrivalIdKey(
      new OriginIdArrivalIdKey.Builder()
        .withOriginId(1)
        .withArrivalId(1)
        .build()
    )
    .withTimeErrorCode(1)
    .withAzimuthErrorCode(1)
    .withSlownessErrorCode(1)
    .withCorrectionCode(1)
    .withVelocityModel("AA")
    .withTotalTravelTime(10)
    .withBaseModelTravelTime(10)
    .withTravelTimeEllipticityCorrection(10)
    .withTravelTimeElevationCorrection(10)
    .withTravelTimeStaticCorrection(10)
    .withTravelTimeSourceSpecificCorrection(9)
    .withTravelTimeModelError(10)
    .withTravelTimeMeasurementError(10)
    .withTravelTimeModelPlusMeasurementError(10)
    .withAzimuthSourceSpecificCorrection(10)
    .withAzimuthModelError(10)
    .withAzimuthMeasurementError(10)
    .withAzimuthModelPlusMeasurementError(10)
    .withSlownessSourceSpecificCorrection(10)
    .withSlownessModelError(10)
    .withSlownessMeasurementError(10)
    .withSlownessModelPlusMeasurementError(10)
    .withTravelTimeImport(1)
    .withAzimuthImport(1)
    .withSlownessImport(1)
    .withSlownessVectorResidual(10)
    .withLoadDate(Instant.ofEpochSecond(325345740));

  public static NetMagDao.Builder happyNetMagDaoBuilder = new NetMagDao.Builder()
    .withMagnitudeId(1)
    .withNetwork("AA")
    .withOriginId(1)
    .withEventId(1)
    .withMagnitudeType("BB")
    .withNumberOfStations(10)
    .withMagnitude(1.0)
    .withMagnitudeUncertainty(1.0)
    .withAuthor("AUTH")
    .withCommentId(1234)
    .withLoadDate(Instant.ofEpochSecond(325345740));

  public static StaMagDao.Builder happyStaMagDaoBuilder = new StaMagDao.Builder()
    .withMagnitudeIdAmplitudeIdStationNameKey(
      new MagnitudeIdAmplitudeIdStationNameKey.Builder()
        .withMagnitudeId(1)
        .withAmplitudeId(2)
        .withStationName("AA")
        .build()
    )
    .withArrivalId(3)
    .withOriginId(1)
    .withEventId(1)
    .withPhaseType("P")
    .withDelta(1)
    .withMagnitudeType("bb")
    .withMagnitude(1)
    .withMagnitudeUncertainty(1)
    .withMagnitudeResidual(1)
    .withMagnitudeDefining(DefiningFlag.DEFAULT_DEFINING)
    .withMagnitudeModel("model")
    .withAuthor("me")
    .withCommentId(12)
    .withLoadDate(Instant.ofEpochSecond(325345740));


  static long julianDate(double timeAsFloat) {
    var instant = Instant.ofEpochMilli((long) (timeAsFloat * 1000L));

    return Long.parseLong(DateTimeFormatter.ISO_ORDINAL_DATE
      .withZone(ZoneId.of("UTC"))
      .format(instant)
      .replace("-", "")
      .replace("Z", "")
    );
  }

}
