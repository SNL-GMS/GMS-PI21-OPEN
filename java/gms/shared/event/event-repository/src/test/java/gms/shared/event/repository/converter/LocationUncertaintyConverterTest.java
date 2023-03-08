package gms.shared.event.repository.converter;

import gms.shared.event.coi.Ellipse;
import gms.shared.event.coi.EventTestFixtures;
import gms.shared.event.coi.ScalingFactorType;
import gms.shared.event.dao.EventControlDao;
import gms.shared.event.dao.OrigerrDao;
import gms.shared.event.repository.BridgeTestFixtures;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.junit.jupiter.api.Assertions.assertEquals;

class LocationUncertaintyConverterTest {

  private static final OrigerrDao INPUT_ORIGERR_DAO = new OrigerrDao.Builder()
    .withOriginId(EventTestFixtures.ORIGIN_ID)
    .withCovarianceMatrixSxx(1)
    .withCovarianceMatrixSxy(2)
    .withCovarianceMatrixSxz(3)
    .withCovarianceMatrixStx(4)
    .withCovarianceMatrixSyy(5)
    .withCovarianceMatrixSyz(6)
    .withCovarianceMatrixSty(7)
    .withCovarianceMatrixSzz(8)
    .withCovarianceMatrixStz(9)
    .withCovarianceMatrixStt(10)
    .withStandardErrorOfObservations(EventTestFixtures.LU_OED_STDERR_OBS)
    .withSemiMajorAxisOfError(EventTestFixtures.LU_OED_MAJOR_AXIS_ERR)
    .withSemiMinorAxisOfError(EventTestFixtures.LU_OED_MINOR_AXIS_ERR)
    .withStrikeOfSemiMajorAxis(EventTestFixtures.LU_OED_STRIKE_MAJOR_AXIS)
    .withDepthError(EventTestFixtures.LU_OED_DEPTH_ERR)
    .withOriginTimeError(EventTestFixtures.LU_OED_ORIGIN_TIME_ERR)
    .withConfidence(EventTestFixtures.LU_CONFIDENCE)
    .withCommentId(1)
    .withLoadDate(Instant.now()).build();

  private static final EventControlDao INPUT_EVENTCONTROL_DAO = EventControlDao.Builder
    .initializeFromInstance(EventTestFixtures.DEFAULT_EVENT_CONTROL_DAO)
    .withEllipseSemiaxisConversionFactor(EventTestFixtures.LU_ELPSE_AXIS_CONV_FACTOR)
    .withEllipseDepthTimeConversionFactor(EventTestFixtures.LU_ELPSE_DEPTH_TIME_CONV_FACTOR)
    .build();

  @Test
  void TestFromLegacyToLocationUncertaintyIllegalArguments() {
    assertThatNullPointerException()
      .isThrownBy(() -> LocationUncertaintyConverterUtility.fromLegacyToLocationUncertainty(null));
  }

  @Test
  void TestFromLegacytoDefaultFacetedLocationUncertainty() {
    var inputEhInfo = BridgeTestFixtures.DEFAULT_BRIDGED_EH_INFORMATION.toBuilder()
      .setOrigerrDao(INPUT_ORIGERR_DAO)
      .setEventControlDao(INPUT_EVENTCONTROL_DAO)
      .build();
    var locationUncertainty = LocationUncertaintyConverterUtility
      .fromLegacyToLocationUncertainty(inputEhInfo);

    assertEquals(1.0, locationUncertainty.getXx(), "Testing LocationUncertainty xx");
    assertEquals(2.0, locationUncertainty.getXy(), "Testing LocationUncertainty xy");
    assertEquals(3.0, locationUncertainty.getXz(), "Testing LocationUncertainty xz");
    assertEquals(4.0, locationUncertainty.getXt(), "Testing LocationUncertainty xt");
    assertEquals(5.0, locationUncertainty.getYy(), "Testing LocationUncertainty yy");
    assertEquals(6.0, locationUncertainty.getYz(), "Testing LocationUncertainty yz");
    assertEquals(7.0, locationUncertainty.getYt(), "Testing LocationUncertainty yt");
    assertEquals(8.0, locationUncertainty.getZz(), "Testing LocationUncertainty zz");
    assertEquals(9.0, locationUncertainty.getZt(), "Testing LocationUncertainty zt");
    assertEquals(10.0, locationUncertainty.getTt(), "Testing LocationUncertainty tt");

    assertEquals(List.of(
        List.of(1.0, 2.0, 3.0, 4.0),
        List.of(2.0, 5.0, 6.0, 7.0),
        List.of(3.0, 6.0, 8.0, 9.0),
        List.of(4.0, 7.0, 9.0, 10.0)), locationUncertainty.getCovarianceMatrix(),
      "Testing getCovarianceMatrix");

    assertEquals(3.14, locationUncertainty.getStDevOneObservation(), "Testing standard deviation calculation");
    assertEquals(Set.of(), locationUncertainty.getEllipsoids(), "Testing no ellipsoids exists");

    var originErrEllipseTest = Ellipse.builder()
      .setScalingFactorType(ScalingFactorType.CONFIDENCE)
      .setkWeight(0.0)
      .setConfidenceLevel(EventTestFixtures.LU_CONFIDENCE)
      .setSemiMajorAxisLengthKm(EventTestFixtures.LU_OED_MAJOR_AXIS_ERR)
      .setSemiMajorAxisTrendDeg(EventTestFixtures.LU_OED_STRIKE_MAJOR_AXIS)
      .setSemiMinorAxisLengthKm(EventTestFixtures.LU_OED_MINOR_AXIS_ERR)
      .setDepthUncertaintyKm(EventTestFixtures.LU_OED_DEPTH_ERR)
      .setTimeUncertainty(Duration.ofSeconds((long) EventTestFixtures.LU_OED_ORIGIN_TIME_ERR))
      .build();

    var eventControlEllipseTest = Ellipse.builder()
      .setScalingFactorType(ScalingFactorType.COVERAGE)
      .setkWeight(Double.POSITIVE_INFINITY)
      .setConfidenceLevel(EventTestFixtures.LU_CONFIDENCE)
      .setSemiMajorAxisLengthKm(EventTestFixtures.LU_OED_MAJOR_AXIS_ERR * EventTestFixtures.LU_ELPSE_AXIS_CONV_FACTOR)
      .setSemiMajorAxisTrendDeg(EventTestFixtures.LU_OED_STRIKE_MAJOR_AXIS)
      .setSemiMinorAxisLengthKm(EventTestFixtures.LU_OED_MINOR_AXIS_ERR * EventTestFixtures.LU_ELPSE_AXIS_CONV_FACTOR)
      .setDepthUncertaintyKm(EventTestFixtures.LU_OED_DEPTH_ERR * EventTestFixtures.LU_ELPSE_DEPTH_TIME_CONV_FACTOR)
      .setTimeUncertainty(Duration.ofSeconds((long) (EventTestFixtures.LU_OED_ORIGIN_TIME_ERR * EventTestFixtures.LU_ELPSE_DEPTH_TIME_CONV_FACTOR)))
      .build();

    assertEquals(Set.of(originErrEllipseTest, eventControlEllipseTest), locationUncertainty.getEllipses());
  }
}
