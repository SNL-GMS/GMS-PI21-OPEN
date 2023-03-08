package gms.shared.event.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrigerrDaoTest {

  private OrigerrDao.Builder happyBuilder;

  @BeforeEach
  void initializeHappyBuilder() {
    happyBuilder = new OrigerrDao.Builder()
      .withOriginId(42342342341L)
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
  }


  @Test
  void testBuilderHappy() {

    assertDoesNotThrow(() -> happyBuilder
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
  void testBuilderSadCovarianceMatrixSxx() {

    happyBuilder
      .withCovarianceMatrixSxx(0);
    assertThrows(IllegalArgumentException.class, () -> happyBuilder
      .build());
  }

  @Test
  void testBuilderSadCovarianceMatrixSyy() {

    happyBuilder
      .withCovarianceMatrixSyy(0);
    assertThrows(IllegalArgumentException.class, () -> happyBuilder
      .build());
  }

  @Test
  void testBuilderSadCovarianceMatrixSzz() {

    happyBuilder
      .withCovarianceMatrixSzz(0);
    assertThrows(IllegalArgumentException.class, () -> happyBuilder
      .build());
  }

  @Test
  void testBuilderSadCovarianceMatrixStt() {

    happyBuilder
      .withCovarianceMatrixStt(0);
    assertThrows(IllegalArgumentException.class, () -> happyBuilder
      .build());
  }

  @Test
  void testBuilderSadStandardErrorOfObservations() {

    happyBuilder
      .withStandardErrorOfObservations(0);
    assertThrows(IllegalArgumentException.class, () -> happyBuilder
      .build());
  }

  @Test
  void testBuilderSadSemiMajorAxisOfError() {

    happyBuilder
      .withSemiMajorAxisOfError(0);
    assertThrows(IllegalArgumentException.class, () -> happyBuilder
      .build());
  }

  @Test
  void testBuilderSadSemiMinorAxisOfError() {

    happyBuilder
      .withSemiMinorAxisOfError(0);
    assertThrows(IllegalArgumentException.class, () -> happyBuilder
      .build());
  }

  @Test
  void testBuilderSadStrikeOfSemiMajorAxis() {

    happyBuilder
      .withStrikeOfSemiMajorAxis(-2);
    assertThrows(IllegalArgumentException.class, () -> happyBuilder
      .build());

    happyBuilder
      .withStrikeOfSemiMajorAxis(361);
    assertThrows(IllegalArgumentException.class, () -> happyBuilder
      .build());
  }

  @Test
  void testBuilderSadDepthError() {

    happyBuilder
      .withDepthError(0);
    assertThrows(IllegalArgumentException.class, () -> happyBuilder
      .build());
  }

  @Test
  void testBuilderSadOriginTimeError() {

    happyBuilder
      .withOriginTimeError(-2);
    assertThrows(IllegalArgumentException.class, () -> happyBuilder
      .build());
  }

  @Test
  void testBuilderSadConfidence() {

    happyBuilder
      .withConfidence(.499);
    assertThrows(IllegalArgumentException.class, () -> happyBuilder
      .build());

    happyBuilder
      .withConfidence(1.01);
    assertThrows(IllegalArgumentException.class, () -> happyBuilder
      .build());
  }

  @Test
  void testBuilderCommentId() {

    happyBuilder
      .withCommentId(-2);
    assertThrows(IllegalArgumentException.class, () -> happyBuilder
      .build());
  }

}