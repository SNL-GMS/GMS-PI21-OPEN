package gms.shared.event.repository.connector;

import gms.shared.event.coi.type.DepthMethod;
import gms.shared.event.dao.ArInfoDao;
import gms.shared.event.dao.EventControlDao;
import gms.shared.event.dao.EventIdOriginIdKey;
import gms.shared.event.dao.LatLonDepthTimeKey;
import gms.shared.event.dao.MagnitudeIdAmplitudeIdStationNameKey;
import gms.shared.event.dao.NetMagDao;
import gms.shared.event.dao.OriginDao;
import gms.shared.event.dao.OriginIdArrivalIdKey;
import gms.shared.event.dao.StaMagDao;
import gms.shared.signaldetection.dao.css.AridOridKey;
import gms.shared.signaldetection.dao.css.AssocDao;
import gms.shared.signaldetection.dao.css.enums.DefiningFlag;
import org.junit.jupiter.api.Test;

import javax.persistence.EntityManagerFactory;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OriginSimulatorDatabaseConnectorTest extends SimulatorDatabaseConnectorTest<OriginSimulatorDatabaseConnector> {

  @Override
  protected OriginSimulatorDatabaseConnector getDatabaseConnector(EntityManagerFactory entityManagerFactory) {
    return OriginSimulatorDatabaseConnector.create(entityManagerFactory);
  }

  @Test
  void testFindOriginDaosByPreciseTime() {

    var expectedOriginDaoList = List.of(
      new OriginDao.Builder()
        .withLatLonDepthTimeKey(
          new LatLonDepthTimeKey.Builder()
            .withLatitude(1)
            .withLongitude(1)
            .withDepth(2)
            .withTime(1629600001.0000)
            .build()
        )
        .withOriginId(11111)
        .withEventId(1111)
        .withJulianDate(1)
        .withNumAssociatedArrivals(1)
        .withNumTimeDefiningPhases(1)
        .withNumDepthPhases(1)
        .withGeographicRegionNumber(1)
        .withSeismicRegionNumber(1)
        .withEventType("etype")
        .withEstimatedDepth(4)
        .withDepthMethod(DepthMethod.A)
        .withBodyWaveMag(23)
        .withBodyWaveMagId(3423)
        .withSurfaceWaveMag(23)
        .withSurfaceWaveMagId(23434)
        .withLocalMag(32)
        .withLocalMagId(23434)
        .withLocationAlgorithm("algorithm")
        .withAuthor("auth")
        .withCommentId(234234242)
        .withLoadDate(Instant.ofEpochSecond(325345740))
        .build(),

      new OriginDao.Builder()
        .withLatLonDepthTimeKey(
          new LatLonDepthTimeKey.Builder()
            .withLatitude(1)
            .withLongitude(5)
            .withDepth(2)
            .withTime(1629600001.0000)
            .build()
        )
        .withOriginId(11112)
        .withEventId(-1)
        .withJulianDate(1)
        .withNumAssociatedArrivals(1)
        .withNumTimeDefiningPhases(1)
        .withNumDepthPhases(1)
        .withGeographicRegionNumber(1)
        .withSeismicRegionNumber(1)
        .withEventType("etype")
        .withEstimatedDepth(4)
        .withDepthMethod(DepthMethod.A)
        .withBodyWaveMag(23)
        .withBodyWaveMagId(3423)
        .withSurfaceWaveMag(23)
        .withSurfaceWaveMagId(23434)
        .withLocalMag(32)
        .withLocalMagId(23434)
        .withLocationAlgorithm("algorithm")
        .withAuthor("auth")
        .withCommentId(234234242)
        .withLoadDate(Instant.ofEpochSecond(325345740))
        .build()
    );

    var result = databaseConnector.findOriginDaosByPreciseTime(
      Instant.ofEpochSecond(1629600001),
      Instant.ofEpochSecond(1629600002)
    );

    assertEquals(expectedOriginDaoList, result);
  }

  @Test
  void testRetrieveEventControlDaoByEventIdAndOriginId() {
    var expectedEventControlDao = new EventControlDao.Builder()
      .withEventIdOriginIdKey(
        new EventIdOriginIdKey.Builder()
          .withOriginId(11111)
          .withEventId(1111)
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
      .withLoadDate(Instant.ofEpochSecond(325345740))
      .build();

    var result = databaseConnector.retrieveEventControlDaoByEventIdAndOriginId(
      new EventIdOriginIdKey.Builder()
        .withOriginId(11111)
        .withEventId(1111)
        .build()
    );

    assertTrue(result.isPresent());

    assertEquals(expectedEventControlDao, result.get());
  }

  @Test
  void testRetrieveArInfoDaoListForOriginId() {
    var expectedArInfoDaoList = List.of(
      new ArInfoDao.Builder()
        .withOriginIdArrivalIdKey(
          new OriginIdArrivalIdKey.Builder()
            .withOriginId(11111)
            .withArrivalId(22222)
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
        .withLoadDate(Instant.ofEpochSecond(325345740))
        .build(),

      new ArInfoDao.Builder()
        .withOriginIdArrivalIdKey(
          new OriginIdArrivalIdKey.Builder()
            .withOriginId(11111)
            .withArrivalId(33333)
            .build()
        )
        .withTimeErrorCode(1)
        .withAzimuthErrorCode(1)
        .withSlownessErrorCode(1)
        .withCorrectionCode(1)
        .withVelocityModel("BB")
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
        .withLoadDate(Instant.ofEpochSecond(325345740))
        .build()
    );

    var result = databaseConnector.retrieveArInfoDaoListForOriginId(11111);

    assertEquals(
      expectedArInfoDaoList,
      result
    );
  }

  @Test
  void testRetrieveAssocDaoListFromOriginId() {

    var expectedAssocDaoList = List.of(
      new AssocDao.Builder()
        .withId(
          new AridOridKey.Builder()
            .withOriginId(11111)
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
        .withLoadDate(Instant.ofEpochSecond(325345740))
        .build()
    );

    var result = databaseConnector.retrieveAssocDaoListFromOriginId(11111);

    assertEquals(expectedAssocDaoList, result);
  }

  @Test
  void testRetrieveNetMagDaoListForOriginId() {

    var expectedNetMagDaoList = List.of(
      new NetMagDao.Builder()
        .withMagnitudeId(1)
        .withNetwork("AA")
        .withOriginId(1111)
        .withEventId(2222)
        .withMagnitudeType("BB")
        .withNumberOfStations(10)
        .withMagnitude(1.0)
        .withMagnitudeUncertainty(1.0)
        .withAuthor("AUTH")
        .withCommentId(1234)
        .withLoadDate(Instant.ofEpochSecond(325345740))
        .build(),

      new NetMagDao.Builder()
        .withMagnitudeId(2)
        .withNetwork("BB")
        .withOriginId(1111)
        .withEventId(1111)
        .withMagnitudeType("AA")
        .withNumberOfStations(10)
        .withMagnitude(2.0)
        .withMagnitudeUncertainty(1.0)
        .withAuthor("AUTH")
        .withCommentId(1234)
        .withLoadDate(Instant.ofEpochSecond(325345740))
        .build()
    );

    var result = databaseConnector.retrieveNetMagDaoListForOriginId(1111);

    assertEquals(expectedNetMagDaoList, result);

  }

  @Test
  void testRetrieveStamagDaoListForOriginId() {
    var expectedStamagDaoList = List.of(
      new StaMagDao.Builder()
        .withMagnitudeIdAmplitudeIdStationNameKey(
          new MagnitudeIdAmplitudeIdStationNameKey.Builder()
            .withMagnitudeId(1)
            .withAmplitudeId(2)
            .withStationName("AA")
            .build()
        )
        .withArrivalId(3)
        .withOriginId(11111)
        .withEventId(1111)
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
        .withLoadDate(Instant.ofEpochSecond(325345740))
        .build(),

      new StaMagDao.Builder()
        .withMagnitudeIdAmplitudeIdStationNameKey(
          new MagnitudeIdAmplitudeIdStationNameKey.Builder()
            .withMagnitudeId(2)
            .withAmplitudeId(2)
            .withStationName("AA")
            .build()
        )
        .withArrivalId(3)
        .withOriginId(11111)
        .withEventId(1111)
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
        .withLoadDate(Instant.ofEpochSecond(325345740))
        .build()
    );

    var result = databaseConnector.retrieveStamagDaoListForOriginId(11111);

    assertEquals(
      expectedStamagDaoList,
      result
    );
  }
}
