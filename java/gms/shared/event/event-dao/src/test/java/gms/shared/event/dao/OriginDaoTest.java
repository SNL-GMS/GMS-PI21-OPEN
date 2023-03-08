package gms.shared.event.dao;

import gms.shared.event.coi.type.DepthMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OriginDaoTest {

  private OriginDao.Builder happyBuilder;
  private LatLonDepthTimeKey.Builder lldKeyBuilder;

  @BeforeEach
  void initializeHappyBuilder() {
    happyBuilder = new OriginDao.Builder()
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
      .withLoadDate(Instant.ofEpochSecond(325345740));
  }

  @Test
  void testBuilderHappy() {

    assertDoesNotThrow(() -> happyBuilder
      .build());
  }

  @Test
  void testBuilderSadLattitude() {

    lldKeyBuilder = new LatLonDepthTimeKey.Builder()
      .withLatitude(-100);
    assertThrows(IllegalArgumentException.class, () -> lldKeyBuilder
      .build());

    lldKeyBuilder = new LatLonDepthTimeKey.Builder()
      .withLatitude(100);
    assertThrows(IllegalArgumentException.class, () -> lldKeyBuilder
      .build());
  }


  @Test
  void testBuilderSadLongitude() {

    lldKeyBuilder = new LatLonDepthTimeKey.Builder()
      .withLongitude(-200);
    assertThrows(IllegalArgumentException.class, () -> lldKeyBuilder
      .build());

    lldKeyBuilder = new LatLonDepthTimeKey.Builder()
      .withLongitude(200);
    assertThrows(IllegalArgumentException.class, () -> lldKeyBuilder
      .build());
  }

  @Test
  void testBuilderSadDepth() {

    lldKeyBuilder = new LatLonDepthTimeKey.Builder()
      .withDepth(-101);
    assertThrows(IllegalArgumentException.class, () -> lldKeyBuilder
      .build());

    lldKeyBuilder
      .withDepth(1001);
    assertThrows(IllegalArgumentException.class, () -> lldKeyBuilder
      .build());
  }

  @Test
  void testBuilderSadTime() {

    lldKeyBuilder = new LatLonDepthTimeKey.Builder()
      .withTime(-10000000000L);
    assertThrows(IllegalArgumentException.class, () -> lldKeyBuilder
      .build());
  }

  @Test
  void testBuilderSadOriginId() {

    happyBuilder
      .withOriginId(-2);
    assertThrows(IllegalArgumentException.class, () -> happyBuilder
      .build());
  }

  @Test
  void testBuilderSadEventId() {

    happyBuilder
      .withEventId(-2);
    assertThrows(IllegalArgumentException.class, () -> happyBuilder
      .build());
  }

  @Test
  void testBuilderSadNumAssociatedArrivals() {

    happyBuilder
      .withNumAssociatedArrivals(-2);
    assertThrows(IllegalArgumentException.class, () -> happyBuilder
      .build());
  }

  @Test
  void testBuilderSadNumTimeDefiningPhases() {

    happyBuilder
      .withNumTimeDefiningPhases(-2);
    assertThrows(IllegalArgumentException.class, () -> happyBuilder
      .build());
  }

  @Test
  void testBuilderSadNumDepthPhases() {

    happyBuilder
      .withNumDepthPhases(-2);
    assertThrows(IllegalArgumentException.class, () -> happyBuilder
      .build());
  }

  @Test
  void testBuilderSadGeographicRegionNumber() {

    happyBuilder
      .withGeographicRegionNumber(0);
    assertThrows(IllegalArgumentException.class, () -> happyBuilder
      .build());

    happyBuilder
      .withGeographicRegionNumber(730);
    assertThrows(IllegalArgumentException.class, () -> happyBuilder
      .build());
  }

  @Test
  void testBuilderSadEstimatedDepth() {

    happyBuilder
      .withEstimatedDepth(-1);
    assertThrows(IllegalArgumentException.class, () -> happyBuilder
      .build());

    happyBuilder
      .withEstimatedDepth(1001);
    assertThrows(IllegalArgumentException.class, () -> happyBuilder
      .build());
  }

  @Test
  void testBuilderSadBodyWaveMag() {

    happyBuilder
      .withBodyWaveMag(-10);
    assertThrows(IllegalArgumentException.class, () -> happyBuilder
      .build());

    happyBuilder
      .withBodyWaveMag(51);
    assertThrows(IllegalArgumentException.class, () -> happyBuilder
      .build());
  }

  @Test
  void testBuilderSadBodyWaveMagId() {

    happyBuilder
      .withBodyWaveMagId(0);
    assertThrows(IllegalArgumentException.class, () -> happyBuilder
      .build());

    happyBuilder
      .withBodyWaveMagId(-2);
    assertThrows(IllegalArgumentException.class, () -> happyBuilder
      .build());
  }

  @Test
  void testBuilderSadSurfaceWaveMag() {

    happyBuilder
      .withSurfaceWaveMag(-10);
    assertThrows(IllegalArgumentException.class, () -> happyBuilder
      .build());

    happyBuilder
      .withSurfaceWaveMag(51);
    assertThrows(IllegalArgumentException.class, () -> happyBuilder
      .build());
  }

  @Test
  void testBuilderSadSurfaceWaveMagId() {

    happyBuilder
      .withSurfaceWaveMagId(0);
    assertThrows(IllegalArgumentException.class, () -> happyBuilder
      .build());

    happyBuilder
      .withSurfaceWaveMagId(-2);
    assertThrows(IllegalArgumentException.class, () -> happyBuilder
      .build());
  }

  @Test
  void testBuilderSadLocalMag() {

    happyBuilder
      .withLocalMag(-10);
    assertThrows(IllegalArgumentException.class, () -> happyBuilder
      .build());

    happyBuilder
      .withLocalMag(51);
    assertThrows(IllegalArgumentException.class, () -> happyBuilder
      .build());
  }

  @Test
  void testBuilderSadLocalMagId() {

    happyBuilder
      .withLocalMagId(0);
    assertThrows(IllegalArgumentException.class, () -> happyBuilder
      .build());
  }

  @Test
  void testBuilderSadCommentId() {

    happyBuilder
      .withCommentId(0);
    assertThrows(IllegalArgumentException.class, () -> happyBuilder
      .build());
  }

}