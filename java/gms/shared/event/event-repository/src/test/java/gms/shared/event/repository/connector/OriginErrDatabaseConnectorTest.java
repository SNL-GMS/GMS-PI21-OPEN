package gms.shared.event.repository.connector;

import gms.shared.event.dao.OrigerrDao;
import org.assertj.core.util.DoubleComparator;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import javax.persistence.EntityManager;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

/**
 * A class to test the OriginErrDatabaseConnector
 */
@Tag("component")
class OriginErrDatabaseConnectorTest extends DatabaseConnectorTest<OriginErrDatabaseConnector> {

  private static final OrigerrDao FIRST_EXPECTED_ORIGINERR = new OrigerrDao.Builder()
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
    .withLoadDate(Instant.ofEpochMilli(1619185740000L))
    .build();

  private static final OrigerrDao SECOND_EXPECTED_ORIGINERR = new OrigerrDao.Builder()
    .withOriginId(42342342342L)
    .withCovarianceMatrixSxx(1)
    .withCovarianceMatrixSyy(1)
    .withCovarianceMatrixSzz(1)
    .withCovarianceMatrixStt(1.2342)
    .withCovarianceMatrixSxy(1)
    .withCovarianceMatrixSxz(1)
    .withCovarianceMatrixSyz(1)
    .withCovarianceMatrixStx(1)
    .withCovarianceMatrixSty(1)
    .withCovarianceMatrixStz(1)
    .withStandardErrorOfObservations(34.3)
    .withSemiMajorAxisOfError(23)
    .withSemiMinorAxisOfError(4.3)
    .withStrikeOfSemiMajorAxis(3.4)
    .withDepthError(2.3)
    .withOriginTimeError(5.5)
    .withConfidence(0.5)
    .withCommentId(1231235948)
    .withLoadDate(Instant.ofEpochMilli(1619185740000L))
    .build();

  @Override
  protected OriginErrDatabaseConnector getDatabaseConnector(EntityManager entityManager) {
    return new OriginErrDatabaseConnector(entityManager);
  }

  @Test
  void testFindByIds() {
    assertThat(databaseConnector.findByIds(
      List.of(FIRST_EXPECTED_ORIGINERR.getOriginId(), SECOND_EXPECTED_ORIGINERR.getOriginId())))
      .usingComparatorForElementFieldsWithType(new DoubleComparator(1e-6), Double.class)
      .usingRecursiveFieldByFieldElementComparator()
      .containsOnly(FIRST_EXPECTED_ORIGINERR, SECOND_EXPECTED_ORIGINERR);
  }

  @Test
  void testFindByIdsIllegalArguments() {
    assertThatNullPointerException().isThrownBy(() -> databaseConnector.findByIds(null));
    assertThatIllegalArgumentException().isThrownBy(() -> databaseConnector.findByIds(List.of()));
  }

  @Test
  void testFindByIdsMissingIds() {
    assertThat(databaseConnector.findByIds(List.of(234232342342L))).isEmpty();
  }

  @Test
  void testFindById() {
    assertThat(databaseConnector.findById(FIRST_EXPECTED_ORIGINERR.getOriginId()))
      .contains(FIRST_EXPECTED_ORIGINERR);
  }

  @Test
  void testFindByIdMissingId() {
    assertThat(databaseConnector.findById(234232342342L)).isEmpty();
  }
}
