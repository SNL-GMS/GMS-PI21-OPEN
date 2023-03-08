package gms.shared.signaldetection.database.connector;

import gms.shared.signaldetection.dao.css.AssocDao;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.persistence.EntityManagerFactory;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class AssocDatabaseConnectorTest extends SignalDetectionDbTest<AssocDatabaseConnector> {

  @Override
  protected AssocDatabaseConnector getRepository(EntityManagerFactory entityManagerFactory) {
    return new AssocDatabaseConnector(entityManagerFactory);
  }

  @Test
  void testFindAssocsByAridValidation() {
    Exception ex = assertThrows(NullPointerException.class,
      () -> repository.findAssocsByArids(null));
    assertEquals("Arids cannot be null", ex.getMessage());
  }

  @ParameterizedTest
  @MethodSource("getFindAssocsByAridArguments")
  void testFindAssocsByArid(List<Long> arids,
    long expectedResultSize) {

    List<AssocDao> assocs = assertDoesNotThrow(() -> repository.findAssocsByArids(arids));
    assertEquals(expectedResultSize, assocs.size());
  }

  static Stream<Arguments> getFindAssocsByAridArguments() {
    return Stream.of(arguments(List.of(), 0),
      arguments(List.of(1, 2), 2),
      arguments(List.of(2, 4), 1));
  }

  @Test
  void testFindAssocsByOrid() {
    //test validation case
    assertThrows(NullPointerException.class, () -> repository.findAssocsByOrids(null));

    //test empty list
    var foundAssocs = repository.findAssocsByOrids(List.of());
    assertEquals(Collections.emptyList(), foundAssocs);

    //test we can pull back actual Assoc records (note 4l is not a valid ORID)
    foundAssocs = repository.findAssocsByOrids(List.of(1L, 2L, 3L, 4L));
    assertEquals(3, foundAssocs.size());
  }

  @Test
  void testFindAssocsByAridsAndOrid() {
    //test validation case
    assertThrows(NullPointerException.class, () -> repository.findAssocsByAridsAndOrids(null));

    //test empty list
    var foundAssocs = repository.findAssocsByAridsAndOrids(List.of());
    assertEquals(Collections.emptyList(), foundAssocs);

    //test we can pull back actual Assoc records (note 4l is not a valid ORID)
    foundAssocs = repository.findAssocsByAridsAndOrids(List.of(Pair.of(1L, 1L),
      Pair.of(2L, 2L),
      Pair.of(3L, 3L),
      Pair.of(4L, 4L)));
    assertEquals(3, foundAssocs.size());
  }
}