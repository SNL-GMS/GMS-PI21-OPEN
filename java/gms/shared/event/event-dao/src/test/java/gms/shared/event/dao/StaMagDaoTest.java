package gms.shared.event.dao;

import gms.shared.signaldetection.dao.css.enums.DefiningFlag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StaMagDaoTest {

  private StaMagDao.Builder happyBuilder;
  private MagnitudeIdAmplitudeIdStationNameKey.Builder staMagKeyBuilder;


  @BeforeEach
  void initializeHappyBuilder() {
    happyBuilder = new StaMagDao.Builder()
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
      .withLoadDate(Instant.ofEpochSecond(325345740));
  }

  @Test
  void testBuilderHappy() {

    assertDoesNotThrow(() -> happyBuilder
      .build());

  }

  @Test
  void testBuilderSadMagnitudeId() {

    staMagKeyBuilder = new MagnitudeIdAmplitudeIdStationNameKey.Builder()
      .withMagnitudeId(-2);
    assertThrows(IllegalArgumentException.class, (() -> staMagKeyBuilder
      .build()));

  }

  @Test
  void testBuilderSadAmplitudeId() {

    staMagKeyBuilder = new MagnitudeIdAmplitudeIdStationNameKey.Builder()
      .withAmplitudeId(-2);
    assertThrows(IllegalArgumentException.class, (() -> staMagKeyBuilder
      .build()));

  }

  @Test
  void testBuilderSadStationName() {

    staMagKeyBuilder = new MagnitudeIdAmplitudeIdStationNameKey.Builder()
      .withStationName("NameTooLong");
    assertThrows(IllegalArgumentException.class, (() -> staMagKeyBuilder
      .build()));

    staMagKeyBuilder = new MagnitudeIdAmplitudeIdStationNameKey.Builder()
      .withStationName("");
    assertThrows(IllegalArgumentException.class, (() -> staMagKeyBuilder
      .build()));

  }

  @Test
  void testBuilderSadArrivalId() {

    happyBuilder
      .withArrivalId(0);
    assertThrows(IllegalArgumentException.class, (() -> happyBuilder
      .build()));

    happyBuilder
      .withArrivalId(-2);
    assertThrows(IllegalArgumentException.class, (() -> happyBuilder
      .build()));

  }

  @Test
  void testBuilderSadOriginId() {

    happyBuilder
      .withOriginId(0);
    assertThrows(IllegalArgumentException.class, (() -> happyBuilder
      .build()));

  }

  @Test
  void testBuilderSadEventId() {

    happyBuilder
      .withEventId(0);
    assertThrows(IllegalArgumentException.class, (() -> happyBuilder
      .build()));

  }

  @Test
  void testBuilderSadPhaseType() {

    happyBuilder
      .withPhaseType(null);
    assertThrows(NullPointerException.class, (() -> happyBuilder
      .build()));

    happyBuilder
      .withPhaseType("");
    assertThrows(IllegalArgumentException.class, (() -> happyBuilder
      .build()));

    happyBuilder
      .withPhaseType("TypeTooLong");
    assertThrows(IllegalArgumentException.class, (() -> happyBuilder
      .build()));

  }

  @Test
  void testBuilderSadDelta() {

    happyBuilder
      .withDelta(-2);
    assertThrows(IllegalArgumentException.class, (() -> happyBuilder
      .build()));

  }

  @Test
  void testBuilderSadMagnitudeType() {

    happyBuilder
      .withMagnitudeType(null);
    assertThrows(NullPointerException.class, (() -> happyBuilder
      .build()));

    happyBuilder
      .withMagnitudeType("");
    assertThrows(IllegalArgumentException.class, (() -> happyBuilder
      .build()));

    happyBuilder
      .withMagnitudeType("TypeTooLong");
    assertThrows(IllegalArgumentException.class, (() -> happyBuilder
      .build()));
  }

  @Test
  void testBuilderSadMagnitude() {

    happyBuilder
      .withMagnitude(-10);
    assertThrows(IllegalArgumentException.class, (() -> happyBuilder
      .build()));

    happyBuilder
      .withMagnitude(51);
    assertThrows(IllegalArgumentException.class, (() -> happyBuilder
      .build()));

  }

  @Test
  void testBuilderSadMagnitudeUncertainty() {

    happyBuilder
      .withMagnitudeUncertainty(0);
    assertThrows(IllegalArgumentException.class, (() -> happyBuilder
      .build()));

  }

  @Test
  void testBuilderSadMagnitudeResidual() {

    happyBuilder
      .withMagnitudeResidual(-51);
    assertThrows(IllegalArgumentException.class, (() -> happyBuilder
      .build()));

    happyBuilder
      .withMagnitudeResidual(51);
    assertThrows(IllegalArgumentException.class, (() -> happyBuilder
      .build()));

  }

  @Test
  void testBuilderSadMagnitudeModel() {

    happyBuilder
      .withMagnitudeModel(null);
    assertThrows(NullPointerException.class, (() -> happyBuilder
      .build()));

    happyBuilder
      .withMagnitudeModel("");
    assertThrows(IllegalArgumentException.class, (() -> happyBuilder
      .build()));

    happyBuilder
      .withMagnitudeModel("MyModelNameIsTooLong");
    assertThrows(IllegalArgumentException.class, (() -> happyBuilder
      .build()));

  }

  @Test
  void testBuilderSadAuthor() {

    happyBuilder
      .withAuthor("");
    assertThrows(IllegalArgumentException.class, (() -> happyBuilder
      .build()));

    happyBuilder
      .withAuthor("AuthorNameTooLong");
    assertThrows(IllegalArgumentException.class, (() -> happyBuilder
      .build()));

  }

  @Test
  void testBuilderSadCommentId() {

    happyBuilder
      .withCommentId(0);
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