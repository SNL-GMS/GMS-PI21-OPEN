package gms.shared.event.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ArInfoDaoTest {

  private ArInfoDao.Builder happyBuilder;
  private OriginIdArrivalIdKey.Builder keyBuilder;


  @BeforeEach
  void initializeHappyBuilder() {
    happyBuilder = new ArInfoDao.Builder()
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
      .withLoadDate(Instant.ofEpochSecond(325345740));
  }

  @Test
  void testBuilderHappy() {

    assertDoesNotThrow(() -> happyBuilder.build());
  }

  @Test
  void testBuilderSadOriginId() {

    keyBuilder = new OriginIdArrivalIdKey.Builder()
      .withOriginId(0);
    assertThrows(IllegalArgumentException.class, () -> keyBuilder
      .build());
  }

  @Test
  void testBuilderSadArrivalId() {

    keyBuilder = new OriginIdArrivalIdKey.Builder()
      .withArrivalId(0);
    assertThrows(IllegalArgumentException.class, () -> keyBuilder
      .build());
  }

  @Test
  void testBuilderSadTimeErrorCode() {
    happyBuilder
      .withTimeErrorCode(-1);
    assertThrows(IllegalArgumentException.class, () -> happyBuilder
      .build());

    happyBuilder
      .withTimeErrorCode(21);
    assertThrows(IllegalArgumentException.class, () -> happyBuilder
      .build());
  }


  @Test
  void testBuilderSadAzimuthErrorCode() {

    happyBuilder
      .withAzimuthErrorCode(-1);
    assertThrows(IllegalArgumentException.class, () -> happyBuilder
      .build());

    happyBuilder
      .withAzimuthErrorCode(20);
    assertThrows(IllegalArgumentException.class, () -> happyBuilder
      .build());
  }

  @Test
  void testBuilderSadSlownessErrorCode() {
    happyBuilder
      .withSlownessErrorCode(-1);
    assertThrows(IllegalArgumentException.class, () -> happyBuilder
      .build());

    happyBuilder
      .withSlownessErrorCode(20);
    assertThrows(IllegalArgumentException.class, () -> happyBuilder
      .build());
  }

  @Test
  void testBuilderSadCorrectionCode() {

    happyBuilder
      .withCorrectionCode(-1);
    assertThrows(IllegalArgumentException.class, () -> happyBuilder
      .build());

    happyBuilder
      .withCorrectionCode(20);
    assertThrows(IllegalArgumentException.class, () -> happyBuilder
      .build());
  }

  @Test
  void testBuilderSadVelocityModel() {

    happyBuilder
      .withVelocityModel("VelocityModelNameTooLong");
    assertThrows(IllegalArgumentException.class, () -> happyBuilder
      .build());
  }

  @Test
  void testBuilderSadTotalTravelTime() {

    happyBuilder
      .withTotalTravelTime(0);
    assertThrows(IllegalArgumentException.class, () -> happyBuilder
      .build());

    happyBuilder
      .withTotalTravelTime(86401);
    assertThrows(IllegalArgumentException.class, () -> happyBuilder
      .build());
  }

  @Test
  void testBuilderSadBaseModelTravelTime() {

    happyBuilder
      .withBaseModelTravelTime(-2);
    assertThrows(IllegalArgumentException.class, () -> happyBuilder
      .build());

    happyBuilder
      .withBaseModelTravelTime(86401);
    assertThrows(IllegalArgumentException.class, () -> happyBuilder
      .build());
  }

  @Test
  void testBuilderSadTravelTimeEllipticityCorrection() {

    happyBuilder
      .withTravelTimeEllipticityCorrection(-51);
    assertThrows(IllegalArgumentException.class, () -> happyBuilder
      .build());

    happyBuilder
      .withTravelTimeEllipticityCorrection(51);
    assertThrows(IllegalArgumentException.class, () -> happyBuilder
      .build());
  }

  @Test
  void testBuilderSadTravelTimeElevationCorrection() {

    happyBuilder
      .withTravelTimeElevationCorrection(-51);
    assertThrows(IllegalArgumentException.class, () -> happyBuilder
      .build());

    happyBuilder
      .withTravelTimeElevationCorrection(51);
    assertThrows(IllegalArgumentException.class, () -> happyBuilder
      .build());
  }

  @Test
  void testBuilderSadTravelTimeStaticCorrection() {

    happyBuilder
      .withTravelTimeStaticCorrection(-51);
    assertThrows(IllegalArgumentException.class, () -> happyBuilder
      .build());

    happyBuilder
      .withTravelTimeStaticCorrection(51);
    assertThrows(IllegalArgumentException.class, () -> happyBuilder
      .build());
  }

  @Test
  void testBuilderSadTravelTimeSourceSpecificCorrection() {

    happyBuilder
      .withTravelTimeSourceSpecificCorrection(-51);
    assertThrows(IllegalArgumentException.class, () -> happyBuilder
      .build());

    happyBuilder
      .withTravelTimeSourceSpecificCorrection(51);
    assertThrows(IllegalArgumentException.class, () -> happyBuilder
      .build());
  }

  @Test
  void testBuilderSadTravelTimeModelError() {

    happyBuilder
      .withTravelTimeModelError(0);
    assertThrows(IllegalArgumentException.class, () -> happyBuilder
      .build());
  }

  @Test
  void testBuilderSadTravelTimeMeasurementError() {

    happyBuilder
      .withTravelTimeMeasurementError(0);
    assertThrows(IllegalArgumentException.class, () -> happyBuilder
      .build());

  }

  @Test
  void testBuilderSadTravelTimeModelPlusMeasurementError() {

    happyBuilder
      .withTravelTimeModelPlusMeasurementError(0);
    assertThrows(IllegalArgumentException.class, () -> happyBuilder
      .build());
  }

  @Test
  void testBuilderSadAzimuthSourceSpecificCorrection() {

    happyBuilder
      .withAzimuthSourceSpecificCorrection(-181);
    assertThrows(IllegalArgumentException.class, () -> happyBuilder
      .build());

    happyBuilder
      .withAzimuthSourceSpecificCorrection(181);
    assertThrows(IllegalArgumentException.class, () -> happyBuilder
      .build());
  }

  @Test
  void testBuilderSadSlownessModelError() {

    happyBuilder
      .withSlownessModelError(-2);
    assertThrows(IllegalArgumentException.class, () -> happyBuilder
      .build());
  }

  @Test
  void testBuilderSadSlownessMeasurementError() {

    happyBuilder
      .withSlownessMeasurementError(-2);
    assertThrows(IllegalArgumentException.class, () -> happyBuilder
      .build());
  }

  @Test
  void testBuilderSadSlownessModelPlusMeasurementError() {

    happyBuilder
      .withSlownessModelPlusMeasurementError(-2);
    assertThrows(IllegalArgumentException.class, () -> happyBuilder
      .build());
  }

  @Test
  void testBuilderSadTravelTimeImport() {

    happyBuilder
      .withTravelTimeImport(-2);
    assertThrows(IllegalArgumentException.class, () -> happyBuilder
      .build());

    happyBuilder
      .withTravelTimeImport(2);
    assertThrows(IllegalArgumentException.class, () -> happyBuilder
      .build());
  }

  @Test
  void testBuilderSadAzimuthImport() {

    happyBuilder
      .withAzimuthImport(-2);
    assertThrows(IllegalArgumentException.class, () -> happyBuilder
      .build());

    happyBuilder
      .withAzimuthImport(2);
    assertThrows(IllegalArgumentException.class, () -> happyBuilder
      .build());
  }

  @Test
  void testBuilderSadSlownessImport() {

    happyBuilder
      .withSlownessImport(-2);
    assertThrows(IllegalArgumentException.class, () -> happyBuilder
      .build());

    happyBuilder
      .withSlownessImport(2);
    assertThrows(IllegalArgumentException.class, () -> happyBuilder
      .build());
  }

  @Test
  void testBuilderSadwithSlownessVectorResidual() {

    happyBuilder
      .withSlownessVectorResidual(-1001);
    assertThrows(IllegalArgumentException.class, () -> happyBuilder
      .build());

    happyBuilder
      .withSlownessVectorResidual(1001);
    assertThrows(IllegalArgumentException.class, () -> happyBuilder
      .build());
  }

  @Test
  void testBuilderSadLoadDate() {

    happyBuilder
      .withLoadDate(null);
    assertThrows(NullPointerException.class, () -> happyBuilder
      .build());
  }

}

