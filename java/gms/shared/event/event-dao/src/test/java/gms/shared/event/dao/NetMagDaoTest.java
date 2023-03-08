package gms.shared.event.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NetMagDaoTest {

  private NetMagDao.Builder happyBuilder;

  @BeforeEach
  void initializeHappyBuilder() {
    happyBuilder = new NetMagDao.Builder()
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
      .withLoadDate(Instant.ofEpochSecond(325345740));
  }


  @Test
  void testBuilderHappy() {

    assertDoesNotThrow(() -> happyBuilder
      .build());
  }

  @Test
  void testBuilderSadMagnitudeId() {

    happyBuilder
      .withMagnitudeId(-2);
    assertThrows(IllegalArgumentException.class, () -> happyBuilder
      .build());
  }

  @Test
  void testBuilderSadOriginId() {

    happyBuilder
      .withOriginId(0);
    assertThrows(IllegalArgumentException.class, () -> happyBuilder
      .build());
  }

  @Test
  void testBuilderSadEventId() {

    happyBuilder
      .withEventId(0);
    assertThrows(IllegalArgumentException.class, () -> happyBuilder
      .build());
  }

  @Test
  void testBuilderSadNumberOfStations() {

    happyBuilder
      .withNumberOfStations(-2);
    assertThrows(IllegalArgumentException.class, () -> happyBuilder
      .build());
  }

  @Test
  void testBuilderSadMagnitude() {

    happyBuilder
      .withMagnitude(51);
    assertThrows(IllegalArgumentException.class, () -> happyBuilder
      .build());

    happyBuilder
      .withMagnitude(-10);
    assertThrows(IllegalArgumentException.class, () -> happyBuilder
      .build());
  }

  @Test
  void testBuilderSadUncertainty() {

    happyBuilder
      .withMagnitudeUncertainty(0);
    assertThrows(IllegalArgumentException.class, () -> happyBuilder
      .build());
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
}